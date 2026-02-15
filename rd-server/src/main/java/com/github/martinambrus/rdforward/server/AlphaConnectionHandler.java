package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.codec.RawPacketDecoder;
import com.github.martinambrus.rdforward.protocol.event.EventResult;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.alpha.*;
import com.github.martinambrus.rdforward.protocol.packet.classic.PlayerTeleportPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockServerPacket;
import com.github.martinambrus.rdforward.server.api.CommandRegistry;
import java.util.List;
import com.github.martinambrus.rdforward.server.event.ServerEvents;
import com.github.martinambrus.rdforward.world.BlockRegistry;
import io.netty.channel.ChannelHandlerContext;
import java.util.concurrent.TimeUnit;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;

import java.util.Map;

/**
 * Handles individual Alpha client connections on the server side.
 *
 * Manages the full Alpha client lifecycle:
 * 1. Handshake (0x02) -> respond with HandshakeS2C("-")
 * 2. Login (0x01) -> register player, send LoginS2C, chunks, position
 * 3. Gameplay packet routing (position, digging, placement, chat)
 * 4. Disconnect/cleanup
 *
 * Incoming Alpha packets are handled directly. Outgoing Classic packets
 * (from PlayerManager broadcasts) are translated by ClassicToAlphaTranslator
 * in the outbound pipeline.
 */
public class AlphaConnectionHandler extends SimpleChannelInboundHandler<Packet> {

    private static final int LOGIN_TIMEOUT_SECONDS = 5;

    /**
     * Player eye height in blocks. Must use (double) 1.62f to match the MC Alpha
     * client's float yOffset precision. The client computes feet = posY - (double)(1.62f).
     * Using the double literal 1.62 causes a ~5e-9 precision mismatch that places
     * feet fractionally below block boundaries, defeating collision detection.
     */
    private static final double PLAYER_EYE_HEIGHT = (double) 1.62f;
    /** Same in fixed-point units, rounded up to avoid sub-block clipping. */
    static final int PLAYER_EYE_HEIGHT_FIXED = (int) Math.ceil(PLAYER_EYE_HEIGHT * 32);

    private final ProtocolVersion serverVersion;
    private final ServerWorld world;
    private final PlayerManager playerManager;
    private final ChunkManager chunkManager;

    /**
     * Delay (ms) after the last block placement before replenishing cobblestone.
     * Batching replenishment avoids sending AddToInventory packets during rapid
     * placement, which can confuse the Alpha client's input handling.
     */
    private static final long REPLENISH_DELAY_MS = 1000;

    private String pendingUsername;
    private ProtocolVersion clientVersion;
    private ConnectedPlayer player;
    private boolean loginComplete = false;

    /**
     * Server-side estimate of the player's cobblestone count. Corrected by
     * inventory sync packets (0x05) from the client. Used to calculate how
     * many to give back when replenishing (top up to 64).
     */
    private int trackedCobblestone = 0;
    private java.util.concurrent.ScheduledFuture<?> replenishTask;

    /**
     * Tracks the feet Y where the current fall started. Used for pre-rewrite
     * clients (v13/v14) that lack UpdateHealthPacket — when the fall exceeds
     * 10 blocks, the server teleports the player to the ground to prevent
     * lethal fall damage.
     */
    private double fallStartFeetY = Double.NaN;

    public AlphaConnectionHandler(ProtocolVersion serverVersion, ServerWorld world,
                                  PlayerManager playerManager, ChunkManager chunkManager) {
        this.serverVersion = serverVersion;
        this.world = world;
        this.playerManager = playerManager;
        this.chunkManager = chunkManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        if (!loginComplete) {
            if (packet instanceof HandshakeC2SPacket) {
                handleHandshake(ctx, (HandshakeC2SPacket) packet);
            } else if (packet instanceof LoginC2SPacket) {
                handleLogin(ctx, ((LoginC2SPacket) packet).getProtocolVersion());
            }
            // Ignore other packets before login completes
            return;
        }

        // Route gameplay packets
        if (packet instanceof PlayerPositionAndLookC2SPacket) {
            handlePositionAndLook(ctx, (PlayerPositionAndLookC2SPacket) packet);
        } else if (packet instanceof PlayerPositionPacket) {
            handlePosition(ctx, (PlayerPositionPacket) packet);
        } else if (packet instanceof PlayerLookPacket) {
            handleLook(ctx, (PlayerLookPacket) packet);
        } else if (packet instanceof PlayerOnGroundPacket) {
            // No-op: no position data, just onGround flag
        } else if (packet instanceof PlayerDiggingPacket) {
            handleDigging(ctx, (PlayerDiggingPacket) packet);
        } else if (packet instanceof BlockPlacementData) {
            handleBlockPlacement(ctx, (BlockPlacementData) packet);
        } else if (packet instanceof ChatPacket) {
            handleChat(ctx, (ChatPacket) packet);
        } else if (packet instanceof HoldingChangePacket) {
            // Silently accept (hotbar slot change)
        } else if (packet instanceof AnimationPacket) {
            // Silently accept (arm swing animation)
        } else if (packet instanceof PlayerInventoryPacket) {
            handleInventorySync((PlayerInventoryPacket) packet);
        } else if (packet instanceof PickupSpawnPacket) {
            // Give back dropped cobblestone immediately. Q-drops are single
            // events that don't interfere with the client's input handling.
            // Use the actual count from the packet (Q may drop a full stack).
            int droppedCount = ((PickupSpawnPacket) packet).getCount() & 0xFF;
            if (droppedCount < 1) droppedCount = 1;
            giveItem(ctx, BlockRegistry.COBBLESTONE, droppedCount);
        } else if (packet instanceof KeepAlivePacket) {
            // Silently accept
        } else if (packet instanceof DisconnectPacket) {
            ctx.close();
        }
    }

