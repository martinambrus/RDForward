// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.scheduler;

import org.bukkit.plugin.Plugin;

/**
 * Bukkit-shaped task handle. Real Bukkit declares this as an
 * {@code interface}; older plugins (VanishNoPacket 3.15's
 * {@code Metrics.start} via {@code invokeinterface}) crash with
 * {@link IncompatibleClassChangeError} when the bridge surface is a
 * concrete class. The implementation lives in {@link
 * com.github.martinambrus.rdforward.bridge.bukkit.RDBukkitTask}.
 */
public interface BukkitTask {

    int getTaskId();

    Plugin getOwner();

    boolean isCancelled();

    /** @return {@code true} if the task is queued to run on the
     *  Bukkit server thread (synchronous). RDForward runs every
     *  scheduler callback on the tick thread, so this is always
     *  {@code true}. */
    default boolean isSync() { return true; }

    /** Cancel the underlying scheduled task. Idempotent. */
    void cancel();
}
