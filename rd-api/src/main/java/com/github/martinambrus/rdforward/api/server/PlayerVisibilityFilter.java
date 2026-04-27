package com.github.martinambrus.rdforward.api.server;

/**
 * Pluggable per-pair visibility check the host consults before
 * broadcasting any packet that reveals one player's presence to
 * another (spawn, despawn, position, tab list entry).
 *
 * <p>The Bukkit bridge installs the canonical implementation: it
 * consults a per-recipient set populated by
 * {@code Player.hidePlayer(target)} / {@code showPlayer(target)} so
 * VanishNoPacket and similar plugins running without ProtocolLib can
 * actually drop the vanished player from each recipient's view.
 *
 * <p>Returning {@code true} means {@code recipient} should receive
 * packets that reveal {@code sender}'s presence (the default). Return
 * {@code false} to suppress those packets for that pair.
 *
 * <p>Names are passed in their on-the-wire form (case-sensitive). Filter
 * implementations that need case-insensitive matching should normalise
 * inside.
 */
@FunctionalInterface
public interface PlayerVisibilityFilter {
    boolean isVisible(String senderName, String recipientName);
}