    private void handleHandshake(ChannelHandlerContext ctx, HandshakeC2SPacket packet) {
        pendingUsername = packet.getUsername();
        // Respond with offline-mode hash
        ctx.writeAndFlush(new HandshakeS2CPacket("-"));
    }

    private void handleLogin(ChannelHandlerContext ctx, int loginProtocolVersion) {
        if (pendingUsername == null) {
            ctx.writeAndFlush(new DisconnectPacket("Handshake not received"));
            ctx.close();
            return;
        }

        // Determine protocol version from the client's login packet
        clientVersion = ProtocolVersion.fromNumber(loginProtocolVersion);
        if (clientVersion == null) {
            String[] messages = buildUnsupportedVersionMessages(loginProtocolVersion);
            System.out.println("Rejected client"
                    + (pendingUsername != null ? " (" + pendingUsername + ")" : "")
                    + ": " + messages[1]);
            ctx.writeAndFlush(new DisconnectPacket(messages[0]));
            ctx.close();
            return;
        }

        // Update decoder's protocol version if needed
        RawPacketDecoder decoder = ctx.pipeline().get(RawPacketDecoder.class);
        if (decoder != null) {
            decoder.setProtocolVersion(clientVersion);
        }

        // Pre-1.2.0 Alpha clients have a broken chunk-distance comparator in
        // RenderGlobal that violates Java 7+'s TimSort transitivity contract.
        // On first connect (no saved position), kick with the required JVM flag
        // and save a default spawn position so they're recognized next time.
        if (!clientVersion.isAtLeast(ProtocolVersion.ALPHA_1_2_0)) {
            Map<String, short[]> savedPositions = world.loadPlayerPositions();
            if (!savedPositions.containsKey(pendingUsername.trim())) {
                System.out.println("Rejected " + pendingUsername
                        + ": " + clientVersion.getDisplayName() + " requires JVM flags (first connect)");
                // Save default spawn so they're allowed through next time
                world.savePlayerPosition(pendingUsername.trim());
                ctx.writeAndFlush(new DisconnectPacket(
                        "Add this Java flag to play: -Djava.util.Arrays.useLegacyMergeSort=true"));
                ctx.close();
                return;
            }
        }

        // If a non-blank username is already online, kick the old connection
        if (pendingUsername != null && !pendingUsername.trim().isEmpty()) {
            playerManager.kickDuplicatePlayer(pendingUsername.trim(), world);
        }

        // Register the player
        player = playerManager.addPlayer(pendingUsername, ctx.channel(), clientVersion);
        if (player == null) {
            ctx.writeAndFlush(new DisconnectPacket("Server is full!"));
            ctx.close();
            return;
        }

        // Entity ID: playerId + 1 (entity 0 is sometimes special in Alpha)
        int entityId = player.getPlayerId() + 1;

        // Send LoginS2C (v2 and earlier use shorter format without mapSeed/dimension)
        if (clientVersion.isAtLeast(ProtocolVersion.ALPHA_1_2_0)) {
            ctx.writeAndFlush(new LoginS2CPacket(entityId, 0L, (byte) 0));
        } else {
            ctx.writeAndFlush(new LoginS2CPacketV2(entityId));
        }

        // Determine spawn position
        Map<String, short[]> savedPositions = world.loadPlayerPositions();
        short[] savedPos = savedPositions.get(player.getUsername());

        double spawnX, spawnY, spawnZ;
        float spawnYaw = 0, spawnPitch = 0;
        if (savedPos != null) {
            // Convert fixed-point back to double
            spawnX = savedPos[0] / 32.0;
            spawnY = savedPos[1] / 32.0;
            spawnZ = savedPos[2] / 32.0;
            spawnYaw = (savedPos[3] & 0xFF) * 360.0f / 256.0f;
            spawnPitch = (savedPos[4] & 0xFF) * 360.0f / 256.0f;

            // Snap feet to nearest block surface if within fixed-point tolerance.
            // Even with Math.round on save, fixed-point can shift feet up to
            // 1/64 block below a surface. If feet are that close to the NEXT
            // integer Y, snap upward to prevent sinking into the block.
            double feetY = spawnY - PLAYER_EYE_HEIGHT;
            double fracFeet = feetY - Math.floor(feetY);
            if (fracFeet > 1.0 - (1.0 / 16.0)) {
                feetY = Math.ceil(feetY);
                spawnY = feetY + PLAYER_EYE_HEIGHT;
            }

            int feetBlockX = (int) Math.floor(spawnX);
            int feetBlockY = (int) Math.floor(feetY);
            int feetBlockZ = (int) Math.floor(spawnZ);
            if (world.inBounds(feetBlockX, feetBlockY, feetBlockZ)
                    && (world.getBlock(feetBlockX, feetBlockY, feetBlockZ) != 0
                        || world.getBlock(feetBlockX, feetBlockY + 1, feetBlockZ) != 0)) {
                int[] safe = world.findSafePosition(feetBlockX, feetBlockY, feetBlockZ, 50);
                spawnX = safe[0] + 0.5;
                spawnY = safe[1] + PLAYER_EYE_HEIGHT;
                spawnZ = safe[2] + 0.5;
                System.out.println("Saved position was inside blocks, relocated to "
                        + safe[0] + "," + safe[1] + "," + safe[2]);
            } else {
                System.out.println("Restored position for " + player.getUsername());
            }
        } else {
            // Default: center of world, find safe position on terrain.
            // Internal Y convention is eye-level (feet + 1.62) to match Classic.
            int cx = world.getWidth() / 2;
            int cz = world.getDepth() / 2;
            int heuristicY = world.getHeight() * 2 / 3 + 1;
            int[] safe = world.findSafePosition(cx, heuristicY, cz, 50);
            spawnX = safe[0] + 0.5;
            spawnY = safe[1] + PLAYER_EYE_HEIGHT; // eye-level
            spawnZ = safe[2] + 0.5;
        }

        // Update player position — internal convention is eye-level Y
        player.updatePositionDouble(spawnX, spawnY, spawnZ, spawnYaw, spawnPitch);

        // Send spawn position (block coords for compass). SpawnPosition (0x06) was
        // added in Alpha 1.1.0 (v2) alongside the compass item. v1 clients don't
        // have this packet and throw "Bad packet id 6" if they receive it.
        int spawnBlockX = (int) Math.floor(spawnX);
        int spawnBlockY = (int) Math.floor(spawnY);
        int spawnBlockZ = (int) Math.floor(spawnZ);
        if (clientVersion.isAtLeast(ProtocolVersion.ALPHA_1_1_0)) {
            ctx.writeAndFlush(new SpawnPositionPacket(spawnBlockX, spawnBlockY, spawnBlockZ));
        }

        // Send player position and look BEFORE chunks.
        // The Alpha 1.1.x client's RenderGlobal chunk comparator violates
        // TimSort's transitivity contract (Java 7+). Sending position first
        // ensures the client knows its location when the first render frame
        // sorts chunks by distance, avoiding the pathological case.
        //
        // spawnY is eye-level (internal convention). Alpha S2C needs posY and feet.
        double feetY = spawnY - PLAYER_EYE_HEIGHT;
        double posY = feetY + PLAYER_EYE_HEIGHT;

        // S2C y = posY (eyes), stance = feet. The client sets posY from y
        // and computes BB.minY (feet) = posY - (double)(1.62f).
        // spawnYaw is Classic convention; Alpha client expects Alpha (0=South)
        float alphaSpawnYaw = (spawnYaw + 180.0f) % 360.0f;
        ctx.writeAndFlush(new PlayerPositionAndLookS2CPacket(
                spawnX, posY, feetY, spawnZ, alphaSpawnYaw, spawnPitch, true));

        // Initialize fall tracking for pre-rewrite clients
        fallStartFeetY = feetY;

        // Register for chunk tracking and send initial chunks.
        chunkManager.addPlayer(player);
        chunkManager.sendInitialChunks(player, spawnBlockX, spawnBlockZ);

        // Send existing players to this client (as Alpha spawn packets).
        // Internal Y is eye-level; Alpha SpawnPlayerPacket expects feet Y.
        for (ConnectedPlayer existing : playerManager.getAllPlayers()) {
            if (existing != player) {
                int existingEntityId = existing.getPlayerId() + 1;
                int existingFeetY = (int) existing.getY() - PLAYER_EYE_HEIGHT_FIXED;
                ctx.writeAndFlush(new com.github.martinambrus.rdforward.protocol.packet.alpha.SpawnPlayerPacket(
                        existingEntityId, existing.getUsername(),
                        (int) existing.getX(), existingFeetY, (int) existing.getZ(),
                        (existing.getYaw() + 128) & 0xFF, existing.getPitch() & 0xFF, (short) 0));
            }
        }

        // Broadcast new player's spawn to everyone else (as Classic packet, translator converts)
        playerManager.broadcastPlayerSpawn(player);

        loginComplete = true;

        // Give the player cobblestone so they can place blocks.
        giveItem(ctx, BlockRegistry.COBBLESTONE, 64);
        trackedCobblestone = 64;

        // Pre-rewrite clients (v13/v14) can't receive UpdateHealthPacket, so
        // fall damage is handled client-side with no server override. Give them
        // cooked pork chops (item 320, heals 4 hearts each, max stack 1) so
        // they can eat to recover from any residual fall damage.
        if (!clientVersion.isAtLeast(ProtocolVersion.ALPHA_1_0_17)) {
            for (int i = 0; i < 35; i++) {
                giveItem(ctx, 320, 1);
            }
        }

        // Remove login timeout
        if (ctx.pipeline().get("loginTimeout") != null) {
            ctx.pipeline().remove("loginTimeout");
        }

        System.out.println("Alpha login complete: " + player.getUsername()
                + " (protocol: " + clientVersion.getDisplayName()
                + ", version " + clientVersion.getVersionNumber()
                + ", ID " + player.getPlayerId()
                + ", " + playerManager.getPlayerCount() + " online)");

        playerManager.broadcastChat((byte) 0, player.getUsername() + " joined the game");
        ServerEvents.PLAYER_JOIN.invoker().onPlayerJoin(player.getUsername(), clientVersion);
    }

