package com.github.martinambrus.rdforward.server.mcpe;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.server.ChunkManager;
import com.github.martinambrus.rdforward.server.ConnectedPlayer;
import com.github.martinambrus.rdforward.server.PlayerManager;
import com.github.martinambrus.rdforward.server.ServerWorld;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

/**
 * Handles the MCPE 0.7.0 login sequence:
 * 1. C->S LoginPacket (0x82) — username, protocol versions, client ID, skin data
 * 2. S->C LoginStatusPacket (0x83) — success/failure
 * 3. S->C StartGamePacket (0x87) — seed, generator, gamemode, entity ID, spawn position
 * 4. S->C SetTimePacket (0x86)
 * 5. S->C SetSpawnPositionPacket (0xAA)
 * 6. S->C ChunkDataPackets (0x9E) — initial world chunks
 * 7. C->S ReadyPacket (0x84) — client ready to spawn
 * 8. S->C MovePlayerPacket (0x94) — teleport to spawn
 * 9. S->C AdventureSettingsPacket (0xB7) — game flags
 */
public class MCPELoginHandler {

    private static final double PLAYER_EYE_HEIGHT = (double) 1.62f;

    private final LegacyRakNetSession session;
    private final ServerWorld world;
    private final PlayerManager playerManager;
    private final ChunkManager chunkManager;
    private final LegacyRakNetServer server;
    private final Runnable pongUpdater;

    private String username;
    private ConnectedPlayer player;
    private MCPESessionWrapper sessionWrapper;

    public MCPELoginHandler(LegacyRakNetSession session, ServerWorld world,
                            PlayerManager playerManager, ChunkManager chunkManager,
                            LegacyRakNetServer server, Runnable pongUpdater) {
        this.session = session;
        this.world = world;
        this.playerManager = playerManager;
        this.chunkManager = chunkManager;
        this.server = server;
        this.pongUpdater = pongUpdater;
    }

    public void handlePacket(ChannelHandlerContext ctx, int packetId, ByteBuf payload) {
        if (packetId == (MCPEConstants.LOGIN & 0xFF)) {
            handleLogin(ctx, payload);
        } else if (packetId == (MCPEConstants.READY & 0xFF)) {
            handleReady(ctx, payload);
        }
    }

