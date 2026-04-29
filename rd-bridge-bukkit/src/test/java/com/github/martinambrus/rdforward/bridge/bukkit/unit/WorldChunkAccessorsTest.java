package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBlock;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitChunk;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Default methods on {@link World} added so CoreProtect's
 * {@code RollbackProcessor.processChunk} can drive the rollback iteration
 * loop — {@code getMinHeight} for the bottom Y, chunk lookup via
 * {@link Location} for {@code isChunkLoaded}, and {@code getBlockAt(loc)}
 * for safe-teleport probing.
 */
class WorldChunkAccessorsTest {

    @Test
    void getMinHeightDefaultsToZero() {
        // RDForward's worlds are 0-based — Y floor is 0, matching Alpha
        // shape. CoreProtect's Bukkit_v1_17.getMinHeight calls this to
        // know where to begin its bottom-up iteration.
        StubWorld w = new StubWorld();
        assertEquals(0, w.getMinHeight());
    }

    @Test
    void getBlockAtLocationForwardsToIntCoords() {
        StubWorld w = new StubWorld();
        Location loc = new Location(w, 4.7d, 5.2d, -3.9d);
        Block b = w.getBlockAt(loc);
        assertNotNull(b);
        // Floor semantics — 4.7 -> 4, 5.2 -> 5, -3.9 -> -4.
        assertEquals(4, w.lastReadX);
        assertEquals(5, w.lastReadY);
        assertEquals(-4, w.lastReadZ);
    }

    @Test
    void getBlockAtNullLocationReturnsNullWithoutNpe() {
        // Defensive — plugin must not crash if it passes a null Location
        // (CoreProtect does this once during inspector mode setup if a
        // saved click never happened).
        StubWorld w = new StubWorld();
        assertNull(w.getBlockAt((Location) null));
    }

    @Test
    void isChunkLoadedChunkAcceptsAnyNonNull() {
        // Whole world held in memory — every chunk is "loaded".
        StubWorld w = new StubWorld();
        Chunk c = new BukkitChunk(w, 0, 0);
        assertTrue(w.isChunkLoaded(c));
    }

    @Test
    void isChunkLoadedNullChunkReturnsFalse() {
        // Defensive — null chunk shouldn't claim "loaded".
        assertFalse(new StubWorld().isChunkLoaded((Chunk) null));
    }

    @Test
    void getChunkAtLocationDerivesChunkCoordsFromBlockCoordsShiftedByFour() {
        StubWorld w = new StubWorld();
        Location loc = new Location(w, 35d, 64d, -17d);
        Chunk c = w.getChunkAt(loc);
        assertNotNull(c);
        assertSame(w, c.getWorld());
        // 35 >> 4 = 2, -17 >> 4 = -2
        assertEquals(2, c.getX());
        assertEquals(-2, c.getZ());
    }

    @Test
    void getChunkAtNullLocationReturnsNull() {
        assertNull(new StubWorld().getChunkAt((Location) null));
    }

    @Test
    void getChunkAtIntCoordsReturnsChunkAtRequestedKey() {
        StubWorld w = new StubWorld();
        Chunk c = w.getChunkAt(7, -3);
        assertNotNull(c);
        assertEquals(7, c.getX());
        assertEquals(-3, c.getZ());
        assertSame(w, c.getWorld());
    }

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
