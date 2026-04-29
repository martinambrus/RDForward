package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.Bukkit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link Bukkit#isPrimaryThread()} is gated by two paths:
 *
 * <ol>
 *   <li>The thread is named {@code RDForward-TickLoop} (the server's
 *       actual tick loop).</li>
 *   <li>The thread sits inside a plugin lifecycle callback
 *       (onLoad/onEnable/onDisable). The bridge wrapper sets the
 *       {@link Bukkit#INSIDE_PLUGIN_LIFECYCLE} ThreadLocal around
 *       every lifecycle invocation; without this, CoreProtect's
 *       Config.parseConfig deadlocks because it schedules to the
 *       primary thread and then joins on the future — but the
 *       tick loop has not started yet during plugin enable.</li>
 * </ol>
 */
class BukkitIsPrimaryThreadTest {

    @AfterEach
    void clearThreadLocal() {
        Bukkit.INSIDE_PLUGIN_LIFECYCLE.remove();
    }

    @Test
    void defaultIsFalseFromArbitraryThread() {
        // Test thread name will not be RDForward-TickLoop and the
        // ThreadLocal is not set.
        assertFalse(Bukkit.isPrimaryThread());
    }

    @Test
    void threadLocalToggleFlipsResult() {
        assertFalse(Bukkit.isPrimaryThread());
        Bukkit.INSIDE_PLUGIN_LIFECYCLE.set(true);
        assertTrue(Bukkit.isPrimaryThread());
        Bukkit.INSIDE_PLUGIN_LIFECYCLE.set(false);
        assertFalse(Bukkit.isPrimaryThread());
    }

    @Test
    void threadLocalRemoveResetsToFalse() {
        Bukkit.INSIDE_PLUGIN_LIFECYCLE.set(true);
        Bukkit.INSIDE_PLUGIN_LIFECYCLE.remove();
        assertFalse(Bukkit.isPrimaryThread());
    }

    @Test
    void threadLocalIsConfinedToCurrentThread() throws Exception {
        // Setting on one thread must not leak to another — the bridge
        // wrapper relies on per-thread isolation so concurrent plugin
        // lifecycle calls (rare but possible) don't cross-contaminate.
        Bukkit.INSIDE_PLUGIN_LIFECYCLE.set(true);
        assertTrue(Bukkit.isPrimaryThread());

        boolean[] otherThreadResult = new boolean[1];
        Thread t = new Thread(() -> otherThreadResult[0] = Bukkit.isPrimaryThread(),
                "test-isolated");
        t.start();
        t.join();
        assertFalse(otherThreadResult[0],
                "ThreadLocal must not leak across threads");
    }

    @Test
    void tickLoopThreadNameAlwaysReportsPrimary() throws Exception {
        // Thread name "RDForward-TickLoop" is the actual server tick
        // thread — it should report primary even if the lifecycle
        // ThreadLocal is unset.
        boolean[] result = new boolean[1];
        Thread t = new Thread(() -> result[0] = Bukkit.isPrimaryThread(),
                "RDForward-TickLoop");
        t.start();
        t.join();
        assertTrue(result[0]);
    }
}
