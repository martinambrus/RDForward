package com.github.martinambrus.rdforward.bridge.pocketmine.fixtures;

import pocketmine.event.HandleEvent;
import pocketmine.event.Listener;
import pocketmine.event.block.BlockBreakEvent;
import pocketmine.event.player.PlayerJoinEvent;

/**
 * Fixture PocketMine listener. Exercises two {@code @HandleEvent}
 * dispatch paths — a non-cancellable {@link PlayerJoinEvent} and a
 * cancellable {@link BlockBreakEvent}. The block-break handler vetoes
 * any break flagged with a {@code "veto"} player name so the test can
 * verify cancellation bubbles back to rd-api as
 * {@code EventResult.CANCEL}.
 */
public class TestPocketMineListener implements Listener {

    public static final String PROP_JOIN = "rdforward.test.pocketmine.join";
    public static final String PROP_BREAK = "rdforward.test.pocketmine.break";

    @HandleEvent(priority = HandleEvent.EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent e) {
        System.setProperty(PROP_JOIN, e.getPlayerName());
    }

    @HandleEvent(priority = HandleEvent.EventPriority.NORMAL, ignoreCancelled = false)
    public void onBreak(BlockBreakEvent e) {
        System.setProperty(PROP_BREAK, e.getPlayerName() + "@" + e.getX() + "," + e.getY() + "," + e.getZ());
        if ("veto".equals(e.getPlayerName())) e.setCancelled(true);
    }
}
