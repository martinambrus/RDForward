package org.bukkit.event.player;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * Bukkit-shaped {@code PlayerMoveEvent}. RDForward does not track a
 * player's previous location, so {@link #getFrom()} returns the same
 * {@link Location} as {@link #getTo()} when the event is synthesized by
 * the bridge. Plugins that rely on {@code from != to} delta detection
 * should treat zero-delta as "no movement" and still execute.
 *
 * <p>Cancellable per upstream contract. {@link #isCancelled()} defaults
 * to {@code false}; setting it to true surfaces as {@code EventResult.CANCEL}
 * on the rd-api side.
 */
public class PlayerMoveEvent extends Event {

    private final Player player;
    private Location from;
    private Location to;
    private boolean cancelled;

    public PlayerMoveEvent(Player player, Location from, Location to) {
        this.player = player;
        this.from = from;
        this.to = to;
    }

    public Player getPlayer() { return player; }
    public Location getFrom() { return from; }
    public Location getTo() { return to; }
    public void setFrom(Location from) { this.from = from; }
    public void setTo(Location to) { this.to = to; }

    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
}
