package com.github.martinambrus.rdforward.world.alpha;

import java.util.Arrays;

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
        // Single fused pass: build palette + paletteIndices + pack both long arrays.
        // Loop order x/z/ly matches AlphaChunk XZY storage (blockIndex = y + z*HEIGHT + x*HEIGHT*DEPTH)
        // for sequential reads. Writes to paletteIndices (16KB) and packed longs (2KB) are scattered
        // but L1-resident.
        int[] paletteIndices = new int[BLOCKS_PER_SECTION];
        // Flat array palette: avoids LinkedHashMap + Integer boxing overhead.
        // Alpha sections typically have <10 unique block types, so linear scan
        // on a small array is faster than hashing.
        int[] paletteKeys = new int[16];
        int paletteSize = 0;
        int nonAirCount = 0;

        // Start with bitsPerBlock=4 (supports 16 palette entries). Alpha sections typically
        // have <10 unique block types, so the repack path below almost never triggers.
        int bitsPerBlock = 4;
        int capacity = 16; // 1 << bitsPerBlock
        long mask = 0xFL;

        // Spanning packing (1.9-1.15): entries CAN cross long boundaries
        int spanningLongsCount = (BLOCKS_PER_SECTION * bitsPerBlock + 63) / 64;
        long[] spanningLongs = new long[spanningLongsCount];

        // Non-spanning (1.16+): for bpb=4, 64%4==0 so identical to spanning
        boolean packingIdentical = true;
        long[] nonSpanningLongs = spanningLongs;
        int nonSpanningLongsCount = spanningLongsCount;
        int entriesPerLong = 64 / bitsPerBlock; // 16 for bpb=4

        for (int x = 0; x < AlphaChunk.WIDTH; x++) {
            for (int z = 0; z < AlphaChunk.DEPTH; z++) {
                for (int ly = 0; ly < 16; ly++) {
                    int blockId = chunk.getBlock(x, baseY + ly, z);
                    int meta = chunk.getBlockData(x, baseY + ly, z);

                    if (blockId != 0) nonAirCount++;

                    int paletteKey = (blockId << 4) | (meta & 0xF);

                    // Linear scan for palette index (typically <10 entries)
                    int palIdx = -1;
                    for (int p = 0; p < paletteSize; p++) {
                        if (paletteKeys[p] == paletteKey) { palIdx = p; break; }
                    }
                    if (palIdx == -1) {
                        palIdx = paletteSize;
                        if (palIdx >= paletteKeys.length) {
                            paletteKeys = Arrays.copyOf(paletteKeys, paletteKeys.length * 2);
                        }
                        paletteKeys[palIdx] = paletteKey;
                        paletteSize++;

                        // Palette exceeded current bitsPerBlock capacity — grow and repack
                        if (palIdx >= capacity) {
                            bitsPerBlock++;
                            while ((1 << bitsPerBlock) < palIdx + 1) bitsPerBlock++;
                            if (bitsPerBlock > 8) bitsPerBlock = 8;
                            capacity = 1 << bitsPerBlock;
                            mask = (1L << bitsPerBlock) - 1;

                            spanningLongsCount = (BLOCKS_PER_SECTION * bitsPerBlock + 63) / 64;
                            spanningLongs = new long[spanningLongsCount];

                            packingIdentical = (64 % bitsPerBlock == 0);
                            entriesPerLong = 64 / bitsPerBlock;
                            if (packingIdentical) {
                                nonSpanningLongs = spanningLongs;
                                nonSpanningLongsCount = spanningLongsCount;
                            } else {
                                nonSpanningLongsCount = (BLOCKS_PER_SECTION + entriesPerLong - 1) / entriesPerLong;
                                nonSpanningLongs = new long[nonSpanningLongsCount];
                            }

                            // Repack all 4096 entries from paletteIndices.
                            // Unfilled positions are 0 (palette entry 0), which packs as zero
                            // bits — correct because later |= will set the real value.
                            for (int r = 0; r < BLOCKS_PER_SECTION; r++) {
                                packIndex(r, paletteIndices[r], mask, bitsPerBlock,
                                        spanningLongs, nonSpanningLongs, entriesPerLong,
                                        packingIdentical);
                            }
                        }
                    }

                    int i = (ly * 16 + z) * 16 + x; // YZX output order for protocol compatibility
                    paletteIndices[i] = palIdx;

                    // Fused packing: write directly to both long arrays
                    packIndex(i, palIdx, mask, bitsPerBlock,
                            spanningLongs, nonSpanningLongs, entriesPerLong,
                            packingIdentical);
                }
            }
        }

        // Extract legacy palette arrays from the flat key array
        int[] legacyPalette = new int[paletteSize];
        int[] legacyPaletteMeta = new int[paletteSize];
        for (int idx = 0; idx < paletteSize; idx++) {
            legacyPalette[idx] = paletteKeys[idx] >> 4;
            legacyPaletteMeta[idx] = paletteKeys[idx] & 0xF;
        }

        // Pack light nibbles (shared across all versions)
        byte[][] light = chunk.packLightNibbles(baseY);

        return new CanonicalSection(
                legacyPalette, legacyPaletteMeta, paletteSize,
                nonAirCount, bitsPerBlock, paletteIndices,
                spanningLongs, spanningLongsCount,
                nonSpanningLongs, nonSpanningLongsCount,
                light[0], light[1]);
    }

    /**
     * Pack a single palette index into both spanning and non-spanning long arrays.
     */
    private static void packIndex(int i, int palIdx, long mask, int bitsPerBlock,
                                   long[] spanningLongs, long[] nonSpanningLongs,
                                   int entriesPerLong, boolean packingIdentical) {
        long val = palIdx & mask;

        // Spanning packing
        int bitIndex = i * bitsPerBlock;
        int longIndex = bitIndex / 64;
        int bitOffset = bitIndex % 64;
        spanningLongs[longIndex] |= val << bitOffset;
        if (bitOffset + bitsPerBlock > 64) {
            spanningLongs[longIndex + 1] |= val >> (64 - bitOffset);
        }

        // Non-spanning packing (skip when identical to spanning)
        if (!packingIdentical) {
            int nsLongIndex = i / entriesPerLong;
            int nsBitOffset = (i % entriesPerLong) * bitsPerBlock;
            nonSpanningLongs[nsLongIndex] |= val << nsBitOffset;
        }
    }

    public CanonicalSection[] getSections() { return sections; }
    public CanonicalSection getSection(int index) { return sections[index]; }
    public byte[] getHeightMap() { return heightMap; }
    public int getPrimaryBitMask() { return primaryBitMask; }
}
