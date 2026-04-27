package com.github.martinambrus.rdforward.api.event.server;

/**
 * Mirror of {@link PlayerJoinAnnounceCallback} for the leave broadcast.
 * Listeners are chained; final returned value is what the server
 * broadcasts. Returning {@code null} or empty suppresses the broadcast.
 */
@FunctionalInterface
public interface PlayerLeaveAnnounceCallback {
    String onAnnounce(String playerName, String defaultMessage);
}
