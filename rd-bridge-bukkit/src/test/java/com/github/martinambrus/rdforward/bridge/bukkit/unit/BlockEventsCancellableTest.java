package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockEventsCancellableTest {

    @Test
    void blockBreakEventImplementsCancellable() {
        BlockBreakEvent e = new BlockBreakEvent((Block) null, (Player) null);
        assertTrue(e instanceof Cancellable,
                "BlockBreakEvent must implement Cancellable for WG6 Events.fireToCancel");
        assertFalse(e.isCancelled());
        e.setCancelled(true);
        assertTrue(e.isCancelled());
    }

    @Test
    void blockPlaceEventImplementsCancellable() {
        BlockPlaceEvent e = new BlockPlaceEvent(null, null, null, null, null, false);
        assertTrue(e instanceof Cancellable,
                "BlockPlaceEvent must implement Cancellable for WG6 Events.fireToCancel");
        assertFalse(e.isCancelled());
        e.setCancelled(true);
        assertTrue(e.isCancelled());
    }
}
