package com.github.martinambrus.rdforward.api.event.server;

import com.github.martinambrus.rdforward.api.version.ProtocolVersion;

/**
 * Called when a player has successfully connected and logged in.
 */
@FunctionalInterface
public interface PlayerJoinCallback {
    void onPlayerJoin(String playerName, ProtocolVersion clientVersion);
}
