package com.github.martinambrus.rdforward.world.alpha;

import com.github.martinambrus.rdforward.protocol.BlockStateMapper;
import com.github.martinambrus.rdforward.world.ChunkSerializationPool;

import java.io.ByteArrayOutputStream;

/**
 * Static utility that writes a {@link CanonicalSection} to a {@link ByteArrayOutputStream}
 * for a specific protocol version target. Replaces per-version block iteration loops —
 * only remaps the small palette (~10-15 entries) and copies pre-packed long arrays.
 *
 * Each target constant encodes the wire format differences between version groups:
 * packing style, state ID mapper, light embedding, blockCount, biome container, etc.
 */
public final class CanonicalSectionWriter {

    private CanonicalSectionWriter() {}

    // --- Version targets ---
    // Each target maps to a unique combination of section wire format parameters.

    /** 1.9-1.12: spanning, (blockId<<4)|meta palette, light in section, no blockCount. */
    public static final int TARGET_V109 = 1;

    /** 1.13: spanning, v393 state IDs, light in section, no blockCount. */
    public static final int TARGET_V393 = 2;

    /** 1.14-1.15: spanning, v393 state IDs, blockCount, no light in section. */
    public static final int TARGET_V477 = 3;

    /** 1.16-1.16.2: non-spanning, v735 state IDs, blockCount, no light. */
    public static final int TARGET_V735 = 4;

    /** 1.17: non-spanning, v755 state IDs, blockCount, no light. */
    public static final int TARGET_V755 = 5;

    /** 1.18: non-spanning, v755 state IDs, blockCount, biome container, no light. */
    public static final int TARGET_V757 = 6;

    /** 1.19-1.21.4: non-spanning, v759 state IDs, blockCount, biome container, no light. */
    public static final int TARGET_V759 = 7;

    /** 1.21.5-1.21.11: non-spanning, v759 state IDs, blockCount, biome container, no VarInt lengths. */
    public static final int TARGET_V770 = 8;

    /** 26.1: non-spanning, v775 state IDs, blockCount, biome container, no VarInt lengths. */
    public static final int TARGET_V775 = 9;

    /** Pre-serialized single-valued biome container (plains) for targets with data array length. */
    private static final byte[] BIOME_SINGLE_PLAINS_WITH_LENGTH = {0x00, 0x01, 0x00};
    /** Pre-serialized single-valued biome container (plains) for v770/v775 (no data array length). */
    private static final byte[] BIOME_SINGLE_PLAINS_NO_LENGTH = {0x00, 0x01};

    /**
     * Write a populated (non-empty) section to the output stream.
     * Remaps only the palette entries, then copies pre-packed long array directly.
     */
    public static void writePopulatedSection(ByteArrayOutputStream baos,
                                              CanonicalSection section,
                                              int target) {
        boolean hasBlockCount = (target >= TARGET_V477);
        boolean spanning = (target <= TARGET_V477);
        boolean hasLightInSection = (target <= TARGET_V393);
        boolean hasBiomeContainer = (target >= TARGET_V757);
        boolean hasDataArrayLength = (target != TARGET_V770 && target != TARGET_V775);
        boolean hasFluidCount = (target >= TARGET_V775);

        // 1. blockCount (short, big-endian) for 1.14+
        if (hasBlockCount) {
            writeShort(baos, section.getNonAirCount());
        }

        // 1b. fluidCount (short, big-endian) for 26.1+
        if (hasFluidCount) {
            writeShort(baos, 0);
        }

        // 2. bitsPerBlock
        baos.write(section.getBitsPerBlock());

        // 3. Palette: VarInt(paletteSize) + VarInt[] remapped entries
        int paletteSize = section.getPaletteSize();
        writeVarInt(baos, paletteSize);
        int[] legacyPalette = section.getLegacyPalette();
        if (target == TARGET_V109) {
            // Pre-1.13: state = (blockId << 4) | metadata
            int[] legacyMeta = section.getLegacyPaletteMeta();
            for (int i = 0; i < paletteSize; i++) {
                writeVarInt(baos, (legacyPalette[i] << 4) | (legacyMeta[i] & 0xF));
            }
        } else {
            // 1.13+: direct table lookup (no switch, no bounds check)
            int[] remapTable = getRemapTable(target);
            for (int i = 0; i < paletteSize; i++) {
                writeVarInt(baos, remapTable[legacyPalette[i]]);
            }
        }

        // 4. Pick pre-packed longs based on packing style
        long[] longs;
        int longsCount;
        if (spanning) {
            longs = section.getSpanningLongs();
            longsCount = section.getSpanningLongsCount();
        } else {
            longs = section.getNonSpanningLongs();
            longsCount = section.getNonSpanningLongsCount();
        }

        // 5. VarInt data array length (all versions except v770)
        if (hasDataArrayLength) {
            writeVarInt(baos, longsCount);
        }

        // 6. Packed longs (big-endian) — bulk write to reduce per-byte method calls
        writeLongs(baos, longs, longsCount);

        // 7. Light data embedded in section (1.9-1.13 only)
        if (hasLightInSection) {
            baos.write(section.getBlockLight(), 0, 2048);
            baos.write(section.getSkyLight(), 0, 2048);
        }

        // 8. Biome PalettedContainer (1.18+ only)
        if (hasBiomeContainer) {
            byte[] biome = hasDataArrayLength ? BIOME_SINGLE_PLAINS_WITH_LENGTH : BIOME_SINGLE_PLAINS_NO_LENGTH;
            baos.write(biome, 0, biome.length);
        }
    }

