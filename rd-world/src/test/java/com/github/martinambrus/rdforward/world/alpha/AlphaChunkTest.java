package com.github.martinambrus.rdforward.world.alpha;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.InflaterInputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AlphaChunk: block storage, nibble arrays, height map,
 * entity/tile entity management, and protocol serialization.
 */
class AlphaChunkTest {

    @Test
    void newChunkIsAllAir() {
        AlphaChunk chunk = new AlphaChunk(0, 0);
        for (int x = 0; x < AlphaChunk.WIDTH; x++) {
            for (int z = 0; z < AlphaChunk.DEPTH; z++) {
                for (int y = 0; y < AlphaChunk.HEIGHT; y++) {
                    assertEquals(0, chunk.getBlock(x, y, z));
                }
            }
        }
    }

    @Test
    void setAndGetBlock() {
        AlphaChunk chunk = new AlphaChunk(3, -5);
        chunk.setBlock(5, 64, 8, 4); // Stone
        assertEquals(4, chunk.getBlock(5, 64, 8));
        assertEquals(0, chunk.getBlock(5, 65, 8)); // Neighbor is still air
    }

    @Test
    void blockIdWrapsUnsigned() {
        AlphaChunk chunk = new AlphaChunk(0, 0);
        chunk.setBlock(0, 0, 0, 200); // High block ID
        assertEquals(200, chunk.getBlock(0, 0, 0));
    }

    @Test
    void blockIndexYZXOrdering() {
        // index = y + (z * 128) + (x * 128 * 16)
        assertEquals(0, AlphaChunk.blockIndex(0, 0, 0));
        assertEquals(1, AlphaChunk.blockIndex(0, 1, 0));
        assertEquals(128, AlphaChunk.blockIndex(0, 0, 1));
        assertEquals(2048, AlphaChunk.blockIndex(1, 0, 0)); // 128 * 16
    }

    @Test
    void setAndGetBlockData() {
        AlphaChunk chunk = new AlphaChunk(0, 0);
        // Test even and odd nibble indices
        chunk.setBlockData(0, 0, 0, 7);
        chunk.setBlockData(0, 1, 0, 12);
        assertEquals(7, chunk.getBlockData(0, 0, 0));
        assertEquals(12, chunk.getBlockData(0, 1, 0));
    }

    @Test
    void nibbleDataClampedTo4Bits() {
        AlphaChunk chunk = new AlphaChunk(0, 0);
        chunk.setBlockData(0, 0, 0, 0xFF); // Only lower 4 bits should be kept
        assertEquals(0x0F, chunk.getBlockData(0, 0, 0));
    }

    @Test
    void heightMapUpdatesOnSetBlock() {
        AlphaChunk chunk = new AlphaChunk(0, 0);
        byte[] hm = chunk.getHeightMap();
        assertEquals(0, hm[0] & 0xFF); // Initially 0 (all air)

        chunk.setBlock(0, 10, 0, 1); // Place block at y=10
        assertEquals(11, hm[0] & 0xFF); // Height = y+1

        chunk.setBlock(0, 20, 0, 1); // Place higher block
        assertEquals(21, hm[0] & 0xFF);
    }

    @Test
    void heightMapRecalculatesOnRemoveTopBlock() {
        AlphaChunk chunk = new AlphaChunk(0, 0);
        chunk.setBlock(0, 5, 0, 1);
        chunk.setBlock(0, 10, 0, 1);
        byte[] hm = chunk.getHeightMap();
        assertEquals(11, hm[0] & 0xFF);

        chunk.setBlock(0, 10, 0, 0); // Remove top block
        assertEquals(6, hm[0] & 0xFF); // Falls back to y=5 -> height 6
    }

    @Test
    void heightMapGoesToZeroWhenColumnEmpty() {
        AlphaChunk chunk = new AlphaChunk(0, 0);
        chunk.setBlock(0, 0, 0, 1);
        byte[] hm = chunk.getHeightMap();
        assertEquals(1, hm[0] & 0xFF);

        chunk.setBlock(0, 0, 0, 0); // Remove only block
        assertEquals(0, hm[0] & 0xFF);
    }

    @Test
    void skylightInitializedToFullBrightness() {
        AlphaChunk chunk = new AlphaChunk(0, 0);
        byte[] sky = chunk.getSkyLight();
        for (byte b : sky) {
            assertEquals((byte) 0xFF, b); // Each byte = two nibbles of 15
        }
    }

