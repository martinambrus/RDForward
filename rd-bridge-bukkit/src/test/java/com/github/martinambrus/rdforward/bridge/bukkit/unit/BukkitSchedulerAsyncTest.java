package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitSchedulerAdapter;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confirms async scheduler submissions run off the calling thread (so a
 * blocking task such as EssentialsX's {@code UpdateChecker.getVersionMessages
 * -> CompletableFuture.join()} cannot stall the rd-server tick loop) and
 * that cancellation reaches both rd-api sync entries and pool futures.
 */
class BukkitSchedulerAsyncTest {

    private StubRdServer rd;
    private BukkitSchedulerAdapter adapter;
    private final org.bukkit.plugin.Plugin plugin = new TestPlugin("AsyncTest");

    @BeforeEach
    void setUp() {
        rd = new StubRdServer();
        adapter = new BukkitSchedulerAdapter(rd.scheduler);
    }

    @AfterEach
    void tearDown() {
        adapter.shutdown();
    }

    @Test
    void runTaskAsynchronouslyExecutesOffCallerThread() throws InterruptedException {
        CountDownLatch ran = new CountDownLatch(1);
        AtomicReference<String> threadName = new AtomicReference<>();
        BukkitTask t = adapter.runTaskAsynchronously(plugin, () -> {
            threadName.set(Thread.currentThread().getName());
            ran.countDown();
        });
        assertNotNull(t);
        assertFalse(t.isSync(), "async task must report isSync=false");
        assertTrue(ran.await(2, TimeUnit.SECONDS), "task did not run within 2s");
        assertNotEquals(Thread.currentThread().getName(), threadName.get());
        assertTrue(threadName.get().startsWith("BukkitAsync-"),
                "expected BukkitAsync-N pool thread, got " + threadName.get());
    }

    @Test
    void runTaskLaterAsynchronouslyHonoursDelay() throws InterruptedException {
        CountDownLatch ran = new CountDownLatch(1);
        long t0 = System.nanoTime();
        adapter.runTaskLaterAsynchronously(plugin, ran::countDown, 4); // 4 ticks = ~200ms
        assertTrue(ran.await(2, TimeUnit.SECONDS), "delayed task never fired");
        long elapsedMs = (System.nanoTime() - t0) / 1_000_000L;
        assertTrue(elapsedMs >= 150L,
                "delayed task fired too soon: " + elapsedMs + "ms (expected >= 150)");
    }

    @Test
    void cancelTasksCancelsScheduledAsyncFuture() throws InterruptedException {
        CountDownLatch ran = new CountDownLatch(1);
        // 100 ticks = 5s — long enough that cancel happens first.
        adapter.runTaskLaterAsynchronously(plugin, ran::countDown, 100);
        int cancelled = adapter.cancelTasks(plugin);
        assertEquals(1, cancelled);
        assertFalse(ran.await(500, TimeUnit.MILLISECONDS),
                "cancelled task still ran");
    }

    @Test
    void cancelTasksCountsBothSyncAndAsync() {
        adapter.runTask(plugin, () -> {});
        adapter.runTaskTimer(plugin, () -> {}, 0, 20);
        adapter.runTaskLaterAsynchronously(plugin, () -> {}, 100);
        int cancelled = adapter.cancelTasks(plugin);
        assertEquals(3, cancelled, "should cancel 2 sync + 1 async");
    }

    @Test
    void shutdownPreventsFurtherAsyncSubmissions() {
        adapter.shutdown();
        // After shutdown, the executor rejects submissions; this must surface
        // as RejectedExecutionException — the bridge does not silently swallow.
        try {
            adapter.runTaskAsynchronously(plugin, () -> {});
        } catch (java.util.concurrent.RejectedExecutionException expected) {
            return;
        }
        org.junit.jupiter.api.Assertions.fail("submit after shutdown should throw RejectedExecutionException");
    }

    private static final class TestPlugin implements org.bukkit.plugin.Plugin {
        private final String name;
        TestPlugin(String name) { this.name = name; }
        @Override public String getName() { return name; }
        @Override public org.bukkit.plugin.PluginDescriptionFile getDescription() { return null; }
        @Override public java.util.logging.Logger getLogger() {
            return java.util.logging.Logger.getLogger(name);
        }
        @Override public org.bukkit.Server getServer() { return org.bukkit.Bukkit.getServer(); }
        @Override public boolean isEnabled() { return true; }
        @Override public void onLoad() {}
        @Override public void onEnable() {}
        @Override public void onDisable() {}
    }
}