    private void handleLogin(ChannelHandlerContext ctx, ByteBuf payload) {
        MCPEPacketBuffer buf = new MCPEPacketBuffer(payload);
        username = buf.readString();
        int protocol1 = buf.readInt();
        int protocol2 = buf.readInt();
        int clientId = buf.readInt();
        // loginData (skin etc.) — skip for now
        // String loginData = buf.readString();

        System.out.println("[MCPE] Login from " + username
                + " (protocol=" + protocol1 + ", clientId=" + clientId + ")");

        // Protocol version check
        if (protocol1 != MCPEConstants.MCPE_PROTOCOL_VERSION) {
            int status = (protocol1 > MCPEConstants.MCPE_PROTOCOL_VERSION)
                    ? MCPEConstants.LOGIN_SERVER_OUTDATED
                    : MCPEConstants.LOGIN_CLIENT_OUTDATED;
            sendLoginStatus(status);
            return;
        }

        // Check for duplicate username
        if (username != null && !username.trim().isEmpty()) {
            playerManager.kickDuplicatePlayer(username.trim(), world);
        }

        // Register player
        player = playerManager.addPlayer(username, null, ProtocolVersion.BEDROCK);
        if (player == null) {
            // Server full — disconnect
            sendLoginStatus(MCPEConstants.LOGIN_CLIENT_OUTDATED); // No "server full" status in protocol 11
            return;
        }

        // Set up session wrapper for Classic packet translation
        sessionWrapper = new MCPESessionWrapper(session, server);
        player.setMcpeSession(sessionWrapper);

        // Send login success
        sendLoginStatus(MCPEConstants.LOGIN_SUCCESS);

        // Calculate spawn position
        double spawnX, spawnY = 0, spawnZ;
        java.util.Map<String, short[]> savedPositions = world.loadPlayerPositions();
        short[] savedPos = savedPositions.get(player.getUsername());

        spawnX = world.getWidth() / 2.0 + 0.5;
        spawnZ = world.getDepth() / 2.0 + 0.5;

        if (savedPos != null && savedPos.length >= 5) {
            spawnX = savedPos[0] / 32.0;
            spawnY = savedPos[1] / 32.0;
            spawnZ = savedPos[2] / 32.0;
        }

        // Always validate spawn against terrain — recalculate if feet would be inside a solid block
        {
            int sx = (int) Math.floor(spawnX);
            int sz = (int) Math.floor(spawnZ);
            int feetBlock = (int) Math.floor(spawnY - PLAYER_EYE_HEIGHT);
            if (savedPos == null || savedPos.length < 5
                    || (feetBlock >= 0 && feetBlock < world.getHeight()
                        && world.getBlock(sx, feetBlock, sz) != 0)) {
                // Recalculate from terrain
                int feetY = 0;
                for (int y = world.getHeight() - 1; y >= 0; y--) {
                    if (world.getBlock(sx, y, sz) != 0) {
                        feetY = y + 1;
                        break;
                    }
                }
                spawnY = feetY + PLAYER_EYE_HEIGHT;
            }
        }

        player.updatePositionDouble(spawnX, spawnY, spawnZ, 0, 0);

        // Debug: print blocks at spawn column
        int sx = (int) Math.floor(spawnX);
        int sz = (int) Math.floor(spawnZ);
        StringBuilder blockDebug = new StringBuilder("[MCPE] Blocks at spawn column (" + sx + "," + sz + "):");
        for (int by = Math.max(0, (int)(spawnY - PLAYER_EYE_HEIGHT) - 3);
             by <= Math.min(world.getHeight() - 1, (int)(spawnY + 3)); by++) {
            blockDebug.append(" y=").append(by).append("=").append(world.getBlock(sx, by, sz));
        }
        System.out.println(blockDebug);
        System.out.println("[MCPE] spawnY(eye)=" + spawnY + " feetY=" + (spawnY - PLAYER_EYE_HEIGHT));

        // Send StartGame — Y is feet-level (matching PocketMine convention)
        sendStartGame((float) spawnX, (float) (spawnY - PLAYER_EYE_HEIGHT), (float) spawnZ);

        // Send SetTime
        sendSetTime((int) (world.getWorldTime() % 24000));

        // Send SetSpawnPosition
        sendSetSpawnPosition((int) spawnX, (int) spawnZ, (int) (spawnY - PLAYER_EYE_HEIGHT));

        // Send initial chunks around spawn
        int spawnCX = (int) Math.floor(spawnX) >> 4;
        int spawnCZ = (int) Math.floor(spawnZ) >> 4;
        int viewRadius = 4; // Send 4-chunk radius initially
        for (int cx = spawnCX - viewRadius; cx <= spawnCX + viewRadius; cx++) {
            for (int cz = spawnCZ - viewRadius; cz <= spawnCZ + viewRadius; cz++) {
                sendChunkData(cx, cz);
            }
        }
    }

