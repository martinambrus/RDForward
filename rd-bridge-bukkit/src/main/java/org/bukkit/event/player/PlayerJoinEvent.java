// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public final class PlayerJoinEvent extends Event {
    private final Player player;
    private String joinMessage;

    public PlayerJoinEvent(Player player) {
        this(player, "");
    }

    /** Bridge-side ctor: seed the event with the host's default
     *  announcement so plugins that read {@code getJoinMessage()} see
     *  what the server is about to broadcast, and so silent-join
     *  listeners that call {@code setJoinMessage("")} can suppress it.
     *  The bridge dispatcher reads {@code getJoinMessage()} back after
     *  listener invocation and feeds it into the
     *  {@code PLAYER_JOIN_ANNOUNCE} return value. */
    public PlayerJoinEvent(Player player, String joinMessage) {
        this.player = player;
        this.joinMessage = joinMessage == null ? "" : joinMessage;
    }

    public Player getPlayer() { return player; }

    public String getJoinMessage() { return joinMessage; }

    public void setJoinMessage(String joinMessage) {
        this.joinMessage = joinMessage == null ? "" : joinMessage;
    }
}
