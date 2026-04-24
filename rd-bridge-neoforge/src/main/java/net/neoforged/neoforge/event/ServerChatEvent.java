package net.neoforged.neoforge.event;

/** NeoForge's chat event. Extends the Forge stub so single-bus dispatch works. */
public class ServerChatEvent extends net.minecraftforge.event.ServerChatEvent {
    public ServerChatEvent(String username, String message) {
        super(username, message);
    }
}