    private void handleReady(ChannelHandlerContext ctx, ByteBuf payload) {
        int status = payload.readUnsignedByte();

        if (status == MCPEConstants.READY_SPAWN_REQUEST) {
            // Client wants to spawn — send position and game settings
            double x = player.getDoubleX();
            double y = player.getDoubleY(); // eye-level (internal)
            double z = player.getDoubleZ();

            // MCPE 0.7.0 MovePlayer Y = feet-level for entities
            sendMovePlayer(player.getPlayerId() + 1, (float) x, (float) (y - PLAYER_EYE_HEIGHT), (float) z, 0, 0);

            // AdventureSettings (creative mode flags)
            sendAdventureSettings(0x01 | 0x40); // allowFlight + creativeMode

            // Send inventory (empty for creative)
            sendInventory();

            // Transition to gameplay handler
            MCPEGameplayHandler gameplayHandler = new MCPEGameplayHandler(
                    session, world, playerManager, chunkManager, server,
                    player, sessionWrapper, pongUpdater);
            session.setGameplayHandler(gameplayHandler);

            // Send existing players to MCPE client
            for (ConnectedPlayer other : playerManager.getAllPlayers()) {
                if (other == player) continue;
                float ox = other.getX() / 32.0f;
                float oy = other.getY() / 32.0f - (float) PLAYER_EYE_HEIGHT; // feet-level for AddPlayer
                float oz = other.getZ() / 32.0f;
                int oeid = (other.getPlayerId() & 0xFF) + 1;

                MCPEPacketBuffer addPkt = new MCPEPacketBuffer();
                addPkt.writeByte(MCPEConstants.ADD_PLAYER);
                addPkt.writeLong(oeid);
                addPkt.writeString(other.getUsername());
                addPkt.writeInt(oeid);
                addPkt.writeFloat(ox);
                addPkt.writeFloat(oy);
                addPkt.writeFloat(oz);
                addPkt.writeByte((other.getYaw() + 128) & 0xFF); // Classic→MCPE yaw: +128
                addPkt.writeByte(other.getPitch());
                addPkt.writeShort(0); // held item
                addPkt.writeShort(0); // held item aux
                // Metadata: flags + air + nametag + show nametag
                addPkt.writeMetaByte(MCPEConstants.META_FLAGS, (byte) 0);
                addPkt.writeMetaShort(MCPEConstants.META_AIR, (short) 300);
                addPkt.writeMetaString(MCPEConstants.META_NAMETAG, other.getUsername());
                addPkt.writeMetaByte(MCPEConstants.META_SHOW_NAMETAG, (byte) 1);
                addPkt.writeMetaEnd();
                server.sendGamePacket(session, addPkt.getBuf());

                // Also send SET_ENTITY_DATA with nametag
                MCPEPacketBuffer meta = new MCPEPacketBuffer();
                meta.writeByte(MCPEConstants.SET_ENTITY_DATA);
                meta.writeInt(oeid);
                meta.writeMetaByte(MCPEConstants.META_FLAGS, (byte) 0);
                meta.writeMetaShort(MCPEConstants.META_AIR, (short) 300);
                meta.writeMetaString(MCPEConstants.META_NAMETAG, other.getUsername());
                meta.writeMetaByte(MCPEConstants.META_SHOW_NAMETAG, (byte) 1);
                meta.writeMetaEnd();
                server.sendGamePacket(session, meta.getBuf());
            }

            // Broadcast player list + spawn to other clients (so Alpha/Netty see us)
            playerManager.broadcastPlayerListAdd(player);
            playerManager.broadcastPlayerSpawn(player);

            playerManager.broadcastChat((byte) 0, player.getUsername() + " joined the game");

            // Fire join event
            com.github.martinambrus.rdforward.server.event.ServerEvents.PLAYER_JOIN
                    .invoker().onPlayerJoin(player.getUsername(), ProtocolVersion.BEDROCK);
            pongUpdater.run();

            System.out.println("[MCPE] " + username + " spawned at "
                    + String.format("%.1f, %.1f, %.1f", x, y, z));
        }
    }

    // ========== Packet Builders ==========

    private void sendLoginStatus(int status) {
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(MCPEConstants.LOGIN_STATUS);
        pkt.writeInt(status);
        server.sendGamePacket(session, pkt.getBuf());
    }

    private void sendStartGame(float x, float y, float z) {
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(MCPEConstants.START_GAME);
        pkt.writeInt(0);                          // seed
        pkt.writeInt(MCPEConstants.GENERATOR_OLD); // generator type
        pkt.writeInt(MCPEConstants.GAMEMODE_CREATIVE); // gamemode
        pkt.writeInt(player.getPlayerId() + 1);   // entity ID (1-based)
        pkt.writeFloat(x);                        // spawn X
        pkt.writeFloat(y);                        // spawn Y
        pkt.writeFloat(z);                        // spawn Z
        server.sendGamePacket(session, pkt.getBuf());
    }

    private void sendSetTime(int time) {
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(MCPEConstants.SET_TIME);
        pkt.writeInt(time);
        pkt.writeByte(0x80); // time flowing
        server.sendGamePacket(session, pkt.getBuf());
    }

    private void sendSetSpawnPosition(int x, int z, int y) {
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(MCPEConstants.SET_SPAWN_POSITION);
        pkt.writeInt(x);
        pkt.writeInt(z);
        pkt.writeByte(y);
        server.sendGamePacket(session, pkt.getBuf());
    }

