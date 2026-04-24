package pocketmine.event.player;

import pocketmine.event.Event;

/**
 * Fired when a player finishes joining the server. Not cancellable
 * (PocketMine uses {@code PlayerPreLoginEvent} for refusal, which the
 * bridge does not mirror).
 */
public class PlayerJoinEvent extends Event {

    private final String playerName;
    private String joinMessage;

    public PlayerJoinEvent(String playerName) {
        this.playerName = playerName;
        this.joinMessage = playerName + " joined the game";
    }

    public String getPlayerName() { return playerName; }

    public String getJoinMessage() { return joinMessage; }

    public void setJoinMessage(String joinMessage) { this.joinMessage = joinMessage; }
}
