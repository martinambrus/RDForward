// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public final class PlayerQuitEvent extends Event {
    private final Player player;
    private String quitMessage;

    public PlayerQuitEvent(Player player) {
        this(player, "");
    }

    /** Bridge-side ctor: seed the leave message the host is about to
     *  broadcast. {@link #setQuitMessage(String)} mutates it; the bridge
     *  reads it back and returns it through
     *  {@code PLAYER_LEAVE_ANNOUNCE}. */
    public PlayerQuitEvent(Player player, String quitMessage) {
        this.player = player;
        this.quitMessage = quitMessage == null ? "" : quitMessage;
    }

    public Player getPlayer() { return player; }

    public String getQuitMessage() { return quitMessage; }

    public void setQuitMessage(String quitMessage) {
        this.quitMessage = quitMessage == null ? "" : quitMessage;
    }
}
