package pocketmine.event.player;

import pocketmine.event.Cancellable;
import pocketmine.event.Event;

/**
 * Fired when a player sends a chat message. Cancellable — setting cancelled
 * suppresses broadcast and returns {@code EventResult.CANCEL} on the
 * rd-api side.
 */
public class PlayerChatEvent extends Event implements Cancellable {

    private final String playerName;
    private String message;

    public PlayerChatEvent(String playerName, String message) {
        this.playerName = playerName;
        this.message = message;
    }

    public String getPlayerName() { return playerName; }

    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }
}
