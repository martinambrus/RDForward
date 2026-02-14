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
    void skylightZeroBeforeGeneration() {
        AlphaChunk chunk = new AlphaChunk(0, 0);
        byte[] sky = chunk.getSkyLight();
        for (byte b : sky) {
            assertEquals((byte) 0x00, b);
        }
    }

    @Test
    void generateSkylightMapSetsCorrectValues() {
        AlphaChunk chunk = new AlphaChunk(0, 0);
        // Place a solid block at y=42 in column (0,0)
        chunk.setBlock(0, 42, 0, 1);
        chunk.generateSkylightMap();

        byte[] sky = chunk.getSkyLight();
        // Above the block (y=43): skylight 15
        assertEquals(15, getNibble(sky, AlphaChunk.blockIndex(0, 43, 0)));
        assertEquals(15, getNibble(sky, AlphaChunk.blockIndex(0, 127, 0)));
        // The solid block at y=42 is opaque: skylight 0
        assertEquals(0, getNibble(sky, AlphaChunk.blockIndex(0, 42, 0)));
        // Underground air at y=0 receives BFS-propagated light from adjacent
        // all-air column (1,0) at y=0 (light=15), so it gets 14
        assertEquals(14, getNibble(sky, AlphaChunk.blockIndex(0, 0, 0)));
        // Empty column (1,0) with heightMap=0: all skylight 15
        assertEquals(15, getNibble(sky, AlphaChunk.blockIndex(1, 0, 0)));
        assertEquals(15, getNibble(sky, AlphaChunk.blockIndex(1, 127, 0)));
    }

    @Test
    void skylightBfsPropagatesIntoUndergroundAir() {
        // Build a surface with an underground air pocket next to an open column.
        //
        //   Column (0,0): solid 0-9, air 10+  (heightMap = 10)
        //   Column (1,0): solid 0-9 EXCEPT air at y=8,9  (heightMap = 10)
        //
        // The air pocket at (1,0) y=8,9 is underground but adjacent to the open
        // column (0,0) at y=8,9 which is also underground and solid. Wait, that
        // doesn't work — (0,0) is solid at y=8,9. Let me redesign:
        //
        //   Column (0,0): solid 0-4, air 5+   (heightMap = 5)
        //   Column (1,0): solid 0-9, air at y=5 carved out  (heightMap = 10)
        //
        // Actually simpler: tall wall next to short column with a cave opening.
        //
        //   Column (0,0): all air (heightMap = 0), sky=15 everywhere
        //   Column (1,0): solid 0-9 (heightMap = 10)
        //   At (1,0) y=5, carve air — this creates an underground air pocket
        //   that should receive light from (0,0) y=5 via BFS.
        AlphaChunk chunk = new AlphaChunk(0, 0);

        // Build a solid column at x=1, z=0, y=0..9
        for (int y = 0; y < 10; y++) {
            chunk.setBlock(1, y, 0, 1); // Stone
        }
        // Carve out air at (1,0) y=5 — underground cave opening facing column (0,0)
        chunk.setBlock(1, 5, 0, 0); // Air

        chunk.generateSkylightMap();

        byte[] sky = chunk.getSkyLight();

        // Column (0,0) at y=5 is open sky: light = 15
        assertEquals(15, getNibble(sky, AlphaChunk.blockIndex(0, 5, 0)));

        // The carved air at (1,0) y=5 should receive light 14 via BFS from (0,0) y=5
        assertEquals(14, getNibble(sky, AlphaChunk.blockIndex(1, 5, 0)));

        // Solid blocks at (1,0) y=4 and y=6 should remain 0 (opaque)
        assertEquals(0, getNibble(sky, AlphaChunk.blockIndex(1, 4, 0)));
        assertEquals(0, getNibble(sky, AlphaChunk.blockIndex(1, 6, 0)));
    }

    @Test
    void skylightBfsPropagatesThroughTunnel() {
        // Build a fully enclosed tunnel with one entrance at x=0.
        // Solid box from x=1..4, z=0..2, y=0..9, with air carved at y=5, z=1, x=1..3.
        // x=4 is the back wall (sealed), x=0 is open sky (entrance).
        AlphaChunk chunk = new AlphaChunk(0, 0);

        for (int x = 1; x <= 4; x++) {
            for (int z = 0; z <= 2; z++) {
                for (int y = 0; y < 10; y++) {
                    chunk.setBlock(x, y, z, 1); // Stone
                }
            }
        }
        // Carve tunnel interior: y=5, z=1, x=1..3 (x=4 stays solid = back wall)
        for (int x = 1; x <= 3; x++) {
            chunk.setBlock(x, 5, 1, 0); // Air
        }

        chunk.generateSkylightMap();
        byte[] sky = chunk.getSkyLight();

        // Light should decrease by 1 per block into the tunnel from x=0
        assertEquals(15, getNibble(sky, AlphaChunk.blockIndex(0, 5, 1))); // Open sky
        assertEquals(14, getNibble(sky, AlphaChunk.blockIndex(1, 5, 1))); // 1 block in
        assertEquals(13, getNibble(sky, AlphaChunk.blockIndex(2, 5, 1))); // 2 blocks in
        assertEquals(12, getNibble(sky, AlphaChunk.blockIndex(3, 5, 1))); // 3 blocks in (dead end)
    }

    @Test
    void skylightBfsReducedByWaterOpacity() {
        // Water has opacity 3, so light decreases by 3 per water block
        AlphaChunk chunk = new AlphaChunk(0, 0);

        // Solid column at x=1 with water at y=5
        for (int y = 0; y < 10; y++) {
            chunk.setBlock(1, y, 0, 1); // Stone
        }
        chunk.setBlock(1, 5, 0, 8); // Water (block ID 8, opacity 3)

        chunk.generateSkylightMap();
        byte[] sky = chunk.getSkyLight();

        // (0,0) y=5 is open sky: 15
        assertEquals(15, getNibble(sky, AlphaChunk.blockIndex(0, 5, 0)));
        // Water at (1,0) y=5 receives 15 - 3 = 12
        assertEquals(12, getNibble(sky, AlphaChunk.blockIndex(1, 5, 0)));
    }

    @Test
    void skylightBfsReducedByLeafOpacity() {
        // Leaves have opacity 1, so light decreases by 1 per leaf block
        AlphaChunk chunk = new AlphaChunk(0, 0);

        // Solid column at x=1 with leaves at y=5
        for (int y = 0; y < 10; y++) {
            chunk.setBlock(1, y, 0, 1); // Stone
        }
        chunk.setBlock(1, 5, 0, 18); // Leaves (block ID 18, opacity 1)

        chunk.generateSkylightMap();
        byte[] sky = chunk.getSkyLight();

        assertEquals(15, getNibble(sky, AlphaChunk.blockIndex(0, 5, 0)));
        // Leaves at (1,0) y=5: 15 - max(1, 1) = 14
        assertEquals(14, getNibble(sky, AlphaChunk.blockIndex(1, 5, 0)));
    }

    @Test
    void skylightBfsDoesNotEnterOpaqueBlocks() {
        // Light should not propagate into solid blocks
        AlphaChunk chunk = new AlphaChunk(0, 0);

        // Place a single solid block at (1,0,0) y=0, surrounded by air
        chunk.setBlock(1, 0, 0, 1); // Stone

        chunk.generateSkylightMap();
        byte[] sky = chunk.getSkyLight();

        // The stone block should have skylight 0
        assertEquals(0, getNibble(sky, AlphaChunk.blockIndex(1, 0, 0)));
        // Adjacent air blocks should have skylight 15 (all open sky, heightMap=1 for that column)
        // Actually (1,0,0) has heightMap=1, so y=1+ is sky. The BFS might not reach y=0 at (0,0,0)
        // since (0,0,0) is already sky-lit at 15 from column sweep.
        assertEquals(15, getNibble(sky, AlphaChunk.blockIndex(0, 0, 0)));
    }

    @Test
    void getLightOpacityValues() {
        assertEquals(0, AlphaChunk.getLightOpacity(0));   // Air
        assertEquals(15, AlphaChunk.getLightOpacity(1));   // Stone
        assertEquals(3, AlphaChunk.getLightOpacity(8));    // Water
        assertEquals(1, AlphaChunk.getLightOpacity(18));   // Leaves
        assertEquals(0, AlphaChunk.getLightOpacity(20));   // Glass
        assertEquals(15, AlphaChunk.getLightOpacity(4));   // Cobblestone
        assertEquals(0, AlphaChunk.getLightOpacity(50));   // Torch
    }

    private static int getNibble(byte[] array, int blockIndex) {
        int byteIndex = blockIndex / 2;
        if ((blockIndex & 1) == 0) {
            return array[byteIndex] & 0x0F;
        } else {
            return (array[byteIndex] >> 4) & 0x0F;
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
