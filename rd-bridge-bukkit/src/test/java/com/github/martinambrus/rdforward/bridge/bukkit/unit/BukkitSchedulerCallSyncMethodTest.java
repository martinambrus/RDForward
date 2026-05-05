package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

class BukkitSchedulerCallSyncMethodTest {

    private final BukkitScheduler scheduler = new StubScheduler();

    @Test
    void returnsCompletedFutureWithResult() throws Exception {
        Future<String> f = scheduler.callSyncMethod(plugin, () -> "hello");
        assertTrue(f.isDone());
        assertEquals("hello", f.get());
    }

    @Test
    void returnsFutureWithNullForNullResult() throws Exception {
        Future<Object> f = scheduler.callSyncMethod(plugin, () -> null);
        assertTrue(f.isDone());
        assertNull(f.get());
    }

    @Test
    void propagatesExceptionViaFuture() {
        Future<Object> f = scheduler.callSyncMethod(plugin, () -> {
            throw new RuntimeException("boom");
        });
        assertTrue(f.isDone());
        assertTrue(f instanceof CompletableFuture<?>);
        assertThrows(java.util.concurrent.ExecutionException.class, f::get);
    }

    @Test
    void callableExecutesImmediatelyOnCallingThread() throws Exception {
        Thread[] captured = {null};
        scheduler.callSyncMethod(plugin, () -> {
            captured[0] = Thread.currentThread();
            return null;
        });
        assertSame(Thread.currentThread(), captured[0],
                "callable must run on the calling thread, not async");
    }

    private static final Plugin plugin = new Plugin() {
        @Override public boolean isEnabled() { return true; }
        @Override public String getName() { return "TestPlugin"; }
        @Override public org.bukkit.plugin.PluginDescriptionFile getDescription() { return null; }
        @Override public java.util.logging.Logger getLogger() { return java.util.logging.Logger.getLogger("TestPlugin"); }
        @Override public org.bukkit.Server getServer() { return null; }
        @Override public void onLoad() {}
        @Override public void onEnable() {}
        @Override public void onDisable() {}
    };

    /** Minimal BukkitScheduler that only provides callSyncMethod (the default impl). */
    private static class StubScheduler implements BukkitScheduler {
        @Override public org.bukkit.scheduler.BukkitTask runTask(Plugin p, Runnable r) { return null; }
        @Override public org.bukkit.scheduler.BukkitTask runTaskLater(Plugin p, Runnable r, long d) { return null; }
        @Override public org.bukkit.scheduler.BukkitTask runTaskTimer(Plugin p, Runnable r, long d, long t) { return null; }
        @Override public org.bukkit.scheduler.BukkitTask runTaskAsynchronously(Plugin p, Runnable r) { return null; }
        @Override public org.bukkit.scheduler.BukkitTask runTaskLaterAsynchronously(Plugin p, Runnable r, long d) { return null; }
        @Override public org.bukkit.scheduler.BukkitTask runTaskTimerAsynchronously(Plugin p, Runnable r, long d, long t) { return null; }
        @Override public void cancelTasks(Plugin p) {}
    }
}
