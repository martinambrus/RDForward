// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.minecraftforge.event.entity.player;

import net.minecraftforge.eventbus.api.Event;

/**
 * Stub of Forge's {@code PlayerEvent} parent. Contains
 * {@link PlayerLoggedInEvent} + {@link PlayerLoggedOutEvent}. Bridge maps
 * these to rd-api {@code PLAYER_JOIN} / {@code PLAYER_LEAVE}.
 */
public class PlayerEvent extends Event {

    private final String playerName;

    public PlayerEvent(String playerName) { this.playerName = playerName; }

    public String getPlayerName() { return playerName; }

    public static class PlayerLoggedInEvent extends PlayerEvent {
        public PlayerLoggedInEvent(String playerName) { super(playerName); }
    }

    public static class PlayerLoggedOutEvent extends PlayerEvent {
        public PlayerLoggedOutEvent(String playerName) { super(playerName); }
    }
}
