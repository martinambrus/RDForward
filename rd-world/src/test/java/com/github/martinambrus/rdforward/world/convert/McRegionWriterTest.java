package com.github.martinambrus.rdforward.world.convert;

import com.github.martinambrus.rdforward.world.BlockRegistry;
import com.github.martinambrus.rdforward.world.alpha.AlphaChunk;
import com.github.martinambrus.rdforward.world.alpha.AlphaLevelFormat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for McRegionWriter: verifies that Alpha chunks are correctly
 * packed into .mcr region files with valid sector layout.
 */
class McRegionWriterTest {

    @TempDir
    File tempDir;

    /**
     * Create an Alpha world directory with the given chunks.
     */
    private File createAlphaWorld(AlphaChunk... chunks) throws IOException {
        File alphaDir = new File(tempDir, "alpha-world");
        alphaDir.mkdirs();

        for (AlphaChunk chunk : chunks) {
            AlphaLevelFormat.saveChunk(alphaDir, chunk);
        }

        // level.dat is needed for the copy step
        AlphaLevelFormat.saveLevelDat(alphaDir, 0L, 0, 64, 0, 0L, System.currentTimeMillis());

        return alphaDir;
    }

    @Test
    void convertsAlphaToRegion() throws IOException {
        AlphaChunk chunk = new AlphaChunk(0, 0);
        chunk.setBlock(8, 64, 8, BlockRegistry.STONE);
        chunk.setTerrainPopulated(true);

        File alphaDir = createAlphaWorld(chunk);
        File outputDir = new File(tempDir, "region-world");

        new McRegionWriter().convertAlphaToRegion(alphaDir, outputDir);

        File regionDir = new File(outputDir, "region");
        assertTrue(regionDir.isDirectory(), "region/ subdirectory should exist");

        File mcrFile = new File(regionDir, "r.0.0.mcr");
        assertTrue(mcrFile.exists(), "r.0.0.mcr should exist");
    }

    @Test
    void regionFileStructure() throws IOException {
        AlphaChunk chunk = new AlphaChunk(0, 0);
        chunk.setBlock(8, 64, 8, BlockRegistry.STONE);

        File alphaDir = createAlphaWorld(chunk);
        File outputDir = new File(tempDir, "region-world");

        new McRegionWriter().convertAlphaToRegion(alphaDir, outputDir);

        File mcrFile = new File(outputDir, "region/r.0.0.mcr");
        assertTrue(mcrFile.exists());

        // Region file must be at least 8192 bytes (2 header sectors)
        assertTrue(mcrFile.length() >= 8192, "Region file must have at least 2 header sectors");

        // Read location table entry for chunk (0,0) — slot 0
        try (RandomAccessFile raf = new RandomAccessFile(mcrFile, "r")) {
            int locationEntry = raf.readInt();
            // Should be non-zero (chunk exists)
            assertTrue(locationEntry != 0, "Location entry for chunk (0,0) should be non-zero");

            int offset = (locationEntry >> 8) & 0xFFFFFF;
            int sectorCount = locationEntry & 0xFF;
            // Offset should be >= 2 (after header)
            assertTrue(offset >= 2, "Chunk data offset should be after header sectors");
            assertTrue(sectorCount > 0, "Sector count should be positive");

            // Verify chunk data header at the sector offset
            raf.seek((long) offset * 4096);
            int chunkLength = raf.readInt();
            byte compressionType = raf.readByte();

            assertTrue(chunkLength > 0, "Chunk data length should be positive");
            assertEquals(2, compressionType, "Compression type should be 2 (Zlib)");
        }
    }

    @Test
    void chunkDataReadable() throws IOException {
        AlphaChunk original = new AlphaChunk(3, 5);
        original.setBlock(0, 0, 0, BlockRegistry.DIRT);
        original.setBlock(15, 127, 15, BlockRegistry.OBSIDIAN);
        original.setTerrainPopulated(true);

        File alphaDir = createAlphaWorld(original);
        File outputDir = new File(tempDir, "region-world");

        new McRegionWriter().convertAlphaToRegion(alphaDir, outputDir);

        File mcrFile = new File(outputDir, "region/r.0.0.mcr");
        assertTrue(mcrFile.exists());

        // Verify the .mcr file has the correct chunk at the right slot
        // Chunk (3,5): slot = (3 & 31) + (5 & 31) * 32 = 3 + 160 = 163
        try (RandomAccessFile raf = new RandomAccessFile(mcrFile, "r")) {
            raf.seek(163 * 4L);
            int locationEntry = raf.readInt();
            assertTrue(locationEntry != 0, "Chunk (3,5) should have a location entry");
        }
    }

    @Test
    void multipleRegionsCreated() throws IOException {
        // Chunk (0,0) goes to region (0,0), chunk (40,40) goes to region (1,1)
        AlphaChunk chunk1 = new AlphaChunk(0, 0);
        chunk1.setBlock(0, 0, 0, BlockRegistry.STONE);

        AlphaChunk chunk2 = new AlphaChunk(40, 40);
        chunk2.setBlock(0, 0, 0, BlockRegistry.DIRT);

        File alphaDir = createAlphaWorld(chunk1, chunk2);
        File outputDir = new File(tempDir, "region-world");

        new McRegionWriter().convertAlphaToRegion(alphaDir, outputDir);

        File regionDir = new File(outputDir, "region");
        assertTrue(new File(regionDir, "r.0.0.mcr").exists(), "Region (0,0) should exist");
        assertTrue(new File(regionDir, "r.1.1.mcr").exists(), "Region (1,1) should exist");
    }
}
