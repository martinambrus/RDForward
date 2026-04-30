// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.scheduler;

import org.bukkit.plugin.Plugin;

/**
 * Bukkit-shaped scheduler. Methods forward to the rd-api
 * {@link com.github.martinambrus.rdforward.api.scheduler.Scheduler} via
 * {@link com.github.martinambrus.rdforward.bridge.bukkit.BukkitSchedulerAdapter}.
 *
 * <p>The {@code Async} variants execute on a dedicated daemon thread
 * pool, so plugins that block on I/O inside async tasks (EssentialsX's
 * {@code UpdateChecker.getVersionMessages} calls
 * {@code CompletableFuture.join()}) do not stall the tick thread.
 */
public interface BukkitScheduler {

    BukkitTask runTask(Plugin plugin, Runnable task);

    BukkitTask runTaskLater(Plugin plugin, Runnable task, long delayTicks);

    BukkitTask runTaskTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks);

    /** Submit {@code task} to the Bukkit-bridge async daemon pool. */
    BukkitTask runTaskAsynchronously(Plugin plugin, Runnable task);

    /** Schedule {@code task} on the Bukkit-bridge async daemon pool after {@code delayTicks}. */
    BukkitTask runTaskLaterAsynchronously(Plugin plugin, Runnable task, long delayTicks);

    /** Schedule {@code task} on the Bukkit-bridge async daemon pool with fixed period. */
    BukkitTask runTaskTimerAsynchronously(Plugin plugin, Runnable task, long delayTicks, long periodTicks);

    /** Cancel every task owned by {@code plugin}. Returns the number cancelled. */
    int cancelTasks(Plugin plugin);

    /**
     * Legacy Bukkit-3 API. LuckPerms's {@code BukkitSchedulerAdapter}
     * binds to this signature directly via {@code MethodHandle}; without
     * a default it errors with {@link NoSuchMethodError} on every
     * context-update buffer flush. Routes through {@link #runTask} and
     * returns a synthetic positive task id (real Bukkit returns &gt;= 0
     * on success, -1 on failure — only the sign is checked by callers).
     */
    default int scheduleSyncDelayedTask(Plugin plugin, Runnable task) {
        runTask(plugin, task);
        return BukkitSchedulerSupport.NEXT_LEGACY_TASK_ID.getAndIncrement();
    }

    default int scheduleSyncDelayedTask(Plugin plugin, Runnable task, long delayTicks) {
        runTaskLater(plugin, task, delayTicks);
        return BukkitSchedulerSupport.NEXT_LEGACY_TASK_ID.getAndIncrement();
    }

    default int scheduleSyncRepeatingTask(Plugin plugin, Runnable task,
                                          long delayTicks, long periodTicks) {
        runTaskTimer(plugin, task, delayTicks, periodTicks);
        return BukkitSchedulerSupport.NEXT_LEGACY_TASK_ID.getAndIncrement();
    }

    /** Legacy variant — routes to {@link #runTaskAsynchronously}. */
    default int scheduleAsyncDelayedTask(Plugin plugin, Runnable task) {
        runTaskAsynchronously(plugin, task);
        return BukkitSchedulerSupport.NEXT_LEGACY_TASK_ID.getAndIncrement();
    }

    default int scheduleAsyncDelayedTask(Plugin plugin, Runnable task, long delayTicks) {
        runTaskLaterAsynchronously(plugin, task, delayTicks);
        return BukkitSchedulerSupport.NEXT_LEGACY_TASK_ID.getAndIncrement();
    }

    default int scheduleAsyncRepeatingTask(Plugin plugin, Runnable task,
                                           long delayTicks, long periodTicks) {
        runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
        return BukkitSchedulerSupport.NEXT_LEGACY_TASK_ID.getAndIncrement();
    }

    /** Legacy cancel-by-id — RDForward's tasks are short-lived enough
     *  that we don't actually track ids; the call is a no-op. */
    default void cancelTask(int taskId) {}
}
