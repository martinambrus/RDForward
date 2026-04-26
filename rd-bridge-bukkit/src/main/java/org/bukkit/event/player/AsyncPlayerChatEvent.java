// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event.player;

import org.bukkit.entity.Player;

import java.util.Set;

/**
 * Stub of Bukkit's {@code AsyncPlayerChatEvent}. Extends
 * {@link PlayerEvent} so plugin bytecode that hands the event to helpers
 * typed against {@code PlayerEvent} (LoginSecurity's
 * {@code PlayerListener.onPlayerChat}) verifies cleanly.
 */
public class AsyncPlayerChatEvent extends PlayerEvent {

    private String message;

    public AsyncPlayerChatEvent(Player player, String message) {
        super(player);
        this.message = message;
    }

    public AsyncPlayerChatEvent(boolean async, Player player, String message,
                                Set<Player> recipients) {
        super(player, async);
        this.message = message;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
