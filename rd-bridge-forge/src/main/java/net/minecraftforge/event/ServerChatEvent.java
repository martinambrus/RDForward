// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.minecraftforge.event;

import net.minecraftforge.eventbus.api.Event;

/**
 * Stub of Forge's {@code ServerChatEvent} (1.13+ replacement for
 * {@code AsyncPlayerChatEvent}). Bridge fires this on
 * {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS} when rd-api's
 * {@code CHAT} fires; cancellation bubbles back.
 */
public class ServerChatEvent extends Event {

    private final String username;
    private String message;

    public ServerChatEvent(String username, String message) {
        this.username = username;
        this.message = message;
    }

    public String getUsername() { return username; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
