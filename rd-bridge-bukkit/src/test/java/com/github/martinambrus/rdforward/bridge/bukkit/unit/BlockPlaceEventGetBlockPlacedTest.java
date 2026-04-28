package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.api.world.BlockTypes;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitPlayer;
import com.github.martinambrus.rdforward.bridge.bukkit.MaterialMapper;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World$Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Essentials's {@code EssentialsBlockListener.onBlockPlace} calls
 * {@code event.getBlockPlaced().getType()}. The RDForward stub
 * synthesises the placed Block from the placer's world + the rd-api
 * block id round-tripped through {@link MaterialMapper}; without this,
 * legacy block-place listeners NoSuchMethodError or NPE.
 */
class BlockPlaceEventGetBlockPlacedTest {

    @Test
    void getBlockPlacedReturnsBlockAtPlacementCoords() {
        Player placer = makePlayerWithWorld(new StubWorld());
        BlockPlaceEvent ev = new BlockPlaceEvent(placer, 4, 5, 6,
                BlockTypes.COBBLE.getId());

        Block placed = ev.getBlockPlaced();
        assertNotNull(placed);
        assertEquals(4, placed.getX());
        assertEquals(5, placed.getY());
        assertEquals(6, placed.getZ());
    }

    @Test
    void getBlockPlacedTypeRoundTripsThroughMaterialMapper() {
        Player placer = makePlayerWithWorld(new StubWorld());
        BlockPlaceEvent ev = new BlockPlaceEvent(placer, 0, 0, 0,
                BlockTypes.STONE.getId());

        // Material must round-trip via MaterialMapper.fromApi —
        // Essentials gates protect/log logic on getType(), so an
        // unmapped id here would silently degrade behaviour.
        assertSame(Material.STONE, ev.getBlockPlaced().getType());
    }

    @Test
    void getBlockPlacedCarriesPlayerWorld() {
        StubWorld w = new StubWorld();
        Player placer = makePlayerWithWorld(w);
        BlockPlaceEvent ev = new BlockPlaceEvent(placer, 1, 2, 3,
                BlockTypes.AIR.getId());

        assertSame(w, ev.getBlockPlaced().getWorld(),
                "placed Block must carry the placer's world for downstream lookups");
    }

    @Test
    void unknownBlockTypeFallsBackToAirMaterial() {
        // MaterialMapper has a default-AIR branch for ids it does not
        // surface as Bukkit blocks. The fall-through must not NPE.
        Player placer = makePlayerWithWorld(new StubWorld());
        int unknownId = 9999;
        BlockPlaceEvent ev = new BlockPlaceEvent(placer, 0, 0, 0, unknownId);
        assertSame(Material.AIR, ev.getBlockPlaced().getType());
    }

    @Test
    void getBlockReturnsSameBlockAsGetBlockPlaced() {
        // Modern Bukkit's BlockEvent surface exposes getBlock();
        // RDForward forwards both accessors to the same synthesised
        // block so plugins on either side observe consistent state.
        Player placer = makePlayerWithWorld(new StubWorld());
        BlockPlaceEvent ev = new BlockPlaceEvent(placer, 1, 1, 1,
                BlockTypes.WOOD.getId());
        assertSame(ev.getBlockPlaced(), ev.getBlock());
    }

    @Test
    void nullPlayerYieldsNullWorldedBlock() {
        // Defensive: legacy ctor used by adapter passes a non-null
        // BukkitPlayer, but the stub must not crash if a test calls
        // it with a null player. World becomes null; coords + Material
        // still populate.
        BlockPlaceEvent ev = new BlockPlaceEvent((Player) null, 2, 3, 4,
                BlockTypes.DIRT.getId());
        assertNotNull(ev.getBlockPlaced());
        assertNull(ev.getBlockPlaced().getWorld());
        assertEquals(2, ev.getBlockPlaced().getX());
        assertSame(Material.DIRT, ev.getBlockPlaced().getType());
    }

    private static Player makePlayerWithWorld(World world) {
        com.github.martinambrus.rdforward.api.world.Location loc =
                new com.github.martinambrus.rdforward.api.world.Location("stub", 0, 0, 0, 0f, 0f);
        StubRdServer.StubRdPlayer backing =
                new StubRdServer.StubRdPlayer("place-test", loc);
        return BukkitPlayer.create("place-test", backing, world);
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
