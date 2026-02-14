package com.github.martinambrus.rdforward.server.bedrock;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.event.EventResult;
import com.github.martinambrus.rdforward.protocol.packet.classic.PlayerTeleportPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockServerPacket;
import com.github.martinambrus.rdforward.server.ChunkManager;
import com.github.martinambrus.rdforward.server.ConnectedPlayer;
import com.github.martinambrus.rdforward.server.PlayerManager;
import com.github.martinambrus.rdforward.server.ServerWorld;
import com.github.martinambrus.rdforward.server.api.CommandRegistry;
import com.github.martinambrus.rdforward.server.event.ServerEvents;
import com.github.martinambrus.rdforward.world.BlockRegistry;
import com.github.martinambrus.rdforward.world.alpha.AlphaChunk;
import org.cloudburstmc.math.vector.Vector2f;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.BedrockServerSession;
import org.cloudburstmc.protocol.bedrock.data.Ability;
import org.cloudburstmc.protocol.bedrock.data.AbilityLayer;
import org.cloudburstmc.protocol.bedrock.data.AuthoritativeMovementMode;
import org.cloudburstmc.protocol.bedrock.data.ChatRestrictionLevel;
import org.cloudburstmc.protocol.bedrock.data.GamePublishSetting;
import org.cloudburstmc.protocol.bedrock.data.GameType;
import org.cloudburstmc.protocol.bedrock.data.PlayerActionType;
import org.cloudburstmc.protocol.bedrock.data.PlayerPermission;
import org.cloudburstmc.protocol.bedrock.data.SpawnBiomeType;
import org.cloudburstmc.protocol.bedrock.data.ExperimentData;
import org.cloudburstmc.protocol.bedrock.data.AttributeData;
import org.cloudburstmc.protocol.bedrock.data.GameRuleData;
import org.cloudburstmc.protocol.bedrock.data.camera.CameraPreset;
import org.cloudburstmc.protocol.bedrock.data.command.CommandPermission;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataMap;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityFlag;
import org.cloudburstmc.protocol.bedrock.data.skin.ImageData;
import org.cloudburstmc.protocol.bedrock.data.skin.SerializedSkin;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType;
import org.cloudburstmc.protocol.bedrock.packet.*;
import org.cloudburstmc.protocol.common.PacketSignal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

/**
 * Handles Bedrock gameplay after login/resource pack negotiation.
 *
 * Responsibilities:
 * - Send StartGamePacket, initial chunks, PlayStatus(PLAYER_SPAWN)
 * - Handle movement, block breaking/placement, chat, disconnect
 * - Fire server events for mod compatibility
 */
public class BedrockGameplayHandler implements BedrockPacketHandler {

    private static final double PLAYER_EYE_HEIGHT = 1.62;

    private final BedrockServerSession session;
    private final ServerWorld world;
    private final PlayerManager playerManager;
    private final ChunkManager chunkManager;
    private final BedrockBlockMapper blockMapper;
    private final BedrockChunkConverter chunkConverter;
    private final BedrockRegistryData registryData;
    private final String username;
    private final Runnable pongUpdater;

    /** Number of biome sections for overworld dimension (Y=-64 to Y=319, 24 sub-chunks). */
    private static final int OVERWORLD_SECTIONS = 24;

    private ConnectedPlayer player;
    private BedrockSessionWrapper sessionWrapper;
    private boolean chunksInitialized = false;
    private boolean disconnected = false;
    private int authInputCount = 0;

    public BedrockGameplayHandler(BedrockServerSession session, ServerWorld world,
                                  PlayerManager playerManager, ChunkManager chunkManager,
                                  BedrockBlockMapper blockMapper, BedrockChunkConverter chunkConverter,
                                  BedrockRegistryData registryData, String username,
                                  Runnable pongUpdater) {
        this.session = session;
        this.world = world;
        this.playerManager = playerManager;
        this.chunkManager = chunkManager;
        this.blockMapper = blockMapper;
        this.chunkConverter = chunkConverter;
        this.registryData = registryData;
        this.username = username;
        this.pongUpdater = pongUpdater;
    }

