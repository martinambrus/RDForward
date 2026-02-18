package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.17 Play state, S2C packet 0x22: Chunk Data.
 *
 * Same as V751 but:
 * - fullChunk boolean REMOVED (always full in 1.17)
 * - primaryBitMask changed from VarInt to BitSet (VarInt longCount + long[])
 *
 * Wire format:
 *   [int]       chunkX
 *   [int]       chunkZ
 *   [VarInt]    bitSetLongCount (1 for 16 sections)
 *   [long[]]    primaryBitMask as BitSet
 *   [NBT]       heightmaps (TAG_Compound with MOTION_BLOCKING + WORLD_SURFACE)
 *   [VarInt]    biomesLength
 *   [VarInt[]]  biomes
 *   [VarInt]    dataSize
 *   [byte[]]    data (section data only, no biomes, no light)
 *   [VarInt]    blockEntityCount (0)
 */
public class MapChunkPacketV755 implements Packet {

    private int chunkX;
    private int chunkZ;
    private int primaryBitMask;
    private long[] motionBlockingHeightmap;
    private long[] worldSurfaceHeightmap;
    private int[] biomes;
    private byte[] data;

    public MapChunkPacketV755() {}

    public MapChunkPacketV755(int chunkX, int chunkZ,
                               int primaryBitMask,
                               long[] motionBlockingHeightmap,
                               long[] worldSurfaceHeightmap,
                               int[] biomes,
                               byte[] data) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.primaryBitMask = primaryBitMask;
        this.motionBlockingHeightmap = motionBlockingHeightmap;
        this.worldSurfaceHeightmap = worldSurfaceHeightmap;
        this.biomes = biomes;
        this.data = data;
    }

    @Override
    public int getPacketId() { return 0x22; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);
        // NO fullChunk boolean (removed in 1.17)

        // primaryBitMask as BitSet (1 long for 16 sections)
        McDataTypes.writeVarInt(buf, 1); // longCount
        buf.writeLong((long) primaryBitMask);

        // Write heightmaps as NBT compound
        writeHeightmapsNbt(buf);

        // Biomes: VarInt-length-prefixed VarInt array (same as v751)
        if (biomes != null) {
            McDataTypes.writeVarInt(buf, biomes.length);
            for (int i = 0; i < biomes.length; i++) {
                McDataTypes.writeVarInt(buf, biomes[i]);
            }
        } else {
            McDataTypes.writeVarInt(buf, 0);
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
        // NO fullChunk (removed in 1.17)

        // Read BitSet primaryBitMask
        int longCount = McDataTypes.readVarInt(buf);
        if (longCount > 0) {
            primaryBitMask = (int) buf.readLong();
            for (int i = 1; i < longCount; i++) {
                buf.readLong(); // skip extra longs
            }
        }

        // Skip NBT heightmaps
        skipNbtCompound(buf);

        // Read biomes (VarInt-length-prefixed VarInt array)
        int biomesLength = McDataTypes.readVarInt(buf);
        biomes = new int[biomesLength];
        for (int i = 0; i < biomesLength; i++) {
            biomes[i] = McDataTypes.readVarInt(buf);
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
