package net.fabricmc.fabric.api.event.lifecycle.v1;

import com.github.martinambrus.rdforward.api.event.Event;

/**
 * Fabric-compatible server lifecycle event registry. Upstream Fabric exposes
 * several phases (SERVER_STARTING, SERVER_STARTED, SERVER_STOPPING, SERVER_STOPPED,
 * START_DATA_PACK_RELOAD, END_DATA_PACK_RELOAD). RDForward only distinguishes
 * the started / stopping pair — data-pack reload and the STARTING / STOPPED
 * bookends collapse onto them or are exposed as noop events.
 *
 * <p>{@link #SERVER_STARTED} and {@link #SERVER_STOPPING} are wired through
 * {@code FabricServerBridge.install()} to rd-api
 * {@code ServerEvents.SERVER_STARTED} / {@code SERVER_STOPPING}. Mods
 * registering against the noop phases compile but never fire.
 */
public final class ServerLifecycleEvents {

    private ServerLifecycleEvents() {}

    @FunctionalInterface
    public interface ServerStarted {
        void onServerStarted(Object server);
    }

    @FunctionalInterface
    public interface ServerStopping {
        void onServerStopping(Object server);
    }

    @FunctionalInterface
    public interface Noop {
        void onNoop(Object server);
    }

    /** Fires after server startup — wired to rd-api ServerEvents.SERVER_STARTED. */
    public static final Event<ServerStarted> SERVER_STARTED = Event.create(
            server -> {},
            listeners -> server -> { for (ServerStarted l : listeners) l.onServerStarted(server); }
    );

    /** Fires when the server begins shutdown — wired to rd-api ServerEvents.SERVER_STOPPING. */
    public static final Event<ServerStopping> SERVER_STOPPING = Event.create(
            server -> {},
            listeners -> server -> { for (ServerStopping l : listeners) l.onServerStopping(server); }
    );

    /** Noop — RDForward has no distinct STARTING phase before STARTED. */
    public static final Event<Noop> SERVER_STARTING = noop();

    /** Noop — RDForward has no distinct STOPPED phase after STOPPING. */
    public static final Event<Noop> SERVER_STOPPED = noop();

    /** Noop — RDForward has no data-pack reload pipeline. */
    public static final Event<Noop> START_DATA_PACK_RELOAD = noop();

    /** Noop — RDForward has no data-pack reload pipeline. */
    public static final Event<Noop> END_DATA_PACK_RELOAD = noop();

    private static Event<Noop> noop() {
        return Event.create(s -> {}, listeners -> s -> {});
    }
}
