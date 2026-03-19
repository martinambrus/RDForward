package com.github.martinambrus.rdforward.server.mcpe;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.server.ChunkManager;
import com.github.martinambrus.rdforward.server.ConnectedPlayer;
import com.github.martinambrus.rdforward.server.PlayerManager;
import com.github.martinambrus.rdforward.server.ServerWorld;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

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

    /** Convert canonical v12 ID to wire ID for this session. */
    private int wireId(int canonicalId) {
        return MCPEConstants.toWireId(canonicalId, session.getMcpeProtocolVersion());
    }

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
        } else {
            System.out.println("[MCPE Login] Unhandled packet 0x"
                    + Integer.toHexString(packetId) + " (" + payload.readableBytes() + " bytes)"
                    + " (v" + session.getMcpeProtocolVersion() + ")");
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

        // Accept protocol 11-12, 14, 17 (skip dev-only 13, 15, 16)
        if (protocol1 < MCPEConstants.MCPE_PROTOCOL_VERSION_11
                || protocol1 > MCPEConstants.MCPE_PROTOCOL_VERSION_MAX
                || protocol1 == 13 || protocol1 == 15 || protocol1 == 16) {
            int status = (protocol1 > MCPEConstants.MCPE_PROTOCOL_VERSION_MAX)
                    ? MCPEConstants.LOGIN_SERVER_OUTDATED
                    : MCPEConstants.LOGIN_CLIENT_OUTDATED;
            sendLoginStatus(status);
            return;
        }
        session.setMcpeProtocolVersion(protocol1);

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

        boolean isV17 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_17;

        // Send SetTime + SetSpawnPosition before chunks (all versions)
        sendSetTime((int) (world.getWorldTime() % 24000));
        sendSetSpawnPosition((int) spawnX, (int) spawnZ, (int) (spawnY - PLAYER_EYE_HEIGHT));

        // v17+: send SetHealth before chunks (PocketMine does this)
        if (isV17) {
            MCPEPacketBuffer hp = new MCPEPacketBuffer();
            hp.writeByte(wireId(MCPEConstants.SET_HEALTH));
            hp.writeByte(20);
            server.sendGamePacket(session, hp.getBuf());
        }

        // Send initial chunks around spawn
        int spawnCX = (int) Math.floor(spawnX) >> 4;
        int spawnCZ = (int) Math.floor(spawnZ) >> 4;
        int viewRadius = 4; // Send 4-chunk radius initially
        int chunkCount = 0;
        for (int cx = spawnCX - viewRadius; cx <= spawnCX + viewRadius; cx++) {
            for (int cz = spawnCZ - viewRadius; cz <= spawnCZ + viewRadius; cz++) {
                sendChunkData(cx, cz);
                chunkCount++;
            }
        }
        System.out.println("[MCPE] Sent " + chunkCount + " chunks around (" + spawnCX + "," + spawnCZ + ")");

        // v17+: send RespawnPacket after chunks to trigger client terrain loading,
        // then wait for Ready packet before completing spawn (MovePlayer etc.)
        if (isV17) {
            double x = player.getDoubleX();
            double y = player.getDoubleY();
            double z = player.getDoubleZ();
            MCPEPacketBuffer resp = new MCPEPacketBuffer();
            resp.writeByte(wireId(MCPEConstants.RESPAWN));
            resp.writeInt(player.getPlayerId() + 1);
            resp.writeFloat((float) x);
            resp.writeFloat((float) (y - PLAYER_EYE_HEIGHT));
            resp.writeFloat((float) z);
            server.sendGamePacket(session, resp.getBuf());
            // v17 client doesn't send Ready — it starts sending MovePlayer immediately
            doSpawn();
        }
    }

    private void handleReady(ChannelHandlerContext ctx, ByteBuf payload) {
        int status = payload.readUnsignedByte();
        System.out.println("[MCPE] Ready packet received: status=" + status
                + " (v" + session.getMcpeProtocolVersion() + ")");
        if (status == MCPEConstants.READY_SPAWN_REQUEST) {
            doSpawn();
        }
    }

    /** Complete the spawn sequence — called from handleReady for all versions. */
    private void doSpawn() {
        double x = player.getDoubleX();
        double y = player.getDoubleY(); // eye-level (internal)
        double z = player.getDoubleZ();

        sendMovePlayer(player.getPlayerId() + 1, (float) x, (float) (y - PLAYER_EYE_HEIGHT), (float) z, 0, 0);
        sendAdventureSettings(0x01 | 0x40); // allowFlight + creativeMode
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
            float oy = other.getY() / 32.0f - (float) PLAYER_EYE_HEIGHT;
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
            addPkt.writeByte((other.getYaw() + 128) & 0xFF);
            addPkt.writeByte(other.getPitch());
            addPkt.writeShort(0);
            addPkt.writeShort(0);
            addPkt.writeMetaByte(MCPEConstants.META_FLAGS, (byte) 0);
            addPkt.writeMetaShort(MCPEConstants.META_AIR, (short) 300);
            addPkt.writeMetaString(MCPEConstants.META_NAMETAG, other.getUsername());
            addPkt.writeMetaByte(MCPEConstants.META_SHOW_NAMETAG, (byte) 1);
            addPkt.writeMetaEnd();
            server.sendGamePacket(session, addPkt.getBuf());

            MCPEPacketBuffer meta = new MCPEPacketBuffer();
            meta.writeByte(wireId(MCPEConstants.SET_ENTITY_DATA));
            meta.writeInt(oeid);
            meta.writeMetaByte(MCPEConstants.META_FLAGS, (byte) 0);
            meta.writeMetaShort(MCPEConstants.META_AIR, (short) 300);
            meta.writeMetaString(MCPEConstants.META_NAMETAG, other.getUsername());
            meta.writeMetaByte(MCPEConstants.META_SHOW_NAMETAG, (byte) 1);
            meta.writeMetaEnd();
            server.sendGamePacket(session, meta.getBuf());
        }

        playerManager.broadcastPlayerListAdd(player);
        playerManager.broadcastPlayerSpawn(player);
        playerManager.broadcastChat((byte) 0, player.getUsername() + " joined the game");

        com.github.martinambrus.rdforward.server.event.ServerEvents.PLAYER_JOIN
                .invoker().onPlayerJoin(player.getUsername(), ProtocolVersion.BEDROCK);
        pongUpdater.run();

        System.out.println("[MCPE] " + username + " spawned at "
                + String.format("%.1f, %.1f, %.1f", x, y, z));
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
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_17) {
            pkt.writeInt(0);                      // dimension (0 = overworld) — new in v17
        }
        pkt.writeInt(MCPEConstants.GENERATOR_FLAT); // generator type — Flat gen reaches state 4
        pkt.writeInt(MCPEConstants.GAMEMODE_CREATIVE); // gamemode
        pkt.writeInt(player.getPlayerId() + 1);   // entity ID (1-based)
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_17) {
            // v17+: world spawn position (ints) after eid, before player position
            pkt.writeInt((int) x);
            pkt.writeInt((int) y);
            pkt.writeInt((int) z);
        }
        pkt.writeFloat(x);                        // player X
        pkt.writeFloat(y);                        // player Y
        pkt.writeFloat(z);                        // player Z
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
        pkt.writeByte(wireId(MCPEConstants.SET_SPAWN_POSITION));
        pkt.writeInt(x);
        pkt.writeInt(z);
        pkt.writeByte(y);
        server.sendGamePacket(session, pkt.getBuf());
    }

    private void sendMovePlayer(int entityId, float x, float y, float z, float yaw, float pitch) {
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(wireId(MCPEConstants.MOVE_PLAYER));
        pkt.writeInt(entityId);
        pkt.writeFloat(x);
        pkt.writeFloat(y);
        pkt.writeFloat(z);
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_14) {
            pkt.writeFloat(yaw);  // bodyYaw
            pkt.writeFloat(pitch);
            pkt.writeFloat(yaw);  // headYaw
        } else {
            pkt.writeFloat(yaw);
            pkt.writeFloat(pitch);
        }
        server.sendGamePacket(session, pkt.getBuf());
    }

    private void sendAdventureSettings(int flags) {
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        int pv = session.getMcpeProtocolVersion();
        if (pv >= MCPEConstants.MCPE_PROTOCOL_VERSION_14) {
            pkt.writeByte(MCPEConstants.toWireId(MCPEConstants.ADVENTURE_SETTINGS_V12, pv));
        } else if (pv >= MCPEConstants.MCPE_PROTOCOL_VERSION_12) {
            pkt.writeByte(MCPEConstants.ADVENTURE_SETTINGS_V12);
        } else {
            pkt.writeByte(MCPEConstants.ADVENTURE_SETTINGS_V11);
        }
        pkt.writeInt(flags);
        server.sendGamePacket(session, pkt.getBuf());
    }

    private void sendInventory() {
        // Send empty player inventory (windowId=0)
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(wireId(MCPEConstants.SEND_INVENTORY));
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
        pkt.writeByte(wireId(MCPEConstants.SEND_INVENTORY));
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
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_17) {
            sendFullChunkDataV17(chunkX, chunkZ);
            return;
        }

        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;

        // Send one packet per Y-section (0-7), matching PocketMine's approach
        for (int section = 0; section < 8; section++) {
            MCPEPacketBuffer pkt = new MCPEPacketBuffer();
            pkt.writeByte(wireId(MCPEConstants.CHUNK_DATA));
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
     * Send a v17 (0.9.0) FullChunkDataPacket (0xBA).
     * Full column format: block IDs (32768) + metadata (16384) + skylight (16384)
     * + blocklight (16384) + heightmap (256) + biome IDs (256) + biome colors (1024).
     * All zlib compressed. Block order: x*2048 + z*128 + y (XZY).
     */
    private void sendFullChunkDataV17(int chunkX, int chunkZ) {
        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;
        int height = 128;

        // Block IDs: 16*16*128 = 32768 bytes, XZY order
        byte[] blockIds = new byte[32768];
        byte[] heightMap = new byte[256];

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;
                int topY = 0;

                for (int y = 0; y < height; y++) {
                    int idx = (x << 11) | (z << 7) | y;
                    if (worldX >= 0 && worldX < world.getWidth()
                            && worldZ >= 0 && worldZ < world.getDepth()
                            && y < world.getHeight()) {
                        int block = mapBlockId(world.getBlock(worldX, y, worldZ));
                        blockIds[idx] = (byte) block;
                        if (block != 0) topY = y + 1;
                    }
                }
                heightMap[(z << 4) | x] = (byte) topY;
            }
        }

        // Metadata nibbles: 16384 bytes (all zero for basic blocks)
        byte[] metadata = new byte[16384];

        // Sky light: 16384 nibble bytes — compute from height map
        byte[] skyLight = new byte[16384];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int top = heightMap[(z << 4) | x] & 0xFF;
                for (int y = 0; y < height; y++) {
                    int idx = (x << 11) | (z << 7) | y;
                    int nibbleIdx = idx >> 1;
                    int light = (y >= top) ? 15 : 0;
                    if ((idx & 1) == 0) {
                        skyLight[nibbleIdx] |= (byte) (light & 0x0F);
                    } else {
                        skyLight[nibbleIdx] |= (byte) ((light << 4) & 0xF0);
                    }
                }
            }
        }

        // Block light: 16384 bytes (all zero)
        byte[] blockLight = new byte[16384];

        // Biome IDs: 256 bytes (all plains = 1)
        byte[] biomeIds = new byte[256];
        java.util.Arrays.fill(biomeIds, (byte) 1);

        // Biome colors: 256 * 4 = 1024 bytes (grass green ARGB)
        byte[] biomeColors = new byte[1024];
        for (int i = 0; i < 256; i++) {
            int offset = i * 4;
            // Grass green: R=0x7A, G=0xBD, B=0x6B, A=0x00
            biomeColors[offset] = 0x01;     // flag (non-zero = has color)
            biomeColors[offset + 1] = 0x7A; // R
            biomeColors[offset + 2] = (byte) 0xBD; // G
            biomeColors[offset + 3] = 0x6B; // B
        }

        // Assemble uncompressed payload: chunkX + chunkZ + terrain data (all inside compressed blob).
        // Binary analysis of FullChunkDataPacket::read confirms: client decompresses first,
        // then reads chunkX/chunkZ from the decompressed stream.
        // Terrain format (from deserializeTerrain): blockIDs(32768) + metadata(16384)
        // + skylight(16384) + blocklight(16384) + biomeIDs(256) + biomeColors(1024).
        int totalSize = 4 + 4 + blockIds.length + metadata.length + skyLight.length
                + blockLight.length + biomeIds.length + biomeColors.length;
        byte[] uncompressed = new byte[totalSize];
        int pos = 0;
        // chunkX and chunkZ (Little-Endian ints) inside the compressed payload
        // x86 client reads these via memcpy + native mov (LE)
        uncompressed[pos++] = (byte) chunkX;
        uncompressed[pos++] = (byte) (chunkX >> 8);
        uncompressed[pos++] = (byte) (chunkX >> 16);
        uncompressed[pos++] = (byte) (chunkX >> 24);
        uncompressed[pos++] = (byte) chunkZ;
        uncompressed[pos++] = (byte) (chunkZ >> 8);
        uncompressed[pos++] = (byte) (chunkZ >> 16);
        uncompressed[pos++] = (byte) (chunkZ >> 24);
        System.arraycopy(blockIds, 0, uncompressed, pos, blockIds.length); pos += blockIds.length;
        System.arraycopy(metadata, 0, uncompressed, pos, metadata.length); pos += metadata.length;
        System.arraycopy(skyLight, 0, uncompressed, pos, skyLight.length); pos += skyLight.length;
        System.arraycopy(blockLight, 0, uncompressed, pos, blockLight.length); pos += blockLight.length;
        System.arraycopy(biomeIds, 0, uncompressed, pos, biomeIds.length); pos += biomeIds.length;
        System.arraycopy(biomeColors, 0, uncompressed, pos, biomeColors.length);

        // Zlib compress the entire payload
        byte[] compressed;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DeflaterOutputStream dos = new DeflaterOutputStream(baos,
                    new Deflater(Deflater.DEFAULT_COMPRESSION));
            dos.write(uncompressed);
            dos.finish();
            dos.close();
            compressed = baos.toByteArray();
        } catch (java.io.IOException e) {
            throw new RuntimeException("zlib compress failed", e);
        }

        // Send FullChunkDataPacket: 0xBA + compressed(chunkX + chunkZ + terrainData)
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(MCPEConstants.FULL_CHUNK_DATA_V17);
        pkt.writeBytes(compressed);
        server.sendGamePacket(session, pkt.getBuf());
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
