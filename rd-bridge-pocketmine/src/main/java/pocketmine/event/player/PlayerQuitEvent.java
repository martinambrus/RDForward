package pocketmine.event.player;

import pocketmine.event.Event;

/**
 * Fired when a player disconnects. Not cancellable.
 */
public class PlayerQuitEvent extends Event {

    private final String playerName;
    private String quitMessage;

    public PlayerQuitEvent(String playerName) {
        this.playerName = playerName;
        this.quitMessage = playerName + " left the game";
    }

    public String getPlayerName() { return playerName; }

    public String getQuitMessage() { return quitMessage; }

    public void setQuitMessage(String quitMessage) { this.quitMessage = quitMessage; }
}
