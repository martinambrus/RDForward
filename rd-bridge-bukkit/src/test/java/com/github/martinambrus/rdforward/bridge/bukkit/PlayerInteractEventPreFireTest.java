package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.api.event.EventResult;
import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * CoreProtect's BlockInspector mode registers a {@link PlayerInteractEvent}
 * listener that cancels the event to prevent the actual break/place. Its
 * BlockBreakListener / BlockPlaceListener are at MONITOR priority and the
 * MONITOR pass discards return values by design (PrioritizedEvent
 * spec), so the inspector's cancellation cannot reach the host through
 * BlockBreakEvent / BlockPlaceEvent itself.
 *
 * <p>The bridge therefore installs a single LOWEST-priority callback on
 * {@code ServerEvents.BLOCK_BREAK} and {@code ServerEvents.BLOCK_PLACE}
 * that synthesises a PIE, dispatches it to every PIE listener, and
 * returns CANCEL if any of them set cancelled. This test pins that
 * contract:
 *
 * <ul>
 *   <li>Inspector PIE listener cancels => BLOCK_BREAK / BLOCK_PLACE
 *       outcome is CANCEL.</li>
 *   <li>MONITOR-priority break/place listener still receives the event
 *       so audit plugins log the attempt regardless of cancellation.</li>
 *   <li>PIE listener that does NOT cancel => outcome is PASS, world
 *       mutation proceeds.</li>
 * </ul>
 */
class PlayerInteractEventPreFireTest {

    @BeforeEach
    void clear() {
        ServerEvents.clearAll();
        BukkitEventAdapter.clearAll();
    }

    @AfterEach
    void clearAfter() {
        ServerEvents.clearAll();
        BukkitEventAdapter.clearAll();
    }

    static final class CancellingInspector implements Listener {
        int hits;
        @EventHandler
        public void onInteract(PlayerInteractEvent e) {
            hits++;
            e.setCancelled(true);
        }
    }

    static final class PassThroughInspector implements Listener {
        int hits;
        @EventHandler
        public void onInteract(PlayerInteractEvent e) {
            hits++;
            // No setCancelled — should not cancel the synthesised break/place.
        }
    }

    static final class MonitorAuditListener implements Listener {
        int breakHits;
        int placeHits;
        @EventHandler(priority = EventPriority.MONITOR)
        public void onBreak(BlockBreakEvent e) { breakHits++; }
        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlace(BlockPlaceEvent e) { placeHits++; }
    }

    @Test
    void cancelledPieAtAnyPriorityCancelsBlockBreak() {
        // Mirrors CoreProtect inspector mode: PIE listener cancels,
        // monitor-priority audit listener still fires.
        CancellingInspector inspector = new CancellingInspector();
        MonitorAuditListener audit = new MonitorAuditListener();
        BukkitEventAdapter.register(inspector, "inspector");
        BukkitEventAdapter.register(audit, "audit");

        EventResult r = ServerEvents.BLOCK_BREAK.invoker()
                .onBlockBreak("alice", 1, 2, 3, 1);

        assertSame(EventResult.CANCEL, r,
                "PIE setCancelled(true) must propagate to BLOCK_BREAK outcome");
        assertEquals(1, inspector.hits, "PIE listener must be invoked");
        assertEquals(1, audit.breakHits,
                "MONITOR audit listener must still fire even after CANCEL");
    }

    @Test
    void cancelledPieAtAnyPriorityCancelsBlockPlace() {
        CancellingInspector inspector = new CancellingInspector();
        MonitorAuditListener audit = new MonitorAuditListener();
        BukkitEventAdapter.register(inspector, "inspector");
        BukkitEventAdapter.register(audit, "audit");

        EventResult r = ServerEvents.BLOCK_PLACE.invoker()
                .onBlockPlace("alice", 1, 2, 3, 1);

        assertSame(EventResult.CANCEL, r);
        assertEquals(1, inspector.hits);
        assertEquals(1, audit.placeHits);
    }

    @Test
    void uncancelledPieLeavesBlockBreakAtPass() {
        // Sanity check — the pre-fire callback must not always return
        // CANCEL; only when the PIE was actually cancelled.
        PassThroughInspector inspector = new PassThroughInspector();
        MonitorAuditListener audit = new MonitorAuditListener();
        BukkitEventAdapter.register(inspector, "inspector");
        BukkitEventAdapter.register(audit, "audit");

        EventResult r = ServerEvents.BLOCK_BREAK.invoker()
                .onBlockBreak("alice", 1, 2, 3, 1);

        assertSame(EventResult.PASS, r);
        assertEquals(1, inspector.hits);
        assertEquals(1, audit.breakHits);
    }

    @Test
    void uncancelledPieLeavesBlockPlaceAtPass() {
        PassThroughInspector inspector = new PassThroughInspector();
        MonitorAuditListener audit = new MonitorAuditListener();
        BukkitEventAdapter.register(inspector, "inspector");
        BukkitEventAdapter.register(audit, "audit");

        EventResult r = ServerEvents.BLOCK_PLACE.invoker()
                .onBlockPlace("alice", 1, 2, 3, 1);

        assertSame(EventResult.PASS, r);
        assertEquals(1, inspector.hits);
        assertEquals(1, audit.placeHits);
    }

    @Test
    void preFireInstallerIsIdempotentAcrossListenerRegistrations() {
        // Two listener-registration passes should not stack pre-fire
        // callbacks. The pre-fire installer is gated by an AtomicBoolean
        // CAS — multiple bind* calls must produce exactly one LOWEST
        // pre-fire callback per event.
        BukkitEventAdapter.register(new MonitorAuditListener(), "audit-1");
        int afterFirstBreak = ServerEvents.BLOCK_BREAK.getListenerInfo().size();
        int afterFirstPlace = ServerEvents.BLOCK_PLACE.getListenerInfo().size();
        BukkitEventAdapter.register(new MonitorAuditListener(), "audit-2");
        int afterSecondBreak = ServerEvents.BLOCK_BREAK.getListenerInfo().size();
        int afterSecondPlace = ServerEvents.BLOCK_PLACE.getListenerInfo().size();

        // Each registration adds exactly one ServerEvents listener
        // (the audit listener itself); the pre-fire callback was
        // installed only on the first pass.
        assertEquals(afterFirstBreak + 1, afterSecondBreak,
                "Second registration must add exactly one BLOCK_BREAK listener (no extra pre-fire)");
        assertEquals(afterFirstPlace + 1, afterSecondPlace,
                "Second registration must add exactly one BLOCK_PLACE listener (no extra pre-fire)");
    }
}
