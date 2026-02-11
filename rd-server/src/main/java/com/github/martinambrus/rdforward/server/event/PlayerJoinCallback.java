package com.github.martinambrus.rdforward.server.event;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;

/**
 * Called when a player has successfully connected and logged in.
 */
@FunctionalInterface
public interface PlayerJoinCallback {
    void onPlayerJoin(String playerName, ProtocolVersion clientVersion);
}
