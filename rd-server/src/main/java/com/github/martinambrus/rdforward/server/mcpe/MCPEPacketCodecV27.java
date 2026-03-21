package com.github.martinambrus.rdforward.server.mcpe;

import com.github.martinambrus.rdforward.server.ServerWorld;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * MCPE packet codec for protocol versions 27-81 (0.11.0 through 0.15.x).
 *
 * Key characteristics:
 * - Long (64-bit) entity IDs
 * - Standard Float coordinates (not LFloat)
 * - Standard String encoding (short-prefixed, not VarInt-prefixed)
 * - v27-v33: V27_* packet IDs
 * - v34-v44: V34_* packet IDs, PlayerList + UUID-based spawning, skin support
 * - v38+: different skin format in PlayerList (string instead of byte for slim flag)
 * - v45-v80: V34_* IDs with 0x8E wrapper byte (handled externally)
 * - v81: V81_* packet IDs, 0xFE wrapper byte, JWT-based login
 */
public class MCPEPacketCodecV27 implements MCPEPacketCodec {

    private final int protocolVersion;

    public MCPEPacketCodecV27(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    // ========== C2S packet readers ==========

    @Override
    public MCPEPacketData.MovePlayerData readMovePlayer(MCPEPacketBuffer buf) {
        long entityId = buf.readLong();
        float x = buf.readFloat();
        float y = buf.readFloat();
        float z = buf.readFloat();
        float yaw = buf.readFloat();
        float headYaw = buf.readFloat();
        float pitch = buf.readFloat();
        int mode = buf.readUnsignedByte();
        int onGround = buf.readUnsignedByte();
        return new MCPEPacketData.MovePlayerData(entityId, x, y, z, yaw, pitch, true);
    }

    @Override
    public MCPEPacketData.RemoveBlockData readRemoveBlock(MCPEPacketBuffer buf) {
        buf.readLong(); // 64-bit entity ID (discarded)
        int x = buf.readInt();
        int z = buf.readInt();
        int y = buf.readUnsignedByte();
        return new MCPEPacketData.RemoveBlockData(x, y, z);
    }

    @Override
    public MCPEPacketData.UseItemData readUseItem(MCPEPacketBuffer buf) {
        int blockX = buf.readInt();
        int blockY = buf.readInt();
        int blockZ = buf.readInt();
        int face;
        int itemId;
        int meta;
        if (protocolVersion >= MCPEConstants.MCPE_PROTOCOL_VERSION_34) {
            // v34+: face is byte, then 6 floats (fx, fy, fz, posX, posY, posZ), then slot compound
            face = buf.readUnsignedByte();
            buf.skipBytes(24); // 6 x 4 bytes
            // v34 slot: short id, [byte count, short damage, short nbtLen, [nbt]]
            itemId = buf.readShort();
            if (itemId != 0) {
                buf.readUnsignedByte(); // count
                meta = buf.readShort(); // damage
                int nbtLen = buf.readShort();
                if (nbtLen > 0 && nbtLen <= buf.readableBytes()) {
                    buf.skipBytes(nbtLen);
                }
            } else {
                meta = 0;
            }
        } else {
            // v27-v33: face is byte (v20+ uses byte)
            face = buf.readUnsignedByte();
            itemId = buf.readShort();
            meta = buf.readShort(); // v27 uses short for meta (v17+ format)
            buf.readLong(); // v27: 64-bit entity ID
        }
        return new MCPEPacketData.UseItemData(blockX, blockY, blockZ, face, itemId, meta);
    }

    @Override
    public String readMessage(MCPEPacketBuffer buf) {
        // v27: type(byte) + source(string) + message(string)
        int type = buf.readUnsignedByte();
        buf.readString(); // source (player name)
        return buf.readString();
    }

    @Override
    public MCPEPacketData.RequestChunkData readRequestChunk(MCPEPacketBuffer buf) {
        // v27-v80: specific chunk coordinates (int x, int z)
        // v81: uses REQUEST_CHUNK_RADIUS (VarInt radius) — but still reads as int pair here
        // The actual v81 REQUEST_CHUNK_RADIUS is handled separately via canonical ID mapping
        int chunkX = buf.readInt();
        int chunkZ = buf.readInt();
        return new MCPEPacketData.RequestChunkData(false, chunkX, chunkZ);
    }

    @Override
    public void readPlayerEquipment(MCPEPacketBuffer buf) {
        buf.readLong(); // v27+: 64-bit entity ID
        if (protocolVersion >= MCPEConstants.MCPE_PROTOCOL_VERSION_34) {
            // v34: slot compound — short id, [byte count, short damage, short nbtLen, [nbt]]
            int itemId = buf.readShort();
            if (itemId != 0 && buf.readableBytes() >= 5) {
                buf.readUnsignedByte(); // count
                buf.readShort(); // damage
                int nbtLen = buf.readShort();
                if (nbtLen > 0 && nbtLen <= buf.readableBytes()) {
                    buf.skipBytes(nbtLen);
                }
            }
            buf.readByte(); // slot
            buf.readByte(); // selectedSlot
        } else {
            buf.readShort(); // item id
            buf.readShort(); // item meta
            buf.readByte();  // slot
            buf.readByte();  // selectedSlot
        }
    }

    @Override
    public MCPEPacketData.PlayerActionData readPlayerAction(MCPEPacketBuffer buf) {
        buf.readLong(); // v27: 64-bit entity ID
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
        int entityId = (int) buf.readLong(); // v27+: 64-bit entity ID (truncate for broadcast)
        return new MCPEPacketData.AnimateData(action, entityId);
    }

    @Override
    public void readInteract(MCPEPacketBuffer buf) {
        int action = buf.readUnsignedByte();
        long targetId = buf.readLong(); // v27: just target entity (long)
        // Interaction handling — ignore for now
    }

    @Override
    public MCPEPacketData.PlaceBlockData readPlaceBlock(MCPEPacketBuffer buf) {
        return null; // v9 only feature
    }

    // ========== S2C packet writers (gameplay broadcasts) ==========

    @Override
    public void writeMovePlayer(MCPEPacketBuffer buf, int entityId,
                                float x, float y, float z, float yaw, float pitch) {
        buf.writeLong(entityId);
        buf.writeFloat(x);
        buf.writeFloat(y);
        buf.writeFloat(z);
        buf.writeFloat(yaw);
        buf.writeFloat(yaw);  // headYaw
        buf.writeFloat(pitch);
        buf.writeByte(0);     // mode = normal
        buf.writeByte(0);     // onGround
    }

    @Override
    public void writeUpdateBlock(MCPEPacketBuffer buf, int x, int y, int z,
                                 int blockId, int meta, int flags) {
        buf.writeInt(x);
        buf.writeInt(z);
        buf.writeByte(y);
        buf.writeByte(blockId);
        buf.writeByte((meta & 0x0F) | (flags << 4));
    }

    @Override
    public void writeAnimate(MCPEPacketBuffer buf, int entityId, int action) {
        buf.writeByte(action);
        buf.writeLong(entityId);
    }

    // ========== S2C translation writers (Classic -> MCPE) ==========

    @Override
    public void writeSetBlock(MCPEPacketBuffer buf, int x, int y, int z, int blockType) {
        buf.writeByte(wireId(MCPEConstants.UPDATE_BLOCK));
        buf.writeInt(x);
        buf.writeInt(z);
        buf.writeByte(y);
        buf.writeByte(blockType);
        buf.writeByte(MCPEConstants.FLAG_ALL_PRIORITY << 4);
    }

    @Override
    public void writeSpawnPlayer(LegacyRakNetServer server, LegacyRakNetSession session,
                                 MCPEPacketData.SpawnPlayerData data) {
        int entityId = (data.playerId & 0xFF) + 1;
        String name = data.playerName;
        float x = data.x;
        float y = data.y;
        float z = data.z;
        float yaw = data.yaw;
        float pitch = data.pitch;

        boolean isV34 = protocolVersion >= MCPEConstants.MCPE_PROTOCOL_VERSION_34;
        boolean isV81 = protocolVersion >= MCPEConstants.MCPE_PROTOCOL_VERSION_81;
        UUID uuid = isV34 ? UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)) : null;