    /**
     * Called after resource pack negotiation completes. Sets up the player,
     * sends StartGame, chunks, and PLAYER_SPAWN.
     */
    public void onReady() {
        // Register player with Bedrock protocol version
        player = playerManager.addPlayer(username, null, ProtocolVersion.BEDROCK);
        if (player == null) {
            session.disconnect("Server is full!");
            return;
        }

        // Create session wrapper for Classic->Bedrock translation
        ClassicToBedrockTranslator translator = new ClassicToBedrockTranslator(blockMapper);
        sessionWrapper = new BedrockSessionWrapper(session, translator);
        player.setBedrockSession(sessionWrapper);

        // Determine spawn position
        Map<String, short[]> savedPositions = world.loadPlayerPositions();
        short[] savedPos = savedPositions.get(player.getUsername());

        double spawnX, spawnY, spawnZ;
        float spawnYaw = 0, spawnPitch = 0;
        if (savedPos != null && savedPos.length >= 5) {
            spawnX = savedPos[0] / 32.0;
            spawnY = savedPos[1] / 32.0; // eye-level
            spawnZ = savedPos[2] / 32.0;
            spawnYaw = (savedPos[3] & 0xFF) * 360.0f / 256.0f;
            spawnPitch = (savedPos[4] & 0xFF) * 360.0f / 256.0f;

            // Safety check: ensure player isn't inside solid blocks.
            // Fixed-point truncation can place feet slightly inside the ground,
            // so check the actual block without any epsilon.
            double feetYCheck = spawnY - PLAYER_EYE_HEIGHT;
            int feetBlockX = (int) Math.floor(spawnX);
            int feetBlockY = (int) Math.floor(feetYCheck);
            int feetBlockZ = (int) Math.floor(spawnZ);
            if (world.inBounds(feetBlockX, feetBlockY, feetBlockZ)
                    && (world.getBlock(feetBlockX, feetBlockY, feetBlockZ) != 0
                        || world.getBlock(feetBlockX, feetBlockY + 1, feetBlockZ) != 0)) {
                int[] safe = world.findSafePosition(feetBlockX, feetBlockY, feetBlockZ, 50);
                spawnX = safe[0] + 0.5;
                spawnY = safe[1] + PLAYER_EYE_HEIGHT;
                spawnZ = safe[2] + 0.5;
                System.out.println("[Bedrock] Saved position was inside blocks, relocated to "
                        + safe[0] + "," + safe[1] + "," + safe[2]);
            }
        } else {
            spawnX = world.getWidth() / 2.0 + 0.5;
            spawnZ = world.getDepth() / 2.0 + 0.5;
            // Find the actual surface: scan down from top for first solid block
            int sx = (int) Math.floor(spawnX);
            int sz = (int) Math.floor(spawnZ);
            int feetBlockY = 0;
            for (int y = world.getHeight() - 1; y >= 0; y--) {
                if (world.getBlock(sx, y, sz) != 0) {
                    feetBlockY = y + 1; // stand on top of the solid block
                    break;
                }
            }
            spawnY = feetBlockY + PLAYER_EYE_HEIGHT;
        }

        player.updatePositionDouble(spawnX, spawnY, spawnZ, spawnYaw, spawnPitch);

        long entityId = player.getPlayerId() + 1;
        float feetY = (float) (spawnY - PLAYER_EYE_HEIGHT);

        // Convert Classic yaw to Bedrock yaw for S2C
        // Internal yaw was stored as Classic byte -> degrees. Bedrock 0=South, Classic 0=North.
        float bedrockYaw = (spawnYaw + 180.0f) % 360.0f;

        // === Spawn sequence (BiomeDefinitionListPacket omitted — causes v924 client crash) ===

        // 1. StartGamePacket
        sendStartGame(entityId, (float) spawnX, feetY, (float) spawnZ, bedrockYaw, spawnPitch);
        System.out.println("[Bedrock] Sent StartGamePacket (entity=" + entityId
                + ", pos=" + spawnX + "," + feetY + "," + spawnZ + ")");

        // 2. ItemComponentPacket (empty)
        ItemComponentPacket itemComponentPkt = new ItemComponentPacket();
        session.sendPacket(itemComponentPkt);
        System.out.println("[Bedrock] Sent ItemComponentPacket");

        // 3. One empty chunk at spawn (real chunks sent later via RequestChunkRadius)
        int spawnCX = (int) Math.floor(spawnX) >> 4;
        int spawnCZ = (int) Math.floor(spawnZ) >> 4;
        sendEmptyChunk(spawnCX, spawnCZ);

        // 4. AvailableEntityIdentifiersPacket
        AvailableEntityIdentifiersPacket entityIdPkt = new AvailableEntityIdentifiersPacket();
        entityIdPkt.setIdentifiers(registryData.getEntityIdentifiers());
        session.sendPacket(entityIdPkt);

        // 5. CameraPresetsPacket (standard Bedrock camera presets)
        CameraPresetsPacket cameraPkt = new CameraPresetsPacket();
        cameraPkt.getPresets().add(CameraPreset.builder().identifier("minecraft:first_person").build());
        cameraPkt.getPresets().add(CameraPreset.builder().identifier("minecraft:third_person").build());
        cameraPkt.getPresets().add(CameraPreset.builder().identifier("minecraft:third_person_front").build());
        cameraPkt.getPresets().add(CameraPreset.builder().identifier("minecraft:free").build());
        session.sendPacket(cameraPkt);

        // 6. CreativeContentPacket (empty)
        CreativeContentPacket creativePkt = new CreativeContentPacket();
        session.sendPacket(creativePkt);

        // 7. PlayStatusPacket (PLAYER_SPAWN) — client finishes loading screen
        PlayStatusPacket spawnStatus = new PlayStatusPacket();
        spawnStatus.setStatus(PlayStatusPacket.Status.PLAYER_SPAWN);
        session.sendPacket(spawnStatus);
        System.out.println("[Bedrock] Sent PLAYER_SPAWN");

        // 8. Post-spawn packets: abilities, attributes, game rules
        sendUpdateAbilities(entityId);
        sendPlayerEntityData(entityId);

        UpdateAttributesPacket attrPkt = new UpdateAttributesPacket();
        attrPkt.setRuntimeEntityId(entityId);
        attrPkt.setAttributes(Collections.singletonList(
                new AttributeData("minecraft:movement", 0.0f, 3.4028235E38f, 0.1f, 0.1f)));
        session.sendPacket(attrPkt);

        GameRulesChangedPacket rulesPkt = new GameRulesChangedPacket();
        rulesPkt.getGameRules().add(new GameRuleData<>("naturalregeneration", false));
        session.sendPacket(rulesPkt);

        SetTimePacket timePkt = new SetTimePacket();
        timePkt.setTime(6000);
        session.sendPacket(timePkt);

        // NOTE: Existing players are sent in handle(SetLocalPlayerAsInitializedPacket)
        // after the client has loaded chunks. Sending AddPlayerPacket before chunks
        // are loaded causes the client to silently discard the entities.

        // Broadcast new player spawn to everyone else (Classic packet, translators convert).
        // This is fine early — other clients already have their chunks loaded.
        playerManager.broadcastPlayerSpawn(player);

        // Set disconnect handler
        session.getPeer().getChannel().closeFuture().addListener(future -> onDisconnect());

        System.out.println("Bedrock login complete: " + player.getUsername()
                + " (ID " + player.getPlayerId()
                + ", " + playerManager.getPlayerCount() + " online)");

        playerManager.broadcastChat((byte) 0, player.getUsername() + " joined the game");
        ServerEvents.PLAYER_JOIN.invoker().onPlayerJoin(player.getUsername(), ProtocolVersion.BEDROCK);
        pongUpdater.run();
    }

