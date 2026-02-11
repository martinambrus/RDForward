package com.github.martinambrus.rdforward.server.event;

/**
 * Called when a player position update is received.
 * Coordinates are in fixed-point format (divide by 32 for blocks).
 */
@FunctionalInterface
public interface PlayerMoveCallback {
    void onPlayerMove(String playerName, short x, short y, short z, byte yaw, byte pitch);
}
