// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.neoforged.neoforge.event;

/**
 * NeoForge's tick event. Extends the Forge parent; nested types mirror
 * Forge's layout so mod source that imported from either package compiles.
 */
public class TickEvent extends net.minecraftforge.event.TickEvent {

    public TickEvent(net.minecraftforge.event.TickEvent.Phase phase) { super(phase); }

    public static class ServerTickEvent extends net.minecraftforge.event.TickEvent.ServerTickEvent {
        public ServerTickEvent(net.minecraftforge.event.TickEvent.Phase phase) { super(phase); }
    }

    public static class ClientTickEvent extends net.minecraftforge.event.TickEvent.ClientTickEvent {
        public ClientTickEvent(net.minecraftforge.event.TickEvent.Phase phase) { super(phase); }
    }
}
