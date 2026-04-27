// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Default {@link BukkitTask} implementation backed by an rd-api
 * {@link com.github.martinambrus.rdforward.api.scheduler.ScheduledTask}.
 * Lives in the bridge package (not {@code org.bukkit}) so the public
 * Bukkit surface stays a pure interface, matching real paper-api.
 */
public final class RDBukkitTask implements BukkitTask {

    private final int taskId;
    private final Plugin owner;
    private final com.github.martinambrus.rdforward.api.scheduler.ScheduledTask backing;
    private volatile boolean cancelled;

    public RDBukkitTask(int taskId, Plugin owner,
                        com.github.martinambrus.rdforward.api.scheduler.ScheduledTask backing) {
        this.taskId = taskId;
        this.owner = owner;
        this.backing = backing;
    }

    @Override public int getTaskId() { return taskId; }
    @Override public Plugin getOwner() { return owner; }
    @Override public boolean isCancelled() { return cancelled; }

    @Override
    public void cancel() {
        if (cancelled) return;
        cancelled = true;
        if (backing != null) backing.cancel();
    }
}
