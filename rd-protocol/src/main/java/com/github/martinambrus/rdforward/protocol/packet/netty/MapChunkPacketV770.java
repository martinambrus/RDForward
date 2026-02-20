package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.21.5 Play state, S2C packet 0x27: Chunk Data and Update Light (combined).
 *
 * Changes from V764:
 * - Heightmaps changed from NBT compound to binary format:
 *   VarInt count, then per heightmap: VarInt typeId + VarInt longCount + long[].
 * - Section palette data arrays no longer have VarInt length prefix (handled
 *   by serializeForV770Protocol in AlphaChunk).
 *
 * Heightmap type enum ordinals (stable across versions):
 *   WORLD_SURFACE_WG=0, WORLD_SURFACE=1, OCEAN_FLOOR_WG=2,
 *   OCEAN_FLOOR=3, MOTION_BLOCKING=4, MOTION_BLOCKING_NO_LEAVES=5
 */
public class MapChunkPacketV770 implements Packet {

    private static final int HEIGHTMAP_WORLD_SURFACE = 1;
    private static final int HEIGHTMAP_MOTION_BLOCKING = 4;

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

    public MapChunkPacketV770() {}

    public MapChunkPacketV770(int chunkX, int chunkZ,
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
    public int getPacketId() { return 0x27; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);

        // Heightmaps — binary format (replaces NBT in 1.21.5)
        writeHeightmapsBinary(buf);

        // Section data (all sections, biomes inside each section)
        McDataTypes.writeVarInt(buf, data.length);
        buf.writeBytes(data);

        // Block entities (none)
        McDataTypes.writeVarInt(buf, 0);

        // Light data (same as v764)

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
     * Binary heightmap format (1.21.5+):
     *   VarInt heightmapCount
     *   Per heightmap:
     *     VarInt typeId (enum ordinal)
     *     VarInt longCount
     *     long[longCount]
     */
    private void writeHeightmapsBinary(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, 2); // 2 heightmaps

        // MOTION_BLOCKING
        McDataTypes.writeVarInt(buf, HEIGHTMAP_MOTION_BLOCKING);
        McDataTypes.writeVarInt(buf, motionBlockingHeightmap.length);
        for (long v : motionBlockingHeightmap) {
            buf.writeLong(v);
        }

        // WORLD_SURFACE
        McDataTypes.writeVarInt(buf, HEIGHTMAP_WORLD_SURFACE);
        McDataTypes.writeVarInt(buf, worldSurfaceHeightmap.length);
        for (long v : worldSurfaceHeightmap) {
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
