// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.api.scheduler.ScheduledTask;
import com.github.martinambrus.rdforward.api.scheduler.Scheduler;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Wraps the rd-api {@link Scheduler} as a Bukkit-shaped
 * {@link BukkitScheduler}. Sync variants tag ownership via the owning
 * plugin's name so rd-mod-loader can sweep tasks on mod unload.
 *
 * <p>Async variants run on a dedicated daemon thread pool — required
 * because plugins (notably EssentialsX's {@code UpdateChecker}) call
 * {@code CompletableFuture.join()} inside async tasks; running them on
 * the tick thread blocks the entire server while the HTTP request
 * resolves (observed as a 30s login stall on Windows where DNS is
 * slower than on Linux). {@link #cancelTasks(Plugin)} cancels both the
 * sync queue entries and any in-flight / scheduled async futures owned
 * by the plugin.
 */
public final class BukkitSchedulerAdapter implements BukkitScheduler {

    private final Scheduler backing;
    private final AtomicInteger taskIds = new AtomicInteger(1);

    private final ExecutorService asyncPool;
    private final ScheduledExecutorService asyncScheduler;
    private final ConcurrentMap<String, Set<Future<?>>> asyncFutures = new ConcurrentHashMap<>();

    public BukkitSchedulerAdapter(Scheduler backing) {
        this.backing = backing;
        AtomicInteger poolN = new AtomicInteger(1);
        ThreadFactory poolTf = r -> {
            Thread t = new Thread(r, "BukkitAsync-" + poolN.getAndIncrement());
            t.setDaemon(true);
            return t;
        };
        this.asyncPool = Executors.newCachedThreadPool(poolTf);
        AtomicInteger schedN = new AtomicInteger(1);
        ThreadFactory schedTf = r -> {
            Thread t = new Thread(r, "BukkitAsyncSched-" + schedN.getAndIncrement());
            t.setDaemon(true);
            return t;
        };
        this.asyncScheduler = Executors.newScheduledThreadPool(2, schedTf);
    }

    private String ownerId(Plugin plugin) {
        return plugin == null || plugin.getName() == null ? "__anonymous__" : plugin.getName();
    }

    private void registerFuture(String owner, Future<?> f) {
        asyncFutures.computeIfAbsent(owner, k -> ConcurrentHashMap.newKeySet()).add(f);
    }

    void unregisterFuture(String owner, Future<?> f) {
        Set<Future<?>> set = asyncFutures.get(owner);
        if (set != null) set.remove(f);
    }

    private Runnable wrapAsync(String owner, Runnable task) {
        return () -> {
            try {
                task.run();
            } catch (Throwable t) {
                System.err.println("[BukkitAsync] Task error in plugin '" + owner + "': " + t);
                t.printStackTrace();
            }
        };
    }

    @Override
    public BukkitTask runTask(Plugin plugin, Runnable task) {
        ScheduledTask st = backing.runLater(ownerId(plugin), 0, task);
        return new RDBukkitTask(taskIds.getAndIncrement(), plugin, st);
    }

    @Override
    public BukkitTask runTaskLater(Plugin plugin, Runnable task, long delayTicks) {
        int delay = delayTicks < 0 ? 0 : (int) Math.min(Integer.MAX_VALUE, delayTicks);
        ScheduledTask st = backing.runLater(ownerId(plugin), delay, task);
        return new RDBukkitTask(taskIds.getAndIncrement(), plugin, st);
    }

    @Override
    public BukkitTask runTaskTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
        int delay = delayTicks < 0 ? 0 : (int) Math.min(Integer.MAX_VALUE, delayTicks);
        int period = periodTicks < 1 ? 1 : (int) Math.min(Integer.MAX_VALUE, periodTicks);
        ScheduledTask st = backing.runRepeating(ownerId(plugin), delay, period, task);
        return new RDBukkitTask(taskIds.getAndIncrement(), plugin, st);
    }

    @Override
    public BukkitTask runTaskAsynchronously(Plugin plugin, Runnable task) {
        String owner = ownerId(plugin);
        Runnable wrapped = wrapAsync(owner, task);
        Future<?>[] holder = new Future<?>[1];
        Future<?> f = asyncPool.submit(() -> {
            try {
                wrapped.run();
            } finally {
                if (holder[0] != null) unregisterFuture(owner, holder[0]);
            }
        });
        holder[0] = f;
        registerFuture(owner, f);
        return new RDBukkitTask(taskIds.getAndIncrement(), plugin, f, owner, this);
    }

    @Override
    public BukkitTask runTaskLaterAsynchronously(Plugin plugin, Runnable task, long delayTicks) {
        String owner = ownerId(plugin);
        Runnable wrapped = wrapAsync(owner, task);
        long delayMs = Math.max(0L, delayTicks) * 50L;
        Future<?>[] holder = new Future<?>[1];
        Future<?> f = asyncScheduler.schedule(() -> {
            try {
                wrapped.run();
            } finally {
                if (holder[0] != null) unregisterFuture(owner, holder[0]);
            }
        }, delayMs, TimeUnit.MILLISECONDS);
        holder[0] = f;
        registerFuture(owner, f);
        return new RDBukkitTask(taskIds.getAndIncrement(), plugin, f, owner, this);
    }

    @Override
    public BukkitTask runTaskTimerAsynchronously(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
        String owner = ownerId(plugin);
        Runnable wrapped = wrapAsync(owner, task);
        long delayMs = Math.max(0L, delayTicks) * 50L;
        long periodMs = Math.max(1L, periodTicks) * 50L;
        Future<?> f = asyncScheduler.scheduleAtFixedRate(wrapped, delayMs, periodMs, TimeUnit.MILLISECONDS);
        registerFuture(owner, f);
        return new RDBukkitTask(taskIds.getAndIncrement(), plugin, f, owner, this);
    }

    @Override
    public void cancelTasks(Plugin plugin) {
        String owner = ownerId(plugin);
        backing.cancelByOwner(owner);
        Set<Future<?>> set = asyncFutures.remove(owner);
        if (set != null) {
            for (Future<?> f : set) {
                f.cancel(false);
            }
        }
    }

    /** Stop the async pools and drop pending futures. Called from
     *  {@link BukkitBridge#uninstall()} so a subsequent install gets
     *  a fresh executor pair (otherwise reusing a shut-down adapter
     *  would reject every async submission). */
    public void shutdown() {
        asyncPool.shutdownNow();
        asyncScheduler.shutdownNow();
        asyncFutures.clear();
    }
}
