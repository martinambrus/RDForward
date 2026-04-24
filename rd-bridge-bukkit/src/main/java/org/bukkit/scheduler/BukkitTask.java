package org.bukkit.scheduler;

import org.bukkit.plugin.Plugin;

/**
 * Bukkit-shaped task handle. Wraps an rd-api
 * {@link com.github.martinambrus.rdforward.api.scheduler.ScheduledTask}.
 * The fields a plugin reads ({@code taskId}, {@code owner}, {@code cancelled})
 * round-trip; actual cancel flows through to the rd-api scheduler.
 */
public class BukkitTask {

    private final int taskId;
    private final Plugin owner;
    private final com.github.martinambrus.rdforward.api.scheduler.ScheduledTask backing;
    private volatile boolean cancelled;

    public BukkitTask(int taskId, Plugin owner,
                      com.github.martinambrus.rdforward.api.scheduler.ScheduledTask backing) {
        this.taskId = taskId;
        this.owner = owner;
        this.backing = backing;
    }

    public int getTaskId() { return taskId; }
    public Plugin getOwner() { return owner; }
    public boolean isCancelled() { return cancelled; }

    /** Cancel the underlying scheduled task. Idempotent. */
    public void cancel() {
        if (cancelled) return;
        cancelled = true;
        if (backing != null) backing.cancel();
    }
}
