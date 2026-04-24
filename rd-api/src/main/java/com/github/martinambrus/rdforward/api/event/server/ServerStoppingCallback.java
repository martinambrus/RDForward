package com.github.martinambrus.rdforward.api.event.server;

/**
 * Called once when the server begins shutdown, before listeners and
 * scheduled tasks are torn down.
 */
@FunctionalInterface
public interface ServerStoppingCallback {
    void onServerStopping();
}
