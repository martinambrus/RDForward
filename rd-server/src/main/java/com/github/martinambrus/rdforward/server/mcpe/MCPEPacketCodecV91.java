package com.github.martinambrus.rdforward.server.mcpe;

import com.github.martinambrus.rdforward.server.ServerWorld;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * MCPE protocol version 91 (0.16.0) packet codec.
 *
 * Wire format characteristics:
 * - VarInt entity IDs (readSignedVarInt/writeSignedVarInt)
 * - LFloat coordinates (readLFloat/writeLFloat)
 * - VarInt-prefixed strings (readStringV91/writeStringV91)
 * - BlockCoords (readBlockCoords/writeBlockCoords)
 * - UnsignedVarInt for block IDs and flags
 * - V91 metadata format (writeMetadataV91Start, writeMetaLongV91, etc.)
 * - Uses V91_* packet IDs from MCPEConstants
 */
public class MCPEPacketCodecV91 implements MCPEPacketCodec {

    @Override
    public MCPEPacketData.MovePlayerData readMovePlayer(MCPEPacketBuffer buf) {
        long entityId = buf.readSignedVarInt();
        float x = buf.readLFloat();
        float y = buf.readLFloat();
        float z = buf.readLFloat();
        float pitch = buf.readLFloat();
        float yaw = buf.readLFloat();
        buf.readLFloat(); // bodyYaw (consumed, not used)
        buf.readUnsignedByte(); // mode
        buf.readUnsignedByte(); // onGround
        return new MCPEPacketData.MovePlayerData(entityId, x, y, z, yaw, pitch, true);
    }

    @Override
    public MCPEPacketData.RemoveBlockData readRemoveBlock(MCPEPacketBuffer buf) {
        int[] coords = buf.readBlockCoords();
        return new MCPEPacketData.RemoveBlockData(coords[0], coords[1], coords[2]);
    }

    @Override
    public MCPEPacketData.UseItemData readUseItem(MCPEPacketBuffer buf) {
        int[] coords = buf.readBlockCoords();
        int face = buf.readSignedVarInt();
        buf.readLFloat(); buf.readLFloat(); buf.readLFloat(); // fx, fy, fz
        buf.readLFloat(); buf.readLFloat(); buf.readLFloat(); // posX, posY, posZ
        buf.readSignedVarInt(); // slot index
        // v91 slot: VarInt id, if id>0: VarInt auxValue (data<<8|count), LShort nbtLen, nbt
        int itemId = buf.readSignedVarInt();
        int meta = 0;
        if (itemId > 0) {
            int auxValue = buf.readSignedVarInt();
            meta = auxValue >> 8;
            int nbtLen = buf.getBuf().readUnsignedShortLE();
            if (nbtLen > 0 && nbtLen <= buf.readableBytes()) {
                buf.skipBytes(nbtLen);
            }
        }
        return new MCPEPacketData.UseItemData(coords[0], coords[1], coords[2], face, itemId, meta);
    }

    @Override
    public String readMessage(MCPEPacketBuffer buf) {
        int type = buf.readUnsignedByte();
        if (type == 1 || type == 3) { // CHAT or POPUP: source + message
            buf.readStringV91(); // source
        }
        return buf.readStringV91();
    }

    @Override
    public MCPEPacketData.RequestChunkData readRequestChunk(MCPEPacketBuffer buf) {
        int radius = buf.readSignedVarInt();
        return new MCPEPacketData.RequestChunkData(true, radius, 0);
    }

    @Override
    public void readPlayerEquipment(MCPEPacketBuffer buf) {
        buf.readSignedVarInt(); // entity ID
        int itemId = buf.readSignedVarInt();
        if (itemId > 0) {
            buf.readSignedVarInt(); // auxValue (data<<8|count)
            int nbtLen = buf.getBuf().readUnsignedShortLE();
            if (nbtLen > 0 && nbtLen <= buf.readableBytes()) {
                buf.skipBytes(nbtLen);
            }
        }
        buf.readByte(); // slot
        buf.readByte(); // selectedSlot
        buf.readByte(); // unknownByte
    }

