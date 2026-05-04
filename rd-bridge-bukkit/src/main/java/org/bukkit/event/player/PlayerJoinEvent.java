// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event.player;

import org.bukkit.entity.Player;

/** Real Bukkit's {@code PlayerJoinEvent extends PlayerEvent}; HomeSpawnPlus's
 *  shaded {@code commonlib.server.bukkit.events.PlayerJoinEvent} is compiled
 *  against that contract and its constructor calls {@code super(PlayerEvent)}.
 *  Keeping the parent as plain {@code Event} causes the JVM verifier to
 *  reject the {@code invokespecial} on load with VerifyError. */
public class PlayerJoinEvent extends PlayerEvent {
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
        super(player);
        this.joinMessage = joinMessage == null ? "" : joinMessage;
    }

    public String getJoinMessage() { return joinMessage; }

    public void setJoinMessage(String joinMessage) {
        this.joinMessage = joinMessage == null ? "" : joinMessage;
    }
}