    private void sendStartGame(long entityId, float x, float y, float z,
                               float yaw, float pitch) {
        StartGamePacket sgp = new StartGamePacket();
        sgp.setUniqueEntityId(entityId);
        sgp.setRuntimeEntityId(entityId);
        sgp.setPlayerGameType(GameType.CREATIVE);
        sgp.setPlayerPosition(Vector3f.from(x, y, z));
        sgp.setRotation(Vector2f.from(yaw, pitch));
        sgp.setSeed(0L);
        sgp.setSpawnBiomeType(SpawnBiomeType.DEFAULT);
        sgp.setCustomBiomeName("");
        sgp.setDimensionId(0);
        sgp.setGeneratorId(1); // flat
        sgp.setLevelGameType(GameType.CREATIVE);
        sgp.setDifficulty(0);
        sgp.setDefaultSpawn(Vector3i.from((int) x, (int) y, (int) z));
        sgp.setAchievementsDisabled(true);
        sgp.setDayCycleStopTime(6000); // noon
        sgp.setMultiplayerGame(true);
        sgp.setBroadcastingToLan(true);
        sgp.setXblBroadcastMode(GamePublishSetting.PUBLIC);
        sgp.setPlatformBroadcastMode(GamePublishSetting.PUBLIC);
        sgp.setCommandsEnabled(true);
        sgp.setTrustingPlayers(true);
        sgp.setDefaultPlayerPermission(PlayerPermission.MEMBER);
        sgp.setServerChunkTickRange(4);
        sgp.setVanillaVersion("*");
        sgp.setLevelId("rdforward");
        sgp.setLevelName("RDForward World");
        sgp.setPremiumWorldTemplateId("00000000-0000-0000-0000-000000000000");
        sgp.setCurrentTick(-1);
        sgp.setEduEditionOffers(0);
        sgp.setEduFeaturesEnabled(false);
        sgp.setEducationProductionId("");
        sgp.setRainLevel(0);
        sgp.setLightningLevel(0);
        sgp.setTexturePacksRequired(false);
        sgp.setBonusChestEnabled(false);
        sgp.setStartingWithMap(false);
        sgp.setBehaviorPackLocked(false);
        sgp.setResourcePackLocked(false);
        sgp.setFromLockedWorldTemplate(false);
        sgp.setUsingMsaGamertagsOnly(false);
        sgp.setFromWorldTemplate(false);
        sgp.setWorldTemplateOptionLocked(false);
        sgp.setForceExperimentalGameplay(org.cloudburstmc.protocol.common.util.OptionalBoolean.empty());
        sgp.setEnchantmentSeed(0);
        sgp.setAuthoritativeMovementMode(AuthoritativeMovementMode.CLIENT);
        sgp.setRewindHistorySize(0);
        sgp.setServerAuthoritativeBlockBreaking(false);
        sgp.setInventoriesServerAuthoritative(true);
        sgp.setServerEngine("");
        sgp.setPlayerPropertyData(org.cloudburstmc.nbt.NbtMap.EMPTY);
        sgp.setWorldTemplateId(java.util.UUID.randomUUID());
        sgp.setMultiplayerCorrelationId("");
        sgp.setChatRestrictionLevel(ChatRestrictionLevel.NONE);
        sgp.setServerId("");
        sgp.setWorldId("");
        sgp.setScenarioId("");
        sgp.setOwnerId("");

        // Experiments (matching GeyserMC)
        sgp.getExperiments().add(new ExperimentData("data_driven_items", true));
        sgp.getExperiments().add(new ExperimentData("upcoming_creator_features", true));
        sgp.getExperiments().add(new ExperimentData("experimental_molang_features", true));

        session.sendPacket(sgp);
    }

