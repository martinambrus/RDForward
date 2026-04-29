package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBlock;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World$Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Real Bukkit's {@code BlockBreakEvent(Block, Player)} ctor is the form
 * Essentials's {@code Commandbreak} reflectively constructs. The
 * RDForward stub kept its earlier 5-arg form for the broadcast pipeline
 * but added the canonical (Block, Player) ctor too. This test pins both
 * shapes — the canonical ctor must record the block and lift x/y/z from
 * it, the legacy 5-arg ctor must keep working with a null block.
 */
class BlockBreakEventCanonicalCtorTest {

    @Test
    void canonicalCtorStoresBlockAndCoords() {
        BukkitBlock block = new BukkitBlock(null, 7, 8, 9, Material.STONE);
        BlockBreakEvent ev = new BlockBreakEvent(block, (Player) null);

        assertSame(block, ev.getBlock());
        assertEquals(7, ev.getX());
        assertEquals(8, ev.getY());
        assertEquals(9, ev.getZ());
    }

    @Test
    void canonicalCtorWithNullBlockZeroesCoords() {
        BlockBreakEvent ev = new BlockBreakEvent((Block) null, (Player) null);

        assertNull(ev.getBlock());
        assertEquals(0, ev.getX());
        assertEquals(0, ev.getY());
        assertEquals(0, ev.getZ());
    }

    @Test
    void legacyFiveArgCtorSynthesisesBlockAtCoordsCarryingPlayerWorld() {
        // CoreProtect's BlockBreakListener throws NPE if event.getBlock()
        // is null, so the 5-arg adapter ctor synthesises a BukkitBlock
        // at the given coords (player's world, MaterialMapper-resolved
        // type id). Older behaviour returned null — change is deliberate
        // and tested here.
        BlockBreakEvent ev = new BlockBreakEvent((Player) null, 1, 2, 3, 17);
        assertNotNull(ev.getBlock(),
                "legacy 5-arg ctor must synthesise a non-null Block (CoreProtect contract)");
        assertEquals(1, ev.getBlock().getX());
        assertEquals(2, ev.getBlock().getY());
        assertEquals(3, ev.getBlock().getZ());
        assertEquals(1, ev.getX());
        assertEquals(2, ev.getY());
        assertEquals(3, ev.getZ());
        assertEquals(17, ev.getBlockType());
    }

    @Test
    void canonicalCtorReadsBlockWorldThroughBukkitBlock() {
        // Round-trip the block's world through getBlock so adapter
        // listeners can drive World-scoped logic from the event.
        StubWorld w = new StubWorld();
        BukkitBlock block = new BukkitBlock(w, 0, 64, 0, Material.DIRT);
        BlockBreakEvent ev = new BlockBreakEvent(block, (Player) null);

        assertNotNull(ev.getBlock());
        assertSame(w, ev.getBlock().getWorld());
    }

    private static final class StubWorld implements World {
        @Override public String getName() { return "stub"; }
        @Override public Block getBlockAt(int x, int y, int z) { return null; }
        @Override public boolean setBlockType(int x, int y, int z, Material type) { return true; }
        @Override public int getMaxHeight() { return 256; }
        @Override public long getTime() { return 0; }
        @Override public void setTime(long time) {}
        @Override public World$Environment getEnvironment() { return World$Environment.NORMAL; }
    }
}