    private void sendMovePlayer(int entityId, float x, float y, float z, float yaw, float pitch) {
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(MCPEConstants.MOVE_PLAYER);
        pkt.writeInt(entityId);
        pkt.writeFloat(x);
        pkt.writeFloat(y);
        pkt.writeFloat(z);
        pkt.writeFloat(yaw);
        pkt.writeFloat(pitch);
        server.sendGamePacket(session, pkt.getBuf());
    }

    private void sendAdventureSettings(int flags) {
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(MCPEConstants.ADVENTURE_SETTINGS);
        pkt.writeInt(flags);
        server.sendGamePacket(session, pkt.getBuf());
    }

    private void sendInventory() {
        // Send empty player inventory (windowId=0)
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(MCPEConstants.SEND_INVENTORY);
        pkt.writeInt(player.getPlayerId() + 1); // entity ID
        pkt.writeByte(0); // windowId = player inventory
        pkt.writeShort(36); // 36 slots
        for (int i = 0; i < 36; i++) {
            pkt.writeShort(0);  // itemId = air
            pkt.writeByte(0);   // count
            pkt.writeShort(0);  // metadata
        }
        server.sendGamePacket(session, pkt.getBuf());

        // Send empty armor (windowId=1)
        pkt = new MCPEPacketBuffer();
        pkt.writeByte(MCPEConstants.SEND_INVENTORY);
        pkt.writeInt(player.getPlayerId() + 1);
        pkt.writeByte(1); // windowId = armor
        pkt.writeShort(4); // 4 armor slots
        for (int i = 0; i < 4; i++) {
            pkt.writeShort(0);
            pkt.writeByte(0);
            pkt.writeShort(0);
        }
        server.sendGamePacket(session, pkt.getBuf());
    }

    /**
     * Convert a world column to MCPE 0.7.0 chunk format and send it.
     * PocketMine Alpha_1.3 sends one Y-section per packet (getOrderedMiniChunk).
     * Each packet: chunkX, chunkZ, then 256 columns of (flag + 16 blockIDs + 8 meta).
     * Flag = (1 << Y) indicating which section this packet represents.
     */
    private void sendChunkData(int chunkX, int chunkZ) {
        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;

        // Send one packet per Y-section (0-7), matching PocketMine's approach
        for (int section = 0; section < 8; section++) {
            MCPEPacketBuffer pkt = new MCPEPacketBuffer();
            pkt.writeByte(MCPEConstants.CHUNK_DATA);
            pkt.writeInt(chunkX);
            pkt.writeInt(chunkZ);

            int flag = 1 << section;
            int sectionBaseY = section * 16;

            // 256 columns: X inner (varies fastest), Z outer
            for (int j = 0; j < 256; j++) {
                int localX = j & 0x0F;
                int localZ = (j >> 4) & 0x0F;
                int worldX = baseX + localX;
                int worldZ = baseZ + localZ;

                pkt.writeByte(flag);

                // 16 block IDs for this column in this section
                for (int localY = 0; localY < 16; localY++) {
                    int worldY = sectionBaseY + localY;
                    if (worldX >= 0 && worldX < world.getWidth()
                            && worldZ >= 0 && worldZ < world.getDepth()
                            && worldY < world.getHeight()) {
                        pkt.writeByte(mapBlockId(world.getBlock(worldX, worldY, worldZ)));
                    } else {
                        pkt.writeByte(0); // air
                    }
                }

                // 8 metadata nibble bytes (all zero for basic blocks)
                for (int i = 0; i < 8; i++) {
                    pkt.writeByte(0);
                }
            }

            server.sendGamePacket(session, pkt.getBuf());
        }
    }

    /**
     * Map internal block IDs to MCPE 0.7.0 block IDs.
     * For basic blocks, the IDs are the same as Java Classic/Alpha.
     */
    private int mapBlockId(int internalId) {
        // MCPE 0.7.0 uses mostly standard Minecraft block IDs
        // Basic blocks (0-49) are identical
        if (internalId >= 0 && internalId <= 49) {
            return internalId;
        }
        // Unknown blocks -> stone
        return 1;
    }

    public ConnectedPlayer getPlayer() { return player; }
    public MCPESessionWrapper getSessionWrapper() { return sessionWrapper; }
}
