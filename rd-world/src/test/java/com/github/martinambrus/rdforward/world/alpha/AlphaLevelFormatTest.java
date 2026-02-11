package com.github.martinambrus.rdforward.world.alpha;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AlphaLevelFormat: chunk save/load round-trip, level.dat,
 * session.lock, and base-36 directory hashing.
 */
class AlphaLevelFormatTest {

    @TempDir
    File tempDir;

    @Test
    void chunkRoundTrip() throws IOException {
        AlphaChunk original = new AlphaChunk(3, -2);
        original.setBlock(5, 64, 8, 42);
        original.setBlockData(5, 64, 8, 7);
        original.setTerrainPopulated(true);
        original.setLastUpdate(999L);

        AlphaLevelFormat.saveChunk(tempDir, original);

        AlphaChunk loaded = AlphaLevelFormat.loadChunk(tempDir, 3, -2);
        assertNotNull(loaded);
        assertEquals(3, loaded.getXPos());
        assertEquals(-2, loaded.getZPos());
        assertEquals(42, loaded.getBlock(5, 64, 8));
        assertEquals(7, loaded.getBlockData(5, 64, 8));
        assertTrue(loaded.isTerrainPopulated());
        assertEquals(999L, loaded.getLastUpdate());
    }

    @Test
    void loadNonExistentChunkReturnsNull() throws IOException {
        assertNull(AlphaLevelFormat.loadChunk(tempDir, 99, 99));
    }

    @Test
    void chunkWithEntitiesRoundTrip() throws IOException {
        AlphaChunk original = new AlphaChunk(0, 0);
        original.addEntity(new AlphaEntity("Pig", 1.5, 65.0, 3.5));
        original.addEntity(new AlphaEntity("Zombie", 10.0, 40.0, 10.0));

        AlphaLevelFormat.saveChunk(tempDir, original);

        AlphaChunk loaded = AlphaLevelFormat.loadChunk(tempDir, 0, 0);
        assertNotNull(loaded);
        assertEquals(2, loaded.getEntities().size());
        assertEquals("Pig", loaded.getEntities().get(0).getId());
        assertEquals("Zombie", loaded.getEntities().get(1).getId());
    }

    @Test
    void chunkWithTileEntitiesRoundTrip() throws IOException {
        AlphaChunk original = new AlphaChunk(1, 1);
        original.addTileEntity(new AlphaTileEntity("Chest", 4, 30, 8));
        original.addTileEntity(new AlphaTileEntity("Sign", 10, 65, 10));

        AlphaLevelFormat.saveChunk(tempDir, original);

        AlphaChunk loaded = AlphaLevelFormat.loadChunk(tempDir, 1, 1);
        assertNotNull(loaded);
        assertEquals(2, loaded.getTileEntities().size());

        AlphaTileEntity chest = loaded.getTileEntityAt(4, 30, 8);
        assertNotNull(chest);
        assertEquals("Chest", chest.getId());
    }

    @Test
    void blockArrayPreservedOnRoundTrip() throws IOException {
        AlphaChunk original = new AlphaChunk(0, 0);
        // Set a variety of blocks
        for (int y = 0; y < 64; y++) {
            original.setBlock(0, y, 0, 1); // Stone column
        }
        original.setBlock(8, 100, 8, 49); // Obsidian

        AlphaLevelFormat.saveChunk(tempDir, original);
        AlphaChunk loaded = AlphaLevelFormat.loadChunk(tempDir, 0, 0);
        assertNotNull(loaded);

        for (int y = 0; y < 64; y++) {
            assertEquals(1, loaded.getBlock(0, y, 0), "Stone at y=" + y);
        }
        assertEquals(49, loaded.getBlock(8, 100, 8));
        assertEquals(0, loaded.getBlock(15, 127, 15)); // Air
    }

    @Test
    void skylightPreservedOnRoundTrip() throws IOException {
        AlphaChunk original = new AlphaChunk(0, 0);
        AlphaLevelFormat.saveChunk(tempDir, original);
        AlphaChunk loaded = AlphaLevelFormat.loadChunk(tempDir, 0, 0);
        assertNotNull(loaded);

        byte[] sky = loaded.getSkyLight();
        for (byte b : sky) {
            assertEquals((byte) 0xFF, b, "Sky light should be preserved at full brightness");
        }
    }

    @Test
    void chunkFilePathUsesBase36() {
        // getChunkFile is package-private, test via round-trip file existence
        File chunkFile = AlphaLevelFormat.getChunkFile(tempDir, 3, -2);
        // x=3: 3%64=3 -> base36="3"; z=-2: -2&63=62 -> base36="1q"
        assertTrue(chunkFile.getPath().contains("3"));
        assertTrue(chunkFile.getName().startsWith("c."));
        assertTrue(chunkFile.getName().endsWith(".dat"));
    }

    @Test
    void negativeCoordinateChunkRoundTrip() throws IOException {
        AlphaChunk original = new AlphaChunk(-10, -20);
        original.setBlock(0, 0, 0, 3); // Dirt

        AlphaLevelFormat.saveChunk(tempDir, original);
        AlphaChunk loaded = AlphaLevelFormat.loadChunk(tempDir, -10, -20);
        assertNotNull(loaded);
        assertEquals(-10, loaded.getXPos());
        assertEquals(-20, loaded.getZPos());
        assertEquals(3, loaded.getBlock(0, 0, 0));
    }

    @Test
    void saveLevelDatCreatesFile() throws IOException {
        AlphaLevelFormat.saveLevelDat(tempDir, 42L, 128, 65, 128, 6000L, System.currentTimeMillis());
        File levelDat = new File(tempDir, "level.dat");
        assertTrue(levelDat.exists());
        assertTrue(levelDat.length() > 0);
    }

    @Test
    void writeSessionLockCreatesFile() throws IOException {
        AlphaLevelFormat.writeSessionLock(tempDir);
        File sessionLock = new File(tempDir, "session.lock");
        assertTrue(sessionLock.exists());
        assertEquals(8, sessionLock.length()); // 8-byte long timestamp
    }
}
