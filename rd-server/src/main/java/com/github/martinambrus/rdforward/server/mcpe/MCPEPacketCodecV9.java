package com.github.martinambrus.rdforward.server.mcpe;

import com.github.martinambrus.rdforward.server.ServerWorld;

/**
 * MCPE packet codec for protocol 9 (0.6.1).
 *
 * Key differences from v11:
 * <ul>
 *   <li>No bodyYaw, no headYaw in MovePlayer</li>
 *   <li>Uses PlaceBlock (0x95) instead of UseItem for block placement</li>
 *   <li>Uses CLIENT_MESSAGE (0xB4 / canonical 0xB5) for chat</li>
 *   <li>Simpler metadata format (no NAMETAG/SHOW_NAMETAG, uses position meta)</li>
 *   <li>No SET_ENTITY_LINK shift in wireId mapping</li>
 *   <li>readPlaceBlock() returns PlaceBlockData; readUseItem() returns null</li>
 *   <li>PlayerEquipment: simpler format (no selectedSlot)</li>
 * </ul>
 */
public class MCPEPacketCodecV9 implements MCPEPacketCodec {

    // ========== C2S packet readers ==========

    @Override
    public MCPEPacketData.MovePlayerData readMovePlayer(MCPEPacketBuffer buf) {
        int entityId = buf.readInt();
        float x = buf.readFloat();
        float y = buf.readFloat();
        float z = buf.readFloat();
        // v9: no bodyYaw, no headYaw — just yaw + pitch
        float yaw = buf.readFloat();
        float pitch = buf.readFloat();
        // v9: feet-level Y
        return new MCPEPacketData.MovePlayerData(entityId, x, y, z, yaw, pitch, false);
    }

    @Override
    public MCPEPacketData.RemoveBlockData readRemoveBlock(MCPEPacketBuffer buf) {
        buf.readInt(); // entity ID (32-bit)
        int x = buf.readInt();
        int z = buf.readInt();
        int y = buf.readUnsignedByte();
        return new MCPEPacketData.RemoveBlockData(x, y, z);
    }

    @Override
    public MCPEPacketData.UseItemData readUseItem(MCPEPacketBuffer buf) {
        // v9 UseItem: int x, int y, int z, int face, short block, byte meta, int eid, float fx, float fy, float fz
        int blockX = buf.readInt();
        int blockY = buf.readInt();
        int blockZ = buf.readInt();
        int face = buf.readInt();
        int itemId = buf.readShort();
        int meta = buf.readUnsignedByte();
        buf.readInt(); // entity ID (discarded)
        buf.readFloat(); buf.readFloat(); buf.readFloat(); // fx, fy, fz hit position
        return new MCPEPacketData.UseItemData(blockX, blockY, blockZ, face, itemId, meta);
    }

    @Override
    public String readMessage(MCPEPacketBuffer buf) {
        // v9: message only (no player name prefix)
        return buf.readString();
    }

    @Override
    public MCPEPacketData.RequestChunkData readRequestChunk(MCPEPacketBuffer buf) {
        int chunkX = buf.readInt();
        int chunkZ = buf.readInt();
        return new MCPEPacketData.RequestChunkData(false, chunkX, chunkZ);
    }

    @Override
    public void readPlayerEquipment(MCPEPacketBuffer buf) {
        buf.readInt(); // entity ID
        buf.readShort(); // item id
        buf.readShort(); // item meta
        // v9: no selectedSlot byte
    }

    @Override
    public MCPEPacketData.PlayerActionData readPlayerAction(MCPEPacketBuffer buf) {
        buf.readInt(); // entity ID
        int action = buf.readInt();
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        int face = buf.readInt();
        return new MCPEPacketData.PlayerActionData(action, x, y, z, face);
    }

    @Override
    public MCPEPacketData.AnimateData readAnimate(MCPEPacketBuffer buf) {
        int action = buf.readUnsignedByte();
        int entityId = buf.readInt();
        return new MCPEPacketData.AnimateData(action, entityId);
    }

    @Override
    public void readInteract(MCPEPacketBuffer buf) {
        buf.skipBytes(1 + 4 + 4); // action(byte) + entityId(int) + targetId(int)
    }

    @Override
    public MCPEPacketData.PlaceBlockData readPlaceBlock(MCPEPacketBuffer buf) {
        // v9 PlaceBlock: eid(int), x(int), z(int), y(byte), block(byte), meta(byte), face(byte)
        buf.readInt(); // entity ID (discarded)
        int blockX = buf.readInt();
        int blockZ = buf.readInt();
        int blockY = buf.readUnsignedByte();
        int block = buf.readUnsignedByte();
        int meta = buf.readUnsignedByte();
        int face = buf.readUnsignedByte();
        return new MCPEPacketData.PlaceBlockData(blockX, blockY, blockZ, block, meta, face);
    }

    // ========== S2C packet writers (gameplay broadcasts) ==========

    @Override
    public void writeMovePlayer(MCPEPacketBuffer buf, int entityId,
                                float x, float y, float z, float yaw, float pitch) {
        buf.writeInt(entityId);
        buf.writeFloat(x);
        // v9: feet-level Y for S2C MovePlayer
        buf.writeFloat(y - (float) PLAYER_EYE_HEIGHT);
        buf.writeFloat(z);
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
    }

