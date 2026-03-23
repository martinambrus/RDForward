package com.github.martinambrus.rdforward.world.alpha;

import java.util.LinkedHashMap;

/**
 * Immutable, version-independent chunk data built once from an {@link AlphaChunk}.
 *
 * Contains canonical sections (palette + pre-packed indices) that can be used to
 * derive version-specific chunk packets cheaply. Instead of iterating all 32,768
 * blocks per version, each version only remaps ~10-15 palette entries and copies
 * the pre-packed long array.
 *
 * Thread safety: instances are immutable after construction. The volatile field in
 * AlphaChunk ensures safe publication. Multiple threads may build concurrently
 * (benign race — both produce equivalent immutable results).
 */
public final class CanonicalChunkData {

    private static final int SECTIONS = 8; // AlphaChunk.HEIGHT / 16
    private static final int BLOCKS_PER_SECTION = 4096; // 16^3

    /** Canonical sections for Y 0-127 (indices 0-7). */
    private final CanonicalSection[] sections;

    /** Heightmap: highest non-air Y+1 per XZ column (256 entries, defensive copy). */
    private final byte[] heightMap;

    /** Section presence bitmask: bit i set if sections[i] has non-air blocks. */
    private final int primaryBitMask;

    private CanonicalChunkData(CanonicalSection[] sections, byte[] heightMap, int primaryBitMask) {
        this.sections = sections;
        this.heightMap = heightMap;
        this.primaryBitMask = primaryBitMask;
    }

    /**
     * Build canonical data from an AlphaChunk. Iterates each section's blocks
     * exactly once to build the legacy palette, palette indices, and both
     * packed long array variants. Also packs light nibbles.
     */
    public static CanonicalChunkData build(AlphaChunk chunk) {
        CanonicalSection[] sections = new CanonicalSection[SECTIONS];
        int primaryBitMask = 0;

        for (int s = 0; s < SECTIONS; s++) {
            int baseY = s * 16;
            sections[s] = buildSection(chunk, baseY);
            if (sections[s].getNonAirCount() > 0) {
                primaryBitMask |= (1 << s);
            }
        }

        byte[] hmCopy = new byte[256];
        System.arraycopy(chunk.getHeightMap(), 0, hmCopy, 0, 256);

        return new CanonicalChunkData(sections, hmCopy, primaryBitMask);
    }

    private static CanonicalSection buildSection(AlphaChunk chunk, int baseY) {
        // Phase 1: Single pass over 4096 blocks to build palette + indices + nonAirCount
        int[] paletteIndices = new int[BLOCKS_PER_SECTION];
        LinkedHashMap<Integer, Integer> paletteMap = new LinkedHashMap<>();
        int nonAirCount = 0;

        for (int ly = 0; ly < 16; ly++) {
            for (int z = 0; z < AlphaChunk.DEPTH; z++) {
                for (int x = 0; x < AlphaChunk.WIDTH; x++) {
                    int blockId = chunk.getBlock(x, baseY + ly, z);
                    int meta = chunk.getBlockData(x, baseY + ly, z);

                    if (blockId != 0) nonAirCount++;

                    // Pack blockId + meta into palette key
                    int paletteKey = (blockId << 4) | (meta & 0xF);
                    Integer palIdx = paletteMap.get(paletteKey);
                    if (palIdx == null) {
                        palIdx = paletteMap.size();
                        paletteMap.put(paletteKey, palIdx);
                    }

                    int i = (ly * 16 + z) * 16 + x; // YZX order
                    paletteIndices[i] = palIdx;
                }
            }
        }

        int paletteSize = paletteMap.size();

        // Extract legacy palette arrays from the map
        int[] legacyPalette = new int[paletteSize];
        int[] legacyPaletteMeta = new int[paletteSize];
        int idx = 0;
        for (int key : paletteMap.keySet()) {
            legacyPalette[idx] = key >> 4;
            legacyPaletteMeta[idx] = key & 0xF;
            idx++;
        }

        // Phase 2: Compute bitsPerBlock
        int bitsPerBlock = 4;
        while ((1 << bitsPerBlock) < paletteSize) {
            bitsPerBlock++;
        }
        // Cap at 8 for indirect palette compatibility (>256 unique blocks impossible in our world)
        if (bitsPerBlock > 8) {
            bitsPerBlock = 8;
        }

        // Phase 3: Pack indices into long arrays
        long mask = (1L << bitsPerBlock) - 1;

        // Spanning packing (1.9-1.15): entries CAN cross long boundaries
        int spanningTotal = BLOCKS_PER_SECTION * bitsPerBlock;
        int spanningLongsCount = (spanningTotal + 63) / 64;
        long[] spanningLongs = new long[spanningLongsCount];

        for (int i = 0; i < BLOCKS_PER_SECTION; i++) {
            long val = paletteIndices[i] & mask;
            int bitIndex = i * bitsPerBlock;
            int longIndex = bitIndex / 64;
            int bitOffset = bitIndex % 64;
            spanningLongs[longIndex] |= val << bitOffset;
            if (bitOffset + bitsPerBlock > 64) {
                int bitsInFirst = 64 - bitOffset;
                spanningLongs[longIndex + 1] |= val >> bitsInFirst;
            }
        }

        // Non-spanning packing (1.16+): entries do NOT cross long boundaries
        long[] nonSpanningLongs;
        int nonSpanningLongsCount;

        if (64 % bitsPerBlock == 0) {
            // bitsPerBlock evenly divides 64 — spanning and non-spanning are identical
            nonSpanningLongs = spanningLongs;
            nonSpanningLongsCount = spanningLongsCount;
        } else {
            int entriesPerLong = 64 / bitsPerBlock;
            nonSpanningLongsCount = (BLOCKS_PER_SECTION + entriesPerLong - 1) / entriesPerLong;
            nonSpanningLongs = new long[nonSpanningLongsCount];

            for (int i = 0; i < BLOCKS_PER_SECTION; i++) {
                long val = paletteIndices[i] & mask;
                int longIndex = i / entriesPerLong;
                int bitOffset = (i % entriesPerLong) * bitsPerBlock;
                nonSpanningLongs[longIndex] |= val << bitOffset;
            }
        }

        // Phase 4: Pack light nibbles (shared across all versions)
        byte[][] light = chunk.packLightNibbles(baseY);

        return new CanonicalSection(
                legacyPalette, legacyPaletteMeta, paletteSize,
                nonAirCount, bitsPerBlock, paletteIndices,
                spanningLongs, spanningLongsCount,
                nonSpanningLongs, nonSpanningLongsCount,
                light[0], light[1]);
    }

    public CanonicalSection[] getSections() { return sections; }
    public CanonicalSection getSection(int index) { return sections[index]; }
    public byte[] getHeightMap() { return heightMap; }
    public int getPrimaryBitMask() { return primaryBitMask; }
}
