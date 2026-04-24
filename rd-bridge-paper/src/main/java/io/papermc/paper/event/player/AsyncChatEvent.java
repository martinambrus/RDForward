package io.papermc.paper.event.player;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * Paper's replacement for {@code AsyncPlayerChatEvent}. Unlike the Bukkit
 * event, the message is an Adventure {@link Component}. The bridge maps
 * this to {@code ServerEvents.CHAT} by serialising the Component to legacy
 * text before dispatch and applying the same translator on any plugin
 * mutation.
 */
public class AsyncChatEvent extends Event {

    private final Player player;
    private Component message;

    public AsyncChatEvent(Player player, Component message) {
        this.player = player;
        this.message = message;
    }

    public Player getPlayer() { return player; }
    public Component message() { return message; }
    public void message(Component message) { this.message = message; }
}