    private void handlePositionAndLook(ChannelHandlerContext ctx,
                                       PlayerPositionAndLookC2SPacket packet) {
        if (player == null) return;
        // Alpha yaw 0=South; internal convention is Classic 0=North. Convert by adding 180°.
        float classicYaw = (packet.getYaw() + 180.0f) % 360.0f;
        updatePosition(ctx, packet.getX(), packet.getY(), packet.getZ(),
                classicYaw, packet.getPitch());
    }

    private void handlePosition(ChannelHandlerContext ctx, PlayerPositionPacket packet) {
        if (player == null) return;
        updatePosition(ctx, packet.getX(), packet.getY(), packet.getZ(),
                player.getFloatYaw(), player.getFloatPitch());
    }

    private void handleLook(ChannelHandlerContext ctx, PlayerLookPacket packet) {
        if (player == null) return;
        // Alpha yaw 0=South; internal convention is Classic 0=North. Convert by adding 180°.
        float classicYaw = (packet.getYaw() + 180.0f) % 360.0f;
        // doubleY is eye-level (internal convention), but updatePosition expects feet Y
        updatePosition(ctx, player.getDoubleX(),
                player.getDoubleY() - PLAYER_EYE_HEIGHT, player.getDoubleZ(),
                classicYaw, packet.getPitch());
    }

