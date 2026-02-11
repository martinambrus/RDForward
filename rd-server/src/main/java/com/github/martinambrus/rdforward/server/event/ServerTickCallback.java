package com.github.martinambrus.rdforward.server.event;

/**
 * Called every server tick (20 TPS, 50ms interval).
 */
@FunctionalInterface
public interface ServerTickCallback {
    void onServerTick(long tickCount);
}
