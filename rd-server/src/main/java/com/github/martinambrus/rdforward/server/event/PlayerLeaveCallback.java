package com.github.martinambrus.rdforward.server.event;

/**
 * Called when a player disconnects from the server.
 */
@FunctionalInterface
public interface PlayerLeaveCallback {
    void onPlayerLeave(String playerName);
}