    private void updatePosition(ChannelHandlerContext ctx,
                                double x, double y, double z, float yaw, float pitch) {
        // y parameter is FEET position (Alpha convention).
        // Internal convention is eye-level (Classic compatible).
        double eyeY = y + PLAYER_EYE_HEIGHT;

        // Pre-rewrite clients (v13/v14) don't support SpawnPosition (0x06),
        // so their world spawn stays at (0, 0, 0). When the player dies, the
        // client creates a new entity at (0.5, ?, 0.5) with a fresh inventory.
        // Detect this position jump and redirect to a proper safe spawn.
        if (!clientVersion.isAtLeast(ProtocolVersion.ALPHA_1_0_17)
                && x < 2 && z < 2 && x >= 0 && z >= 0) {
            double prevX = player.getDoubleX();
            double prevZ = player.getDoubleZ();
            double dxSq = (prevX - x) * (prevX - x);
            double dzSq = (prevZ - z) * (prevZ - z);
            if (dxSq + dzSq > 100) { // > 10 blocks away = respawn, not walking
                respawnToSafePosition(ctx, yaw);
                return;
            }
        }

        // Fall protection for pre-rewrite clients (v13/v14) that lack
        // UpdateHealthPacket. Track fall distance and teleport to ground
        // before lethal fall damage can occur.
        if (!clientVersion.isAtLeast(ProtocolVersion.ALPHA_1_0_17)) {
            double prevFeetY = player.getDoubleY() - PLAYER_EYE_HEIGHT;
            if (y >= prevFeetY) {
                // Moving up or level — reset fall start
                fallStartFeetY = y;
            } else if (fallStartFeetY - y > 5) {
                // Falling more than 5 blocks — teleport to ground
                teleportToGround(ctx, x, z, yaw, pitch);
                return;
            }
        }

        // If player falls below the world, teleport to spawn
        if (y < -10) {
            respawnToSafePosition(ctx, yaw);
            return;
        }

        // Convert to fixed-point for Classic broadcast (eye-level)
        short fixedX = toFixedPoint(x);
        short fixedY = toFixedPoint(eyeY);
        short fixedZ = toFixedPoint(z);
        byte byteYaw = toByteRotation(yaw);
        byte bytePitch = toByteRotation(pitch);

        // Store eye-level Y internally for Classic compatibility
        player.updatePositionDouble(x, eyeY, z, yaw, pitch);

        // Fire move event (eye-level fixed-point)
        ServerEvents.PLAYER_MOVE.invoker().onPlayerMove(
                player.getUsername(), fixedX, fixedY, fixedZ, byteYaw, bytePitch);

        // Broadcast as Classic PlayerTeleportPacket (eye-level, translator adjusts for Alpha)
        playerManager.broadcastPacketExcept(
                new PlayerTeleportPacket(player.getPlayerId(),
                        fixedX, fixedY, fixedZ, byteYaw & 0xFF, bytePitch & 0xFF),
                player);
    }

