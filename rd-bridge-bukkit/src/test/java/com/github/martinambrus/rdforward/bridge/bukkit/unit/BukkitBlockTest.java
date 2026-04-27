package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World$Environment;
import org.bukkit.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Block was converted from a class to an interface in this iteration
 * (WE 5.6.1's invokeinterface raised IncompatibleClassChangeError on a
 * class). BukkitBlock is the concrete impl; setType must forward
 * through World.setBlockType so the BlockPolicy chokepoint at
 * ServerWorld.setBlock(byte) runs regardless of who originated the
 * write.
 */
class BukkitBlockTest {

    @Test
    void instanceImplementsTheBlockInterface() {
        BukkitBlock b = new BukkitBlock(null, 1, 2, 3, Material.STONE);
        assertTrue(b instanceof Block, "BukkitBlock must implement org.bukkit.block.Block");
    }

    @Test
    void coordsAndTypeAreSnapshotted() {
        BukkitBlock b = new BukkitBlock(null, 7, 8, 9, Material.OAK_PLANKS);
        assertEquals(7, b.getX());
        assertEquals(8, b.getY());
        assertEquals(9, b.getZ());
        assertSame(Material.OAK_PLANKS, b.getType());
    }

    @Test
    void getLocationCarriesCoordsAndWorld() {
        StubWorld w = new StubWorld();
        BukkitBlock b = new BukkitBlock(w, 4, 5, 6, Material.STONE);
        Location loc = b.getLocation();
        assertNotNull(loc);
        assertSame(w, loc.getWorld());
        assertEquals(4.0, loc.getX(), 1e-9);
        assertEquals(5.0, loc.getY(), 1e-9);
        assertEquals(6.0, loc.getZ(), 1e-9);
    }

    @Test
    void setTypeForwardsToEnclosingWorld() {
        StubWorld w = new StubWorld();
        BukkitBlock b = new BukkitBlock(w, 1, 2, 3, Material.AIR);

        b.setType(Material.STONE);

        assertSame(Material.STONE, b.getType());
        assertEquals(1, w.calls, "world.setBlockType must run once on setType");
        assertEquals(1, w.lastX);
        assertEquals(2, w.lastY);
        assertEquals(3, w.lastZ);
        assertSame(Material.STONE, w.lastType);
    }

    @Test
    void setTypeWithNullWorldDoesNotCrash() {
        // Some test fixtures wrap a Block without a backing world (e.g.
        // event-construction tests). The forwarding must be a guarded
        // no-op rather than NPE.
        BukkitBlock b = new BukkitBlock(null, 0, 0, 0, Material.AIR);
        b.setType(Material.COBBLESTONE);
        assertSame(Material.COBBLESTONE, b.getType());
    }

    @Test
    void isEmptyDelegatesToType() {
        BukkitBlock air = new BukkitBlock(null, 0, 0, 0, Material.AIR);
        BukkitBlock stone = new BukkitBlock(null, 0, 0, 0, Material.STONE);
        assertTrue(air.isEmpty());
        assertTrue(!stone.isEmpty());
    }

    @Test
    void getTypeIdMirrorsMaterialLegacyId() {
        BukkitBlock b = new BukkitBlock(null, 0, 0, 0, Material.COBBLESTONE);
        assertEquals(Material.COBBLESTONE.getId(), b.getTypeId());
    }

    @Test
    void getDataReturnsZeroByDefault() {
        BukkitBlock b = new BukkitBlock(null, 0, 0, 0, Material.STONE);
        assertEquals((byte) 0, b.getData());
    }

    /** Minimal World stub — counts setBlockType calls. */
    private static final class StubWorld implements World {
        int calls;
        int lastX, lastY, lastZ;
        Material lastType;

        @Override public String getName() { return "stub"; }
        @Override public Block getBlockAt(int x, int y, int z) { return null; }
        @Override public boolean setBlockType(int x, int y, int z, Material type) {
            calls++;
            lastX = x; lastY = y; lastZ = z; lastType = type;
            return true;
        }
        @Override public int getMaxHeight() { return 256; }
        @Override public long getTime() { return 0; }
        @Override public void setTime(long time) {}
        @Override public World$Environment getEnvironment() { return World$Environment.NORMAL; }
    }
}
