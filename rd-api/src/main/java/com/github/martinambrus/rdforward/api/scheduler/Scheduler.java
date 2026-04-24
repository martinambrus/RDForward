package com.github.martinambrus.rdforward.api.scheduler;

/**
 * Server-tick-based task scheduler. Tasks run on the tick loop thread,
 * making them safe for world and player manipulation without extra
 * synchronization.
 *
 * <p>All methods tag ownership using the caller's mod id. Tasks are
 * auto-cancelled when the owning mod is unloaded.
 */
public interface Scheduler {

    /** Schedule a one-shot task to run after {@code delayTicks} ticks. */
    ScheduledTask runLater(String modId, int delayTicks, Runnable task);

    /** Schedule a repeating task. First run after {@code initialDelay}. */
    ScheduledTask runRepeating(String modId, int initialDelay, int periodTicks, Runnable task);

    /** Cancel every task owned by the given mod. Returns the number cancelled. */
    int cancelByOwner(String modId);
}
