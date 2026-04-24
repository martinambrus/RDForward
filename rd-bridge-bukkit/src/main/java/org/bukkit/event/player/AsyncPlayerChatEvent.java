// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.Set;

public class AsyncPlayerChatEvent extends Event {
    private final Player player;
    private String message;

    public AsyncPlayerChatEvent(Player player, String message) {
        this.player = player;
        this.message = message;
    }

    public AsyncPlayerChatEvent(boolean async, Player player, String message,
                                Set<Player> recipients) {
        this.player = player;
        this.message = message;
    }

    public Player getPlayer() { return player; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
