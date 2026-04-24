package org.bukkit.scheduler;

import org.bukkit.plugin.Plugin;

/**
 * Bukkit-shaped abstract runnable. Subclasses implement {@link #run()} and
 * schedule via {@link #runTask(Plugin)}, {@link #runTaskLater(Plugin, long)},
 * or {@link #runTaskTimer(Plugin, long, long)}. All variants go through the
 * plugin's {@link org.bukkit.Bukkit#getScheduler()}.
 */
public abstract class BukkitRunnable implements Runnable {

    private BukkitTask task;

    public synchronized BukkitTask runTask(Plugin plugin) {
        checkNotScheduled();
        task = plugin.getServer().getScheduler().runTask(plugin, this);
        return task;
    }

    public synchronized BukkitTask runTaskLater(Plugin plugin, long delayTicks) {
        checkNotScheduled();
        task = plugin.getServer().getScheduler().runTaskLater(plugin, this, delayTicks);
        return task;
    }

    public synchronized BukkitTask runTaskTimer(Plugin plugin, long delayTicks, long periodTicks) {
        checkNotScheduled();
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this, delayTicks, periodTicks);
        return task;
    }

    /** @return task id once scheduled. */
    public synchronized int getTaskId() {
        if (task == null) throw new IllegalStateException("not scheduled yet");
        return task.getTaskId();
    }

    public synchronized boolean isCancelled() {
        return task != null && task.isCancelled();
    }

    public synchronized void cancel() {
        if (task != null) task.cancel();
    }

    private void checkNotScheduled() {
        if (task != null) throw new IllegalStateException("already scheduled");
    }
}
