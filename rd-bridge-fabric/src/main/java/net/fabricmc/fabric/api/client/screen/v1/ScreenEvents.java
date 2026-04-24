// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.fabricmc.fabric.api.client.screen.v1;

import com.github.martinambrus.rdforward.api.client.GameScreen;
import com.github.martinambrus.rdforward.api.event.Event;

/**
 * Fabric-compatible screen event registry. Upstream Fabric distinguishes
 * BEFORE_INIT / AFTER_INIT / REMOVE. RDForward has no init/layout pipeline,
 * so every phase collapses onto a single open/close pair that forwards
 * from {@code ClientEvents.SCREEN_OPEN} / {@code SCREEN_CLOSE} via
 * {@code FabricClientBridge.install()}.
 *
 * <p>Mods registering against the non-wired phases ({@link #BEFORE_INIT},
 * {@link #AFTER_INIT}) compile and run, but the listeners never fire.
 */
public final class ScreenEvents {

    private ScreenEvents() {}

    @FunctionalInterface
    public interface Open {
        void onOpen(GameScreen screen);
    }

    @FunctionalInterface
    public interface Close {
        void onClose(GameScreen screen);
    }

    @FunctionalInterface
    public interface Noop {
        void onNoop(GameScreen screen);
    }

    /** Fires when a screen opens. Wired to {@code ClientEvents.SCREEN_OPEN}. */
    public static final Event<Open> OPEN = Event.create(
            screen -> {},
            listeners -> screen -> { for (Open l : listeners) l.onOpen(screen); }
    );

    /** Fires when a screen closes. Wired to {@code ClientEvents.SCREEN_CLOSE}. */
    public static final Event<Close> CLOSE = Event.create(
            screen -> {},
            listeners -> screen -> { for (Close l : listeners) l.onClose(screen); }
    );

    /** Noop — RDForward has no BEFORE_INIT phase. Mods compile against it safely. */
    public static final Event<Noop> BEFORE_INIT = noop();

    /** Noop — RDForward has no AFTER_INIT phase. Mods compile against it safely. */
    public static final Event<Noop> AFTER_INIT = noop();

    /** Noop — RDForward has no REMOVE phase distinct from close. */
    public static final Event<Noop> REMOVE = noop();

    private static Event<Noop> noop() {
        return Event.create(s -> {}, listeners -> s -> {});
    }
}