    /**
     * Write an empty air section for 1.17+ formats that require all sections present.
     */
    public static void writeEmptySection(ByteArrayOutputStream baos, int target) {
        boolean hasBiomeContainer = (target >= TARGET_V757);
        boolean hasDataArrayLength = (target != TARGET_V770 && target != TARGET_V775);
        boolean hasFluidCount = (target >= TARGET_V775);

        // blockCount = 0
        writeShort(baos, 0);
        // fluidCount = 0 (26.1+)
        if (hasFluidCount) {
            writeShort(baos, 0);
        }
        // bitsPerBlock = 0 (single-valued palette)
        baos.write(0);
        // palette entry: air = 0
        writeVarInt(baos, 0);
        // data array length: 0 (except v770 which omits it)
        if (hasDataArrayLength) {
            writeVarInt(baos, 0);
        }

        // Biome container (1.18+)
        if (hasBiomeContainer) {
            byte[] biome = hasDataArrayLength ? BIOME_SINGLE_PLAINS_WITH_LENGTH : BIOME_SINGLE_PLAINS_NO_LENGTH;
            baos.write(biome, 0, biome.length);
        }
    }

    /**
     * Get the pre-computed remap table for a version target (1.13+).
     * Returns a int[256] where index = legacy block ID, value = version-specific state ID.
     * Lazily loaded via JVM inner-class holders in BlockStateMapper.
     */
    private static int[] getRemapTable(int target) {
        switch (target) {
            case TARGET_V393:
            case TARGET_V477:
                return BlockStateMapper.getV393RemapTable();
            case TARGET_V735:
                return BlockStateMapper.getV735RemapTable();
            case TARGET_V755:
            case TARGET_V757:
                return BlockStateMapper.getV755RemapTable();
            case TARGET_V759:
            case TARGET_V770:
                return BlockStateMapper.getV759RemapTable();
            case TARGET_V775:
                return BlockStateMapper.getV775RemapTable();
            default:
                throw new IllegalArgumentException("Unknown CanonicalSectionWriter target: " + target);
        }
    }

    // --- Write helpers (same encoding as AlphaChunk statics) ---

    static void writeVarInt(ByteArrayOutputStream out, int value) {
        while (true) {
            if ((value & ~0x7F) == 0) {
                out.write(value);
                return;
            }
            out.write((value & 0x7F) | 0x80);
            value >>>= 7;
        }
    }

    static void writeShort(ByteArrayOutputStream out, int value) {
        out.write((value >> 8) & 0xFF);
        out.write(value & 0xFF);
    }

    /**
     * Bulk-write an array of longs as big-endian bytes.
     * Converts to a pooled byte[] buffer first, then does a single baos.write() call
     * instead of 8 * longsCount individual write() calls.
     */
    static void writeLongs(ByteArrayOutputStream out, long[] longs, int count) {
        if (count == 0) return;
        byte[] buf = ChunkSerializationPool.borrowLongWriteBuf();
        try {
            int pos = 0;
            for (int i = 0; i < count; i++) {
                long v = longs[i];
                buf[pos++] = (byte) (v >> 56);
                buf[pos++] = (byte) (v >> 48);
                buf[pos++] = (byte) (v >> 40);
                buf[pos++] = (byte) (v >> 32);
                buf[pos++] = (byte) (v >> 24);
                buf[pos++] = (byte) (v >> 16);
                buf[pos++] = (byte) (v >> 8);
                buf[pos++] = (byte) v;
            }
            out.write(buf, 0, pos);
        } finally {
            ChunkSerializationPool.returnLongWriteBuf(buf);
        }
    }
}