        // v34+: send PlayerListAdd before AddPlayer (registers skin)
        if (isV34) {
            byte[] skinData = data.skinData;
            if (skinData == null || skinData.length == 0) {
                skinData = MCPEConstants.getDefaultSkin();
            }
            MCPEPacketBuffer plPkt = new MCPEPacketBuffer();
            plPkt.writeByte(isV81 ? (MCPEConstants.V81_PLAYER_LIST & 0xFF)
                    : (MCPEConstants.V34_PLAYER_LIST & 0xFF));
            plPkt.writeByte(0); // TYPE_ADD
            plPkt.writeInt(1);  // entry count
            plPkt.writeLong(uuid.getMostSignificantBits());
            plPkt.writeLong(uuid.getLeastSignificantBits());
            plPkt.writeLong(entityId);  // entityId
            plPkt.writeString(name);
            if (isV81) {
                plPkt.writeString("Standard_Steve");
                plPkt.writeShort(skinData.length);
                plPkt.writeBytes(skinData);
            } else {
                if (protocolVersion >= MCPEConstants.MCPE_PROTOCOL_VERSION_38) {
                    plPkt.writeString("");
                } else {
                    plPkt.writeByte(0);
                }
                plPkt.writeShort(skinData.length);
                plPkt.writeBytes(skinData);
            }
            server.sendGamePacket(session, plPkt.getBuf());
        }

        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        buf.writeByte(wireId(MCPEConstants.ADD_PLAYER));
        if (isV34) {
            buf.writeLong(uuid.getMostSignificantBits());
            buf.writeLong(uuid.getLeastSignificantBits());
            buf.writeString(name);
            buf.writeLong(entityId);
            buf.writeFloat(x);
            buf.writeFloat(y);
            buf.writeFloat(z);
            buf.writeFloat(0); buf.writeFloat(0); buf.writeFloat(0);
            buf.writeFloat(yaw);
            buf.writeFloat(yaw);
            buf.writeFloat(pitch);
            buf.writeShort(0); // held item (air)
        } else {
            // v27-v33
            buf.writeLong(entityId);
            buf.writeString(name);
            buf.writeLong(entityId);
            buf.writeFloat(x);
            buf.writeFloat(y);
            buf.writeFloat(z);
            buf.writeFloat(0); buf.writeFloat(0); buf.writeFloat(0);
            buf.writeFloat(yaw);
            buf.writeFloat(yaw);
            buf.writeFloat(pitch);
            buf.writeShort(0); buf.writeShort(0);
            // skin data
            if (data.skinData != null && data.skinData.length > 0) {
                buf.writeByte(data.skinSlim);
                buf.writeShort(data.skinData.length);
                buf.writeBytes(data.skinData);
            } else {
                buf.writeByte(0);
                buf.writeShort(MCPEConstants.getDefaultSkin().length);
                buf.writeBytes(MCPEConstants.getDefaultSkin());
            }
        }

