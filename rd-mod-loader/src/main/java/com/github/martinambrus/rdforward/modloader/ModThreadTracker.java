package com.github.martinambrus.rdforward.modloader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Tracks threads spawned by mods so that hot-reload and unload can stop
 * them deterministically. Per plan §3.2.5, threads created through this
 * tracker are tagged with the owning mod id; {@link #stopThreadsOwnedBy}
 * interrupts them, joins for up to 5 seconds, and warns about any that
 * refuse to terminate.
 *
 * <p>Usage: mods never call this directly — the {@code ModLoader} sets a
 * thread-local owner via {@link com.github.martinambrus.rdforward.api.event.EventOwnership}
 * during {@code onEnable}, and the {@link #createThread} helper tags the
 * thread accordingly. Mods may obtain the tracker through
 * {@link ModSystem#threadTracker()} if they need explicit thread control.
 */
public final class ModThreadTracker {

    private static final Logger LOG = Logger.getLogger(ModThreadTracker.class.getName());
    private static final long JOIN_TIMEOUT_MS = 5_000L;

    private final Map<Thread, String> threadOwners = new ConcurrentHashMap<>();

    /**
     * Create a daemon thread owned by {@code modId}. The thread is NOT started.
     * JVM-shutdown safety: threads created by this tracker are daemons so a
     * stuck mod thread cannot prevent the JVM from exiting on Ctrl+C.
     */
    public Thread createThread(String modId, Runnable task, String name) {
        Thread t = new Thread(task, modId + "/" + name);
        t.setDaemon(true);
        threadOwners.put(t, modId);
        return t;
    }

    /**
     * Interrupt and join all threads owned by {@code modId}. Phase 1: send
     * {@link Thread#interrupt()} to every live thread. Phase 2: join each
     * for up to 5 seconds. Phase 3: warn on threads that did not terminate.
     * @return the number of threads that were alive when this method was called.
     */
    public int stopThreadsOwnedBy(String modId) {
        List<Thread> modThreads = new ArrayList<>();
        for (Map.Entry<Thread, String> e : threadOwners.entrySet()) {
            if (e.getValue().equals(modId) && e.getKey().isAlive()) modThreads.add(e.getKey());
        }
        for (Thread t : modThreads) t.interrupt();
        for (Thread t : modThreads) {
            try { t.join(JOIN_TIMEOUT_MS); } catch (InterruptedException ignored) {}
            if (t.isAlive()) {
                LOG.warning("[ModLoader] WARNING: Thread '" + t.getName()
                        + "' from mod " + modId + " did not terminate within "
                        + (JOIN_TIMEOUT_MS / 1000) + "s");
            }
        }
        for (Thread t : modThreads) threadOwners.remove(t);
        // Also drop any dead threads still tracked to keep the map from growing.
        threadOwners.entrySet().removeIf(e -> !e.getKey().isAlive());
        return modThreads.size();
    }

    /** Snapshot of current tracking — used by admin tooling and tests. */
    public Map<Thread, String> snapshot() {
        return new java.util.HashMap<>(threadOwners);
    }
}
