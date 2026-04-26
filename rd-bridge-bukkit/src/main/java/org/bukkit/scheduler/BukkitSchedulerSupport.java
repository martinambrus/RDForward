// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.scheduler;

/**
 * Backing-state holder for {@link BukkitScheduler} default methods.
 * Kept in a separate class because Java interfaces cannot declare
 * mutable static fields, and the legacy Bukkit-3 schedule APIs need a
 * shared counter to mint distinct positive task ids.
 */
final class BukkitSchedulerSupport {

    private BukkitSchedulerSupport() {}

    /** Synthetic task-id source. Real Bukkit returns server-managed
     *  ids; RDForward's legacy stubs only need a stable positive int
     *  per call so callers that gate on {@code id >= 0} treat the
     *  schedule as having succeeded. */
    static final java.util.concurrent.atomic.AtomicInteger NEXT_LEGACY_TASK_ID =
            new java.util.concurrent.atomic.AtomicInteger(1);
}
