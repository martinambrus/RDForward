package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * EssentialsSpawn's {@code SpawnStorage.setSpawn} calls
 * {@code world.setSpawnLocation(x, y, z)} after stashing the per-group
 * spawn in its own {@code spawns.yml} — purely to keep the world's
 * vanilla spawn in sync. Without these default no-op overloads on
 * {@link World}, {@code /setspawn} crashes with NoSuchMethodError on
 * every invocation. The contract verified here is "method exists,
 * returns false, does not throw" for every overload Essentials may
 * pick.
 */
class WorldSetSpawnLocationDefaultsTest {

    @Test
    void intOverloadReturnsFalse() {
        World w = new BareWorld();
        assertFalse(w.setSpawnLocation(10, 20, 30),
                "default returns false to indicate spawn unchanged");
    }

    @Test
    void intAngleOverloadReturnsFalse() {
        World w = new BareWorld();
        assertFalse(w.setSpawnLocation(10, 20, 30, 90.0f),
                "default returns false to indicate spawn unchanged");
    }

    @Test
    void locationOverloadReturnsFalse() {
        World w = new BareWorld();
        Location anywhere = new Location(w, 1, 2, 3);
        assertFalse(w.setSpawnLocation(anywhere),
                "default returns false to indicate spawn unchanged");
    }

    @Test
    void locationOverloadAcceptsNullWithoutThrowing() {
        // Defensive: Essentials passes a non-null location, but the
        // facade should not throw on null either — real Bukkit's
        // implementation only NPEs when it tries to dereference the
        // location's coordinates, which the no-op default never does.
        World w = new BareWorld();
        assertFalse(w.setSpawnLocation((Location) null));
    }

    /** Minimal {@link World} stub that only implements the abstract
     *  surface needed for the spawn-overload defaults to be reachable.
     *  Anything else throws so tests that accidentally call into it
     *  surface immediately. */
    private static final class BareWorld implements World {
        @Override public String getName() { return "bare"; }
        @Override public Block getBlockAt(int x, int y, int z) { return null; }
        @Override public boolean setBlockType(int x, int y, int z, org.bukkit.Material type) { return false; }
        @Override public int getMaxHeight() { return 256; }
        @Override public long getTime() { return 0; }
        @Override public void setTime(long time) { /* no-op */ }
    }
}
