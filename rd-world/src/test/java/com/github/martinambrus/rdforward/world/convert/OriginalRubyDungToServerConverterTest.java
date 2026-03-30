package com.github.martinambrus.rdforward.world.convert;

import com.github.martinambrus.rdforward.world.BlockRegistry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for OriginalRubyDungToServerConverter: verifies that the original
 * RubyDung level.dat (no header, block ID 1 = solid) is correctly converted
 * to the server-world.dat format (versioned header, grass/cobblestone block IDs).
 */
class OriginalRubyDungToServerConverterTest {

    @TempDir
    File tempDir;

    /**
     * Create an original RubyDung level.dat with the standard terrain.
     */
    private File writeOriginalRdLevel(int width, int height, int depth) throws IOException {
        byte[] blocks = new byte[width * height * depth];
        int surfaceY = height * 2 / 3; // Original RD: d*2/3 where d = vertical

        // Original RubyDung uses (y * height + z) * width + x
        // but "depth" in original is what we call "height" (vertical),
        // and "height" is what we call "depth" (z-axis).
        // Our WorldGenerator uses (y * depth + z) * width + x.
        // Since the converter reads the raw bytes as-is with the same
        // YZX ordering, we match the server's convention here.
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                for (int y = 0; y < height; y++) {
                    int idx = (y * depth + z) * width + x;
                    blocks[idx] = (byte) (y <= surfaceY ? 1 : 0);
                }
            }
        }

        File levelDat = new File(tempDir, "level.dat");
        try (GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(levelDat))) {
            gos.write(blocks);
        }
        return levelDat;
    }

    @Test
    void convertsBlockIdsCorrectly() throws IOException {
        int w = 16, h = 64, d = 16;
        File levelDat = writeOriginalRdLevel(w, h, d);
        File output = new File(tempDir, "server-world.dat");

        new OriginalRubyDungToServerConverter(w, h, d).convert(levelDat, output, 0L);

        // Read back the converted file
        try (DataInputStream dis = new DataInputStream(
                new GZIPInputStream(new FileInputStream(output)))) {
            assertEquals(ServerWorldHeader.FORMAT_MAGIC, dis.readInt(), "Expected FORMAT_MAGIC");
            assertEquals(ServerWorldHeader.CURRENT_FORMAT_VERSION, dis.readInt(), "Expected current format version");
            assertEquals(w, dis.readInt());
            assertEquals(h, dis.readInt());
            assertEquals(d, dis.readInt());

            byte[] blocks = new byte[w * h * d];
            dis.readFully(blocks);

            int surfaceY = h * 2 / 3;

            // Check a column: surface should be grass, below should be cobblestone
            for (int y = 0; y < h; y++) {
                int idx = (y * d + 0) * w + 0;
                int blockId = blocks[idx] & 0xFF;
                if (y < surfaceY) {
                    assertEquals(BlockRegistry.COBBLESTONE, blockId,
                            "Below surface at y=" + y + " should be cobblestone");
                } else if (y == surfaceY) {
                    assertEquals(BlockRegistry.GRASS, blockId,
                            "Surface at y=" + y + " should be grass");
                } else {
                    assertEquals(BlockRegistry.AIR, blockId,
                            "Above surface at y=" + y + " should be air");
                }
            }
        }
    }

    @Test
    void outputHasDimensionHeader() throws IOException {
        int w = 16, h = 64, d = 16;
        File levelDat = writeOriginalRdLevel(w, h, d);
        File output = new File(tempDir, "server-world.dat");

        new OriginalRubyDungToServerConverter(w, h, d).convert(levelDat, output, 0L);

        // The output should be detected as RUBYDUNG_SERVER format
        assertEquals(WorldFormat.RUBYDUNG_SERVER, WorldFormatDetector.detect(output));
    }

    @Test
    void airBlocksPreserved() throws IOException {
        int w = 16, h = 64, d = 16;
        // Create a world that's all air (no block 1)
        byte[] blocks = new byte[w * h * d]; // all zeros = air

        File levelDat = new File(tempDir, "level.dat");
        try (GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(levelDat))) {
            gos.write(blocks);
        }

        File output = new File(tempDir, "server-world.dat");
        new OriginalRubyDungToServerConverter(w, h, d).convert(levelDat, output, 0L);

        try (DataInputStream dis = new DataInputStream(
                new GZIPInputStream(new FileInputStream(output)))) {
            dis.readInt(); // magic
            dis.readInt(); // version
            dis.readInt(); // width
            dis.readInt(); // height
            dis.readInt(); // depth
            byte[] converted = new byte[w * h * d];
            dis.readFully(converted);

            // All should still be air
            for (int i = 0; i < converted.length; i++) {
                assertEquals(0, converted[i], "Block at index " + i + " should be air");
            }
        }
    }

    @Test
    void formatConverterInterface() {
        OriginalRubyDungToServerConverter converter = new OriginalRubyDungToServerConverter();
        assertEquals(WorldFormat.RUBYDUNG, converter.sourceFormat());
        assertEquals(WorldFormat.RUBYDUNG_SERVER, converter.targetFormat());
    }

    @Test
    void wrongSizeThrows() throws IOException {
        // Create a file that's too small for the expected dimensions
        byte[] tooSmall = new byte[100];
        File levelDat = new File(tempDir, "level.dat");
        try (GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(levelDat))) {
            gos.write(tooSmall);
        }

        File output = new File(tempDir, "server-world.dat");
        OriginalRubyDungToServerConverter converter =
                new OriginalRubyDungToServerConverter(16, 64, 16);

        assertThrows(IOException.class, () -> converter.convert(levelDat, output, 0L));
    }
}