    @Override
    public MCPEPacketData.PlayerActionData readPlayerAction(MCPEPacketBuffer buf) {
        buf.readSignedVarInt(); // entity ID
        int action = buf.readSignedVarInt();
        int[] coords = buf.readBlockCoords();
        int face = buf.readSignedVarInt();
        return new MCPEPacketData.PlayerActionData(action, coords[0], coords[1], coords[2], face);
    }

    @Override
    public MCPEPacketData.AnimateData readAnimate(MCPEPacketBuffer buf) {
        int action = buf.readUnsignedByte();
        int entityId = buf.readSignedVarInt();
        return new MCPEPacketData.AnimateData(action, entityId);
    }

    @Override
    public void readInteract(MCPEPacketBuffer buf) {
        buf.readUnsignedByte(); // action
        buf.readSignedVarInt(); // target entity ID
    }

    @Override
    public MCPEPacketData.PlaceBlockData readPlaceBlock(MCPEPacketBuffer buf) {
        return null; // v91 uses UseItem for block placement
    }

    // ========== S2C packet writers (gameplay broadcasts) ==========

    @Override
    public void writeMovePlayer(MCPEPacketBuffer buf, int entityId,
                                float x, float y, float z, float yaw, float pitch) {
        buf.writeSignedVarInt(entityId);
        buf.writeLFloat(x);
        buf.writeLFloat(y); // eye-level
        buf.writeLFloat(z);
        buf.writeLFloat(pitch);
        buf.writeLFloat(yaw);
        buf.writeLFloat(yaw); // bodyYaw
        buf.writeByte(0);     // mode = normal
        buf.writeByte(0);     // onGround
    }

    @Override
    public void writeUpdateBlock(MCPEPacketBuffer buf, int x, int y, int z,
                                 int blockId, int meta, int flags) {
        buf.writeBlockCoords(x, y, z);
        buf.writeUnsignedVarInt(blockId);
        buf.writeUnsignedVarInt((flags << 4) | (meta & 0x0F));
    }

    @Override
    public void writeAnimate(MCPEPacketBuffer buf, int entityId, int action) {
        buf.writeByte(action);
        buf.writeSignedVarInt(entityId);
    }

    // ========== S2C translation writers (Classic -> MCPE) ==========

    @Override
    public void writeSetBlock(MCPEPacketBuffer buf, int x, int y, int z, int blockType) {
        buf.writeByte(MCPEConstants.V91_UPDATE_BLOCK & 0xFF);
        buf.writeBlockCoords(x, y, z);
        buf.writeUnsignedVarInt(blockType);
        buf.writeUnsignedVarInt(MCPEConstants.FLAG_ALL_PRIORITY << 4); // meta=0
    }

