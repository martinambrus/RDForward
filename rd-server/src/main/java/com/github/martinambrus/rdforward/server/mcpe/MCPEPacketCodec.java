package com.github.martinambrus.rdforward.server.mcpe;

import com.github.martinambrus.rdforward.server.ServerWorld;

/**
 * Version-specific MCPE packet codec interface.
 * Encapsulates wire format differences between MCPE protocol versions
 * (v9, v11-v20, v27-v81, v91) for both reading C2S packets and writing
 * S2C packets.
 *
 * Implementations: MCPEPacketCodecV9, MCPEPacketCodecLegacy (v11-v20),
 * MCPEPacketCodecV27 (v27-v81), MCPEPacketCodecV91.
 */
public interface MCPEPacketCodec {

    // ========== C2S packet readers ==========

    /**
     * Read a MovePlayer C2S packet.
     * @param buf the packet payload (after packet ID)
     * @return decoded movement data
     */
    MCPEPacketData.MovePlayerData readMovePlayer(MCPEPacketBuffer buf);

    /**
     * Read a RemoveBlock C2S packet.
     * @param buf the packet payload (after packet ID)
     * @return decoded block coordinates
     */
    MCPEPacketData.RemoveBlockData readRemoveBlock(MCPEPacketBuffer buf);

    /**
     * Read a UseItem C2S packet (block placement).
     * @param buf the packet payload (after packet ID)
     * @return decoded placement data
     */
    MCPEPacketData.UseItemData readUseItem(MCPEPacketBuffer buf);

    /**
     * Read a Message/Text C2S packet.
     * @param buf the packet payload (after packet ID)
     * @return the chat message text
     */
    String readMessage(MCPEPacketBuffer buf);

    /**
     * Read a RequestChunk or RequestChunkRadius C2S packet.
     * @param buf the packet payload (after packet ID)
     * @return chunk request data (specific coords or radius)
     */
    MCPEPacketData.RequestChunkData readRequestChunk(MCPEPacketBuffer buf);

    /**
     * Read and consume a PlayerEquipment C2S packet.
     * Currently discarded; just needs to consume the bytes to advance the buffer.
     * @param buf the packet payload (after packet ID)
     */
    void readPlayerEquipment(MCPEPacketBuffer buf);

    /**
     * Read a PlayerAction C2S packet.
     * @param buf the packet payload (after packet ID)
     * @return decoded action data
     */
    MCPEPacketData.PlayerActionData readPlayerAction(MCPEPacketBuffer buf);

    /**
     * Read an Animate C2S packet.
     * @param buf the packet payload (after packet ID)
     * @return decoded animate data (action + entity ID)
     */
    MCPEPacketData.AnimateData readAnimate(MCPEPacketBuffer buf);

    /**
     * Read and consume an Interact C2S packet.
     * Currently discarded; just needs to consume the bytes.
     * @param buf the packet payload (after packet ID)
     */
    void readInteract(MCPEPacketBuffer buf);

    /**
     * Read a PlaceBlock C2S packet (v9 only).
     * @param buf the packet payload (after packet ID)
     * @return decoded placement data, or null if this codec does not support PlaceBlock
     */
    MCPEPacketData.PlaceBlockData readPlaceBlock(MCPEPacketBuffer buf);

    // ========== S2C packet writers (gameplay broadcasts) ==========

    /**
     * Write a MovePlayer S2C packet for broadcasting movement of another player.
     * @param buf buffer to write to (packet ID already written by caller)
     * @param entityId the player's entity ID
     * @param x eye-level X position
     * @param y eye-level Y position (codec adjusts to feet-level if needed)
     * @param z eye-level Z position
     * @param yaw yaw in degrees
     * @param pitch pitch in degrees
     */
    void writeMovePlayer(MCPEPacketBuffer buf, int entityId,
                         float x, float y, float z, float yaw, float pitch);

    /**
     * Write an UpdateBlock S2C packet.
     * @param buf buffer to write to (packet ID already written by caller)
     * @param x block X
     * @param y block Y
     * @param z block Z
     * @param blockId block type
     * @param meta block metadata
     * @param flags update flags (e.g. 0x0B for FLAG_ALL_PRIORITY)
     */
    void writeUpdateBlock(MCPEPacketBuffer buf, int x, int y, int z,
                          int blockId, int meta, int flags);