    /**
     * Send UpdateAbilitiesPacket with creative-mode abilities.
     */
    private void sendUpdateAbilities(long entityId) {
        UpdateAbilitiesPacket pkt = new UpdateAbilitiesPacket();
        pkt.setUniqueEntityId(entityId);
        pkt.setPlayerPermission(PlayerPermission.OPERATOR);
        pkt.setCommandPermission(CommandPermission.ANY);

        AbilityLayer layer = new AbilityLayer();
        layer.setLayerType(AbilityLayer.Type.BASE);

        // Define which abilities are present in this layer
        layer.getAbilitiesSet().add(Ability.BUILD);
        layer.getAbilitiesSet().add(Ability.MINE);
        layer.getAbilitiesSet().add(Ability.DOORS_AND_SWITCHES);
        layer.getAbilitiesSet().add(Ability.OPEN_CONTAINERS);
        layer.getAbilitiesSet().add(Ability.ATTACK_PLAYERS);
        layer.getAbilitiesSet().add(Ability.ATTACK_MOBS);
        layer.getAbilitiesSet().add(Ability.OPERATOR_COMMANDS);
        layer.getAbilitiesSet().add(Ability.TELEPORT);
        layer.getAbilitiesSet().add(Ability.INVULNERABLE);
        layer.getAbilitiesSet().add(Ability.FLYING);
        layer.getAbilitiesSet().add(Ability.MAY_FLY);
        layer.getAbilitiesSet().add(Ability.INSTABUILD);
        layer.getAbilitiesSet().add(Ability.LIGHTNING);
        layer.getAbilitiesSet().add(Ability.FLY_SPEED);
        layer.getAbilitiesSet().add(Ability.WALK_SPEED);
        layer.getAbilitiesSet().add(Ability.MUTED);
        layer.getAbilitiesSet().add(Ability.WORLD_BUILDER);
        layer.getAbilitiesSet().add(Ability.NO_CLIP);

        // Enable the appropriate abilities for creative mode
        layer.getAbilityValues().add(Ability.BUILD);
        layer.getAbilityValues().add(Ability.MINE);
        layer.getAbilityValues().add(Ability.DOORS_AND_SWITCHES);
        layer.getAbilityValues().add(Ability.OPEN_CONTAINERS);
        layer.getAbilityValues().add(Ability.ATTACK_PLAYERS);
        layer.getAbilityValues().add(Ability.ATTACK_MOBS);
        layer.getAbilityValues().add(Ability.OPERATOR_COMMANDS);
        layer.getAbilityValues().add(Ability.TELEPORT);
        layer.getAbilityValues().add(Ability.INVULNERABLE);
        layer.getAbilityValues().add(Ability.MAY_FLY);
        layer.getAbilityValues().add(Ability.INSTABUILD);

        layer.setFlySpeed(0.05f);
        layer.setWalkSpeed(0.1f);

        pkt.getAbilityLayers().add(layer);
        session.sendPacket(pkt);
    }

    /**
     * Send SetEntityDataPacket with basic player entity metadata.
     */
    private void sendPlayerEntityData(long entityId) {
        SetEntityDataPacket pkt = new SetEntityDataPacket();
        pkt.setRuntimeEntityId(entityId);
        pkt.setTick(0);

        EntityDataMap metadata = pkt.getMetadata();
        // Standard player entity flags
        metadata.setFlag(EntityFlag.HAS_COLLISION, true);
        metadata.setFlag(EntityFlag.CAN_CLIMB, true);
        metadata.setFlag(EntityFlag.CAN_WALK, true);
        metadata.setFlag(EntityFlag.CAN_SWIM, true);
        metadata.setFlag(EntityFlag.BREATHING, true);
        metadata.setFlag(EntityFlag.HAS_GRAVITY, true);

        // Player dimensions and scale
        metadata.putType(EntityDataTypes.SCALE, 1.0f);
        metadata.putType(EntityDataTypes.WIDTH, 0.6f);
        metadata.putType(EntityDataTypes.HEIGHT, 1.8f);

        // Air supply
        metadata.putType(EntityDataTypes.AIR_SUPPLY, (short) 400);
        metadata.putType(EntityDataTypes.AIR_SUPPLY_MAX, (short) 400);

        // Player nametag
        metadata.putType(EntityDataTypes.NAME, username);

        session.sendPacket(pkt);
    }

    /**
     * Send a single empty chunk (no sub-chunks, biome data only).
     * Biome data must cover the full overworld dimension height (24 sections).
     */
    private void sendEmptyChunk(int chunkX, int chunkZ) {
        io.netty.buffer.ByteBuf data = io.netty.buffer.ByteBufAllocator.DEFAULT.buffer();
        try {
            // No sub-chunks (subChunksLength=0), but need biome palette for full dimension
            // First biome section: header=(0 << 1)|1 = 1 (0 bpb, runtime format)
            data.writeByte((0 << 1) | 1);
            org.cloudburstmc.protocol.common.util.VarInts.writeInt(data, 0); // biome ID 0
            // Remaining 23 biome sections: copy-previous marker (127 << 1 | 1 = 0xFF)
            for (int i = 1; i < OVERWORLD_SECTIONS; i++) {
                data.writeByte(0xFF);
            }
            data.writeByte(0); // border blocks count

            LevelChunkPacket packet = new LevelChunkPacket();
            packet.setChunkX(chunkX);
            packet.setChunkZ(chunkZ);
            packet.setSubChunksLength(0);
            packet.setCachingEnabled(false);
            packet.setDimension(0);
            packet.setData(data.retain());
            session.sendPacket(packet);
        } finally {
            data.release();
        }
    }


