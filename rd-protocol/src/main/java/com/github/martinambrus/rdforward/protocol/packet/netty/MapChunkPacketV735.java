package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.16 Play state, S2C packet 0x21: Chunk Data.
 *
 * Same as v573 but with an added ignoreOldLightData boolean after fullChunk.
 * Section data and heightmaps use non-spanning bit packing (handled by caller).
 *
 * Wire format:
 *   [int]       chunkX
 *   [int]       chunkZ
 *   [boolean]   fullChunk
 *   [boolean]   ignoreOldLightData (NEW in 1.16)
 *   [VarInt]    primaryBitMask
 *   [NBT]       heightmaps (TAG_Compound with MOTION_BLOCKING + WORLD_SURFACE)
 *   [int[1024]] biomes (if fullChunk)
 *   [VarInt]    dataSize
 *   [byte[]]    data (section data only, no biomes, no light)
 *   [VarInt]    blockEntityCount (0)
 */
public class MapChunkPacketV735 implements Packet {

    private int chunkX;
    private int chunkZ;
    private boolean fullChunk;
    private int primaryBitMask;
    private long[] motionBlockingHeightmap;
    private long[] worldSurfaceHeightmap;
    private int[] biomes;
    private byte[] data;

    public MapChunkPacketV735() {}

    public MapChunkPacketV735(int chunkX, int chunkZ, boolean fullChunk,
                               int primaryBitMask,
                               long[] motionBlockingHeightmap,
                               long[] worldSurfaceHeightmap,
                               int[] biomes,
                               byte[] data) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.fullChunk = fullChunk;
        this.primaryBitMask = primaryBitMask;
        this.motionBlockingHeightmap = motionBlockingHeightmap;
        this.worldSurfaceHeightmap = worldSurfaceHeightmap;
        this.biomes = biomes;
        this.data = data;
    }

    @Override
    public int getPacketId() { return 0x21; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);
        buf.writeBoolean(fullChunk);
        buf.writeBoolean(fullChunk); // ignoreOldLightData = true when fullChunk
        McDataTypes.writeVarInt(buf, primaryBitMask);

        // Write heightmaps as NBT compound
        writeHeightmapsNbt(buf);

        // Biomes: separate field in 1.15+ (was inside data array in 1.14)
        if (fullChunk && biomes != null) {
            for (int i = 0; i < biomes.length; i++) {
                buf.writeInt(biomes[i]);
            }
        }

        McDataTypes.writeVarInt(buf, data.length);
        buf.writeBytes(data);
        McDataTypes.writeVarInt(buf, 0); // blockEntityCount
    }

    private void writeHeightmapsNbt(ByteBuf buf) {
        // TAG_Compound (type 0x0A) with empty name
        buf.writeByte(0x0A);
        buf.writeShort(0); // name length = 0

        // MOTION_BLOCKING: TAG_Long_Array (type 0x0C)
        writeLongArrayTag(buf, "MOTION_BLOCKING", motionBlockingHeightmap);

        // WORLD_SURFACE: TAG_Long_Array (type 0x0C)
        writeLongArrayTag(buf, "WORLD_SURFACE", worldSurfaceHeightmap);

        // TAG_End
        buf.writeByte(0x00);
    }

    private void writeLongArrayTag(ByteBuf buf, String name, long[] values) {
        buf.writeByte(0x0C); // TAG_Long_Array
        byte[] nameBytes = name.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        buf.writeShort(nameBytes.length);
        buf.writeBytes(nameBytes);
        buf.writeInt(values.length);
        for (long v : values) {
            buf.writeLong(v);
        }
    }

    @Override
    public void read(ByteBuf buf) {
        chunkX = buf.readInt();
        chunkZ = buf.readInt();
        fullChunk = buf.readBoolean();
        buf.readBoolean(); // ignoreOldLightData
        primaryBitMask = McDataTypes.readVarInt(buf);

        // Skip NBT heightmaps
        skipNbtCompound(buf);

        // Read biomes (if fullChunk)
        if (fullChunk) {
            biomes = new int[1024];
            for (int i = 0; i < 1024; i++) {
                biomes[i] = buf.readInt();
            }
        }

        int dataSize = McDataTypes.readVarInt(buf);
        data = new byte[dataSize];
        buf.readBytes(data);

        // Skip block entities
        int blockEntityCount = McDataTypes.readVarInt(buf);
        for (int i = 0; i < blockEntityCount; i++) {
            skipNbtCompound(buf);
        }
    }

    private void skipNbtCompound(ByteBuf buf) {
        byte type = buf.readByte();
        if (type == 0) return;
        int nameLen = buf.readUnsignedShort();
        buf.skipBytes(nameLen);
        skipNbtPayload(buf, type);
    }

    private void skipNbtPayload(ByteBuf buf, byte type) {
        switch (type) {
            case 1: buf.skipBytes(1); break;
            case 2: buf.skipBytes(2); break;
            case 3: buf.skipBytes(4); break;
            case 4: buf.skipBytes(8); break;
            case 5: buf.skipBytes(4); break;
            case 6: buf.skipBytes(8); break;
            case 7: { int len = buf.readInt(); buf.skipBytes(len); break; }
            case 8: { int len = buf.readUnsignedShort(); buf.skipBytes(len); break; }
            case 9: {
                byte listType = buf.readByte();
                int count = buf.readInt();
                for (int i = 0; i < count; i++) skipNbtPayload(buf, listType);
                break;
            }
            case 10: {
                while (true) {
                    byte childType = buf.readByte();
                    if (childType == 0) break;
                    int nameLen = buf.readUnsignedShort();
                    buf.skipBytes(nameLen);
                    skipNbtPayload(buf, childType);
                }
                break;
            }
            case 11: { int len = buf.readInt(); buf.skipBytes(len * 4); break; }
            case 12: { int len = buf.readInt(); buf.skipBytes(len * 8); break; }
        }
    }

    public int getChunkX() { return chunkX; }
    public int getChunkZ() { return chunkZ; }
    public int getPrimaryBitMask() { return primaryBitMask; }
    public byte[] getData() { return data; }
}
