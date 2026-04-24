// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.api.scheduler.ScheduledTask;
import com.github.martinambrus.rdforward.api.scheduler.Scheduler;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Wraps the rd-api {@link Scheduler} as a Bukkit-shaped
 * {@link BukkitScheduler}. Every {@code runTaskXxx} tags ownership via
 * the owning plugin's name so rd-mod-loader can sweep tasks on mod
 * unload.
 *
 * <p>Async variants run synchronously (on the tick thread) — RDForward
 * does not currently provide an async worker pool. Callers must spin
 * their own threads if they need true async execution.
 */
public final class BukkitSchedulerAdapter implements BukkitScheduler {

    private final Scheduler backing;
    private final AtomicInteger taskIds = new AtomicInteger(1);

    public BukkitSchedulerAdapter(Scheduler backing) {
        this.backing = backing;
    }

    private String ownerId(Plugin plugin) {
        return plugin == null || plugin.getName() == null ? "__anonymous__" : plugin.getName();
    }

    @Override
    public BukkitTask runTask(Plugin plugin, Runnable task) {
        ScheduledTask st = backing.runLater(ownerId(plugin), 0, task);
        return new BukkitTask(taskIds.getAndIncrement(), plugin, st);
    }

    @Override
    public BukkitTask runTaskLater(Plugin plugin, Runnable task, long delayTicks) {
        int delay = delayTicks < 0 ? 0 : (int) Math.min(Integer.MAX_VALUE, delayTicks);
        ScheduledTask st = backing.runLater(ownerId(plugin), delay, task);
        return new BukkitTask(taskIds.getAndIncrement(), plugin, st);
    }

    @Override
    public BukkitTask runTaskTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
        int delay = delayTicks < 0 ? 0 : (int) Math.min(Integer.MAX_VALUE, delayTicks);
        int period = periodTicks < 1 ? 1 : (int) Math.min(Integer.MAX_VALUE, periodTicks);
        ScheduledTask st = backing.runRepeating(ownerId(plugin), delay, period, task);
        return new BukkitTask(taskIds.getAndIncrement(), plugin, st);
    }

    @Override
    public BukkitTask runTaskAsynchronously(Plugin plugin, Runnable task) {
        return runTask(plugin, task);
    }

    @Override
    public BukkitTask runTaskLaterAsynchronously(Plugin plugin, Runnable task, long delayTicks) {
        return runTaskLater(plugin, task, delayTicks);
    }

    @Override
    public BukkitTask runTaskTimerAsynchronously(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
        return runTaskTimer(plugin, task, delayTicks, periodTicks);
    }

    @Override
    public int cancelTasks(Plugin plugin) {
        return backing.cancelByOwner(ownerId(plugin));
    }
}