    @Override
    public void writeUpdateBlock(MCPEPacketBuffer buf, int x, int y, int z,
                                 int blockId, int meta, int flags) {
        buf.writeInt(x);
        buf.writeInt(z);
        buf.writeByte(y);
        buf.writeByte(blockId);
        buf.writeByte((meta & 0x0F) | ((flags & 0x0F) << 4));
    }

    @Override
    public void writeAnimate(MCPEPacketBuffer buf, int entityId, int action) {
        buf.writeByte(action);
        buf.writeInt(entityId);
    }

    // ========== S2C translation writers (Classic -> MCPE) ==========

    @Override
    public void writeSetBlock(MCPEPacketBuffer buf, int x, int y, int z, int blockType) {
        buf.writeByte(wireId(MCPEConstants.UPDATE_BLOCK));
        buf.writeInt(x);
        buf.writeInt(z);
        buf.writeByte(y);
        buf.writeByte(blockType);
        buf.writeByte(MCPEConstants.FLAG_ALL_PRIORITY << 4); // meta=0, flags=FLAG_ALL_PRIORITY
    }

    @Override
    public void writeSpawnPlayer(LegacyRakNetServer server, LegacyRakNetSession session,
                                 MCPEPacketData.SpawnPlayerData data) {
        int entityId = (data.playerId & 0xFF) + 1;
        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        buf.writeByte(wireId(MCPEConstants.ADD_PLAYER));
        buf.writeLong(entityId);
        buf.writeString(data.playerName);
        buf.writeInt(entityId);
        buf.writeFloat(data.x);
        buf.writeFloat(data.y);
        buf.writeFloat(data.z);

        // v9 metadata: simpler format (no NAMETAG/SHOW_NAMETAG, uses position meta)
        buf.writeMetaByte(MCPEConstants.META_FLAGS, (byte) 0);
        buf.writeMetaShort(MCPEConstants.META_AIR, (short) 300);
        buf.writeMetaByte(16, (byte) 0);
        buf.writeMetaPosition(17, 0, 0, 0);
        buf.writeMetaEnd();
        server.sendGamePacket(session, buf.getBuf());

        // v9: send PlayerEquipment after AddPlayer (no SetEntityData)
        MCPEPacketBuffer eqPkt = new MCPEPacketBuffer();
        eqPkt.writeByte(wireId(MCPEConstants.PLAYER_EQUIPMENT));
        eqPkt.writeInt(entityId);
        eqPkt.writeShort(0); eqPkt.writeShort(0);
        server.sendGamePacket(session, eqPkt.getBuf());
    }

    @Override
    public void writeDespawnPlayer(LegacyRakNetServer server, LegacyRakNetSession session,
                                   MCPEPacketData.DespawnPlayerData data) {
        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        buf.writeByte(wireId(MCPEConstants.REMOVE_PLAYER));
        buf.writeInt(data.entityId);
        buf.writeLong(data.entityId);
        server.sendGamePacket(session, buf.getBuf());
    }

    @Override
    public void writePlayerTeleport(MCPEPacketBuffer buf, int entityId,
                                    float x, float y, float z, float yaw, float pitch) {
        buf.writeByte(wireId(MCPEConstants.MOVE_PLAYER));
        buf.writeInt(entityId);
        buf.writeFloat(x);
        // v9: feet-level Y
        buf.writeFloat(y - (float) PLAYER_EYE_HEIGHT);
        buf.writeFloat(z);
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
    }

    @Override
    public void writeMessage(MCPEPacketBuffer buf, String message) {
        buf.writeByte(wireId(MCPEConstants.MESSAGE));
        buf.writeString(message);
    }

    @Override
    public void writeTimeUpdate(MCPEPacketBuffer buf, int time) {
        buf.writeByte(wireId(MCPEConstants.SET_TIME));
        buf.writeInt(time);
        // v9: no trailing byte (v11+ adds 0x80 or 0x01)
    }

    // ========== Chunk sending ==========

    @Override
    public void sendChunkData(LegacyRakNetServer server, LegacyRakNetSession session,
                              ServerWorld world, int chunkX, int chunkZ) {
        MCPEPacketCodec.sendColumnChunkData(server, session, world, chunkX, chunkZ,
                wireId(MCPEConstants.CHUNK_DATA));
    }

    // ========== Version info ==========

    @Override
    public int wireId(int canonicalId) {
        return MCPEConstants.toWireId(canonicalId, MCPEConstants.MCPE_PROTOCOL_VERSION_9);
    }

    @Override
    public boolean requiresChunkResendForBlockUpdate() {
        return false;
    }

    @Override
    public boolean isEyeLevelMovePlayer() {
        return false; // v9: feet-level
    }

    @Override
    public boolean shouldBreakOnAction(int action) {
        // v9: same as v11-v13 — STOP_BREAK triggers creative destroy
        return action == MCPEConstants.ACTION_STOP_BREAK;
    }

    @Override
    public boolean usesRaycastBreaking() {
        // v9: sends RemoveBlock natively, so skip raycast
        return false;
    }

}