    /**
     * Write an Animate S2C packet.
     * @param buf buffer to write to (packet ID already written by caller)
     * @param entityId the entity ID
     * @param action the animation action
     */
    void writeAnimate(MCPEPacketBuffer buf, int entityId, int action);

    // ========== S2C translation writers (Classic -> MCPE) ==========

    /**
     * Write a SetBlock translation (Classic SetBlockServerPacket -> MCPE UpdateBlock).
     * Writes the complete packet including packet ID byte.
     * @param buf buffer to write to
     * @param x block X
     * @param y block Y
     * @param z block Z
     * @param blockType the block type
     */
    void writeSetBlock(MCPEPacketBuffer buf, int x, int y, int z, int blockType);

    /**
     * Write a SpawnPlayer translation (Classic SpawnPlayerPacket -> MCPE AddPlayer + PlayerList).
     * May write multiple packets to the server. Includes PlayerList entry for v34+.
     * @param server the RakNet server for sending sub-packets
     * @param session the target session
     * @param data spawn player parameters
     */
    void writeSpawnPlayer(LegacyRakNetServer server, LegacyRakNetSession session,
                          MCPEPacketData.SpawnPlayerData data);

    /**
     * Write a DespawnPlayer translation (Classic DespawnPlayerPacket -> MCPE RemovePlayer/RemoveEntity + PlayerList remove).
     * @param server the RakNet server for sending sub-packets
     * @param session the target session
     * @param data despawn player parameters
     */
    void writeDespawnPlayer(LegacyRakNetServer server, LegacyRakNetSession session,
                            MCPEPacketData.DespawnPlayerData data);

    /**
     * Write a PlayerTeleport translation (Classic PlayerTeleportPacket -> MCPE MovePlayer).
     * Writes the complete packet including packet ID byte.
     * @param buf buffer to write to
     * @param entityId the player's entity ID
     * @param x position X (eye-level, codec adjusts if needed)
     * @param y position Y (eye-level, codec adjusts if needed)
     * @param z position Z
     * @param yaw yaw in degrees
     * @param pitch pitch in degrees
     */
    void writePlayerTeleport(MCPEPacketBuffer buf, int entityId,
                             float x, float y, float z, float yaw, float pitch);

    /**
     * Write a chat Message S2C packet.
     * Writes the complete packet including packet ID byte.
     * @param buf buffer to write to
     * @param message the chat message text
     */
    void writeMessage(MCPEPacketBuffer buf, String message);

    /**
     * Write a SetTime S2C packet.
     * Writes the complete packet including packet ID byte.
     * @param buf buffer to write to
     * @param time the world time
     */
    void writeTimeUpdate(MCPEPacketBuffer buf, int time);

    // ========== Chunk sending ==========

    /**
     * Send chunk data to the session using the version-specific chunk format.
     * @param server the RakNet server for sending packets
     * @param session the target session
     * @param world the server world to read block data from
     * @param chunkX chunk X coordinate
     * @param chunkZ chunk Z coordinate
     */
    void sendChunkData(LegacyRakNetServer server, LegacyRakNetSession session,
                       ServerWorld world, int chunkX, int chunkZ);

    // ========== Version info ==========

    /**
     * Convert a v12-canonical packet ID to the wire ID for this codec's version.
     * @param canonicalId the v12-canonical packet ID
     * @return the wire ID
     */
    int wireId(int canonicalId);

    /**
     * Whether v27+ UpdateBlock is silently ignored and a chunk resend is needed
     * after block updates.
     * @return true if chunk resend is required after UpdateBlock
     */
    boolean requiresChunkResendForBlockUpdate();

    /**
     * Whether C2S MovePlayer Y coordinate is eye-level (v27+) or feet-level (v9-v20).
     * @return true if Y is eye-level
     */
    boolean isEyeLevelMovePlayer();

    /**
     * Whether the given PlayerAction action ID should trigger creative block breaking.
     * @param action the action ID from the PlayerAction packet
     * @return true if this action should break the block
     */
    boolean shouldBreakOnAction(int action);

    /**
     * Whether arm-swing (Animate action=1) should trigger server-side raycast breaking.
     * True for v11-v13 and v17-v18 which lack direct RemoveBlock or PlayerAction-based breaking.
     * @return true if arm swing triggers raycast breaking
     */
    boolean usesRaycastBreaking();