    /**
     * Teleport the player to a safe ground-level position at world center
     * and re-give cobblestone. Used for void-fall recovery and pre-rewrite
     * client respawn (v13/v14 don't support SpawnPosition so the client
     * respawns at (0, 0, 0) with a fresh empty inventory).
     */
    private void respawnToSafePosition(ChannelHandlerContext ctx, float yaw) {
        int cx = world.getWidth() / 2;
        int cz = world.getDepth() / 2;
        int heuristicY = world.getHeight() * 2 / 3 + 1;
        int[] safe = world.findSafePosition(cx, heuristicY, cz, 50);
        double spawnX = safe[0] + 0.5;
        double spawnFeetY = safe[1];
        double spawnEyeY = spawnFeetY + PLAYER_EYE_HEIGHT;
        double spawnZ = safe[2] + 0.5;
        player.updatePositionDouble(spawnX, spawnEyeY, spawnZ, yaw, 0);

        // S2C y = posY (eyes), stance = feet
        // yaw is Classic convention internally; Alpha client expects Alpha (0=South)
        float alphaYaw = (yaw + 180.0f) % 360.0f;
        ctx.writeAndFlush(new PlayerPositionAndLookS2CPacket(
                spawnX, spawnFeetY + PLAYER_EYE_HEIGHT, spawnFeetY, spawnZ,
                alphaYaw, 0, true));

        // Update chunk tracking for the new position
        chunkManager.updatePlayerChunks(player);

        // Broadcast updated position to other players
        short fixedX = toFixedPoint(spawnX);
        short fixedY = toFixedPoint(spawnEyeY);
        short fixedZ = toFixedPoint(spawnZ);
        byte byteYaw = toByteRotation(yaw);
        playerManager.broadcastPacketExcept(
                new PlayerTeleportPacket(player.getPlayerId(),
                        fixedX, fixedY, fixedZ, byteYaw & 0xFF, 0),
                player);

        // Reset fall tracking
        fallStartFeetY = spawnFeetY;
    }

