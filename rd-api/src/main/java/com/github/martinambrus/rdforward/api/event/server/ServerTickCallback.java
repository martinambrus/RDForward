package com.github.martinambrus.rdforward.api.event.server;

/**
 * Called every server tick (20 TPS, 50ms interval).
 */
@FunctionalInterface
public interface ServerTickCallback {
    void onServerTick(long tickCount);
}
