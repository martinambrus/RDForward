// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.scheduler;

import org.bukkit.plugin.Plugin;

/**
 * Bukkit-shaped scheduler. Methods forward to the rd-api
 * {@link com.github.martinambrus.rdforward.api.scheduler.Scheduler} via
 * {@link com.github.martinambrus.rdforward.bridge.bukkit.BukkitSchedulerAdapter}.
 *
 * <p>The {@code Async} variants also run on the server tick thread —
 * RDForward does not offer an async task pool. Plugins requiring true
 * async work must spin their own threads.
 */
public interface BukkitScheduler {

    BukkitTask runTask(Plugin plugin, Runnable task);

    BukkitTask runTaskLater(Plugin plugin, Runnable task, long delayTicks);

    BukkitTask runTaskTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks);

    /** Same as {@link #runTask} — RDForward runs everything on the tick thread. */
    BukkitTask runTaskAsynchronously(Plugin plugin, Runnable task);

    /** Same as {@link #runTaskLater} — RDForward has no async pool. */
    BukkitTask runTaskLaterAsynchronously(Plugin plugin, Runnable task, long delayTicks);

    /** Same as {@link #runTaskTimer} — RDForward has no async pool. */
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

    /** Legacy variant — RDForward has no async pool, so this routes to
     *  the synchronous task path like {@link #runTaskAsynchronously}. */
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
