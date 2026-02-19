package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.20.2 Play state, S2C packet 0x25: Chunk Data and Update Light (combined).
 *
 * Same as MapChunkPacketV763 (1.20) except heightmaps NBT uses network NBT
 * format (no root name). In 1.20.2, all NBT over the network omits the
 * root compound name.
 *
 * Difference from V763: writeHeightmapsNbt omits writeShort(0) after 0x0A.
 */
public class MapChunkPacketV764 implements Packet {

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

    public MapChunkPacketV764() {}

    public MapChunkPacketV764(int chunkX, int chunkZ,
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
    public int getPacketId() { return 0x25; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);

        // Heightmaps NBT — network NBT format (no root name)
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

    /**
     * Network NBT: 0x0A (TAG_Compound type) + children + 0x00 (TAG_End).
     * No root name — this is the 1.20.2+ network NBT format.
     */
    private void writeHeightmapsNbt(ByteBuf buf) {
        buf.writeByte(0x0A);
        // NO writeShort(0) — network NBT omits root name

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
        // Not needed for server-side encoding
    }

    public int getChunkX() { return chunkX; }
    public int getChunkZ() { return chunkZ; }
    public byte[] getData() { return data; }
}
