package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBlock;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBlockData;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBlockState;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitChunk;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Default methods on {@link Block} added so CoreProtect (and similar
 * modern audit plugins) can navigate from a block back to its enclosing
 * chunk, snapshot its state, and apply rollback writes through the
 * {@code setBlockData} signature without {@link NoSuchMethodError}s.
 */
class BlockDefaultsTest {

    @Test
    void getStateReturnsSnapshotMatchingBlockCoords() {
        StubWorld w = new StubWorld();
        Block b = new BukkitBlock(w, 7, 8, 9, Material.STONE);
        BlockState st = b.getState();
        assertNotNull(st);
        assertSame(w, st.getWorld());
        assertEquals(7, st.getX());
        assertEquals(8, st.getY());
        assertEquals(9, st.getZ());
        assertSame(Material.STONE, st.getType());
    }

    @Test
    void getBlockDataWrapsMaterialInBukkitBlockData() {
        Block b = new BukkitBlock(new StubWorld(), 0, 0, 0, Material.DIRT);
        BlockData data = b.getBlockData();
        assertNotNull(data);
        assertSame(Material.DIRT, data.getMaterial());
        // CoreProtect persists this string in its audit log — must
        // include the minecraft: namespace prefix.
        assertEquals("minecraft:dirt", data.getAsString());
    }

    @Test
    void getRelativeSelfReturnsBlockAtSameCoordsCarryingWorld() {
        // BlockFace.SELF has modX/Y/Z = 0; getRelative must return a
        // Block at the same coordinates that still carries the source
        // block's world (so downstream getWorld() lookups work).
        StubWorld w = new StubWorld();
        Block b = new BukkitBlock(w, 4, 5, 6, Material.STONE);
        Block rel = b.getRelative(BlockFace.SELF);
        assertNotNull(rel);
        assertSame(w, rel.getWorld());
        assertEquals(4, rel.getX());
        assertEquals(5, rel.getY());
        assertEquals(6, rel.getZ());
    }

    @Test
    void getRelativeNullFaceReturnsThisBlock() {
        // Defensive: we early-return `this` for null face (real Bukkit
        // throws NPE — RDForward prefers crash-free behaviour for
        // plugins that pass an uninitialised face during cleanup).
        Block b = new BukkitBlock(new StubWorld(), 1, 2, 3, Material.AIR);
        assertSame(b, b.getRelative(null));
        assertSame(b, b.getRelative(null, 5));
    }

    @Test
    void getRelativeWithDistanceComposesFaceModsTimesDistance() {
        // Even with stub BlockFace.getModX/Y/Z returning 0, the
        // computation "distance * mod" must hold — verify formula
        // executes without NPE when distance is non-zero.
        Block b = new BukkitBlock(new StubWorld(), 10, 11, 12, Material.STONE);
        Block rel = b.getRelative(BlockFace.NORTH, 3);
        assertEquals(10, rel.getX());
        assertEquals(11, rel.getY());
        assertEquals(12, rel.getZ());
    }

    @Test
    void getChunkReturnsBukkitChunkAtWorldCoordsShiftedByFour() {
        // CoreProtect's RollbackProcessor.processChunk reads block
        // coords back to the chunk via getChunk() then queries
        // chunk.getX/getZ. Block at world (35, 64, -17) -> chunk (2, -2).
        StubWorld w = new StubWorld();
        Block b = new BukkitBlock(w, 35, 64, -17, Material.STONE);
        Chunk c = b.getChunk();
        assertNotNull(c);
        assertSame(w, c.getWorld());
        assertEquals(35 >> 4, c.getX());
        assertEquals(-17 >> 4, c.getZ());
    }

    @Test
    void isPassableTrueForAirAndNullType() {
        // Pathfinding-style passable check — only AIR and missing
        // type are passable in RDForward's collision-less world model.
        assertTrue(new BukkitBlock(new StubWorld(), 0, 0, 0, Material.AIR).isPassable());
        assertTrue(new BukkitBlock(new StubWorld(), 0, 0, 0, null).isPassable());
    }

    @Test
    void isPassableFalseForSolidBlocks() {
        assertFalse(new BukkitBlock(new StubWorld(), 0, 0, 0, Material.STONE).isPassable());
        assertFalse(new BukkitBlock(new StubWorld(), 0, 0, 0, Material.DIRT).isPassable());
    }

    @Test
    void setBlockDataForwardsMaterialThroughSetType() {
        // CoreProtect's BlockUtils.setTypeAndData rolls back via
        // block.setBlockData(data, false) — the underlying Material
        // must reach world.setBlockType so the rd-api world updates.
        StubWorld w = new StubWorld();
        Block b = new BukkitBlock(w, 1, 2, 3, Material.AIR);
        b.setBlockData(new BukkitBlockData(Material.STONE), false);
        assertSame(Material.STONE, w.lastSetType);
        assertEquals(1, w.lastSetX);
        assertEquals(2, w.lastSetY);
        assertEquals(3, w.lastSetZ);
    }

    @Test
    void setBlockDataNullDataResetsToAir() {
        // Defensive — a null BlockData should not NPE; reset to AIR
        // so plugins that pass an uninitialised data carrier still
        // produce a consistent world state.
        StubWorld w = new StubWorld();
        Block b = new BukkitBlock(w, 0, 0, 0, Material.STONE);
        b.setBlockData(null, true);
        assertSame(Material.AIR, w.lastSetType);
    }

    @Test
    void setBlockDataSingleArgDelegatesToTwoArgWithPhysicsTrue() {
        StubWorld w = new StubWorld();
        Block b = new BukkitBlock(w, 5, 5, 5, Material.AIR);
        b.setBlockData(new BukkitBlockData(Material.DIRT));
        assertSame(Material.DIRT, w.lastSetType);
    }

    /** Minimal World stub recording last setBlockType call. */
    private static final class StubWorld implements World {
        Material lastSetType;
        int lastSetX, lastSetY, lastSetZ;

        @Override public String getName() { return "stub"; }
        @Override public Block getBlockAt(int x, int y, int z) { return null; }
        @Override public boolean setBlockType(int x, int y, int z, Material type) {
            this.lastSetX = x; this.lastSetY = y; this.lastSetZ = z;
            this.lastSetType = type;
            return true;
        }
        @Override public int getMaxHeight() { return 256; }
        @Override public long getTime() { return 0; }
        @Override public void setTime(long time) {}
    }
}
