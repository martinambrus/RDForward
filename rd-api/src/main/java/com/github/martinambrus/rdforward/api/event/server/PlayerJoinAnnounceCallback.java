package com.github.martinambrus.rdforward.api.event.server;

import com.github.martinambrus.rdforward.api.version.ProtocolVersion;

/**
 * Fired BEFORE a join is broadcast so listeners (chiefly the Bukkit
 * bridge dispatching {@code PlayerJoinEvent}) can override or suppress
 * the announce message.
 *
 * <p>Listeners are chained: the message returned by listener N is the
 * default passed to listener N+1. The final returned value is what the
 * server broadcasts. Returning {@code null} or an empty string from the
 * final listener suppresses the broadcast entirely. Plain post-join
 * listeners (e.g. block-owner cleanup, pong updaters) stay on the
 * existing {@link PlayerJoinCallback} which fires AFTER the announce.
 */
@FunctionalInterface
public interface PlayerJoinAnnounceCallback {
    String onAnnounce(String playerName, ProtocolVersion clientVersion, String defaultMessage);
}
