package com.github.martinambrus.rdforward.server.api;

import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Scheduler: verifies task scheduling, delay, cancellation,
 * and repeating execution. Drives the scheduler via manual tick invocations.
 */
class SchedulerTest {

    @BeforeEach
    void setUp() {
        ServerEvents.SERVER_TICK.clearListeners();
        Scheduler.reset();
        Scheduler.init();
    }

    @AfterEach
    void tearDown() {
        Scheduler.reset();
    }

    private void fireTick() {
        ServerEvents.SERVER_TICK.invoker().onServerTick(0);
    }

    @Test
    void runLaterZeroDelayExecutesOnFirstTick() {
        AtomicBoolean ran = new AtomicBoolean();
        Scheduler.runLater(0, () -> ran.set(true));

        assertFalse(ran.get(), "Should not run before tick");
        fireTick();
        assertTrue(ran.get(), "Should run after first tick");
    }

    @Test
    void runLaterRespectsDelay() {
        AtomicBoolean ran = new AtomicBoolean();
        Scheduler.runLater(3, () -> ran.set(true));

        fireTick();
        assertFalse(ran.get(), "Should not run after 1 tick (delay=3)");
        fireTick();
        assertFalse(ran.get(), "Should not run after 2 ticks (delay=3)");
        fireTick();
        assertTrue(ran.get(), "Should run after 3 ticks (delay=3)");
    }

    @Test
    void cancelPreventsExecution() {
        AtomicBoolean ran = new AtomicBoolean();
        Scheduler.ScheduledTask task = Scheduler.runLater(1, () -> ran.set(true));

        task.cancel();
        fireTick();
        assertFalse(ran.get(), "Cancelled task must not execute");
    }

    @Test
    void runRepeatingExecutesMultipleTimes() {
        AtomicInteger count = new AtomicInteger();
        Scheduler.runRepeating(0, 1, count::incrementAndGet);

        fireTick();
        assertEquals(1, count.get());
        fireTick();
        assertEquals(2, count.get());
        fireTick();
        assertEquals(3, count.get());
    }

    @Test
    void runRepeatingRespectsInitialDelay() {
        AtomicInteger count = new AtomicInteger();
        Scheduler.runRepeating(2, 1, count::incrementAndGet);

        fireTick();
        assertEquals(0, count.get(), "Should not fire during initial delay");
        fireTick();
        assertEquals(1, count.get(), "Should fire after initial delay");
        fireTick();
        assertEquals(2, count.get());
    }

    @Test
    void repeatingTaskCancelledMidRun() {
        AtomicInteger count = new AtomicInteger();
        Scheduler.ScheduledTask task = Scheduler.runRepeating(0, 1, count::incrementAndGet);

        fireTick();
        assertEquals(1, count.get());
        task.cancel();
        fireTick();
        assertEquals(1, count.get(), "Should not fire after cancellation");
    }

    @Test
    void taskRecordsExecutionThread() {
        AtomicReference<String> threadName = new AtomicReference<>();
        Scheduler.runLater(0, () -> threadName.set(Thread.currentThread().getName()));

        fireTick();
        assertNotNull(threadName.get(), "Task should have executed");
        // In tests the tick fires on the test thread; in production it fires
        // on "RDForward-TickLoop". The key invariant is that Scheduler
        // delegates to the tick event thread, not a random thread pool.
    }
}
