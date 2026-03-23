package com.github.martinambrus.rdforward.world.alpha;

/**
 * Immutable, version-independent representation of a single 16x16x16 chunk section.
 *
 * Built once per section by {@link CanonicalChunkData#build(AlphaChunk)}, then reused
 * to derive version-specific chunk packets cheaply via palette remapping + bulk long copy.
 *
 * The key insight: with indirect palette, the packed index data (long arrays) is identical
 * across all protocol versions — only the palette entries (typically 5-15) need remapping.
 * This eliminates redundant 4096-block iterations per version.
 */
public final class CanonicalSection {

    /** Unique legacy block IDs in this section, in palette order. */
    private final int[] legacyPalette;

    /** 4-bit metadata for each palette entry. */
    private final int[] legacyPaletteMeta;

    /** Number of entries in the palette. */
    private final int paletteSize;

    /** Number of non-air blocks in this section. */
    private final int nonAirCount;

    /** Bits per block for the indirect palette: max(4, ceil(log2(paletteSize))). */
    private final int bitsPerBlock;

    /**
     * Per-block palette indices in YZX order (4096 entries).
     * Index = (ly * 16 + z) * 16 + x, each value in [0, paletteSize).
     * Kept for Bedrock conversion which needs XZY reordering.
     */
    private final int[] paletteIndices;

    /**
     * Pre-packed palette indices using spanning bit packing (1.9-1.15).
     * Entries CAN cross long boundaries.
     * When bitsPerBlock evenly divides 64, this is the same as nonSpanningLongs.
     */
    private final long[] spanningLongs;

    /** Number of valid longs in spanningLongs. */
    private final int spanningLongsCount;

    /**
     * Pre-packed palette indices using non-spanning bit packing (1.16+).
     * Entries do NOT cross long boundaries.
     * When bitsPerBlock evenly divides 64, this shares the same array as spanningLongs.
     */
    private final long[] nonSpanningLongs;

    /** Number of valid longs in nonSpanningLongs. */
    private final int nonSpanningLongsCount;

    /** Block light nibble array (2048 bytes). */
    private final byte[] blockLight;

    /** Sky light nibble array (2048 bytes). */
    private final byte[] skyLight;

    CanonicalSection(int[] legacyPalette, int[] legacyPaletteMeta, int paletteSize,
                     int nonAirCount, int bitsPerBlock, int[] paletteIndices,
                     long[] spanningLongs, int spanningLongsCount,
                     long[] nonSpanningLongs, int nonSpanningLongsCount,
                     byte[] blockLight, byte[] skyLight) {
        this.legacyPalette = legacyPalette;
        this.legacyPaletteMeta = legacyPaletteMeta;
        this.paletteSize = paletteSize;
        this.nonAirCount = nonAirCount;
        this.bitsPerBlock = bitsPerBlock;
        this.paletteIndices = paletteIndices;
        this.spanningLongs = spanningLongs;
        this.spanningLongsCount = spanningLongsCount;
        this.nonSpanningLongs = nonSpanningLongs;
        this.nonSpanningLongsCount = nonSpanningLongsCount;
        this.blockLight = blockLight;
        this.skyLight = skyLight;
    }

    /** Whether this section has no non-air blocks. */
    public boolean isEmpty() { return nonAirCount == 0; }

    public int[] getLegacyPalette() { return legacyPalette; }
    public int[] getLegacyPaletteMeta() { return legacyPaletteMeta; }
    public int getPaletteSize() { return paletteSize; }
    public int getNonAirCount() { return nonAirCount; }
    public int getBitsPerBlock() { return bitsPerBlock; }
    public int[] getPaletteIndices() { return paletteIndices; }
    public long[] getSpanningLongs() { return spanningLongs; }
    public int getSpanningLongsCount() { return spanningLongsCount; }
    public long[] getNonSpanningLongs() { return nonSpanningLongs; }
    public int getNonSpanningLongsCount() { return nonSpanningLongsCount; }
    public byte[] getBlockLight() { return blockLight; }
    public byte[] getSkyLight() { return skyLight; }
}
