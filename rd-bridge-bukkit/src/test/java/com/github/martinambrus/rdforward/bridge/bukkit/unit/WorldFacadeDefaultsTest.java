package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World$Environment;
import org.bukkit.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Default methods added to the org.bukkit.World facade so plugins
 * compiled against pre-Flattening Bukkit (WorldEdit 5.6.1, EssentialsX
 * pre-2.x) and modern plugins reading getEnvironment / isChunkLoaded
 * survive without requiring every implementor to override them.
 */
class WorldFacadeDefaultsTest {

    @Test
    void getBlockTypeIdAtDelegatesThroughGetBlockAtAndGetType() {
        // World with a STONE block at (3, 4, 5) — getBlockTypeIdAt
        // should compose getBlockAt + getType + getId.
        StubWorld w = new StubWorld(Material.STONE);
        assertEquals(Material.STONE.getId(), w.getBlockTypeIdAt(3, 4, 5));
        assertEquals(3, w.lastReadX);
        assertEquals(4, w.lastReadY);
        assertEquals(5, w.lastReadZ);
    }

    @Test
    void getBlockTypeIdAtReturnsZeroWhenBlockAtIsNull() {
        // Out-of-bounds reads return null Block — getBlockTypeIdAt
        // must coalesce to AIR (id 0) rather than NPE.
        StubWorld w = new StubWorld(null);
        assertEquals(0, w.getBlockTypeIdAt(0, 0, 0));
    }

    @Test
    void getBlockTypeIdAtReturnsZeroWhenTypeIsNull() {
        // Synthetic Block with null Material (rare but possible from
        // misconfigured stubs). Must still not NPE.
        StubWorld w = new StubWorld(null) {
            @Override public Block getBlockAt(int x, int y, int z) {
                return new BukkitBlock(this, x, y, z, null);
            }
        };
        assertEquals(0, w.getBlockTypeIdAt(0, 0, 0));
    }

    @Test
    void blockGetDataReturnsZeroByDefault() {
        // RDForward does not model the data nibble. WE 5.6.1's
        // BukkitWorld.getBlockData reads it via {@code block.getData()}
        // (Block, not World), so the zero default must live on Block.
        // The legacy {@code World.getBlockData(int,int,int)} byte form
        // was removed — it conflicted with RegionAccessor's
        // {@code BlockData getBlockData(int,int,int)}, and WE never
        // actually called it.
        StubWorld w = new StubWorld(Material.STONE);
        assertEquals((byte) 0, w.getBlockAt(0, 0, 0).getData());
    }

    @Test
    void isChunkLoadedAlwaysTrue() {
        // Whole world held in memory; every chunk is "loaded" so WE's
        // BukkitWorld.checkLoadedChunk guard never bails.
        StubWorld w = new StubWorld(Material.STONE);
        assertSame(true, w.isChunkLoaded(0, 0));
        assertSame(true, w.isChunkLoaded(-1234, 5678));
    }

    @Test
    void getEnvironmentReturnsNormal() {
        // LP's BukkitPlayerCalculator NSME'd here; default must be NORMAL.
        StubWorld w = new StubWorld(Material.STONE);
        assertSame(World$Environment.NORMAL, w.getEnvironment());
    }

    /** Minimal World stub. Returns a synthetic BukkitBlock holding the
     *  configured Material from getBlockAt; tracks coordinates. */
    private static class StubWorld implements World {
        private final Material fixedType;
        int lastReadX, lastReadY, lastReadZ;

        StubWorld(Material fixedType) { this.fixedType = fixedType; }

        @Override public String getName() { return "stub"; }
        @Override public Block getBlockAt(int x, int y, int z) {
            lastReadX = x; lastReadY = y; lastReadZ = z;
            if (fixedType == null) return null;
            return new BukkitBlock(this, x, y, z, fixedType);
        }
        @Override public boolean setBlockType(int x, int y, int z, Material type) { return true; }
        @Override public int getMaxHeight() { return 256; }
        @Override public long getTime() { return 0; }
        @Override public void setTime(long time) {}
    }
}
