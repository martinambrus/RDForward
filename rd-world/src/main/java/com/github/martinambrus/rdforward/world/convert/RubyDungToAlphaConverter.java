package com.github.martinambrus.rdforward.world.convert;

import com.github.martinambrus.rdforward.world.alpha.AlphaChunk;
import com.github.martinambrus.rdforward.world.alpha.AlphaLevelFormat;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/**
 * Converts a RubyDung server-world.dat to the Minecraft Alpha level format.
 *
 * RubyDung worlds are stored as a flat GZip'd binary file:
 *   [int width] [int height] [int depth] [byte[] blocks]
 * with block index = (y * depth + z) * width + x (YZX ordering),
 * where height is the vertical axis (typically 64).
 *
 * Alpha worlds use individual 16x128x16 chunk files stored in a
 * base-36 directory tree, plus a level.dat with world metadata.
 *
 * The converter:
 *   1. Reads the RubyDung binary format
 *   2. Splits the flat array into 16x128x16 AlphaChunks
 *   3. Adds default NBT fields that Alpha expects (entities, tile entities,
 *      terrain populated flag, lighting)
 *   4. Writes each chunk via {@link AlphaLevelFormat}
 *   5. Creates level.dat with sensible defaults (spawn at world center)
 *
 * Block IDs pass through unchanged — RubyDung uses 0 (air), 2 (grass),
 * and 4 (cobblestone), which are valid Alpha block IDs.
 */
public class RubyDungToAlphaConverter {

    /**
     * Convert a RubyDung server-world.dat to Alpha chunk format.
     *
     * @param inputFile  the server-world.dat file
     * @param outputDir  the Alpha world directory to create
     * @param seed       world seed to store in level.dat
     * @throws IOException if reading or writing fails
     */
    public void convert(File inputFile, File outputDir, long seed) throws IOException {
        System.out.println("Reading RubyDung world from " + inputFile.getAbsolutePath() + "...");

        // Read the server-world.dat header and block data
        int width;
        int height;
        int depth;
        byte[] blocks;

        try (DataInputStream dis = new DataInputStream(new GZIPInputStream(new FileInputStream(inputFile)))) {
            width = dis.readInt();
            height = dis.readInt();  // vertical axis
            depth = dis.readInt();
            blocks = new byte[width * height * depth];
            dis.readFully(blocks);
        }

        System.out.println("World dimensions: " + width + "x" + height + "x" + depth
            + " (" + blocks.length + " blocks)");

        // Create output directory
        outputDir.mkdirs();

        // Calculate chunk grid dimensions
        int chunksX = (width + 15) / 16;
        int chunksZ = (depth + 15) / 16;
        int totalChunks = chunksX * chunksZ;
        int savedChunks = 0;

        System.out.println("Converting to " + chunksX + "x" + chunksZ + " chunks (" + totalChunks + " total)...");

        // Convert each chunk
        for (int cx = 0; cx < chunksX; cx++) {
            for (int cz = 0; cz < chunksZ; cz++) {
                AlphaChunk chunk = convertChunk(blocks, width, height, depth, cx, cz);
                AlphaLevelFormat.saveChunk(outputDir, chunk);
                savedChunks++;

                if (savedChunks % 64 == 0 || savedChunks == totalChunks) {
                    System.out.println("  " + savedChunks + "/" + totalChunks + " chunks saved");
                }
            }
        }

        // Write level.dat with spawn at world center, on the surface
        int spawnX = width / 2;
        int spawnZ = depth / 2;
        int spawnY = findSurfaceY(blocks, width, height, depth, spawnX, spawnZ) + 1;

        AlphaLevelFormat.saveLevelDat(outputDir, seed, spawnX, spawnY, spawnZ,
            0L, System.currentTimeMillis());

        // Write session.lock
        AlphaLevelFormat.writeSessionLock(outputDir);

        System.out.println("Conversion complete: " + savedChunks + " chunks written to "
            + outputDir.getAbsolutePath());
        System.out.println("Spawn point: (" + spawnX + ", " + spawnY + ", " + spawnZ + ")");
    }

    /**
     * Convert a single chunk's worth of blocks from the flat RubyDung array
     * to an AlphaChunk.
     */
    private AlphaChunk convertChunk(byte[] blocks, int width, int height, int depth,
                                     int chunkX, int chunkZ) {
        AlphaChunk chunk = new AlphaChunk(chunkX, chunkZ);

        for (int localX = 0; localX < 16; localX++) {
            int worldX = chunkX * 16 + localX;
            if (worldX >= width) continue;

            for (int localZ = 0; localZ < 16; localZ++) {
                int worldZ = chunkZ * 16 + localZ;
                if (worldZ >= depth) continue;

                for (int y = 0; y < height && y < AlphaChunk.HEIGHT; y++) {
                    // RubyDung index: (y * depth + z) * width + x
                    int rdIndex = (y * depth + worldZ) * width + worldX;
                    int blockId = blocks[rdIndex] & 0xFF;

                    if (blockId != 0) {
                        chunk.setBlock(localX, y, localZ, blockId);
                    }
                }
            }
        }

        chunk.setTerrainPopulated(true);
        return chunk;
    }

    /**
     * Find the highest non-air block at (x, z) in the RubyDung block array.
     */
    private int findSurfaceY(byte[] blocks, int width, int height, int depth, int x, int z) {
        for (int y = height - 1; y >= 0; y--) {
            int idx = (y * depth + z) * width + x;
            if (idx >= 0 && idx < blocks.length && blocks[idx] != 0) {
                return y;
            }
        }
        return height / 2;
    }
}