    private void sendBedrockInitialChunks(int blockX, int blockZ) {
        int viewDist = chunkManager.getViewDistance();
        int centerChunkX = blockX >> 4;
        int centerChunkZ = blockZ >> 4;
        int sent = 0;
        int nullChunks = 0;

        for (int dx = -viewDist; dx <= viewDist; dx++) {
            for (int dz = -viewDist; dz <= viewDist; dz++) {
                int cx = centerChunkX + dx;
                int cz = centerChunkZ + dz;
                AlphaChunk chunk = chunkManager.getOrLoadChunk(
                        new com.github.martinambrus.rdforward.server.ChunkCoord(cx, cz));
                if (chunk != null) {
                    LevelChunkPacket chunkPacket = chunkConverter.convertChunk(chunk);
                    session.sendPacket(chunkPacket);
                    sent++;
                } else {
                    nullChunks++;
                }
            }
        }
        System.out.println("[Bedrock] Sent " + sent + " chunks, " + nullChunks + " null"
                + " (center=" + centerChunkX + "," + centerChunkZ
                + ", viewDist=" + viewDist + ")");
    }

    private void sendAddPlayer(ConnectedPlayer existing) {
        long entityId = existing.getPlayerId() + 1;
        UUID playerUuid = UUID.nameUUIDFromBytes(
                ("RDForward:" + existing.getUsername()).getBytes());

        // 1. Send PlayerListPacket (ADD) — required before AddPlayerPacket
        //    so the client has a skin and UUID mapping for the entity.
        PlayerListPacket playerList = new PlayerListPacket();
        playerList.setAction(PlayerListPacket.Action.ADD);
        PlayerListPacket.Entry entry = new PlayerListPacket.Entry(playerUuid);
        entry.setEntityId(entityId);
        entry.setName(existing.getUsername());
        entry.setSkin(createDefaultSkin());
        entry.setXuid("");
        entry.setPlatformChatId("");
        entry.setTrustedSkin(true);
        playerList.getEntries().add(entry);
        session.sendPacket(playerList);

        // 2. Send AddPlayerPacket with entity data
        AddPlayerPacket app = new AddPlayerPacket();
        app.setUuid(playerUuid);
        app.setUsername(existing.getUsername());
        app.setUniqueEntityId(entityId);
        app.setRuntimeEntityId(entityId);

        // Internal Y is eye-level; Bedrock position is feet
        float feetY = (float) (existing.getY() / 32.0 - PLAYER_EYE_HEIGHT);
        app.setPosition(Vector3f.from(
                existing.getX() / 32.0f,
                feetY,
                existing.getZ() / 32.0f));

        float bedrockYaw = ((existing.getYaw() & 0xFF) * 360.0f / 256.0f + 180.0f) % 360.0f;
        float pitch = (existing.getPitch() & 0xFF) * 360.0f / 256.0f;
        app.setRotation(Vector3f.from(pitch, bedrockYaw, 0));
        app.setPlatformChatId("");
        app.setDeviceId("");
        app.setGameType(GameType.CREATIVE);

        // Entity metadata required for the player to render
        EntityDataMap metadata = app.getMetadata();
        metadata.setFlag(EntityFlag.HAS_COLLISION, true);
        metadata.setFlag(EntityFlag.CAN_CLIMB, true);
        metadata.setFlag(EntityFlag.CAN_WALK, true);
        metadata.setFlag(EntityFlag.CAN_SWIM, true);
        metadata.setFlag(EntityFlag.BREATHING, true);
        metadata.setFlag(EntityFlag.HAS_GRAVITY, true);
        metadata.putType(EntityDataTypes.SCALE, 1.0f);
        metadata.putType(EntityDataTypes.WIDTH, 0.6f);
        metadata.putType(EntityDataTypes.HEIGHT, 1.8f);
        metadata.putType(EntityDataTypes.AIR_SUPPLY, (short) 400);
        metadata.putType(EntityDataTypes.AIR_SUPPLY_MAX, (short) 400);
        metadata.putType(EntityDataTypes.NAME, existing.getUsername());

        session.sendPacket(app);

        // 3. MovePlayerPacket — some Bedrock clients need this after AddPlayer
        //    to make the entity actually render
        MovePlayerPacket mpp = new MovePlayerPacket();
        mpp.setRuntimeEntityId(entityId);
        mpp.setPosition(app.getPosition());
        mpp.setRotation(app.getRotation());
        mpp.setMode(MovePlayerPacket.Mode.TELEPORT);
        mpp.setOnGround(true);
        session.sendPacket(mpp);
    }

    /**
     * Create a default Steve skin for PlayerListPacket entries.
     * Uses a solid-colored 64x64 RGBA skin (not the real Steve texture,
     * but sufficient for the client to render the player entity).
     */
    private static SerializedSkin createDefaultSkin() {
        // 64x64 RGBA pixels = 16384 bytes
        byte[] skinBytes = new byte[64 * 64 * 4];
        // Fill with a simple skin color (tan/brown for body visibility)
        for (int i = 0; i < skinBytes.length; i += 4) {
            skinBytes[i] = (byte) 0xC6;     // R
            skinBytes[i + 1] = (byte) 0x8E;  // G
            skinBytes[i + 2] = (byte) 0x5A;  // B
            skinBytes[i + 3] = (byte) 0xFF;  // A (opaque)
        }
        return SerializedSkin.of(
                "Standard_Custom", "", ImageData.of(64, 64, skinBytes),
                ImageData.of(0, 0, new byte[0]), "geometry.humanoid.custom",
                "", false);
    }