    @Override
    public void writeSpawnPlayer(LegacyRakNetServer server, LegacyRakNetSession session,
                                 MCPEPacketData.SpawnPlayerData data) {
        int entityId = (data.playerId & 0xFF) + 1;
        String name = data.playerName;
        byte[] skinData = data.skinData;
        if (skinData == null || skinData.length == 0) {
            skinData = MCPEConstants.getDefaultSkin();
        }

        UUID uuid = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));

        // PlayerListAdd (registers skin before AddPlayer)
        MCPEPacketBuffer plPkt = new MCPEPacketBuffer();
        plPkt.writeByte(MCPEConstants.V91_PLAYER_LIST & 0xFF);
        plPkt.writeByte(0); // TYPE_ADD
        plPkt.writeUnsignedVarInt(1); // entry count
        plPkt.writeLong(uuid.getMostSignificantBits());
        plPkt.writeLong(uuid.getLeastSignificantBits());
        plPkt.writeSignedVarInt(entityId);
        plPkt.writeStringV91(name);
        plPkt.writeStringV91("Standard_Steve");
        plPkt.writeUnsignedVarInt(skinData.length);
        plPkt.writeBytes(skinData);
        server.sendGamePacket(session, plPkt.getBuf());

        // AddPlayer
        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        buf.writeByte(MCPEConstants.V91_ADD_PLAYER & 0xFF);
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
        buf.writeStringV91(name);
        buf.writeSignedVarInt(entityId); // uniqueId
        buf.writeSignedVarInt(entityId); // runtimeId
        buf.writeLFloat(data.x);
        buf.writeLFloat(data.y);
        buf.writeLFloat(data.z);
        buf.writeLFloat(0); // speedX
        buf.writeLFloat(0); // speedY
        buf.writeLFloat(0); // speedZ
        buf.writeLFloat(data.yaw);
        buf.writeLFloat(data.yaw);  // headYaw
        buf.writeLFloat(data.pitch);
        buf.writeSignedVarInt(0); // held item (air)
        writeV91PlayerMetadata(buf, name);
        server.sendGamePacket(session, buf.getBuf());

        // SetEntityData
        MCPEPacketBuffer meta = new MCPEPacketBuffer();
        meta.writeByte(MCPEConstants.V91_SET_ENTITY_DATA & 0xFF);
        meta.writeSignedVarInt(entityId);
        writeV91PlayerMetadata(meta, name);
        server.sendGamePacket(session, meta.getBuf());
    }

    @Override
    public void writeDespawnPlayer(LegacyRakNetServer server, LegacyRakNetSession session,
                                   MCPEPacketData.DespawnPlayerData data) {
        int entityId = data.entityId;

        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        buf.writeByte(MCPEConstants.V91_REMOVE_ENTITY & 0xFF);
        buf.writeSignedVarInt(entityId);
        server.sendGamePacket(session, buf.getBuf());

        // PlayerList TYPE_REMOVE
        UUID despawnUuid = (data.uuid != null) ? data.uuid : new UUID(0, entityId);
        MCPEPacketBuffer plPkt = new MCPEPacketBuffer();
        plPkt.writeByte(MCPEConstants.V91_PLAYER_LIST & 0xFF);
        plPkt.writeByte(1); // TYPE_REMOVE
        plPkt.writeUnsignedVarInt(1);
        plPkt.writeLong(despawnUuid.getMostSignificantBits());
        plPkt.writeLong(despawnUuid.getLeastSignificantBits());
        server.sendGamePacket(session, plPkt.getBuf());
    }

    @Override
    public void writePlayerTeleport(MCPEPacketBuffer buf, int entityId,
                                    float x, float y, float z, float yaw, float pitch) {
        buf.writeByte(MCPEConstants.V91_MOVE_PLAYER & 0xFF);
        buf.writeSignedVarInt(entityId);
        buf.writeLFloat(x);
        buf.writeLFloat(y);
        buf.writeLFloat(z);
        buf.writeLFloat(pitch);
        buf.writeLFloat(yaw);
        buf.writeLFloat(yaw); // bodyYaw
        buf.writeByte(0);     // mode = normal
        buf.writeByte(0);     // onGround
    }

    @Override
    public void writeMessage(MCPEPacketBuffer buf, String message) {
        buf.writeByte(MCPEConstants.V91_TEXT & 0xFF);
        buf.writeByte(1); // type = CHAT
        buf.writeStringV91(""); // source
        buf.writeStringV91(message);
    }

    @Override
    public void writeTimeUpdate(MCPEPacketBuffer buf, int time) {
        buf.writeByte(MCPEConstants.V91_SET_TIME & 0xFF);
        buf.writeSignedVarInt(time);
        buf.writeByte(1); // started
    }

    // ========== Chunk sending ==========

    @Override
    public void sendChunkData(LegacyRakNetServer server, LegacyRakNetSession session,
                              ServerWorld world, int chunkX, int chunkZ) {
        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;
        int height = 128;

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
                        int block = MCPEPacketCodec.mapBlockId(world.getBlock(worldX, y, worldZ));
                        blockIds[idx] = (byte) block;
                        if (block != 0) topY = y + 1;
                    }
                }
                heightMap[(z << 4) | x] = (byte) topY;
            }
        }

        byte[] metadata = new byte[16384];
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

        byte[] blockLight = new byte[16384];
        byte[] biomeColors = new byte[1024];
        for (int i = 0; i < 256; i++) {
            int offset = i * 4;
            biomeColors[offset] = 0x01;
            biomeColors[offset + 1] = 0x7A;
            biomeColors[offset + 2] = (byte) 0xBD;
            biomeColors[offset + 3] = 0x6B;
        }

        // Terrain: blockIds + metadata + skyLight + blockLight + heightMap + biomeColors + int(0) extraData
        int terrainSize = blockIds.length + metadata.length + skyLight.length
                + blockLight.length + heightMap.length + biomeColors.length + 4;
        byte[] terrain = new byte[terrainSize];
        int pos = 0;
        System.arraycopy(blockIds, 0, terrain, pos, blockIds.length); pos += blockIds.length;
        System.arraycopy(metadata, 0, terrain, pos, metadata.length); pos += metadata.length;
        System.arraycopy(skyLight, 0, terrain, pos, skyLight.length); pos += skyLight.length;
        System.arraycopy(blockLight, 0, terrain, pos, blockLight.length); pos += blockLight.length;
        System.arraycopy(heightMap, 0, terrain, pos, heightMap.length); pos += heightMap.length;
        System.arraycopy(biomeColors, 0, terrain, pos, biomeColors.length);
        // extraData count = 0 (last 4 bytes already zeroed)

        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(MCPEConstants.V91_FULL_CHUNK_DATA & 0xFF);
        pkt.writeSignedVarInt(chunkX);
        pkt.writeSignedVarInt(chunkZ);
        pkt.writeByte(0); // ORDER_COLUMNS
        pkt.writeUnsignedVarInt(terrain.length);
        pkt.writeBytes(terrain);
        server.sendGamePacket(session, pkt.getBuf());
    }

    // ========== Version info ==========

    @Override
    public int wireId(int canonicalId) {
        return MCPEConstants.toWireId(canonicalId, MCPEConstants.MCPE_PROTOCOL_VERSION_91);
    }

    @Override
    public boolean requiresChunkResendForBlockUpdate() {
        return true;
    }

    @Override
    public boolean isEyeLevelMovePlayer() {
        return true;
    }

    @Override
    public boolean shouldBreakOnAction(int action) {
        return false; // v91 creative breaking uses RemoveBlock, not PlayerAction
    }

    @Override
    public boolean usesRaycastBreaking() {
        return false;
    }

    // ========== Private helpers ==========

    /**
     * Write the standard v91 player metadata block (6 entries: flags, air, maxAir, nametag, lead, scale).
     * Used by both AddPlayer and SetEntityData packets.
     */
    private void writeV91PlayerMetadata(MCPEPacketBuffer buf, String name) {
        int flags = (1 << MCPEConstants.V91_FLAG_CAN_SHOW_NAMETAG)
                  | (1 << MCPEConstants.V91_FLAG_ALWAYS_SHOW_NAMETAG);
        buf.writeMetadataV91Start(6);
        buf.writeMetaLongV91(MCPEConstants.V91_META_FLAGS, flags);
        buf.writeMetaShortV91(MCPEConstants.V91_META_AIR, (short) 400);
        buf.writeMetaShortV91(MCPEConstants.V91_META_MAX_AIR, (short) 400);
        buf.writeMetaStringV91Entry(MCPEConstants.V91_META_NAMETAG, name);
        buf.writeMetaLongV91(MCPEConstants.V91_META_LEAD_HOLDER_EID, -1);
        buf.writeMetaFloatV91(MCPEConstants.V91_META_SCALE, 1.0f);
    }
}
