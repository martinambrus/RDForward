package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.20 Play state, S2C packet 0x24: Chunk Data and Update Light (combined).
 *
 * Same as MapChunkPacketV757 (1.18) except the trustEdges boolean has been
 * removed from the light data portion. Used for 1.20+ clients.
 *
 * Wire format:
 *   [int]       chunkX
 *   [int]       chunkZ
 *   [NBT]       heightmaps (MOTION_BLOCKING + WORLD_SURFACE)
 *   [VarInt]    dataSize
 *   [byte[]]    data (all 16 sections, each = short blockCount + block PalettedContainer + biome PalettedContainer)
 *   [VarInt]    blockEntityCount (0)
 *   [BitSet]    skyLightMask (VarInt longCount + long[])
 *   [BitSet]    blockLightMask
 *   [BitSet]    emptySkyLightMask
 *   [BitSet]    emptyBlockLightMask
 *   [VarInt]    skyLightArrayCount + VarInt-prefixed byte[2048] arrays
 *   [VarInt]    blockLightArrayCount + VarInt-prefixed byte[2048] arrays
 */
public class MapChunkPacketV763 implements Packet {

    private int chunkX;
    private int chunkZ;
    private long[] motionBlockingHeightmap;
    private long[] worldSurfaceHeightmap;
    private byte[] data;
    private int skyLightMask;
    private int blockLightMask;
    private int emptySkyLightMask;
    private int emptyBlockLightMask;
    private byte[][] skyLightArrays;
    private byte[][] blockLightArrays;

    public MapChunkPacketV763() {}

    public MapChunkPacketV763(int chunkX, int chunkZ,
                               long[] motionBlockingHeightmap,
                               long[] worldSurfaceHeightmap,
                               byte[] data,
                               int skyLightMask, int blockLightMask,
                               int emptySkyLightMask, int emptyBlockLightMask,
                               byte[][] skyLightArrays, byte[][] blockLightArrays) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.motionBlockingHeightmap = motionBlockingHeightmap;
        this.worldSurfaceHeightmap = worldSurfaceHeightmap;
        this.data = data;
        this.skyLightMask = skyLightMask;
        this.blockLightMask = blockLightMask;
        this.emptySkyLightMask = emptySkyLightMask;
        this.emptyBlockLightMask = emptyBlockLightMask;
        this.skyLightArrays = skyLightArrays;
        this.blockLightArrays = blockLightArrays;
    }

    @Override
    public int getPacketId() { return 0x24; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);

        // Heightmaps NBT
        writeHeightmapsNbt(buf);

        // Section data (all 16 sections, biomes inside each section)
        McDataTypes.writeVarInt(buf, data.length);
        buf.writeBytes(data);

        // Block entities (none)
        McDataTypes.writeVarInt(buf, 0);

        // Light data (no trustEdges in 1.20+)

        writeBitSet(buf, skyLightMask);
        writeBitSet(buf, blockLightMask);
        writeBitSet(buf, emptySkyLightMask);
        writeBitSet(buf, emptyBlockLightMask);

        // Sky light arrays
        int skyCount = skyLightArrays != null ? skyLightArrays.length : 0;
        McDataTypes.writeVarInt(buf, skyCount);
        if (skyLightArrays != null) {
            for (byte[] arr : skyLightArrays) {
                McDataTypes.writeVarInt(buf, arr.length);
                buf.writeBytes(arr);
            }
        }

        // Block light arrays
        int blockCount = blockLightArrays != null ? blockLightArrays.length : 0;
        McDataTypes.writeVarInt(buf, blockCount);
        if (blockLightArrays != null) {
            for (byte[] arr : blockLightArrays) {
                McDataTypes.writeVarInt(buf, arr.length);
                buf.writeBytes(arr);
            }
        }
    }

    private void writeHeightmapsNbt(ByteBuf buf) {
        buf.writeByte(0x0A);
        buf.writeShort(0); // empty name

        writeLongArrayTag(buf, "MOTION_BLOCKING", motionBlockingHeightmap);
        writeLongArrayTag(buf, "WORLD_SURFACE", worldSurfaceHeightmap);

        buf.writeByte(0x00); // TAG_End
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

    private static void writeBitSet(ByteBuf buf, int mask) {
        McDataTypes.writeVarInt(buf, 1); // 1 long for 18-bit mask
        buf.writeLong((long) mask);
    }

    @Override
    public void read(ByteBuf buf) {
        chunkX = buf.readInt();
        chunkZ = buf.readInt();

        // Skip NBT heightmaps
        skipNbtCompound(buf);

        // Read section data
        int dataSize = McDataTypes.readVarInt(buf);
        data = new byte[dataSize];
        buf.readBytes(data);

        // Skip block entities
        int blockEntityCount = McDataTypes.readVarInt(buf);
        for (int i = 0; i < blockEntityCount; i++) {
            skipNbtCompound(buf);
        }

        // Read light data (no trustEdges in 1.20+)

        skyLightMask = readBitSetAsInt(buf);
        blockLightMask = readBitSetAsInt(buf);
        emptySkyLightMask = readBitSetAsInt(buf);
        emptyBlockLightMask = readBitSetAsInt(buf);

        int skyCount = McDataTypes.readVarInt(buf);
        skyLightArrays = new byte[skyCount][];
        for (int i = 0; i < skyCount; i++) {
            int len = McDataTypes.readVarInt(buf);
            skyLightArrays[i] = new byte[len];
            buf.readBytes(skyLightArrays[i]);
        }

        int blockCount = McDataTypes.readVarInt(buf);
        blockLightArrays = new byte[blockCount][];
        for (int i = 0; i < blockCount; i++) {
            int len = McDataTypes.readVarInt(buf);
            blockLightArrays[i] = new byte[len];
            buf.readBytes(blockLightArrays[i]);
        }
    }

    private static int readBitSetAsInt(ByteBuf buf) {
        int longCount = McDataTypes.readVarInt(buf);
        if (longCount == 0) return 0;
        int result = (int) buf.readLong();
        for (int i = 1; i < longCount; i++) {
            buf.readLong();
        }
        return result;
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
    public byte[] getData() { return data; }
}
