package com.github.martinambrus.rdforward.world.convert;

import com.github.martinambrus.rdforward.world.BlockRegistry;
import com.github.martinambrus.rdforward.world.alpha.AlphaChunk;
import com.github.martinambrus.rdforward.world.alpha.AlphaLevelFormat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RubyDungToAlphaConverter: verifies that a RubyDung flat world
 * is correctly split into Alpha chunks with proper block data, level.dat,
 * and session.lock.
 */
class RubyDungToAlphaConverterTest {

    @TempDir
    File tempDir;

    /**
     * Write a RubyDung-format world file (GZip'd [width][height][depth][blocks]).
     */
    private File writeRubyDungWorld(int width, int height, int depth, byte[] blocks)
            throws IOException {
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

    /**
     * Create a simple RubyDung world with cobblestone below surfaceY and grass at surfaceY.
     */
    private byte[] createSimpleWorld(int width, int height, int depth) {
        byte[] blocks = new byte[width * height * depth];
        int surfaceY = height * 2 / 3;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                for (int y = 0; y < height; y++) {
                    int idx = (y * depth + z) * width + x;
                    if (y < surfaceY) {
                        blocks[idx] = (byte) BlockRegistry.COBBLESTONE;
                    } else if (y == surfaceY) {
                        blocks[idx] = (byte) BlockRegistry.GRASS;
                    }
                }
            }
        }
        return blocks;
    }

    @Test
    void convertsSmallWorld() throws IOException {
        int width = 32, height = 64, depth = 32;
        byte[] blocks = createSimpleWorld(width, height, depth);
        File worldFile = writeRubyDungWorld(width, height, depth, blocks);
        File outputDir = new File(tempDir, "alpha-world");

        new RubyDungToAlphaConverter().convert(worldFile, outputDir, 42L);

        // Should produce 2x2 chunks for a 32x32 world
        AlphaChunk chunk00 = AlphaLevelFormat.loadChunk(outputDir, 0, 0);
        AlphaChunk chunk10 = AlphaLevelFormat.loadChunk(outputDir, 1, 0);
        AlphaChunk chunk01 = AlphaLevelFormat.loadChunk(outputDir, 0, 1);
        AlphaChunk chunk11 = AlphaLevelFormat.loadChunk(outputDir, 1, 1);

        assertNotNull(chunk00);
        assertNotNull(chunk10);
        assertNotNull(chunk01);
        assertNotNull(chunk11);

        // Verify surface block in chunk (0,0) at local (8, surfaceY, 8) = world (8, 42, 8)
        int surfaceY = height * 2 / 3;
        assertEquals(BlockRegistry.GRASS, chunk00.getBlock(8, surfaceY, 8));
        assertEquals(BlockRegistry.COBBLESTONE, chunk00.getBlock(8, surfaceY - 1, 8));
        assertEquals(BlockRegistry.AIR, chunk00.getBlock(8, surfaceY + 1, 8));
    }

    @Test
    void blockIdsPreserved() throws IOException {
        int width = 16, height = 64, depth = 16;
        byte[] blocks = new byte[width * height * depth];
        // Place specific blocks at known positions
        blocks[(10 * depth + 5) * width + 3] = (byte) BlockRegistry.AIR;
        blocks[(20 * depth + 5) * width + 3] = (byte) BlockRegistry.GRASS;
        blocks[(30 * depth + 5) * width + 3] = (byte) BlockRegistry.COBBLESTONE;

        File worldFile = writeRubyDungWorld(width, height, depth, blocks);
        File outputDir = new File(tempDir, "alpha-world");

        new RubyDungToAlphaConverter().convert(worldFile, outputDir, 0L);

        AlphaChunk chunk = AlphaLevelFormat.loadChunk(outputDir, 0, 0);
        assertNotNull(chunk);
        assertEquals(BlockRegistry.AIR, chunk.getBlock(3, 10, 5));
        assertEquals(BlockRegistry.GRASS, chunk.getBlock(3, 20, 5));
        assertEquals(BlockRegistry.COBBLESTONE, chunk.getBlock(3, 30, 5));
    }

    @Test
    void levelDatCreated() throws IOException {
        int width = 16, height = 64, depth = 16;
        byte[] blocks = createSimpleWorld(width, height, depth);
        File worldFile = writeRubyDungWorld(width, height, depth, blocks);
        File outputDir = new File(tempDir, "alpha-world");

        new RubyDungToAlphaConverter().convert(worldFile, outputDir, 12345L);

        File levelDat = new File(outputDir, "level.dat");
        assertTrue(levelDat.exists(), "level.dat should be created");
        assertTrue(levelDat.length() > 0, "level.dat should not be empty");
    }

    @Test
    void sessionLockCreated() throws IOException {
        int width = 16, height = 64, depth = 16;
        byte[] blocks = createSimpleWorld(width, height, depth);
        File worldFile = writeRubyDungWorld(width, height, depth, blocks);
        File outputDir = new File(tempDir, "alpha-world");

        new RubyDungToAlphaConverter().convert(worldFile, outputDir, 0L);

        File sessionLock = new File(outputDir, "session.lock");
        assertTrue(sessionLock.exists(), "session.lock should be created");
        assertEquals(8, sessionLock.length(), "session.lock should be 8 bytes");
    }

    @Test
    void chunkCoordinatesCorrect() throws IOException {
        int width = 32, height = 64, depth = 32;
        byte[] blocks = createSimpleWorld(width, height, depth);
        File worldFile = writeRubyDungWorld(width, height, depth, blocks);
        File outputDir = new File(tempDir, "alpha-world");

        new RubyDungToAlphaConverter().convert(worldFile, outputDir, 0L);

        // 32x32 world = 2x2 chunks
        for (int cx = 0; cx < 2; cx++) {
            for (int cz = 0; cz < 2; cz++) {
                AlphaChunk chunk = AlphaLevelFormat.loadChunk(outputDir, cx, cz);
                assertNotNull(chunk, "Chunk (" + cx + "," + cz + ") should exist");
                assertEquals(cx, chunk.getXPos());
                assertEquals(cz, chunk.getZPos());
                assertTrue(chunk.isTerrainPopulated());
            }
        }

        // Beyond world bounds should not exist
        assertNull(AlphaLevelFormat.loadChunk(outputDir, 2, 0));
        assertNull(AlphaLevelFormat.loadChunk(outputDir, 0, 2));
    }

    @Test
    void heightMapPopulated() throws IOException {
        int width = 16, height = 64, depth = 16;
        byte[] blocks = createSimpleWorld(width, height, depth);
        File worldFile = writeRubyDungWorld(width, height, depth, blocks);
        File outputDir = new File(tempDir, "alpha-world");

        new RubyDungToAlphaConverter().convert(worldFile, outputDir, 0L);

        AlphaChunk chunk = AlphaLevelFormat.loadChunk(outputDir, 0, 0);
        assertNotNull(chunk);

        byte[] heightMap = chunk.getHeightMap();
        int surfaceY = height * 2 / 3;
        // All columns should have height = surfaceY + 1 (grass is at surfaceY)
        for (int i = 0; i < heightMap.length; i++) {
            assertEquals(surfaceY + 1, heightMap[i] & 0xFF,
                    "Height map at index " + i + " should be " + (surfaceY + 1));
        }
    }

    @Test
    void nonSquareWorldDimensions() throws IOException {
        // Non-square world: 48 wide, 64 tall, 32 deep
        int width = 48, height = 64, depth = 32;
        byte[] blocks = createSimpleWorld(width, height, depth);
        File worldFile = writeRubyDungWorld(width, height, depth, blocks);
        File outputDir = new File(tempDir, "alpha-world");

        new RubyDungToAlphaConverter().convert(worldFile, outputDir, 0L);

        // 48/16 = 3 chunks X, 32/16 = 2 chunks Z
        assertNotNull(AlphaLevelFormat.loadChunk(outputDir, 0, 0));
        assertNotNull(AlphaLevelFormat.loadChunk(outputDir, 2, 0));
        assertNotNull(AlphaLevelFormat.loadChunk(outputDir, 0, 1));
        assertNotNull(AlphaLevelFormat.loadChunk(outputDir, 2, 1));
        assertNull(AlphaLevelFormat.loadChunk(outputDir, 3, 0));
    }
}
