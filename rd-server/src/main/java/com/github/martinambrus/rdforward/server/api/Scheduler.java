package com.github.martinambrus.rdforward.server.api;

import com.github.martinambrus.rdforward.server.event.ServerEvents;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Server-side task scheduler. Runs tasks on future server ticks.
 *
 * The scheduler hooks into the server tick event to process pending tasks.
 * Tasks are guaranteed to run on the tick loop thread, making them safe
 * for world/player manipulation without additional synchronization.
 *
 * Example:
 * <pre>
 *   // Run something 5 seconds (100 ticks) from now
 *   Scheduler.runLater(100, () -> System.out.println("Delayed!"));
 *
 *   // Run something every 20 ticks (1 second)
 *   Scheduler.runRepeating(0, 20, () -> System.out.println("Repeating!"));
 * </pre>
 */
public final class Scheduler {

    private Scheduler() {}

    private static final Queue<ScheduledTask> tasks = new ConcurrentLinkedQueue<>();
    private static boolean initialized = false;

    /**
     * Initialize the scheduler by registering with the server tick event.
     * Called once during server startup.
     */
    public static void init() {
        if (initialized) return;
        initialized = true;
        ServerEvents.SERVER_TICK.register(Scheduler::tick);
    }

    /**
     * Reset the scheduler state. Called during server shutdown so that
     * a subsequent {@link #init()} re-registers with the (cleared) tick event.
     */
    public static void reset() {
        initialized = false;
        tasks.clear();
    }

    /**
     * Schedule a task to run after a delay.
     *
     * @param delayTicks number of ticks to wait (20 ticks = 1 second)
     * @param task       the task to run
     * @return a handle that can be used to cancel the task
     */
    public static ScheduledTask runLater(int delayTicks, Runnable task) {
        ScheduledTask scheduled = new ScheduledTask(task, delayTicks, 0);
        tasks.add(scheduled);
        return scheduled;
    }

    /**
     * Schedule a repeating task.
     *
     * @param initialDelay ticks before first execution
     * @param periodTicks  ticks between subsequent executions
     * @param task         the task to run
     * @return a handle that can be used to cancel the task
     */
    public static ScheduledTask runRepeating(int initialDelay, int periodTicks, Runnable task) {
        ScheduledTask scheduled = new ScheduledTask(task, initialDelay, periodTicks);
        tasks.add(scheduled);
        return scheduled;
    }

    private static void tick(long tickCount) {
        Iterator<ScheduledTask> it = tasks.iterator();
        while (it.hasNext()) {
            ScheduledTask task = it.next();
            if (task.cancelled) {
                it.remove();
                continue;
            }
            task.remainingDelay--;
            if (task.remainingDelay <= 0) {
                try {
                    task.runnable.run();
                } catch (Exception e) {
                    System.err.println("Scheduled task error: " + e.getMessage());
                }
                if (task.periodTicks > 0 && !task.cancelled) {
                    task.remainingDelay = task.periodTicks;
                } else {
                    it.remove();
                }
            }
        }
    }

    public static class ScheduledTask {
        final Runnable runnable;
        final int periodTicks;
        int remainingDelay;
        volatile boolean cancelled = false;

        ScheduledTask(Runnable runnable, int delayTicks, int periodTicks) {
            this.runnable = runnable;
            this.remainingDelay = delayTicks;
            this.periodTicks = periodTicks;
        }

        /** Cancel this task. It will be removed on the next tick. */
        public void cancel() {
            cancelled = true;
        }

        public boolean isCancelled() {
            return cancelled;
        }
    }
}