    @Test
    void chunkPositionStored() {
        AlphaChunk chunk = new AlphaChunk(7, -3);
        assertEquals(7, chunk.getXPos());
        assertEquals(-3, chunk.getZPos());
    }

    @Test
    void terrainPopulatedFlag() {
        AlphaChunk chunk = new AlphaChunk(0, 0);
        assertFalse(chunk.isTerrainPopulated());
        chunk.setTerrainPopulated(true);
        assertTrue(chunk.isTerrainPopulated());
    }

    @Test
    void lastUpdateTimestamp() {
        AlphaChunk chunk = new AlphaChunk(0, 0);
        assertEquals(0, chunk.getLastUpdate());
        chunk.setLastUpdate(12345L);
        assertEquals(12345L, chunk.getLastUpdate());
    }

    // === Entity management ===

    @Test
    void addAndRetrieveEntities() {
        AlphaChunk chunk = new AlphaChunk(0, 0);
        assertTrue(chunk.getEntities().isEmpty());

        AlphaEntity pig = new AlphaEntity("Pig", 1.0, 2.0, 3.0);
        chunk.addEntity(pig);
        assertEquals(1, chunk.getEntities().size());
        assertEquals("Pig", chunk.getEntities().get(0).getId());
    }

    @Test
    void clearEntities() {
        AlphaChunk chunk = new AlphaChunk(0, 0);
        chunk.addEntity(new AlphaEntity("Pig", 0, 0, 0));
        chunk.addEntity(new AlphaEntity("Zombie", 1, 1, 1));
        assertEquals(2, chunk.getEntities().size());
        chunk.clearEntities();
        assertTrue(chunk.getEntities().isEmpty());
    }

    // === Tile entity management ===

    @Test
    void addAndFindTileEntity() {
        AlphaChunk chunk = new AlphaChunk(0, 0);
        AlphaTileEntity chest = new AlphaTileEntity("Chest", 5, 10, 8);
        chunk.addTileEntity(chest);

        AlphaTileEntity found = chunk.getTileEntityAt(5, 10, 8);
        assertNotNull(found);
        assertEquals("Chest", found.getId());

        assertNull(chunk.getTileEntityAt(0, 0, 0)); // Not found
    }

    @Test
    void removeTileEntity() {
        AlphaChunk chunk = new AlphaChunk(0, 0);
        chunk.addTileEntity(new AlphaTileEntity("Furnace", 1, 2, 3));
        assertTrue(chunk.removeTileEntityAt(1, 2, 3));
        assertFalse(chunk.removeTileEntityAt(1, 2, 3)); // Already removed
        assertNull(chunk.getTileEntityAt(1, 2, 3));
    }

    // === Serialization ===

    @Test
    void serializeForAlphaProtocolProducesValidZlib() throws IOException {
        AlphaChunk chunk = new AlphaChunk(0, 0);
        chunk.setBlock(8, 64, 8, 1); // Place one stone block

        byte[] compressed = chunk.serializeForAlphaProtocol();
        assertTrue(compressed.length > 0);

        // Decompress and verify size: blocks(32768) + data(16384) + blockLight(16384) + skyLight(16384) = 81920
        InflaterInputStream iis = new InflaterInputStream(new ByteArrayInputStream(compressed));
        byte[] decompressed = iis.readAllBytes();
        assertEquals(81920, decompressed.length);

        // Verify the stone block is at the correct index in the decompressed data
        int index = AlphaChunk.blockIndex(8, 64, 8);
        assertEquals(1, decompressed[index] & 0xFF);
    }

    @Test
    void arraySizesCorrect() {
        AlphaChunk chunk = new AlphaChunk(0, 0);
        assertEquals(AlphaChunk.BLOCK_COUNT, chunk.getBlocks().length);
        assertEquals(AlphaChunk.NIBBLE_COUNT, chunk.getData().length);
        assertEquals(AlphaChunk.NIBBLE_COUNT, chunk.getBlockLight().length);
        assertEquals(AlphaChunk.NIBBLE_COUNT, chunk.getSkyLight().length);
        assertEquals(AlphaChunk.WIDTH * AlphaChunk.DEPTH, chunk.getHeightMap().length);
    }
}