    /**
     * Teleport the player to the ground directly below their current XZ
     * position. Used by fall protection to catch the player before lethal
     * fall damage. Scans downward for a safe landing spot (solid ground
     * with 2 air blocks above).
     */
    private void teleportToGround(ChannelHandlerContext ctx,
                                   double x, double z, float yaw, float pitch) {
        int blockX = (int) Math.floor(x);
        int blockZ = (int) Math.floor(z);
        int startY = Math.min((int) Math.floor(fallStartFeetY), world.getHeight() - 3);

        // Scan downward from where the fall started to find ground
        int groundFeetY = -1;
        for (int testY = startY; testY >= 0; testY--) {
            if (world.inBounds(blockX, testY, blockZ)
                    && world.getBlock(blockX, testY, blockZ) != 0
                    && world.getBlock(blockX, testY + 1, blockZ) == 0
                    && world.getBlock(blockX, testY + 2, blockZ) == 0) {
                groundFeetY = testY + 1;
                break;
            }
        }
        if (groundFeetY < 0) {
            // No ground below — fall back to world center spawn
            respawnToSafePosition(ctx, yaw);
            return;
        }

        double landX = blockX + 0.5;
        double landFeetY = groundFeetY;
        double landEyeY = landFeetY + PLAYER_EYE_HEIGHT;
        double landZ = blockZ + 0.5;
        player.updatePositionDouble(landX, landEyeY, landZ, yaw, pitch);
        fallStartFeetY = landFeetY;

        float alphaYaw = (yaw + 180.0f) % 360.0f;
        ctx.writeAndFlush(new PlayerPositionAndLookS2CPacket(
                landX, landFeetY + PLAYER_EYE_HEIGHT, landFeetY, landZ,
                alphaYaw, pitch, true));

        // Update chunk tracking and broadcast to other players
        chunkManager.updatePlayerChunks(player);
        short fixedX = toFixedPoint(landX);
        short fixedY = toFixedPoint(landEyeY);
        short fixedZ = toFixedPoint(landZ);
        byte byteYaw = toByteRotation(yaw);
        byte bytePitch = toByteRotation(pitch);
        playerManager.broadcastPacketExcept(
                new PlayerTeleportPacket(player.getPlayerId(),
                        fixedX, fixedY, fixedZ, byteYaw & 0xFF, bytePitch & 0xFF),
                player);
    }

    private void handleDigging(ChannelHandlerContext ctx, PlayerDiggingPacket packet) {
        if (player == null) return;

        // Alpha creative mode: instant block breaking on STARTED or FINISHED
        if (packet.getStatus() == PlayerDiggingPacket.STATUS_STARTED
                || packet.getStatus() == PlayerDiggingPacket.STATUS_FINISHED) {

            int x = packet.getX();
            int y = packet.getY();
            int z = packet.getZ();

            if (!world.inBounds(x, y, z)) return;

            byte existingBlock = world.getBlock(x, y, z);
            if (existingBlock == 0) return;

            EventResult result = ServerEvents.BLOCK_BREAK.invoker()
                    .onBlockBreak(player.getUsername(), x, y, z, existingBlock & 0xFF);
            if (result == EventResult.CANCEL) return;

            world.queueBlockChange(x, y, z, (byte) 0);
        }
    }

