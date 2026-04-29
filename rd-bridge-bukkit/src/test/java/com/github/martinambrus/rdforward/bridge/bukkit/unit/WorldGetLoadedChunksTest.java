package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Pins the empty-array contract on {@link World#getLoadedChunks()}.
 * Pre-fix, the method did not exist and Essentials's {@code /gc}
 * threw {@link NoSuchMethodError}. RDForward keeps the whole world
 * resident in memory rather than streaming chunks, so an empty
 * snapshot is the honest answer — {@code /gc} reports zero loaded
 * chunks instead of crashing.
 */
class WorldGetLoadedChunksTest {

    @Test
    void getLoadedChunksReturnsEmptyArray() {
        World w = new MinimalWorld();
        Chunk[] chunks = w.getLoadedChunks();
        assertNotNull(chunks);
        assertEquals(0, chunks.length);
    }

    /** Minimal World stub — only the six non-default abstract methods
     *  are implemented; {@code getLoadedChunks} flows through the
     *  interface default added by the fix. */
    private static final class MinimalWorld implements World {
        @Override public String getName() { return "stub"; }
        @Override public Block getBlockAt(int x, int y, int z) { return null; }
        @Override public boolean setBlockType(int x, int y, int z, Material type) { return false; }
        @Override public int getMaxHeight() { return 256; }
        @Override public long getTime() { return 0L; }
        @Override public void setTime(long time) {}
    }
}
