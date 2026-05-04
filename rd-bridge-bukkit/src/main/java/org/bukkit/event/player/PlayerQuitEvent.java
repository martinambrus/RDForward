// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event.player;

import org.bukkit.entity.Player;

/** Real Bukkit's {@code PlayerQuitEvent extends PlayerEvent}; HomeSpawnPlus's
 *  shaded {@code commonlib.server.bukkit.events.PlayerQuitEvent} is compiled
 *  against that contract and its constructor calls {@code super(PlayerEvent)}.
 *  Extending {@code Event} directly causes VerifyError on plugin load. */
public final class PlayerQuitEvent extends PlayerEvent {
    private String quitMessage;

    public PlayerQuitEvent(Player player) {
        this(player, "");
    }

    /** Bridge-side ctor: seed the leave message the host is about to
     *  broadcast. {@link #setQuitMessage(String)} mutates it; the bridge
     *  reads it back and returns it through
     *  {@code PLAYER_LEAVE_ANNOUNCE}. */
    public PlayerQuitEvent(Player player, String quitMessage) {
        super(player);
        this.quitMessage = quitMessage == null ? "" : quitMessage;
    }

    public String getQuitMessage() { return quitMessage; }

    public void setQuitMessage(String quitMessage) {
        this.quitMessage = quitMessage == null ? "" : quitMessage;
    }
}
