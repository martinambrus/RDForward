package com.github.martinambrus.rdforward.api.event.server;

/**
 * Called when a player disconnects from the server.
 */
@FunctionalInterface
public interface PlayerLeaveCallback {
    void onPlayerLeave(String playerName);
}