    // ---- Inbound packet handling ----

    @Override
    public PacketSignal handle(MovePlayerPacket packet) {
        if (player == null) return PacketSignal.HANDLED;

        Vector3f pos = packet.getPosition();
        Vector3f rot = packet.getRotation();

        // Bedrock Y is feet position; internal is eye-level
        double eyeY = pos.getY() + PLAYER_EYE_HEIGHT;

        // If player falls below the world, teleport to spawn
        if (pos.getY() < -10) {
            teleportToSpawn(rot.getY());
            return PacketSignal.HANDLED;
        }

        // Convert Bedrock yaw (0=South) to Classic yaw (0=North): subtract 180°
        float classicYawDeg = (rot.getY() - 180.0f + 360.0f) % 360.0f;
        player.updatePositionDouble(pos.getX(), eyeY, pos.getZ(), classicYawDeg, rot.getX());

        // Broadcast as Classic PlayerTeleportPacket (eye-level, Classic yaw)
        short fixedX = (short) (pos.getX() * 32);
        short fixedY = (short) (eyeY * 32);
        short fixedZ = (short) (pos.getZ() * 32);
        byte byteYaw = (byte) ((classicYawDeg / 360.0f) * 256);
        byte bytePitch = (byte) ((rot.getX() / 360.0f) * 256);

        ServerEvents.PLAYER_MOVE.invoker().onPlayerMove(
                player.getUsername(), fixedX, fixedY, fixedZ, byteYaw, bytePitch);

        playerManager.broadcastPacketExcept(
                new PlayerTeleportPacket(player.getPlayerId(),
                        fixedX, fixedY, fixedZ, byteYaw & 0xFF, bytePitch & 0xFF),
                player);

        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(PlayerAuthInputPacket packet) {
        if (player == null) return PacketSignal.HANDLED;

        Vector3f pos = packet.getPosition();
        Vector3f rot = packet.getRotation();

        // Log first few positions for debugging
        if (authInputCount < 5) {
            System.out.println("[Bedrock] AuthInput #" + authInputCount
                    + ": pos=" + pos.getX() + "," + pos.getY() + "," + pos.getZ()
                    + " rot=" + rot.getX() + "," + rot.getY());
            authInputCount++;
        }

        // Bedrock Y is feet position; internal is eye-level
        double eyeY = pos.getY() + PLAYER_EYE_HEIGHT;

        // If player falls below the world, teleport to spawn
        if (pos.getY() < -10) {
            teleportToSpawn(rot.getY());
            return PacketSignal.HANDLED;
        }

        // Convert Bedrock yaw (0=South) to Classic yaw (0=North): subtract 180
        float classicYawDeg = (rot.getY() - 180.0f + 360.0f) % 360.0f;
        player.updatePositionDouble(pos.getX(), eyeY, pos.getZ(), classicYawDeg, rot.getX());

        // Broadcast as Classic PlayerTeleportPacket (eye-level, Classic yaw)
        short fixedX = (short) (pos.getX() * 32);
        short fixedY = (short) (eyeY * 32);
        short fixedZ = (short) (pos.getZ() * 32);
        byte byteYaw = (byte) ((classicYawDeg / 360.0f) * 256);
        byte bytePitch = (byte) ((rot.getX() / 360.0f) * 256);

        ServerEvents.PLAYER_MOVE.invoker().onPlayerMove(
                player.getUsername(), fixedX, fixedY, fixedZ, byteYaw, bytePitch);

        playerManager.broadcastPacketExcept(
                new PlayerTeleportPacket(player.getPlayerId(),
                        fixedX, fixedY, fixedZ, byteYaw & 0xFF, bytePitch & 0xFF),
                player);

        return PacketSignal.HANDLED;
    }

    /**
     * Teleport player to world spawn (on top of the highest solid block at world center).
     */
    private void teleportToSpawn(float yaw) {
        double spawnX = world.getWidth() / 2.0 + 0.5;
        double spawnZ = world.getDepth() / 2.0 + 0.5;
        int sx = (int) Math.floor(spawnX);
        int sz = (int) Math.floor(spawnZ);
        int feetBlockY = 0;
        for (int y = world.getHeight() - 1; y >= 0; y--) {
            if (world.getBlock(sx, y, sz) != 0) {
                feetBlockY = y + 1;
                break;
            }
        }
        double spawnEyeY = feetBlockY + PLAYER_EYE_HEIGHT;
        player.updatePositionDouble(spawnX, spawnEyeY, spawnZ, yaw, 0);

        MovePlayerPacket respawn = new MovePlayerPacket();
        respawn.setRuntimeEntityId(player.getPlayerId() + 1);
        respawn.setPosition(Vector3f.from((float) spawnX, (float) feetBlockY, (float) spawnZ));
        respawn.setRotation(Vector3f.from(0, yaw, 0));
        respawn.setMode(MovePlayerPacket.Mode.TELEPORT);
        respawn.setOnGround(true);
        session.sendPacket(respawn);
    }

    @Override
    public PacketSignal handle(PlayerActionPacket packet) {
        if (player == null) return PacketSignal.HANDLED;

        PlayerActionType action = packet.getAction();

        if (action == PlayerActionType.START_BREAK) {
            // Creative mode = instant break
            Vector3i pos = packet.getBlockPosition();
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();

            if (!world.inBounds(x, y, z)) return PacketSignal.HANDLED;

            byte existingBlock = world.getBlock(x, y, z);
            if (existingBlock == 0) return PacketSignal.HANDLED;

            EventResult result = ServerEvents.BLOCK_BREAK.invoker()
                    .onBlockBreak(player.getUsername(), x, y, z, existingBlock & 0xFF);
            if (result == EventResult.CANCEL) return PacketSignal.HANDLED;

            world.queueBlockChange(x, y, z, (byte) 0);
        }

        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(InventoryTransactionPacket packet) {
        if (player == null) return PacketSignal.HANDLED;

        if (packet.getTransactionType() == InventoryTransactionType.ITEM_USE) {
            int actionType = packet.getActionType();
            // actionType 0 = click block (place), 1 = click air, 2 = break
            if (actionType == 0) {
                handleBlockPlace(packet);
            } else if (actionType == 2) {
                // Break via inventory transaction (alternative to PlayerAction)
                handleBlockBreak(packet);
            }
        }

        return PacketSignal.HANDLED;
    }

    private void handleBlockPlace(InventoryTransactionPacket packet) {
        Vector3i blockPos = packet.getBlockPosition();
        int face = packet.getBlockFace();

        int targetX = blockPos.getX();
        int targetY = blockPos.getY();
        int targetZ = blockPos.getZ();

        // Offset by face direction
        switch (face) {
            case 0: targetY--; break; // bottom (-Y)
            case 1: targetY++; break; // top (+Y)
            case 2: targetZ--; break; // -Z
            case 3: targetZ++; break; // +Z
            case 4: targetX--; break; // -X
            case 5: targetX++; break; // +X
        }

        if (!world.inBounds(targetX, targetY, targetZ)) return;

        // Determine block type: RubyDung palette (grass at surface, cobblestone elsewhere)
        byte worldBlockType;
        int surfaceY = world.getHeight() * 2 / 3;
        worldBlockType = (targetY == surfaceY)
                ? (byte) BlockRegistry.GRASS
                : (byte) BlockRegistry.COBBLESTONE;

        EventResult result = ServerEvents.BLOCK_PLACE.invoker()
                .onBlockPlace(player.getUsername(), targetX, targetY, targetZ, worldBlockType & 0xFF);
        if (result == EventResult.CANCEL) return;

        // Set block directly (bypasses tick queue to avoid double-broadcast)
        if (!world.setBlock(targetX, targetY, targetZ, worldBlockType)) return;
        chunkManager.setBlock(targetX, targetY, targetZ, worldBlockType);

        // Broadcast to all other players as Classic packet
        playerManager.broadcastPacketExcept(
                new SetBlockServerPacket(targetX, targetY, targetZ, worldBlockType & 0xFF),
                player);

        // Send block confirmation to this Bedrock client
        UpdateBlockPacket ubp = new UpdateBlockPacket();
        ubp.setBlockPosition(Vector3i.from(targetX, targetY, targetZ));
        ubp.setDefinition(blockMapper.toDefinition(worldBlockType & 0xFF));
        ubp.setDataLayer(0);
        ubp.getFlags().addAll(EnumSet.of(
                UpdateBlockPacket.Flag.NEIGHBORS,
                UpdateBlockPacket.Flag.NETWORK));
        session.sendPacket(ubp);
    }

    private void handleBlockBreak(InventoryTransactionPacket packet) {
        Vector3i pos = packet.getBlockPosition();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        if (!world.inBounds(x, y, z)) return;

        byte existingBlock = world.getBlock(x, y, z);
        if (existingBlock == 0) return;

        EventResult result = ServerEvents.BLOCK_BREAK.invoker()
                .onBlockBreak(player.getUsername(), x, y, z, existingBlock & 0xFF);
        if (result == EventResult.CANCEL) return;

        world.queueBlockChange(x, y, z, (byte) 0);
    }

    @Override
    public PacketSignal handle(TextPacket packet) {
        if (player == null) return PacketSignal.HANDLED;

        String message = packet.getMessage();

        if (message.startsWith("/")) {
            String command = message.substring(1);
            boolean handled = CommandRegistry.dispatch(command, player.getUsername(), false,
                    reply -> playerManager.sendChat(player, reply));
            if (!handled) {
                playerManager.sendChat(player, "Unknown command: " + command.split("\\s+")[0]);
            }
            return PacketSignal.HANDLED;
        }

        EventResult result = ServerEvents.CHAT.invoker().onChat(player.getUsername(), message);
        if (result == EventResult.CANCEL) return PacketSignal.HANDLED;

        System.out.println("[Chat] " + player.getUsername() + ": " + message);
        playerManager.broadcastChat(player.getPlayerId(), player.getUsername() + ": " + message);

        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(RequestNetworkSettingsPacket packet) {
        // Shouldn't happen during gameplay, but ignore gracefully
        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(ServerboundLoadingScreenPacket packet) {
        System.out.println("[Bedrock] Loading screen: type=" + packet.getType()
                + ", id=" + packet.getLoadingScreenId());
        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(ClientCacheStatusPacket packet) {
        System.out.println("[Bedrock] Client cache status: supported=" + packet.isSupported());
        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(SetLocalPlayerAsInitializedPacket packet) {
        System.out.println("[Bedrock] Client initialized (entity=" + packet.getRuntimeEntityId() + ")");

        // Now that the client has loaded chunks and is fully initialized,
        // send existing players so they render correctly.
        if (player != null) {
            for (ConnectedPlayer existing : playerManager.getAllPlayers()) {
                if (existing != player) {
                    sendAddPlayer(existing);
                    System.out.println("[Bedrock] Sent AddPlayer for " + existing.getUsername()
                            + " (entity=" + (existing.getPlayerId() + 1)
                            + ", pos=" + existing.getX() / 32.0f + ","
                            + existing.getY() / 32.0f + ","
                            + existing.getZ() / 32.0f + ")");
                }
            }
        }

        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(RequestChunkRadiusPacket packet) {
        System.out.println("[Bedrock] Client requested chunk radius: " + packet.getRadius());

        // Use the client's requested radius (capped at 16) so empty chunks cover
        // the full area — prevents invisible solid blocks beyond the world edge
        int radius = Math.min(packet.getRadius(), 16);

        ChunkRadiusUpdatedPacket response = new ChunkRadiusUpdatedPacket();
        response.setRadius(radius);
        session.sendPacket(response);

        // On first request, register with chunk manager and send real world chunks
        if (!chunksInitialized && player != null) {
            chunksInitialized = true;
            chunkManager.addPlayer(player);

            double spawnX = player.getX() / 32.0;
            double spawnZ = player.getZ() / 32.0;
            float feetY = (float) (player.getY() / 32.0 - PLAYER_EYE_HEIGHT);
            int spawnBlockX = (int) Math.floor(spawnX);
            int spawnBlockZ = (int) Math.floor(spawnZ);
            int spawnCX = spawnBlockX >> 4;
            int spawnCZ = spawnBlockZ >> 4;

            // Tell client which area chunks will be published for
            NetworkChunkPublisherUpdatePacket publisherPkt = new NetworkChunkPublisherUpdatePacket();
            publisherPkt.setPosition(Vector3i.from(spawnBlockX, (int) feetY, spawnBlockZ));
            publisherPkt.setRadius(radius * 16);
            session.sendPacket(publisherPkt);

            // Send chunks for full radius — empty chunks for out-of-world areas
            int maxChunkX = (world.getWidth() - 1) >> 4;
            int maxChunkZ = (world.getDepth() - 1) >> 4;
            int sent = 0;
            int empty = 0;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int cx = spawnCX + dx;
                    int cz = spawnCZ + dz;
                    if (cx < 0 || cx > maxChunkX || cz < 0 || cz > maxChunkZ) {
                        sendEmptyChunk(cx, cz);
                        empty++;
                    } else {
                        LevelChunkPacket chunkPkt = chunkConverter.convertWorldColumn(world, cx, cz);
                        session.sendPacket(chunkPkt);
                        sent++;
                    }
                }
            }
            System.out.println("[Bedrock] Sent " + sent + " world chunks, " + empty + " empty");
        }

        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(TickSyncPacket packet) {
        // Respond to tick sync with server timestamp
        TickSyncPacket response = new TickSyncPacket();
        response.setRequestTimestamp(packet.getRequestTimestamp());
        response.setResponseTimestamp(System.currentTimeMillis());
        session.sendPacket(response);
        return PacketSignal.HANDLED;
    }

    @Override
    public void onDisconnect(CharSequence reason) {
        System.out.println("[Bedrock] Disconnect reason: " + reason);
        onDisconnect();
    }

    @Override
    public PacketSignal handle(ServerboundDiagnosticsPacket packet) {
        return PacketSignal.HANDLED; // silently ignore
    }

    @Override
    public PacketSignal handle(AnimatePacket packet) {
        return PacketSignal.HANDLED; // silently ignore
    }

    @Override
    public PacketSignal handle(InteractPacket packet) {
        return PacketSignal.HANDLED; // silently ignore
    }

    // Catch-all: log any packet we don't explicitly handle
    @Override
    public PacketSignal handlePacket(org.cloudburstmc.protocol.bedrock.packet.BedrockPacket packet) {
        PacketSignal result = packet.handle(this);
        if (result == PacketSignal.UNHANDLED) {
            System.out.println("[Bedrock] Unhandled C2S: " + packet.getClass().getSimpleName());
        }
        return result;
    }

    private void onDisconnect() {
        if (player == null || disconnected) return;
        disconnected = true;

        System.out.println(player.getUsername() + " disconnected"
                + " (" + (playerManager.getPlayerCount() - 1) + " online)");
        ServerEvents.PLAYER_LEAVE.invoker().onPlayerLeave(player.getUsername());
        world.rememberPlayerPosition(player);
        chunkManager.removePlayer(player);
        playerManager.broadcastChat((byte) 0, player.getUsername() + " left the game");
        playerManager.broadcastPlayerDespawn(player);
        playerManager.removePlayerById(player.getPlayerId());
        pongUpdater.run();
    }

    public ConnectedPlayer getPlayer() {
        return player;
    }
}