        writePlayerMetadata(buf, name);
        server.sendGamePacket(session, buf.getBuf());

        // Send SetEntityData for nametag visibility
        MCPEPacketBuffer meta = new MCPEPacketBuffer();
        meta.writeByte(wireId(MCPEConstants.SET_ENTITY_DATA));
        meta.writeLong(entityId);
        writePlayerMetadata(meta, name);
        server.sendGamePacket(session, meta.getBuf());
    }

    @Override
    public void writeDespawnPlayer(LegacyRakNetServer server, LegacyRakNetSession session,
                                   MCPEPacketData.DespawnPlayerData data) {
        int entityId = data.entityId;
        boolean isV34 = protocolVersion >= MCPEConstants.MCPE_PROTOCOL_VERSION_34;
        boolean isV81 = protocolVersion >= MCPEConstants.MCPE_PROTOCOL_VERSION_81;
        UUID despawnUuid = isV34 ? ((data.uuid != null) ? data.uuid : new UUID(0, entityId)) : null;

        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        if (isV81) {
            buf.writeByte(MCPEConstants.V81_REMOVE_ENTITY & 0xFF);
            buf.writeLong(entityId);
        } else {
            buf.writeByte(wireId(MCPEConstants.REMOVE_PLAYER));
            buf.writeLong(entityId);
            if (isV34) {
                buf.writeLong(despawnUuid.getMostSignificantBits());
                buf.writeLong(despawnUuid.getLeastSignificantBits());
            } else {
                buf.writeLong(entityId);
            }
        }
        server.sendGamePacket(session, buf.getBuf());

        // v34+: also send PlayerList TYPE_REMOVE
        if (isV34) {
            MCPEPacketBuffer plPkt = new MCPEPacketBuffer();
            plPkt.writeByte(isV81 ? (MCPEConstants.V81_PLAYER_LIST & 0xFF)
                    : (MCPEConstants.V34_PLAYER_LIST & 0xFF));
            plPkt.writeByte(1); // TYPE_REMOVE
            plPkt.writeInt(1);
            plPkt.writeLong(despawnUuid.getMostSignificantBits());
            plPkt.writeLong(despawnUuid.getLeastSignificantBits());
            server.sendGamePacket(session, plPkt.getBuf());
        }
    }

    @Override
    public void writePlayerTeleport(MCPEPacketBuffer buf, int entityId,
                                    float x, float y, float z, float yaw, float pitch) {
        buf.writeByte(wireId(MCPEConstants.MOVE_PLAYER));
        buf.writeLong(entityId);
        buf.writeFloat(x);
        buf.writeFloat(y);
        buf.writeFloat(z);
        buf.writeFloat(yaw);
        buf.writeFloat(yaw);  // headYaw
        buf.writeFloat(pitch);
        buf.writeByte(0);     // mode = normal
        buf.writeByte(0);     // onGround
    }

    @Override
    public void writeMessage(MCPEPacketBuffer buf, String message) {
        buf.writeByte(wireId(MCPEConstants.MESSAGE));
        buf.writeByte(1); // type = CHAT
        buf.writeString("");
        buf.writeString(message);
    }

    @Override
    public void writeTimeUpdate(MCPEPacketBuffer buf, int time) {
        buf.writeByte(wireId(MCPEConstants.SET_TIME));
        buf.writeInt(time);
        buf.writeByte(protocolVersion >= MCPEConstants.MCPE_PROTOCOL_VERSION_34 ? 1 : 0x80);
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

        // v34: extra data count (int 0 = no extra data) appended after biomeColors
        boolean isV34 = protocolVersion >= MCPEConstants.MCPE_PROTOCOL_VERSION_34;
        int extraDataSize = isV34 ? 4 : 0;

        int terrainSize = blockIds.length + metadata.length + skyLight.length
                + blockLight.length + heightMap.length + biomeColors.length + extraDataSize;
        byte[] terrain = new byte[terrainSize];
        int pos = 0;
        System.arraycopy(blockIds, 0, terrain, pos, blockIds.length); pos += blockIds.length;
        System.arraycopy(metadata, 0, terrain, pos, metadata.length); pos += metadata.length;
        System.arraycopy(skyLight, 0, terrain, pos, skyLight.length); pos += skyLight.length;
        System.arraycopy(blockLight, 0, terrain, pos, blockLight.length); pos += blockLight.length;
        System.arraycopy(heightMap, 0, terrain, pos, heightMap.length); pos += heightMap.length;
        System.arraycopy(biomeColors, 0, terrain, pos, biomeColors.length); pos += biomeColors.length;
        // v34: write extraData count = 0 (big-endian int, already zeroed in array)

        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(wireId(MCPEConstants.CHUNK_DATA));
        pkt.writeInt(chunkX);
        pkt.writeInt(chunkZ);
        if (isV34) {
            pkt.writeByte(0); // v34+: order byte (0 = ORDER_COLUMNS)
        }
        pkt.writeInt(terrain.length);
        pkt.writeBytes(terrain);
        server.sendGamePacket(session, pkt.getBuf());
    }

    // ========== Version info ==========

    @Override
    public int wireId(int canonicalId) {
        return MCPEConstants.toWireId(canonicalId, protocolVersion);
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
        // v34+: ACTION_CREATIVE_DESTROY (13) is used for creative block breaking
        // v27-v33: START_BREAK triggers destroy (same as v14-v27 range)
        if (protocolVersion >= MCPEConstants.MCPE_PROTOCOL_VERSION_34) {
            return action == MCPEConstants.ACTION_CREATIVE_DESTROY;
        }
        return action == MCPEConstants.ACTION_START_BREAK;
    }

    @Override
    public boolean usesRaycastBreaking() {
        return false; // v27+ sends RemoveBlock or PlayerAction, no need for raycast
    }

    // ========== Private helpers ==========

    /**
     * Write the standard pre-v91 player metadata block (4 entries: flags, air, nametag, showNametag).
     * Used by both AddPlayer and SetEntityData packets.
     */
    private void writePlayerMetadata(MCPEPacketBuffer buf, String name) {
        buf.writeMetaByte(MCPEConstants.META_FLAGS, (byte) 0);
        buf.writeMetaShort(MCPEConstants.META_AIR, (short) 300);
        buf.writeMetaString(MCPEConstants.META_NAMETAG, name);
        buf.writeMetaByte(MCPEConstants.META_SHOW_NAMETAG, (byte) 1);
        buf.writeMetaEnd();
    }
}
