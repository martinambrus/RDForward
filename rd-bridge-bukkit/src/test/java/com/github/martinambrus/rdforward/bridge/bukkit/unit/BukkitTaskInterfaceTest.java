package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.RDBukkitTask;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confirms {@link BukkitTask} is an interface (not a concrete class) so
 * plugins compiled against real Bukkit's interface — VanishNoPacket
 * 3.15's bundled {@code Metrics.start} dispatches via
 * {@code invokeinterface} — don't crash with
 * {@link IncompatibleClassChangeError}.
 */
class BukkitTaskInterfaceTest {

    @Test
    void bukkitTaskIsAnInterface() {
        assertTrue(BukkitTask.class.isInterface(),
                "BukkitTask must remain an interface to match real paper-api");
    }

    @Test
    void rdImplementsBukkitTask() {
        BukkitTask t = new RDBukkitTask(42, null, null);
        assertEquals(42, t.getTaskId());
        assertNull(t.getOwner());
        assertFalse(t.isCancelled());
    }

    @Test
    void cancelFlipsCancelledFlagIdempotently() {
        BukkitTask t = new RDBukkitTask(1, null, null);
        t.cancel();
        t.cancel();
        assertTrue(t.isCancelled());
    }
}
