package com.github.martinambrus.rdforward.world.convert;

import com.github.martinambrus.rdforward.world.BlockRegistry;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Converts an RDForward server-world.dat to the original RubyDung level.dat format.
 *
 * RDForward server format:
 *   GZip'd [int width][int height][int depth][byte[] blocks].
 *   Block ID 2 = grass, 4 = cobblestone, 0 = air.
 *
 * Original RubyDung format:
 *   GZip'd raw byte[] blocks, no header.
 *   Block ID 1 = all solid blocks, 0 = air.
 *   Dimensions are hardcoded in the game (256x64x256).
 *
 * The converter:
 *   1. Reads the server format (strips the 3-int header)
 *   2. Maps all non-air block IDs to 1 (the only solid block in original RubyDung)
 *   3. Writes raw blocks as GZip'd level.dat (no header)
 */
public class ServerToOriginalRubyDungConverter implements FormatConverter {

    @Override
    public WorldFormat sourceFormat() {
        return WorldFormat.RUBYDUNG_SERVER;
    }

    @Override
    public WorldFormat targetFormat() {
        return WorldFormat.RUBYDUNG;
    }

    @Override
    public void convert(File inputPath, File outputPath, long seed) throws IOException {
        System.out.println("Reading server world from " + inputPath.getAbsolutePath() + "...");

        int width;
        int height;
        int depth;
        byte[] blocks;

        try (DataInputStream dis = new DataInputStream(
                new GZIPInputStream(new FileInputStream(inputPath)))) {
            width = dis.readInt();
            height = dis.readInt();
            depth = dis.readInt();
            blocks = new byte[width * height * depth];
            dis.readFully(blocks);
        }

        System.out.println("World dimensions: " + width + "x" + height + "x" + depth
                + " (" + blocks.length + " blocks)");

        // Map all non-air blocks to block ID 1 (original RubyDung solid)
        int mapped = 0;
        for (int i = 0; i < blocks.length; i++) {
            if (blocks[i] != 0) {
                blocks[i] = 1;
                mapped++;
            }
        }

        System.out.println("Mapped " + mapped + " blocks to RubyDung solid (ID 1)");

        // Write raw blocks without header (original RubyDung format)
        outputPath.getParentFile().mkdirs();
        try (GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(outputPath))) {
            gos.write(blocks);
        }

        System.out.println("Conversion complete: " + outputPath.getAbsolutePath());
    }
}
