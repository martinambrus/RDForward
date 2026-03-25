package com.github.martinambrus.rdforward.world.alpha;

import com.github.martinambrus.rdforward.protocol.BlockStateMapper;

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
        writeVarInt(baos, section.getPaletteSize());
        int[] legacyPalette = section.getLegacyPalette();
        int[] legacyMeta = section.getLegacyPaletteMeta();
        for (int i = 0; i < section.getPaletteSize(); i++) {
            int stateId = remapToVersionState(legacyPalette[i], legacyMeta[i], target);
            writeVarInt(baos, stateId);
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

        // 6. Packed longs (big-endian)
        for (int i = 0; i < longsCount; i++) {
            writeLong(baos, longs[i]);
        }

        // 7. Light data embedded in section (1.9-1.13 only)
        if (hasLightInSection) {
            baos.write(section.getBlockLight(), 0, 2048);
            baos.write(section.getSkyLight(), 0, 2048);
        }

        // 8. Biome PalettedContainer (1.18+ only)
        if (hasBiomeContainer) {
            baos.write(0);              // bitsPerEntry = 0 (single-valued)
            writeVarInt(baos, 1);       // biome value: plains
            if (hasDataArrayLength) {
                writeVarInt(baos, 0);   // data array length: 0
            }
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
            baos.write(0);              // bitsPerEntry = 0
            writeVarInt(baos, 1);       // biome value: plains
            if (hasDataArrayLength) {
                writeVarInt(baos, 0);   // data array length: 0
            }
        }
    }

    /**
     * Remap a legacy block ID + metadata to the version-specific block state ID.
     */
    static int remapToVersionState(int legacyId, int meta, int target) {
        switch (target) {
            case TARGET_V109:
                return (legacyId << 4) | (meta & 0xF);
            case TARGET_V393:
            case TARGET_V477:
                return BlockStateMapper.toV393BlockState(legacyId);
            case TARGET_V735:
                return BlockStateMapper.toV735BlockState(legacyId);
            case TARGET_V755:
            case TARGET_V757:
                return BlockStateMapper.toV755BlockState(legacyId);
            case TARGET_V759:
            case TARGET_V770:
                return BlockStateMapper.toV759BlockState(legacyId);
            case TARGET_V775:
                return BlockStateMapper.toV775BlockState(legacyId);
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

    static void writeLong(ByteArrayOutputStream out, long value) {
        out.write((int) (value >> 56) & 0xFF);
        out.write((int) (value >> 48) & 0xFF);
        out.write((int) (value >> 40) & 0xFF);
        out.write((int) (value >> 32) & 0xFF);
        out.write((int) (value >> 24) & 0xFF);
        out.write((int) (value >> 16) & 0xFF);
        out.write((int) (value >> 8) & 0xFF);
        out.write((int) value & 0xFF);
    }
}