    private void handleBlockPlacement(ChannelHandlerContext ctx, BlockPlacementData packet) {
        if (player == null) return;

        int x = packet.getX();
        int y = packet.getY();
        int z = packet.getZ();
        int direction = packet.getDirection();

        // Special case: direction -1 means "use item" without placing, ignore
        if (direction == -1) return;

        // Compute target position based on face
        int targetX = x;
        int targetY = y;
        int targetZ = z;
        switch (direction) {
            case 0: targetY--; break; // bottom (-Y)
            case 1: targetY++; break; // top (+Y)
            case 2: targetZ--; break; // -Z
            case 3: targetZ++; break; // +Z
            case 4: targetX--; break; // -X
            case 5: targetX++; break; // +X
        }

        short itemId = packet.getItemId();
        if (itemId < 0) return;

        // Items (item ID >= 256) and non-placeable blocks are not valid for placement.
        // Without this check, item IDs get truncated by (byte)(itemId & 0xFF),
        // e.g. cooked pork chop (320) would become block 64.
        if (!BlockRegistry.isValidBlock(itemId, serverVersion)) {
            ctx.writeAndFlush(new BlockChangePacket(targetX, targetY, targetZ, 0, 0));
            return;
        }

        if (!world.inBounds(targetX, targetY, targetZ)) {
            // Cancel the client's predicted block immediately (otherwise the
            // phantom block persists for ~4 seconds, allowing further building
            // above the world height limit).
            ctx.writeAndFlush(new BlockChangePacket(targetX, targetY, targetZ, 0, 0));
            // Don't decrement trackedCobblestone — the client's own OOB check
            // usually prevents consumption. But during rapid jumping, some
            // clicks may target in-bounds side faces that the client consumes
            // before the server rejects. Schedule replenishment so the
            // inventory sync (which has the real count) can drive a top-up.
            // v1 has no inventory sync and no way to know if the client
            // consumed, so skip the give-back to avoid inflation.
            if (clientVersion.isAtLeast(ProtocolVersion.ALPHA_1_1_0)) {
                scheduleReplenishment(ctx);
            }
            return;
        }

        // Prevent placing blocks inside the player's body.
        // Player AABB: 0.6 wide (±0.3), 1.8 tall from feet.
        double px = player.getDoubleX();
        double feetY = player.getDoubleY() - PLAYER_EYE_HEIGHT;
        double pz = player.getDoubleZ();
        boolean overlapsX = targetX < px + 0.3 && px - 0.3 < targetX + 1;
        boolean overlapsY = targetY < feetY + 1.8 && feetY < targetY + 1;
        boolean overlapsZ = targetZ < pz + 0.3 && pz - 0.3 < targetZ + 1;
        if (overlapsX && overlapsY && overlapsZ) {
            ctx.writeAndFlush(new BlockChangePacket(targetX, targetY, targetZ, 0, 0));
            // Count as consumed for v2+: during rapid jumping the client's
            // position is ahead of the server's, so the client's own overlap
            // check may pass (consuming the item) while the server's check
            // fails. The batched replenishment tops up to 64, so slight
            // over-counting is harmless.
            // v1 has no inventory sync — skip give-back to avoid inflation
            // when both client and server reject the overlap.
            if (clientVersion.isAtLeast(ProtocolVersion.ALPHA_1_1_0)) {
                trackedCobblestone--;
                scheduleReplenishment(ctx);
            }
            return;
        }

        // Determine world block type: RubyDung palette overrides surface to grass
        byte worldBlockType;
        if (serverVersion == ProtocolVersion.RUBYDUNG) {
            int surfaceY = world.getHeight() * 2 / 3;
            worldBlockType = (targetY == surfaceY)
                    ? (byte) BlockRegistry.GRASS
                    : (byte) BlockRegistry.COBBLESTONE;
        } else {
            worldBlockType = (byte) (itemId & 0xFF);
        }

        EventResult result = ServerEvents.BLOCK_PLACE.invoker()
                .onBlockPlace(player.getUsername(), targetX, targetY, targetZ, worldBlockType & 0xFF);
        if (result == EventResult.CANCEL) return;

        // Set block directly in world (bypasses tick queue to avoid double-broadcast)
        if (!world.setBlock(targetX, targetY, targetZ, worldBlockType)) return;
        chunkManager.setBlock(targetX, targetY, targetZ, worldBlockType);

        // Broadcast grass/cobblestone to all other players (Classic translator handles Alpha)
        playerManager.broadcastPacketExcept(
                new SetBlockServerPacket(targetX, targetY, targetZ, worldBlockType & 0xFF), player);

        // Confirm block to this Alpha client. Without server confirmation,
        // the client reverts its predicted blocks after ~4 seconds (80 ticks).
        ctx.writeAndFlush(new BlockChangePacket(
                targetX, targetY, targetZ, itemId & 0xFF, 0));

        // If the world block differs from held item (e.g. grass at surface),
        // send a delayed conversion so the client sees: cobble placed → transforms to grass
        if (worldBlockType != (byte) (itemId & 0xFF)) {
            final int fx = targetX, fy = targetY, fz = targetZ;
            final int fBlockType = worldBlockType & 0xFF;
            ctx.executor().schedule(() -> {
                if (ctx.channel().isActive()) {
                    ctx.writeAndFlush(new BlockChangePacket(fx, fy, fz, fBlockType, 0));
                }
            }, 200, TimeUnit.MILLISECONDS);
        }

        // Replenish cobblestone. v1 (Alpha 1.0.17) has no PlayerInventory (0x05)
        // packet and sends nothing back after AddToInventory, so immediate per-
        // placement give-back is safe. v2+ clients send 0x04/0x05 responses that
        // interfere with rapid placement input, so they use batched replenishment.
        if (!clientVersion.isAtLeast(ProtocolVersion.ALPHA_1_1_0)) {
            giveItem(ctx, BlockRegistry.COBBLESTONE, 1);
        } else {
            trackedCobblestone--;
            scheduleReplenishment(ctx);
        }
    }

    /**
     * Give items to the player using the Alpha AddToInventory packet (0x11).
     * The client handles slot assignment (first matching stack or first empty slot).
     */
    private void giveItem(ChannelHandlerContext ctx, int itemId, int count) {
        ctx.writeAndFlush(new AddToInventoryPacket(itemId, count, 0));
    }

    /**
     * Handle inventory sync from the client (0x05). The client sends this after
     * any inventory change. We parse the cobblestone count from the main
     * inventory section (type -1) to keep our tracked count accurate.
     */
    private void handleInventorySync(PlayerInventoryPacket packet) {
        if (packet.getType() == -1) {
            // Main inventory — update tracked count from actual client data
            trackedCobblestone = packet.getItemCount(BlockRegistry.COBBLESTONE);
        }
    }

