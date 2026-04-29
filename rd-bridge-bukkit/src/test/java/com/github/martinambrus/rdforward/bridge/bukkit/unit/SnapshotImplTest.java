package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBlock;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBlockData;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBlockState;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitChunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Snapshot impls returned to plugins from the Block surface.
 * {@link BukkitBlockData}, {@link BukkitBlockState}, {@link BukkitChunk}
 * are all read-only from the plugin's perspective — RDForward synthesises
 * them on demand rather than tracking per-block state.
 */
class SnapshotImplTest {

    /* ---- BukkitBlockData ---- */

    @Test
    void blockDataNullMaterialDefaultsToAir() {
        BlockData d = new BukkitBlockData(null);
        assertSame(Material.AIR, d.getMaterial());
        assertEquals("minecraft:air", d.getAsString());
    }

    @Test
    void blockDataAsStringFormatMatchesMinecraftNamespace() {
        // CoreProtect persists this exact string in its audit log
        // (no property bracket suffix because RDForward has no per-block
        // state model).
        assertEquals("minecraft:stone", new BukkitBlockData(Material.STONE).getAsString());
        assertEquals("minecraft:dirt", new BukkitBlockData(Material.DIRT).getAsString());
    }

    @Test
    void blockDataAsStringHideUnspecifiedSameAsDefault() {
        BlockData d = new BukkitBlockData(Material.STONE);
        assertEquals(d.getAsString(), d.getAsString(true));
        assertEquals(d.getAsString(), d.getAsString(false));
    }

    @Test
    void blockDataMatchesByMaterial() {
        BlockData a = new BukkitBlockData(Material.STONE);
        BlockData b = new BukkitBlockData(Material.STONE);
        BlockData c = new BukkitBlockData(Material.DIRT);
        assertTrue(a.matches(b));
        assertFalse(a.matches(c));
        assertFalse(a.matches(null));
    }

    @Test
    void blockDataReplaceableOnlyForAir() {
        assertTrue(new BukkitBlockData(Material.AIR).isReplaceable());
        assertFalse(new BukkitBlockData(Material.STONE).isReplaceable());
    }

    @Test
    void blockDataCloneCarriesMaterial() {
        BlockData a = new BukkitBlockData(Material.STONE);
        BlockData copy = a.clone();
        assertNotNull(copy);
        assertSame(Material.STONE, copy.getMaterial());
    }

    /* ---- BukkitBlockState ---- */

    @Test
    void blockStatePreservesAllSnapshotFields() {
        StubWorld w = new StubWorld();
        BukkitBlockState st = new BukkitBlockState(w, 1, 2, 3, Material.STONE);
        assertSame(w, st.getWorld());
        assertEquals(1, st.getX());
        assertEquals(2, st.getY());
        assertEquals(3, st.getZ());
        assertSame(Material.STONE, st.getType());
    }

    @Test
    void blockStateNullTypeNormalizesToAir() {
        BukkitBlockState st = new BukkitBlockState(new StubWorld(), 0, 0, 0, null);
        assertSame(Material.AIR, st.getType());
    }

    @Test
    void blockStateGetBlockDataMirrorsMaterial() {
        BukkitBlockState st = new BukkitBlockState(new StubWorld(), 0, 0, 0, Material.DIRT);
        BlockData d = st.getBlockData();
        assertNotNull(d);
        assertSame(Material.DIRT, d.getMaterial());
    }

    @Test
    void blockStateGetBlockReturnsBukkitBlockAtSameCoords() {
        StubWorld w = new StubWorld();
        BukkitBlockState st = new BukkitBlockState(w, 5, 6, 7, Material.STONE);
        Block b = st.getBlock();
        assertNotNull(b);
        assertSame(w, b.getWorld());
        assertEquals(5, b.getX());
        assertEquals(6, b.getY());
        assertEquals(7, b.getZ());
        assertSame(Material.STONE, b.getType());
    }

    @Test
    void blockStateIsCollidableFalseForAirTrueForSolid() {
        assertFalse(new BukkitBlockState(new StubWorld(), 0, 0, 0, Material.AIR).isCollidable());
        assertTrue(new BukkitBlockState(new StubWorld(), 0, 0, 0, Material.STONE).isCollidable());
    }

    /* ---- BukkitChunk ---- */

    @Test
    void chunkCarriesItsCoordsAndWorld() {
        StubWorld w = new StubWorld();
        BukkitChunk c = new BukkitChunk(w, 4, -7);
        assertSame(w, c.getWorld());
        assertEquals(4, c.getX());
        assertEquals(-7, c.getZ());
    }

    @Test
    void chunkLoadedAndGeneratedAlwaysTrue() {
        // RDForward holds the whole world in memory — every chunk is
        // both loaded and generated, so CoreProtect's load-guard checks
        // never bail out.
        BukkitChunk c = new BukkitChunk(new StubWorld(), 0, 0);
        assertTrue(c.isLoaded());
        assertTrue(c.isGenerated());
        assertTrue(c.isEntitiesLoaded());
    }

    @Test
    void chunkGetEntitiesEmptyArray() {
        // CoreProtect iterates this in rollback to look for entity
        // changes; RDForward has no per-chunk entity index, so the
        // array must be empty (not null) to keep the for-loop safe.
        Entity[] arr = new BukkitChunk(new StubWorld(), 0, 0).getEntities();
        assertNotNull(arr);
        assertEquals(0, arr.length);
    }

    @Test
    void chunkGetBlockComposesChunkOriginPlusLocalOffset() {
        // Chunk (2, -2) origin = (32, -32). Local (5, 64, 7) -> world (37, 64, -25).
        StubWorld w = new StubWorld();
        BukkitChunk c = new BukkitChunk(w, 2, -2);
        Block b = c.getBlock(5, 64, 7);
        assertEquals(37, w.lastReadX);
        assertEquals(64, w.lastReadY);
        assertEquals(-25, w.lastReadZ);
    }

    @Test
    void chunkGetBlockNullWorldReturnsNullWithoutNpe() {
        // Defensive — never NPE if the synthesised chunk has no world
        // (caller typically passes the block's world but might not).
        assertSame(null, new BukkitChunk(null, 0, 0).getBlock(0, 0, 0));
    }

    @Test
    void chunkPersistentDataContainerIsNonNullStub() {
        // PersistentDataHolder method on Chunk; CoreProtect doesn't
        // exercise it but plugins that walk chunks via reflection will.
        assertNotNull(new BukkitChunk(new StubWorld(), 0, 0).getPersistentDataContainer());
    }

    /* ---- shared stub world ---- */

    private static final class StubWorld implements World {
        int lastReadX, lastReadY, lastReadZ;

        @Override public String getName() { return "stub"; }
        @Override public Block getBlockAt(int x, int y, int z) {
            this.lastReadX = x; this.lastReadY = y; this.lastReadZ = z;
            return new BukkitBlock(this, x, y, z, Material.AIR);
        }
        @Override public boolean setBlockType(int x, int y, int z, Material type) { return true; }
        @Override public int getMaxHeight() { return 256; }
        @Override public long getTime() { return 0; }
        @Override public void setTime(long time) {}
    }
}
