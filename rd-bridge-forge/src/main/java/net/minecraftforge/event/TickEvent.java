// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.minecraftforge.event;

import net.minecraftforge.eventbus.api.Event;

/**
 * Stub of Forge's {@code TickEvent} parent. Contains {@link ServerTickEvent}
 * and {@link ClientTickEvent} nested types plus the {@link Phase} enum. The
 * bridge fires {@link ServerTickEvent} once per rd-api {@code SERVER_TICK}
 * with phase {@link Phase#START}.
 */
public class TickEvent extends Event {

    public final Phase phase;

    public TickEvent(Phase phase) { this.phase = phase; }

    public Phase getPhase() { return phase; }

    public enum Phase { START, END }

    public static class ServerTickEvent extends TickEvent {
        public ServerTickEvent(Phase phase) { super(phase); }
    }

    public static class ClientTickEvent extends TickEvent {
        public ClientTickEvent(Phase phase) { super(phase); }
    }
}