    /**
     * Schedule a batched cobblestone replenishment. Resets the 2-second timer
     * on each call. When the timer fires (no placement for 2 seconds), tops
     * up to 64 using trackedCobblestone (corrected by inventory sync packets).
     * This avoids sending AddToInventory packets during rapid placement, which
     * can confuse the Alpha client's input handling.
     */
    private void scheduleReplenishment(ChannelHandlerContext ctx) {
        if (replenishTask != null) {
            replenishTask.cancel(false);
        }
        replenishTask = ctx.executor().schedule(() -> {
            int deficit = 64 - trackedCobblestone;
            if (ctx.channel().isActive() && deficit > 0) {
                giveItem(ctx, BlockRegistry.COBBLESTONE, deficit);
                trackedCobblestone = 64;
            }
            replenishTask = null;
        }, REPLENISH_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    private void handleChat(ChannelHandlerContext ctx, ChatPacket packet) {
        if (player == null) return;

        String message = packet.getMessage();

        // Route commands
        if (message.startsWith("/")) {
            String command = message.substring(1);
            boolean handled = CommandRegistry.dispatch(command, player.getUsername(), false,
                    reply -> playerManager.sendChat(player, reply));
            if (!handled) {
                playerManager.sendChat(player, "Unknown command: " + command.split("\\s+")[0]);
            }
            return;
        }

        // Fire cancellable chat event
        EventResult result = ServerEvents.CHAT.invoker().onChat(player.getUsername(), message);
        if (result == EventResult.CANCEL) return;

        System.out.println("[Chat] " + player.getUsername() + ": " + message);
        playerManager.broadcastChat(player.getPlayerId(), player.getUsername() + ": " + message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (player != null) {
            System.out.println(player.getUsername() + " disconnected"
                    + " (" + (playerManager.getPlayerCount() - 1) + " online)");
            ServerEvents.PLAYER_LEAVE.invoker().onPlayerLeave(player.getUsername());
            world.rememberPlayerPosition(player);
            chunkManager.removePlayer(player);
            playerManager.broadcastChat((byte) 0, player.getUsername() + " left the game");
            playerManager.broadcastPlayerDespawn(player);
            playerManager.removePlayer(ctx.channel());
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof ReadTimeoutException) {
            if (!loginComplete) {
                System.err.println("Alpha login timeout — client did not complete login within "
                        + LOGIN_TIMEOUT_SECONDS + " seconds");
                ctx.writeAndFlush(new DisconnectPacket("Login timed out"));
            }
        } else {
            System.err.println("Alpha connection error"
                    + (player != null ? " (" + player.getUsername() + ")" : "")
                    + ": " + cause.getMessage());
        }
        ctx.close();
    }

    /**
     * Build disconnect messages for an unsupported protocol version.
     * Returns [clientMessage, consoleMessage]. The client message is kept short
     * because the Alpha disconnect screen renders text on a single line.
     * The console message includes full version details.
     */
    private static String[] buildUnsupportedVersionMessages(int pv) {
        // Determine which family the unknown version likely belongs to.
        // AlphaConnectionHandler handles all pre-Netty TCP clients (Alpha and Beta).
        // Alpha SMP versions: 1-14. Beta will use higher numbers when added.
        ProtocolVersion.Family family;
        if (pv >= 1 && pv <= 14) {
            family = ProtocolVersion.Family.ALPHA;
        } else {
            String msg = "Unknown protocol version: " + pv;
            return new String[]{msg, msg};
        }

        List<ProtocolVersion> supported = ProtocolVersion.getByFamily(family);

        // Short message for the client's single-line disconnect screen
        StringBuilder clientMsg = new StringBuilder();
        clientMsg.append("Unsupported v").append(pv).append(". Supported: ");
        for (int i = 0; i < supported.size(); i++) {
            if (i > 0) clientMsg.append(", ");
            clientMsg.append('v').append(supported.get(i).getVersionNumber());
        }

        // Detailed message for the server console
        StringBuilder consoleMsg = new StringBuilder();
        consoleMsg.append("protocol v").append(pv);
        String gameVersions = ProtocolVersion.describeAlphaProtocol(pv);
        if (gameVersions != null) {
            consoleMsg.append(" (").append(gameVersions).append(')');
        }
        consoleMsg.append(" not supported. Supported ");
        consoleMsg.append(family.name().toLowerCase()).append(" versions: ");
        for (int i = 0; i < supported.size(); i++) {
            if (i > 0) consoleMsg.append(", ");
            consoleMsg.append(supported.get(i).getDisplayName());
        }

        return new String[]{clientMsg.toString(), consoleMsg.toString()};
    }

    private static short toFixedPoint(double d) {
        return (short) (d * 32);
    }

    private static byte toByteRotation(float degrees) {
        // Callers already pass Classic convention degrees (0=North).
        // Simple degrees-to-byte conversion with no offset.
        return (byte) ((degrees / 360.0f) * 256);
    }
}
