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
}
