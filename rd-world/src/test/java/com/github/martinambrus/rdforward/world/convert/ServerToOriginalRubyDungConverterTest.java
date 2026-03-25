package com.github.martinambrus.rdforward.world.convert;

import com.github.martinambrus.rdforward.world.BlockRegistry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ServerToOriginalRubyDungConverter: verifies that server-world.dat
 * is correctly converted to the original RubyDung level.dat format (no header,
 * all non-air blocks mapped to ID 1).
 */
class ServerToOriginalRubyDungConverterTest {

    @TempDir
    File tempDir;

    private File writeServerWorld(int width, int height, int depth, byte[] blocks) throws IOException {
        File worldFile = new File(tempDir, "server-world.dat");
        try (DataOutputStream dos = new DataOutputStream(
                new GZIPOutputStream(new FileOutputStream(worldFile)))) {
            dos.writeInt(width);
            dos.writeInt(height);
            dos.writeInt(depth);
            dos.write(blocks);
        }
        return worldFile;
    }

    @Test
    void mapsAllSolidBlocksToOne() throws IOException {
        int w = 16, h = 64, d = 16;
        byte[] blocks = new byte[w * h * d];

        // Place various block types
        blocks[0] = (byte) BlockRegistry.GRASS;       // 2
        blocks[1] = (byte) BlockRegistry.COBBLESTONE;  // 4
        blocks[2] = (byte) BlockRegistry.STONE;        // 1
        blocks[3] = (byte) BlockRegistry.DIRT;         // 3
        blocks[4] = (byte) BlockRegistry.AIR;          // 0

        File serverWorld = writeServerWorld(w, h, d, blocks);
        File output = new File(tempDir, "level.dat");

        new ServerToOriginalRubyDungConverter().convert(serverWorld, output, 0L);

        // Read back and verify
        byte[] converted = readRawGzip(output);
        assertEquals(w * h * d, converted.length);
        assertEquals(1, converted[0], "Grass should map to 1");
        assertEquals(1, converted[1], "Cobblestone should map to 1");
        assertEquals(1, converted[2], "Stone should map to 1");
        assertEquals(1, converted[3], "Dirt should map to 1");
        assertEquals(0, converted[4], "Air should stay 0");
    }

    @Test
    void outputHasNoHeader() throws IOException {
        int w = 16, h = 64, d = 16;
        byte[] blocks = new byte[w * h * d];

        File serverWorld = writeServerWorld(w, h, d, blocks);
        File output = new File(tempDir, "level.dat");

        new ServerToOriginalRubyDungConverter().convert(serverWorld, output, 0L);

        // The output should be raw blocks with no 12-byte header
        byte[] raw = readRawGzip(output);
        assertEquals(w * h * d, raw.length,
                "Output should be exactly width*height*depth bytes (no header)");
    }

    @Test
    void roundTripPreservesAirPattern() throws IOException {
        int w = 16, h = 64, d = 16;
        byte[] blocks = new byte[w * h * d];

        // Create terrain: solid below y=42, air above
        int surfaceY = h * 2 / 3;
        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                for (int y = 0; y <= surfaceY; y++) {
                    blocks[(y * d + z) * w + x] = (byte) BlockRegistry.COBBLESTONE;
                }
            }
        }

        File serverWorld = writeServerWorld(w, h, d, blocks);
        File output = new File(tempDir, "level.dat");

        new ServerToOriginalRubyDungConverter().convert(serverWorld, output, 0L);

        byte[] converted = readRawGzip(output);

        // Verify solid/air pattern is preserved (just mapped to 1/0)
        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                for (int y = 0; y < h; y++) {
                    int idx = (y * d + z) * w + x;
                    if (y <= surfaceY) {
                        assertEquals(1, converted[idx],
                                "Solid at (" + x + "," + y + "," + z + ") should be 1");
                    } else {
                        assertEquals(0, converted[idx],
                                "Air at (" + x + "," + y + "," + z + ") should be 0");
                    }
                }
            }
        }
    }

    @Test
    void formatConverterInterface() {
        ServerToOriginalRubyDungConverter converter = new ServerToOriginalRubyDungConverter();
        assertEquals(WorldFormat.RUBYDUNG_SERVER, converter.sourceFormat());
        assertEquals(WorldFormat.RUBYDUNG, converter.targetFormat());
    }

    private byte[] readRawGzip(File file) throws IOException {
        try (InputStream is = new GZIPInputStream(new FileInputStream(file))) {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int read;
            while ((read = is.read(buf)) != -1) {
                baos.write(buf, 0, read);
            }
            return baos.toByteArray();
        }
    }
}
