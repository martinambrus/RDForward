package com.github.martinambrus.rdforward.server.mcpe;

import com.github.martinambrus.rdforward.server.ServerWorld;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * MCPE packet codec for protocols 11-20 (0.7.0 through 0.10.0).
 *
 * Key version differences:
 * <ul>
 *   <li>v11-v13: feet-level MovePlayer Y, no bodyYaw, arm-swing raycast breaking</li>
 *   <li>v14: bodyYaw added to MovePlayer (bodyYaw, pitch, headYaw), sends RemoveBlock</li>
 *   <li>v17: short metadata in UseItem (vs byte), sendFullChunkDataV17 format, arm-swing breaking</li>
 *   <li>v18: adds 1-bit teleport flag (trailing byte) to MovePlayer</li>
 *   <li>v20: face is byte (not int) in UseItem, sends RemoveBlock</li>
 * </ul>
 */
public class MCPEPacketCodecLegacy implements MCPEPacketCodec {

    private final int protocolVersion;

    public MCPEPacketCodecLegacy(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    // ========== C2S packet readers ==========

    @Override
    public MCPEPacketData.MovePlayerData readMovePlayer(MCPEPacketBuffer buf) {
        int entityId = buf.readInt();
        float x = buf.readFloat();
        float y = buf.readFloat();
        float z = buf.readFloat();
        float yaw, pitch;
        if (protocolVersion >= MCPEConstants.MCPE_PROTOCOL_VERSION_14) {
            buf.readFloat(); // bodyYaw (discarded — we use headYaw for both)
            pitch = buf.readFloat();
            yaw = buf.readFloat(); // headYaw
        } else {
            yaw = buf.readFloat();
            pitch = buf.readFloat();
        }
        if (protocolVersion >= MCPEConstants.MCPE_PROTOCOL_VERSION_18) {
            buf.readUnsignedByte(); // teleport flag (1 RakNet bit, occupies 1 byte on wire)
        }
        // v11-v20: feet-level Y
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
        int blockX = buf.readInt();
        int blockY = buf.readInt();
        int blockZ = buf.readInt();
        int face;
        if (protocolVersion >= MCPEConstants.MCPE_PROTOCOL_VERSION_20) {
            face = buf.readUnsignedByte();
        } else {
            face = buf.readInt();
        }
        int itemId = buf.readShort();
        int meta;
        if (protocolVersion >= MCPEConstants.MCPE_PROTOCOL_VERSION_17) {
            meta = buf.readShort();
        } else {
            meta = buf.readUnsignedByte();
        }
        buf.readInt(); // 32-bit entity ID
        return new MCPEPacketData.UseItemData(blockX, blockY, blockZ, face, itemId, meta);
    }

    @Override
    public String readMessage(MCPEPacketBuffer buf) {
        if (protocolVersion >= MCPEConstants.MCPE_PROTOCOL_VERSION_12) {
            buf.readString(); // player name
        }
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
        buf.readByte(); // slot
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
        // Legacy (v11-v20) does not use PlaceBlock — block placement is via UseItem
        return null;
    }

    // ========== S2C packet writers (gameplay broadcasts) ==========

    @Override
    public void writeMovePlayer(MCPEPacketBuffer buf, int entityId,
                                float x, float y, float z, float yaw, float pitch) {
        buf.writeInt(entityId);
        buf.writeFloat(x);
        if (protocolVersion >= MCPEConstants.MCPE_PROTOCOL_VERSION_18) {
            // v18+ (0.9.5): eye-level Y for S2C MovePlayer
            buf.writeFloat(y);
        } else {
            // v11-v17: feet-level Y for S2C MovePlayer
            buf.writeFloat(y - (float) PLAYER_EYE_HEIGHT);
        }
        buf.writeFloat(z);
        if (protocolVersion >= MCPEConstants.MCPE_PROTOCOL_VERSION_14) {
            buf.writeFloat(yaw);  // bodyYaw
            buf.writeFloat(pitch);
            buf.writeFloat(yaw);  // headYaw
        } else {
            buf.writeFloat(yaw);
            buf.writeFloat(pitch);
        }
        if (protocolVersion >= MCPEConstants.MCPE_PROTOCOL_VERSION_18) {
            buf.writeByte(0); // teleport flag = false (normal movement)
        }
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
        float yaw = data.yaw;
        float y = data.y; // feet-level for AddPlayer

        MCPEPacketBuffer buf = new MCPEPacketBuffer();
        buf.writeByte(wireId(MCPEConstants.ADD_PLAYER));
        buf.writeLong(entityId);
        buf.writeString(data.playerName);
        buf.writeInt(entityId);
        buf.writeFloat(data.x);
        buf.writeFloat(y);
        buf.writeFloat(data.z);
        // v11-v20: byte yaw, byte pitch (converted back from float to byte)
        buf.writeByte((int) (yaw * 256.0f / 360.0f) & 0xFF);
        buf.writeByte((int) (data.pitch * 256.0f / 360.0f) & 0xFF);
        buf.writeShort(0); // held item
        buf.writeShort(0); // item meta

        // Metadata
        buf.writeMetaByte(MCPEConstants.META_FLAGS, (byte) 0);
        buf.writeMetaShort(MCPEConstants.META_AIR, (short) 300);
        buf.writeMetaString(MCPEConstants.META_NAMETAG, data.playerName);
        buf.writeMetaByte(MCPEConstants.META_SHOW_NAMETAG, (byte) 1);
        buf.writeMetaEnd();
        server.sendGamePacket(session, buf.getBuf());

        // Send SetEntityData with metadata
        MCPEPacketBuffer meta = new MCPEPacketBuffer();
        meta.writeByte(wireId(MCPEConstants.SET_ENTITY_DATA));
        meta.writeInt(entityId);
        meta.writeMetaByte(MCPEConstants.META_FLAGS, (byte) 0);
        meta.writeMetaShort(MCPEConstants.META_AIR, (short) 300);
        meta.writeMetaString(MCPEConstants.META_NAMETAG, data.playerName);
        meta.writeMetaByte(MCPEConstants.META_SHOW_NAMETAG, (byte) 1);
        meta.writeMetaEnd();
        server.sendGamePacket(session, meta.getBuf());
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
        if (protocolVersion >= MCPEConstants.MCPE_PROTOCOL_VERSION_18) {
            // v18+ (0.9.5): eye-level Y for S2C MovePlayer
            buf.writeFloat(y);
        } else {
            // v11-v17: feet-level Y for S2C MovePlayer
            buf.writeFloat(y - (float) PLAYER_EYE_HEIGHT);
        }
        buf.writeFloat(z);
        if (protocolVersion >= MCPEConstants.MCPE_PROTOCOL_VERSION_14) {
            buf.writeFloat(yaw);  // bodyYaw
            buf.writeFloat(pitch);
            buf.writeFloat(yaw);  // headYaw
        } else {
            buf.writeFloat(yaw);
            buf.writeFloat(pitch);
        }
        if (protocolVersion >= MCPEConstants.MCPE_PROTOCOL_VERSION_18) {
            buf.writeByte(0x80); // teleport flag = true (snap to position)
        }
    }

    @Override
    public void writeMessage(MCPEPacketBuffer buf, String message) {
        buf.writeByte(wireId(MCPEConstants.MESSAGE));
        if (protocolVersion >= MCPEConstants.MCPE_PROTOCOL_VERSION_12) {
            buf.writeString("");
            buf.writeString(message);
        } else {
            buf.writeString(message);
        }
    }

    @Override
    public void writeTimeUpdate(MCPEPacketBuffer buf, int time) {
        buf.writeByte(wireId(MCPEConstants.SET_TIME));
        buf.writeInt(time);
        buf.writeByte(0x80); // "started" flag (v11-v20 format, v34+ uses 0x01)
    }

    // ========== Chunk sending ==========

    @Override
    public void sendChunkData(LegacyRakNetServer server, LegacyRakNetSession session,
                              ServerWorld world, int chunkX, int chunkZ) {
        if (protocolVersion >= MCPEConstants.MCPE_PROTOCOL_VERSION_17) {
            sendFullChunkDataV17(server, session, world, chunkX, chunkZ);
        } else {
            MCPEPacketCodec.sendColumnChunkData(server, session, world, chunkX, chunkZ,
                    wireId(MCPEConstants.CHUNK_DATA));
        }
    }

    /**
     * v17+ FullChunkDataPacket — zlib-compressed column data.
     * Binary analysis of FullChunkDataPacket::read confirms: client decompresses first,
     * then reads chunkX/chunkZ from the decompressed stream.
     * Terrain format (from deserializeTerrain): blockIDs(32768) + metadata(16384)
     * + skylight(16384) + blocklight(16384) + heightMap(256) + biomeColors(1024).
     */
    private void sendFullChunkDataV17(LegacyRakNetServer server, LegacyRakNetSession session,
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

        // Biome IDs: 256 bytes (all plains = 1). This field sits between blockLight
        // and biomeColors in the terrain stream. The client reads it into the heightMap
        // struct field but recalcHeightmap() overwrites it immediately after, so the
        // content doesn't affect rendering. PocketMine fills it with biome IDs.
        byte[] biomeIds = new byte[256];
        java.util.Arrays.fill(biomeIds, (byte) 1);

        byte[] biomeColors = new byte[1024];
        for (int i = 0; i < 256; i++) {
            int offset = i * 4;
            biomeColors[offset] = 0x01;
            biomeColors[offset + 1] = 0x7A;
            biomeColors[offset + 2] = (byte) 0xBD;
            biomeColors[offset + 3] = 0x6B;
        }

        // Assemble: chunkX + chunkZ + terrain data (all inside compressed payload)
        // chunkX/chunkZ are Little-Endian inside the compressed blob
        int totalSize = 4 + 4 + blockIds.length + metadata.length + skyLight.length
                + blockLight.length + biomeIds.length + biomeColors.length;
        byte[] uncompressed = new byte[totalSize];
        int pos = 0;
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

        // FULL_CHUNK_DATA_V17 is already a v17 wire ID, not v12 canonical — don't use wireId()
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(MCPEConstants.FULL_CHUNK_DATA_V17);
        pkt.writeBytes(compressed);
        server.sendGamePacket(session, pkt.getBuf());
    }

    // ========== Version info ==========

    @Override
    public int wireId(int canonicalId) {
        return MCPEConstants.toWireId(canonicalId, protocolVersion);
    }

    @Override
    public boolean requiresChunkResendForBlockUpdate() {
        return false;
    }

    @Override
    public boolean isEyeLevelMovePlayer() {
        return false; // v11-v20: feet-level
    }

    @Override
    public boolean shouldBreakOnAction(int action) {
        // v14-v20: START_BREAK triggers creative destroy
        // v11-v13: STOP_BREAK triggers creative destroy
        if (protocolVersion >= MCPEConstants.MCPE_PROTOCOL_VERSION_14) {
            return action == MCPEConstants.ACTION_START_BREAK;
        }
        return action == MCPEConstants.ACTION_STOP_BREAK;
    }

    @Override
    public boolean usesRaycastBreaking() {
        // v11-v13: arm swing triggers raycast breaking
        // v14: sends RemoveBlock directly — no raycast
        // v17-v18: arm swing for breaking
        // v20: sends RemoveBlock — no raycast
        return (protocolVersion >= MCPEConstants.MCPE_PROTOCOL_VERSION_11
                && protocolVersion < MCPEConstants.MCPE_PROTOCOL_VERSION_14)
            || (protocolVersion >= MCPEConstants.MCPE_PROTOCOL_VERSION_17
                && protocolVersion < MCPEConstants.MCPE_PROTOCOL_VERSION_20);
    }

}
