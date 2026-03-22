package com.github.martinambrus.rdforward.world.convert;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/**
 * Detects the world format of a file or directory.
 *
 * Detection heuristics:
 *   ALPHA     — directory with level.dat and at least one c.*.dat chunk file
 *   MCREGION  — directory with region/*.mcr files
 *   RUBYDUNG_SERVER — GZip'd .dat file whose header (3 ints) matches the data size
 *   RUBYDUNG  — GZip'd .dat file with no header (raw blocks, original RubyDung level.dat)
 */
public final class WorldFormatDetector {

    private WorldFormatDetector() {}

    /**
     * Detect the world format at the given path.
     *
     * @param path a file or directory to inspect
     * @return the detected format, or null if unrecognized or path does not exist
     */
    public static WorldFormat detect(File path) {
        if (path == null || !path.exists()) {
            return null;
        }

        if (path.isDirectory()) {
            return detectDirectory(path);
        }

        if (path.isFile()) {
            return detectFile(path);
        }

        return null;
    }

    private static WorldFormat detectDirectory(File dir) {
        // Check for Alpha format: level.dat + base-36 chunk files
        if (isAlphaDirectory(dir)) {
            return WorldFormat.ALPHA;
        }

        // Check for McRegion format: region/*.mcr
        if (isMcRegionDirectory(dir)) {
            return WorldFormat.MCREGION;
        }

        return null;
    }

    private static WorldFormat detectFile(File file) {
        if (!file.getName().endsWith(".dat")) {
            return null;
        }

        // Try server format first (has 3-int header): GZip'd [width][height][depth][blocks]
        if (isRubyDungServerFile(file)) {
            return WorldFormat.RUBYDUNG_SERVER;
        }

        // Try original RubyDung format (no header, just raw blocks)
        if (isOriginalRubyDungFile(file)) {
            return WorldFormat.RUBYDUNG;
        }

        return null;
    }

    /**
     * Check if the directory looks like an Alpha world:
     * must have level.dat and at least one chunk file (c.*.dat) in the
     * base-36 directory tree.
     */
    static boolean isAlphaDirectory(File dir) {
        File levelDat = new File(dir, "level.dat");
        if (!levelDat.exists()) {
            return false;
        }

        // Look for at least one chunk file in the directory tree
        return hasChunkFiles(dir);
    }

    /**
     * Check if the directory contains McRegion files (region/*.mcr).
     */
    static boolean isMcRegionDirectory(File dir) {
        File regionDir = new File(dir, "region");
        if (!regionDir.isDirectory()) {
            return false;
        }

        File[] mcrFiles = regionDir.listFiles((d, name) -> name.endsWith(".mcr"));
        return mcrFiles != null && mcrFiles.length > 0;
    }

    /**
     * Check if a file is a valid RDForward server-world.dat file.
     * Reads only the 12-byte header (3 ints) and checks that the dimensions
     * are positive and within sane bounds — avoids decompressing the entire file.
     */
    static boolean isRubyDungServerFile(File file) {
        try (DataInputStream dis = new DataInputStream(
                new GZIPInputStream(new FileInputStream(file)))) {
            int width = dis.readInt();
            int height = dis.readInt();
            int depth = dis.readInt();

            if (width <= 0 || height <= 0 || depth <= 0) {
                return false;
            }

            // Dimensions should be reasonable for a Minecraft world
            long expectedSize = (long) width * height * depth;
            return expectedSize <= 512L * 256 * 512;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Check if a file is an original RubyDung level.dat file.
     * The original format has no header — just GZip'd raw blocks.
     * We detect this by checking if the decompressed size matches
     * the expected 256*64*256 = 4,194,304 bytes.
     */
    static boolean isOriginalRubyDungFile(File file) {
        long expectedSize = (long) OriginalRubyDungToServerConverter.DEFAULT_WIDTH
                * OriginalRubyDungToServerConverter.DEFAULT_HEIGHT
                * OriginalRubyDungToServerConverter.DEFAULT_DEPTH;

        try (java.io.InputStream is = new GZIPInputStream(new FileInputStream(file))) {
            long total = 0;
            byte[] buf = new byte[8192];
            int read;
            while ((read = is.read(buf)) != -1) {
                total += read;
                // Early exit if it's already too large
                if (total > expectedSize) {
                    return false;
                }
            }
            return total == expectedSize;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Recursively look for Alpha chunk files (c.*.dat) in the base-36 directory tree.
     * Only searches two levels deep (the Alpha format uses exactly two directory levels).
     */
    private static boolean hasChunkFiles(File dir) {
        File[] level1Dirs = dir.listFiles(File::isDirectory);
        if (level1Dirs == null) {
            return false;
        }

        for (File dir1 : level1Dirs) {
            if ("region".equals(dir1.getName()) || "players".equals(dir1.getName())) {
                continue;
            }

            File[] level2Dirs = dir1.listFiles(File::isDirectory);
            if (level2Dirs == null) {
                continue;
            }

            for (File dir2 : level2Dirs) {
                File[] chunkFiles = dir2.listFiles((d, name) ->
                        name.startsWith("c.") && name.endsWith(".dat"));
                if (chunkFiles != null && chunkFiles.length > 0) {
                    return true;
                }
            }
        }

        return false;
    }
}