    // ========== Shared helpers ==========

    /** Eye height used by all MCPE codecs for feet/eye-level Y conversion. */
    double PLAYER_EYE_HEIGHT = (double) 1.62f;

    /** Maximum Classic block ID that maps 1:1 to MCPE. IDs above this become stone. */
    int MAX_PASSTHROUGH_BLOCK_ID = 49;

    /**
     * Map an internal block ID to an MCPE block ID.
     * Classic block IDs 0-49 pass through unchanged; anything else becomes stone (1).
     */
    static int mapBlockId(int internalId) {
        if (internalId >= 0 && internalId <= MAX_PASSTHROUGH_BLOCK_ID) return internalId;
        return 1; // stone fallback
    }

    /**
     * Send pre-v17 column-based chunk data (PocketMine-style, section-based columns).
     * Shared between v9 and v11-v14 codecs.
     */
    static void sendColumnChunkData(LegacyRakNetServer server, LegacyRakNetSession session,
                                    ServerWorld world, int chunkX, int chunkZ,
                                    int chunkDataWireId) {
        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;
        int worldHeight = world.getHeight();
        int worldWidth = world.getWidth();
        int worldDepth = world.getDepth();

        // PocketMine formula: columns per packet based on MTU
        int columnsPerPacket = Math.max(1,
                (session.getMtu() - 16 - 255) / 192);
        if (columnsPerPacket > 256) columnsPerPacket = 256;

        // Pre-build column data: 193 bytes per column (flag + 8 sections)
        byte[][] columnData = new byte[256][];
        for (int j = 0; j < 256; j++) {
            int localX = j & 0x0F;
            int localZ = (j >> 4) & 0x0F;
            int worldX = baseX + localX;
            int worldZ = baseZ + localZ;

            byte[] col = new byte[193];
            col[0] = (byte) 0xFF;
            int offset = 1;

            for (int section = 0; section < 8; section++) {
                int sectionBaseY = section * 16;
                for (int localY = 0; localY < 16; localY++) {
                    int worldY = sectionBaseY + localY;
                    if (worldX >= 0 && worldX < worldWidth
                            && worldZ >= 0 && worldZ < worldDepth
                            && worldY < worldHeight) {
                        col[offset++] = (byte) mapBlockId(world.getBlock(worldX, worldY, worldZ));
                    } else {
                        col[offset++] = 0;
                    }
                }
                offset += 8; // metadata (zero-initialized)
            }
            columnData[j] = col;
        }

        // Send columns in batches with zero-padding for prior columns
        int columnsSent = 0;
        while (columnsSent < 256) {
            int batchSize = Math.min(columnsPerPacket, 256 - columnsSent);

            MCPEPacketBuffer pkt = new MCPEPacketBuffer();
            pkt.writeByte(chunkDataWireId);
            pkt.writeInt(chunkX);
            pkt.writeInt(chunkZ);

            for (int p = 0; p < columnsSent; p++) {
                pkt.writeByte(0);
            }

            for (int c = 0; c < batchSize; c++) {
                pkt.writeBytes(columnData[columnsSent + c]);
            }

            server.sendGamePacket(session, pkt.getBuf());
            columnsSent += batchSize;
        }
    }

    // ========== Factory ==========

    /**
     * Create the appropriate codec for the given MCPE protocol version.
     * Version ranges:
     * - v91+: MCPEPacketCodecV91
     * - v27-v81: MCPEPacketCodecV27(protocolVersion)
     * - v11-v20: MCPEPacketCodecLegacy(protocolVersion)
     * - v9: MCPEPacketCodecV9
     *
     * @param protocolVersion the MCPE protocol version number
     * @return a codec instance for that version
     */
    static MCPEPacketCodec forVersion(int protocolVersion) {
        if (protocolVersion >= MCPEConstants.MCPE_PROTOCOL_VERSION_91) {
            return new MCPEPacketCodecV91();
        }
        if (protocolVersion >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
            return new MCPEPacketCodecV27(protocolVersion);
        }
        if (protocolVersion >= MCPEConstants.MCPE_PROTOCOL_VERSION_11) {
            return new MCPEPacketCodecLegacy(protocolVersion);
        }
        return new MCPEPacketCodecV9();
    }
}
