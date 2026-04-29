package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Pins the shared {@link HandlerList} on the base {@code Event} class.
 * Essentials's {@code SignPlayerListener.onSignPlayerInteract} self-
 * unregisters via {@code event.getHandlers().unregister(this)} when
 * sign support is disabled in config; the prior null-returning stubs
 * NPE'd on the chained {@code .unregister} call. Worse,
 * {@link BlockBreakEvent} carried no override at all and threw
 * {@code NoSuchMethodError} on the same dispatch path.
 *
 * <p>Contract validated:
 * <ul>
 *   <li>{@code Event#getHandlers} and {@code Event#getHandlerList}
 *       return a non-null shared list.</li>
 *   <li>Subclasses without explicit overrides (BlockBreakEvent) inherit
 *       the base method cleanly.</li>
 *   <li>PlayerInteractEvent — whose explicit null overrides were
 *       removed — now resolves through the inherited base.</li>
 * </ul>
 */
class EventGetHandlersInheritanceTest {

    @Test
    void blockBreakEventInheritsGetHandlersFromBase() {
        BlockBreakEvent ev = new BlockBreakEvent(null, 0, 0, 0, 0);
        HandlerList list = ev.getHandlers();
        assertNotNull(list,
                "BlockBreakEvent must inherit a non-null HandlerList from Event base — "
                + "Essentials's SignBlockListener self-unregister NPE'd otherwise");
    }

    @Test
    void playerInteractEventInheritsGetHandlers() {
        PlayerInteractEvent ev = new PlayerInteractEvent();
        HandlerList list = ev.getHandlers();
        assertNotNull(list,
                "PlayerInteractEvent must surface a non-null HandlerList so SignPlayerListener "
                + "can self-unregister without NPE");
    }

    @Test
    void instanceAndStaticReturnSameSharedList() {
        BlockBreakEvent ev = new BlockBreakEvent(null, 0, 0, 0, 0);
        assertSame(ev.getHandlers(), BlockBreakEvent.getHandlerList(),
                "instance + static accessors must hand back the same shared list — "
                + "matches Bukkit's per-event-class convention");
    }

    @Test
    void unregisterListenerOnSharedListIsNoop() {
        // The shim's unregister stub is a no-op (logs once, then silent).
        // Verify the call doesn't throw — Essentials's listener self-
        // removal would re-throw any unexpected exception up to its own
        // try/catch with a SEVERE log entry.
        BlockBreakEvent ev = new BlockBreakEvent(null, 0, 0, 0, 0);
        ev.getHandlers().unregister((org.bukkit.event.Listener) null);
    }
}
