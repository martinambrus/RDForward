package com.github.martinambrus.rdforward.world.convert;

import com.github.martinambrus.rdforward.world.BlockRegistry;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Converts an original RubyDung level.dat to the RDForward server-world.dat format.
 *
 * Original RubyDung format:
 *   GZip'd raw byte[] blocks, no header. Dimensions hardcoded at 256x64x256.
 *   Block ID 1 = all solid blocks (renderer decides grass/cobblestone visually).
 *   Block ordering: (y * depth + z) * width + x (YZX).
 *
 * RDForward server format:
 *   GZip'd [magic][version][width][height][depth][byte[] blocks].
 *   See {@link ServerWorldHeader} for header details.
 *   Block ID 2 = grass (surface), 4 = cobblestone (subsurface), 0 = air.
 *   Same YZX block ordering.
 *
 * The converter:
 *   1. Reads the raw blocks from level.dat
 *   2. Maps block ID 1 to grass (surface) / cobblestone (below surface)
 *   3. Writes as server-world.dat with versioned header
 */
public class OriginalRubyDungToServerConverter implements FormatConverter {

    /** Default world dimensions matching the original RubyDung Level constructor. */
    public static final int DEFAULT_WIDTH = 256;
    public static final int DEFAULT_HEIGHT = 64;
    public static final int DEFAULT_DEPTH = 256;

    private final int width;
    private final int height;
    private final int depth;

    public OriginalRubyDungToServerConverter() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_DEPTH);
    }

    public OriginalRubyDungToServerConverter(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    @Override
    public WorldFormat sourceFormat() {
        return WorldFormat.RUBYDUNG;
    }

    @Override
    public WorldFormat targetFormat() {
        return WorldFormat.RUBYDUNG_SERVER;
    }

    @Override
    public void convert(File inputPath, File outputPath, long seed) throws IOException {
        System.out.println("Reading original RubyDung level from " + inputPath.getAbsolutePath() + "...");

        int expectedSize = width * height * depth;
        byte[] blocks = new byte[expectedSize];

        try (java.io.InputStream is = new GZIPInputStream(new FileInputStream(inputPath))) {
            int offset = 0;
            int read;
            while (offset < expectedSize && (read = is.read(blocks, offset, expectedSize - offset)) != -1) {
                offset += read;
            }
            if (offset != expectedSize) {
                throw new IOException("Expected " + expectedSize + " bytes but read " + offset
                        + ". Dimensions " + width + "x" + height + "x" + depth + " may be wrong.");
            }
        }

        System.out.println("Original world dimensions: " + width + "x" + height + "x" + depth
                + " (" + expectedSize + " blocks)");

        // Map block IDs: original uses 1 for all solid.
        // The original RubyDung Level.java generates terrain as:
        //   this.blocks[i] = (byte)(y <= d * 2 / 3 ? 1 : 0)
        // where d is the vertical dimension. So the natural surface is at
        // exactly height * 2 / 3. We use this fixed surface Y to decide
        // grass vs cobblestone, matching what the RD client expects.
        int surfaceY = height * 2 / 3;
        int mapped = 0;
        int sliceSize = depth * width;
        for (int y = 0; y < height; y++) {
            byte replacement = (y == surfaceY)
                    ? (byte) BlockRegistry.GRASS
                    : (byte) BlockRegistry.COBBLESTONE;
            int base = y * sliceSize;
            for (int j = 0; j < sliceSize; j++) {
                if (blocks[base + j] == 1) {
                    blocks[base + j] = replacement;
                    mapped++;
                }
            }
        }

        System.out.println("Mapped " + mapped + " blocks (ID 1 -> grass/cobblestone)");

        // Write in server-world.dat format (versioned header + dimensions + blocks)
        outputPath.getParentFile().mkdirs();
        try (DataOutputStream dos = new DataOutputStream(
                new GZIPOutputStream(new FileOutputStream(outputPath)))) {
            ServerWorldHeader.write(dos, width, height, depth);
            dos.write(blocks);
        }

        System.out.println("Conversion complete: " + outputPath.getAbsolutePath());
    }
}
