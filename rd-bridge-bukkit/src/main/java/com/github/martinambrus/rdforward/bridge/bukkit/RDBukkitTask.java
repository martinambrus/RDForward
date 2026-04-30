// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.Future;

/**
 * Default {@link BukkitTask} implementation. Lives in the bridge package
 * (not {@code org.bukkit}) so the public Bukkit surface stays a pure
 * interface, matching real paper-api.
 *
 * <p>Two backing flavours are supported:
 * <ul>
 *   <li>Sync — wraps an rd-api
 *       {@link com.github.martinambrus.rdforward.api.scheduler.ScheduledTask}
 *       running on the tick thread.</li>
 *   <li>Async — wraps a {@link Future} returned by
 *       {@link BukkitSchedulerAdapter}'s daemon thread pools. Cancel
 *       interrupts the future and unregisters it from the per-plugin
 *       registry so {@code cancelTasks} doesn't double-count.</li>
 * </ul>
 */
public final class RDBukkitTask implements BukkitTask {

    private final int taskId;
    private final Plugin owner;
    private final com.github.martinambrus.rdforward.api.scheduler.ScheduledTask backingSync;
    private final Future<?> backingAsync;
    private final String asyncOwnerId;
    private final BukkitSchedulerAdapter asyncAdapter;
    private final boolean sync;
    private volatile boolean cancelled;

    public RDBukkitTask(int taskId, Plugin owner,
                        com.github.martinambrus.rdforward.api.scheduler.ScheduledTask backing) {
        this.taskId = taskId;
        this.owner = owner;
        this.backingSync = backing;
        this.backingAsync = null;
        this.asyncOwnerId = null;
        this.asyncAdapter = null;
        this.sync = true;
    }

    public RDBukkitTask(int taskId, Plugin owner, Future<?> future,
                        String asyncOwnerId, BukkitSchedulerAdapter asyncAdapter) {
        this.taskId = taskId;
        this.owner = owner;
        this.backingSync = null;
        this.backingAsync = future;
        this.asyncOwnerId = asyncOwnerId;
        this.asyncAdapter = asyncAdapter;
        this.sync = false;
    }

    @Override public int getTaskId() { return taskId; }
    @Override public Plugin getOwner() { return owner; }
    @Override public boolean isCancelled() { return cancelled; }
    @Override public boolean isSync() { return sync; }

    @Override
    public void cancel() {
        if (cancelled) return;
        cancelled = true;
        if (backingSync != null) backingSync.cancel();
        if (backingAsync != null) {
            backingAsync.cancel(false);
            if (asyncAdapter != null && asyncOwnerId != null) {
                asyncAdapter.unregisterFuture(asyncOwnerId, backingAsync);
            }
        }
    }
}
