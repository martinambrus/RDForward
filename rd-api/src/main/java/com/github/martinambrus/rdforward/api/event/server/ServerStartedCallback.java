package com.github.martinambrus.rdforward.api.event.server;

/**
 * Called once after the server finishes startup, before it begins
 * accepting connections.
 */
@FunctionalInterface
public interface ServerStartedCallback {
    void onServerStarted();
}
