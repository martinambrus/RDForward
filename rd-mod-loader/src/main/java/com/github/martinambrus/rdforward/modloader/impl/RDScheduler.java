package com.github.martinambrus.rdforward.modloader.impl;

import com.github.martinambrus.rdforward.api.scheduler.ScheduledTask;
import com.github.martinambrus.rdforward.api.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter from {@link com.github.martinambrus.rdforward.server.api.Scheduler}
 * (static, server-internal) to the mod-facing {@link Scheduler}. Tags each
 * task with its owning mod id so {@link #cancelByOwner(String)} can wipe
 * every remaining task on hot-reload / unload.
 */
public final class RDScheduler implements Scheduler {

    private final Map<String, List<com.github.martinambrus.rdforward.server.api.Scheduler.ScheduledTask>> tasksByOwner
            = new ConcurrentHashMap<>();

    @Override
    public ScheduledTask runLater(String modId, int delayTicks, Runnable task) {
        var handle = com.github.martinambrus.rdforward.server.api.Scheduler.runLater(delayTicks, task);
        track(modId, handle);
        return new TaskAdapter(handle);
    }

    @Override
    public ScheduledTask runRepeating(String modId, int initialDelay, int periodTicks, Runnable task) {
        var handle = com.github.martinambrus.rdforward.server.api.Scheduler.runRepeating(initialDelay, periodTicks, task);
        track(modId, handle);
        return new TaskAdapter(handle);
    }

    @Override
    public int cancelByOwner(String modId) {
        List<com.github.martinambrus.rdforward.server.api.Scheduler.ScheduledTask> tasks = tasksByOwner.remove(modId);
        if (tasks == null) return 0;
        int cancelled = 0;
        for (var t : tasks) {
            if (!t.isCancelled()) {
                t.cancel();
                cancelled++;
            }
        }
        return cancelled;
    }

    private void track(String modId, com.github.martinambrus.rdforward.server.api.Scheduler.ScheduledTask task) {
        tasksByOwner.computeIfAbsent(modId, k -> new ArrayList<>()).add(task);
    }

    private record TaskAdapter(com.github.martinambrus.rdforward.server.api.Scheduler.ScheduledTask delegate)
            implements ScheduledTask {
        @Override public void cancel() { delegate.cancel(); }
        @Override public boolean isCancelled() { return delegate.isCancelled(); }
    }
}
