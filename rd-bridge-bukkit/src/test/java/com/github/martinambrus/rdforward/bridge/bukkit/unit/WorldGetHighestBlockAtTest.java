package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression test for HomeSpawnPlus spawn logic, which calls
 * {@code world.getHighestBlockAt(location)} to find a safe Y.
 */
class WorldGetHighestBlockAtTest {

    private static final int MAX_HEIGHT = 64;

    private static World stubWorld() {
        return new World() {
            @Override public org.bukkit.block.Block getBlockAt(int x, int y, int z) {
                return new StubBlock(x, y, z, y == 5 ? Material.STONE : Material.AIR);
            }
            @Override public int getMaxHeight() { return MAX_HEIGHT; }
            @Override public int getHighestBlockYAt(int x, int z) {
                for (int y = getMaxHeight() - 1; y >= 0; y--) {
                    Block b = getBlockAt(x, y, z);
                    if (b != null && b.getType() != Material.AIR) return y;
                }
                return 0;
            }
            @Override public String getName() { return "test"; }
            @Override public void setTime(long time) {}
            @Override public long getTime() { return 0; }
            @Override public boolean setBlockType(int x, int y, int z, Material type) { return false; }
        };
    }

    @Test
    void getHighestBlockAtCoordsReturnsBlockAtHighestY() {
        World w = stubWorld();
        Block b = w.getHighestBlockAt(0, 0);
        assertNotNull(b);
        assertEquals(5, b.getY());
        assertEquals(Material.STONE, b.getType());
    }

    @Test
    void getHighestBlockAtLocationOverloadReturnsBlock() {
        World w = stubWorld();
        Block b = w.getHighestBlockAt(new Location(w, 0, 0, 0));
        assertNotNull(b);
        assertEquals(5, b.getY());
    }

    @Test
    void getHighestBlockAtLocationNullReturnsNull() {
        World w = stubWorld();
        assertNull(w.getHighestBlockAt((Location) null));
    }

    @Test
    void getHighestBlockAtCoordsScansFromMaxHeightDown() {
        World w = new World() {
            @Override public org.bukkit.block.Block getBlockAt(int x, int y, int z) {
                // Stone at y=60 and y=10; highest should find 60
                return new StubBlock(x, y, z, y == 60 || y == 10 ? Material.STONE : Material.AIR);
            }
            @Override public int getMaxHeight() { return 64; }
            @Override public int getHighestBlockYAt(int x, int z) {
                for (int y = getMaxHeight() - 1; y >= 0; y--) {
                    Block b = getBlockAt(x, y, z);
                    if (b != null && b.getType() != Material.AIR) return y;
                }
                return 0;
            }
            @Override public String getName() { return "test"; }
            @Override public void setTime(long time) {}
            @Override public long getTime() { return 0; }
            @Override public boolean setBlockType(int x, int y, int z, Material type) { return false; }
        };
        Block b = w.getHighestBlockAt(0, 0);
        assertEquals(60, b.getY(), "must find the highest block, not just any block");
    }

    private static class StubBlock implements org.bukkit.block.Block {
        private final int x, y, z;
        private final Material type;
        StubBlock(int x, int y, int z, Material type) { this.x = x; this.y = y; this.z = z; this.type = type; }
        @Override public int getX() { return x; }
        @Override public int getY() { return y; }
        @Override public int getZ() { return z; }
        @Override public Material getType() { return type; }
        @Override public Location getLocation() { return new Location(null, x, y, z); }
        @Override public org.bukkit.block.Block getRelative(org.bukkit.block.BlockFace face) { return null; }
        @Override public org.bukkit.block.BlockState getState() { return null; }
        @Override public void setType(Material type) {}
        @Override public byte getData() { return 0; }
        @Override public void setData(byte data) {}
        @Override public World getWorld() { return null; }
        @Override public org.bukkit.Chunk getChunk() { return null; }
    }
}
