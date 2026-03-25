package com.github.martinambrus.rdforward.world.convert;

import com.github.martinambrus.rdforward.world.BlockRegistry;
import com.github.martinambrus.rdforward.world.alpha.AlphaLevelFormat;
import com.github.martinambrus.rdforward.world.alpha.AlphaChunk;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ConversionRegistry: verifies BFS path-finding and
 * chained multi-step conversion execution.
 */
class ConversionRegistryTest {

    @TempDir
    File tempDir;

    @Test
    void findDirectPath() {
        ConversionRegistry registry = ConversionRegistry.createDefault();
        List<FormatConverter> path = registry.findPath(WorldFormat.RUBYDUNG_SERVER, WorldFormat.ALPHA);

        assertEquals(1, path.size());
        assertEquals(WorldFormat.RUBYDUNG_SERVER, path.get(0).sourceFormat());
        assertEquals(WorldFormat.ALPHA, path.get(0).targetFormat());
    }

    @Test
    void findChainedPath() {
        ConversionRegistry registry = ConversionRegistry.createDefault();
        List<FormatConverter> path = registry.findPath(WorldFormat.RUBYDUNG_SERVER, WorldFormat.MCREGION);

        assertEquals(2, path.size());
        assertEquals(WorldFormat.RUBYDUNG_SERVER, path.get(0).sourceFormat());
        assertEquals(WorldFormat.ALPHA, path.get(0).targetFormat());
        assertEquals(WorldFormat.ALPHA, path.get(1).sourceFormat());
        assertEquals(WorldFormat.MCREGION, path.get(1).targetFormat());
    }

    @Test
    void findOriginalRdToAlphaPath() {
        ConversionRegistry registry = ConversionRegistry.createDefault();
        // RUBYDUNG -> RUBYDUNG_SERVER -> ALPHA
        List<FormatConverter> path = registry.findPath(WorldFormat.RUBYDUNG, WorldFormat.ALPHA);

        assertEquals(2, path.size());
        assertEquals(WorldFormat.RUBYDUNG, path.get(0).sourceFormat());
        assertEquals(WorldFormat.RUBYDUNG_SERVER, path.get(0).targetFormat());
        assertEquals(WorldFormat.RUBYDUNG_SERVER, path.get(1).sourceFormat());
        assertEquals(WorldFormat.ALPHA, path.get(1).targetFormat());
    }

    @Test
    void findOriginalRdToMcRegionPath() {
        ConversionRegistry registry = ConversionRegistry.createDefault();
        // RUBYDUNG -> RUBYDUNG_SERVER -> ALPHA -> MCREGION
        List<FormatConverter> path = registry.findPath(WorldFormat.RUBYDUNG, WorldFormat.MCREGION);

        assertEquals(3, path.size());
        assertEquals(WorldFormat.RUBYDUNG, path.get(0).sourceFormat());
        assertEquals(WorldFormat.RUBYDUNG_SERVER, path.get(0).targetFormat());
        assertEquals(WorldFormat.RUBYDUNG_SERVER, path.get(1).sourceFormat());
        assertEquals(WorldFormat.ALPHA, path.get(1).targetFormat());
        assertEquals(WorldFormat.ALPHA, path.get(2).sourceFormat());
        assertEquals(WorldFormat.MCREGION, path.get(2).targetFormat());
    }

    @Test
    void noPathReturnsEmpty() {
        ConversionRegistry registry = ConversionRegistry.createDefault();
        // No converter registered for ALPHA -> RUBYDUNG
        List<FormatConverter> path = registry.findPath(WorldFormat.ALPHA, WorldFormat.RUBYDUNG);
        assertTrue(path.isEmpty());
    }

    @Test
    void sameFormatReturnsEmpty() {
        ConversionRegistry registry = ConversionRegistry.createDefault();
        List<FormatConverter> path = registry.findPath(WorldFormat.ALPHA, WorldFormat.ALPHA);
        assertTrue(path.isEmpty());
    }

    @Test
    void executeChainedConversion() throws IOException {
        // Create a small RubyDung server-world.dat
        int width = 16, height = 64, depth = 16;
        byte[] blocks = new byte[width * height * depth];
        int surfaceY = height * 2 / 3;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                for (int y = 0; y <= surfaceY; y++) {
                    blocks[(y * depth + z) * width + x] =
                            (y == surfaceY) ? (byte) BlockRegistry.GRASS
                                            : (byte) BlockRegistry.COBBLESTONE;
                }
            }
        }

        File worldFile = new File(tempDir, "server-world.dat");
        try (DataOutputStream dos = new DataOutputStream(
                new GZIPOutputStream(new FileOutputStream(worldFile)))) {
            dos.writeInt(width);
            dos.writeInt(height);
            dos.writeInt(depth);
            dos.write(blocks);
        }

        // Convert RUBYDUNG_SERVER -> McRegion (chained via Alpha)
        File outputDir = new File(tempDir, "region-world");
        ConversionRegistry registry = ConversionRegistry.createDefault();
        registry.convert(worldFile, outputDir, WorldFormat.RUBYDUNG_SERVER, WorldFormat.MCREGION, 0L);

        // Verify McRegion output exists
        File regionDir = new File(outputDir, "region");
        assertTrue(regionDir.isDirectory(), "region/ subdirectory should exist");

        File mcrFile = new File(regionDir, "r.0.0.mcr");
        assertTrue(mcrFile.exists(), "r.0.0.mcr should exist");
        assertTrue(mcrFile.length() >= 8192, "Region file should have valid header");
    }

    @Test
    void convertSameFormatIsNoOp() throws IOException {
        File input = new File(tempDir, "input");
        File output = new File(tempDir, "output");
        input.mkdirs();

        ConversionRegistry registry = ConversionRegistry.createDefault();
        // Should not throw — same format is a no-op
        registry.convert(input, output, WorldFormat.ALPHA, WorldFormat.ALPHA, 0L);
        assertFalse(output.exists(), "No output should be created for same-format conversion");
    }

    @Test
    void convertNoPathThrows() {
        ConversionRegistry registry = ConversionRegistry.createDefault();
        File input = new File(tempDir, "input");
        File output = new File(tempDir, "output");

        assertThrows(IllegalArgumentException.class, () ->
                registry.convert(input, output, WorldFormat.MCREGION, WorldFormat.RUBYDUNG, 0L));
    }
}
