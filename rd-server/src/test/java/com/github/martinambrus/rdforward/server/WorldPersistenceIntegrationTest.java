package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.world.alpha.AlphaChunk;
import com.github.martinambrus.rdforward.world.alpha.AlphaEntity;
import com.github.martinambrus.rdforward.world.alpha.AlphaLevelFormat;
import com.github.martinambrus.rdforward.world.alpha.AlphaTileEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for world persistence: verifies that complex world
 * state (multiple chunks, entities, tile entities) survives save/reload
 * cycles through the AlphaLevelFormat.
 */
class WorldPersistenceIntegrationTest {

    @TempDir
    File tempDir;

    @Test
    void multipleChunksSaveAndReloadIndependently() throws IOException {
        // Create and save two chunks at different positions
        AlphaChunk chunk1 = new AlphaChunk(0, 0);
        chunk1.setBlock(0, 0, 0, 1);
        chunk1.setBlock(15, 127, 15, 49);

        AlphaChunk chunk2 = new AlphaChunk(5, -3);
        chunk2.setBlock(8, 64, 8, 4);
        chunk2.setTerrainPopulated(true);

        AlphaLevelFormat.saveChunk(tempDir, chunk1);
        AlphaLevelFormat.saveChunk(tempDir, chunk2);

        // Load them back in reverse order
        AlphaChunk loaded2 = AlphaLevelFormat.loadChunk(tempDir, 5, -3);
        AlphaChunk loaded1 = AlphaLevelFormat.loadChunk(tempDir, 0, 0);

        assertNotNull(loaded1);
        assertNotNull(loaded2);

        assertEquals(1, loaded1.getBlock(0, 0, 0));
        assertEquals(49, loaded1.getBlock(15, 127, 15));
        assertFalse(loaded1.isTerrainPopulated());

        assertEquals(4, loaded2.getBlock(8, 64, 8));
        assertTrue(loaded2.isTerrainPopulated());
    }

    @Test
    void fullWorldStateRoundTrip() throws IOException {
        // Simulate a realistic chunk with entities and tile entities
        AlphaChunk original = new AlphaChunk(10, -20);

        // Terrain: bedrock at y=0, stone below surface, grass at surface
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                original.setBlock(x, 0, z, 7);    // Bedrock
                for (int y = 1; y < 60; y++) {
                    original.setBlock(x, y, z, 1); // Stone
                }
                original.setBlock(x, 60, z, 2);    // Grass
            }
        }

        // Metadata on some blocks
        original.setBlockData(5, 60, 5, 3);

        // Entities
        AlphaEntity pig = new AlphaEntity("Pig", 162.5, 61.0, -317.5);
        AlphaEntity zombie = new AlphaEntity("Zombie", 165.0, 50.0, -310.0);
        original.addEntity(pig);
        original.addEntity(zombie);

        // Tile entities
        AlphaTileEntity chest = new AlphaTileEntity("Chest", 168, 61, -320);
        AlphaTileEntity sign = new AlphaTileEntity("Sign", 170, 62, -318);
        original.addTileEntity(chest);
        original.addTileEntity(sign);

        original.setTerrainPopulated(true);
        original.setLastUpdate(54321L);

        // Save and reload
        AlphaLevelFormat.saveChunk(tempDir, original);
        AlphaChunk loaded = AlphaLevelFormat.loadChunk(tempDir, 10, -20);
        assertNotNull(loaded);

        // Verify terrain
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                assertEquals(7, loaded.getBlock(x, 0, z), "Bedrock at " + x + "," + z);
                assertEquals(1, loaded.getBlock(x, 30, z), "Stone at " + x + "," + z);
                assertEquals(2, loaded.getBlock(x, 60, z), "Grass at " + x + "," + z);
                assertEquals(0, loaded.getBlock(x, 61, z), "Air above at " + x + "," + z);
            }
        }

        // Verify metadata
        assertEquals(3, loaded.getBlockData(5, 60, 5));

        // Verify entities
        assertEquals(2, loaded.getEntities().size());
        assertEquals("Pig", loaded.getEntities().get(0).getId());
        assertEquals("Zombie", loaded.getEntities().get(1).getId());

        double[] pigPos = loaded.getEntities().get(0).getPos();
        assertEquals(162.5, pigPos[0], 0.001);
        assertEquals(61.0, pigPos[1], 0.001);
        assertEquals(-317.5, pigPos[2], 0.001);

        // Verify tile entities
        assertEquals(2, loaded.getTileEntities().size());
        AlphaTileEntity loadedChest = loaded.getTileEntityAt(168, 61, -320);
        assertNotNull(loadedChest);
        assertEquals("Chest", loadedChest.getId());

        // Verify flags
        assertTrue(loaded.isTerrainPopulated());
        assertEquals(54321L, loaded.getLastUpdate());
    }

    @Test
    void overwriteChunkPreservesLatestVersion() throws IOException {
        AlphaChunk v1 = new AlphaChunk(0, 0);
        v1.setBlock(0, 0, 0, 1);
        AlphaLevelFormat.saveChunk(tempDir, v1);

        // Overwrite with different data
        AlphaChunk v2 = new AlphaChunk(0, 0);
        v2.setBlock(0, 0, 0, 42);
        v2.setBlock(1, 1, 1, 7);
        AlphaLevelFormat.saveChunk(tempDir, v2);

        AlphaChunk loaded = AlphaLevelFormat.loadChunk(tempDir, 0, 0);
        assertNotNull(loaded);
        assertEquals(42, loaded.getBlock(0, 0, 0)); // v2's value
        assertEquals(7, loaded.getBlock(1, 1, 1));
    }

    @Test
    void levelDatAndSessionLockCreated() throws IOException {
        AlphaLevelFormat.saveLevelDat(tempDir, 12345L, 0, 65, 0, 1000L, System.currentTimeMillis());
        AlphaLevelFormat.writeSessionLock(tempDir);

        assertTrue(new File(tempDir, "level.dat").exists(), "level.dat should exist");
        assertTrue(new File(tempDir, "session.lock").exists(), "session.lock should exist");
        assertEquals(8, new File(tempDir, "session.lock").length(), "session.lock should be 8 bytes");
    }

    @Test
    void chunkWithLargeCoordinates() throws IOException {
        // Test with coordinates that exercise base-36 hashing edge cases
        AlphaChunk chunk = new AlphaChunk(1000, -500);
        chunk.setBlock(0, 64, 0, 3);
        AlphaLevelFormat.saveChunk(tempDir, chunk);

        AlphaChunk loaded = AlphaLevelFormat.loadChunk(tempDir, 1000, -500);
        assertNotNull(loaded);
        assertEquals(1000, loaded.getXPos());
        assertEquals(-500, loaded.getZPos());
        assertEquals(3, loaded.getBlock(0, 64, 0));
    }
}
