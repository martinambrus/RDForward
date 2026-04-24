// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.fabricmc.fabric.api.event.lifecycle.v1;

import com.github.martinambrus.rdforward.api.event.Event;

/**
 * Fabric-compatible server tick event registry. Upstream distinguishes
 * {@link #START_SERVER_TICK} / {@link #END_SERVER_TICK} and provides world
 * scoped variants. RDForward has a single {@code ServerEvents.SERVER_TICK}
 * hook, so {@code FabricServerBridge.install()} forwards every tick to both
 * the START and END invokers. The world-scoped phases are noops.
 */
public final class ServerTickEvents {

    private ServerTickEvents() {}

    @FunctionalInterface
    public interface StartTick {
        void onStartTick(Object server);
    }

    @FunctionalInterface
    public interface EndTick {
        void onEndTick(Object server);
    }

    @FunctionalInterface
    public interface StartWorldTick {
        void onStartTick(Object world);
    }

    @FunctionalInterface
    public interface EndWorldTick {
        void onEndTick(Object world);
    }

    /** Fires at the start of every tick — wired to rd-api ServerEvents.SERVER_TICK. */
    public static final Event<StartTick> START_SERVER_TICK = Event.create(
            server -> {},
            listeners -> server -> { for (StartTick l : listeners) l.onStartTick(server); }
    );

    /** Fires at the end of every tick — wired to rd-api ServerEvents.SERVER_TICK. */
    public static final Event<EndTick> END_SERVER_TICK = Event.create(
            server -> {},
            listeners -> server -> { for (EndTick l : listeners) l.onEndTick(server); }
    );

    /** Noop — RDForward does not surface a per-world tick hook. */
    public static final Event<StartWorldTick> START_WORLD_TICK = Event.create(
            world -> {},
            listeners -> world -> {}
    );

    /** Noop — RDForward does not surface a per-world tick hook. */
    public static final Event<EndWorldTick> END_WORLD_TICK = Event.create(
            world -> {},
            listeners -> world -> {}
    );
}
