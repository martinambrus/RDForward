package net.fabricmc.fabric.api.client.rendering.v1;

import com.github.martinambrus.rdforward.api.event.Event;

/**
 * Fabric-compatible world-render event registry. Upstream Fabric offers
 * seven phases (START, AFTER_SETUP, BEFORE_ENTITIES, ...); RDForward only
 * exposes a single "after 3D world render" hook, so the upstream phases
 * all collapse onto {@link #LAST} / {@link #END}. The unused phases are
 * provided as noop {@link Event} instances so mods that register against
 * them compile and run without error.
 *
 * <p>{@link #LAST} and {@link #END} are wired through
 * {@code FabricClientBridge.install()} to
 * {@code ClientEvents.RENDER_WORLD}.
 */
public final class WorldRenderEvents {

    private WorldRenderEvents() {}

    @FunctionalInterface
    public interface Last {
        void onLast(WorldRenderContext context);
    }

    @FunctionalInterface
    public interface End {
        void onEnd(WorldRenderContext context);
    }

    @FunctionalInterface
    public interface Noop {
        void onNoop(WorldRenderContext context);
    }

    /** Fires right after the 3D world is rendered — wired to RENDER_WORLD. */
    public static final Event<Last> LAST = Event.create(
            ctx -> {},
            listeners -> ctx -> { for (Last l : listeners) l.onLast(ctx); }
    );

    /** Fires right before buffer swap — in RDForward, same hook as LAST. */
    public static final Event<End> END = Event.create(
            ctx -> {},
            listeners -> ctx -> { for (End l : listeners) l.onEnd(ctx); }
    );

    /**
     * Noop events corresponding to phases RDForward does not surface.
     * Registering listeners here compiles but the listeners never fire.
     */
    public static final Event<Noop> START = noop();
    public static final Event<Noop> AFTER_SETUP = noop();
    public static final Event<Noop> BEFORE_ENTITIES = noop();
    public static final Event<Noop> AFTER_ENTITIES = noop();
    public static final Event<Noop> BEFORE_BLOCK_OUTLINE = noop();
    public static final Event<Noop> BLOCK_OUTLINE = noop();
    public static final Event<Noop> BEFORE_DEBUG_RENDER = noop();
    public static final Event<Noop> AFTER_TRANSLUCENT = noop();

    private static Event<Noop> noop() {
        return Event.create(ctx -> {}, listeners -> ctx -> {});
    }
}
