package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitSchedulerAdapter;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the async scheduling methods on {@link BukkitRunnable}:
 * {@code runTaskAsynchronously}, {@code runTaskLaterAsynchronously},
 * and {@code runTaskTimerAsynchronously}.
 */
class BukkitRunnableAsyncTest {

    private StubRdServer rd;
    private BukkitSchedulerAdapter adapter;
    private static final org.bukkit.plugin.Plugin PLUGIN =
            new org.bukkit.plugin.Plugin() {
                public String getName() { return "RunnableTest"; }
                public org.bukkit.plugin.PluginDescriptionFile getDescription() { return null; }
                public java.util.logging.Logger getLogger() { return java.util.logging.Logger.getLogger("RunnableTest"); }
                public org.bukkit.Server getServer() { return Bukkit.getServer(); }
                public boolean isEnabled() { return true; }
                public void onLoad() {}
                public void onEnable() {}
                public void onDisable() {}
            };

    @BeforeEach
    void setUp() {
        rd = new StubRdServer();
        adapter = new BukkitSchedulerAdapter(rd.scheduler);
        BukkitBridge.install(rd);
    }

    @AfterEach
    void tearDown() {
        BukkitBridge.uninstall();
        adapter.shutdown();
    }

    @Test
    void runTaskAsynchronouslyExecutesOffCallerThread() throws InterruptedException {
        CountDownLatch ran = new CountDownLatch(1);
        AtomicReference<String> threadName = new AtomicReference<>();

        new BukkitRunnable() {
            @Override public void run() {
                threadName.set(Thread.currentThread().getName());
                ran.countDown();
            }
        }.runTaskAsynchronously(PLUGIN);

        assertTrue(ran.await(2, TimeUnit.SECONDS), "task did not run within 2s");
        assertNotEquals(Thread.currentThread().getName(), threadName.get(),
                "async BukkitRunnable should execute off the caller thread");
    }

    @Test
    void runTaskLaterAsynchronouslyHonoursDelay() throws InterruptedException {
        CountDownLatch ran = new CountDownLatch(1);
        long t0 = System.nanoTime();

        new BukkitRunnable() {
            @Override public void run() { ran.countDown(); }
        }.runTaskLaterAsynchronously(PLUGIN, 4); // ~200ms

        assertTrue(ran.await(2, TimeUnit.SECONDS), "delayed task never fired");
        long elapsedMs = (System.nanoTime() - t0) / 1_000_000L;
        assertTrue(elapsedMs >= 150L,
                "delayed task fired too soon: " + elapsedMs + "ms");
    }

    @Test
    void runTaskTimerAsynchronouslyRepeats() throws InterruptedException {
        AtomicInteger count = new AtomicInteger(0);
        CountDownLatch thirdFire = new CountDownLatch(3);

        BukkitTask task = new BukkitRunnable() {
            @Override public void run() {
                count.incrementAndGet();
                thirdFire.countDown();
            }
        }.runTaskTimerAsynchronously(PLUGIN, 0, 2); // immediate, every ~100ms

        assertNotNull(task, "runTaskTimerAsynchronously must return a BukkitTask");
        assertFalse(task.isSync(), "async timer must report isSync=false");

        assertTrue(thirdFire.await(3, TimeUnit.SECONDS),
                "timer did not fire 3 times within 3s (fired " + count.get() + ")");
        assertTrue(count.get() >= 3, "expected at least 3 firings, got " + count.get());

        task.cancel();
        assertTrue(task.isCancelled(), "cancelled task must report isCancelled=true");
    }

    @Test
    void cannotRescheduleSameRunnable() throws InterruptedException {
        CountDownLatch ran = new CountDownLatch(1);
        BukkitRunnable br = new BukkitRunnable() {
            @Override public void run() { ran.countDown(); }
        };
        br.runTaskAsynchronously(PLUGIN);
        assertTrue(ran.await(2, TimeUnit.SECONDS));

        IllegalStateException ex = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> br.runTaskAsynchronously(PLUGIN));
        assertTrue(ex.getMessage().toLowerCase().contains("already"),
                "expected 'already scheduled' message, got: " + ex.getMessage());
    }

    @Test
    void syncRunTaskStillWorks() throws InterruptedException {
        CountDownLatch ran = new CountDownLatch(1);
        new BukkitRunnable() {
            @Override public void run() { ran.countDown(); }
        }.runTask(PLUGIN);

        // Sync tasks are queued and fire on the next scheduler tick.
        // StubScheduler stores them; run the first one manually.
        for (var s : rd.scheduler.scheduled) {
            s.task.run();
        }
        assertTrue(ran.await(1, TimeUnit.SECONDS), "sync runTask should fire");
    }
}
