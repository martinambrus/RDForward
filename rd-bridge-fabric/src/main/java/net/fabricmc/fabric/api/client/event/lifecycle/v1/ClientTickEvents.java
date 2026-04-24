// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.fabricmc.fabric.api.client.event.lifecycle.v1;

import com.github.martinambrus.rdforward.api.event.Event;

/**
 * Fabric-compatible client tick events. Fires each game tick (20 TPS).
 *
 * <p>Signatures deviate from upstream Fabric: the callbacks take no
 * arguments (upstream passes {@code MinecraftClient} / {@code ClientWorld};
 * neither exists in RDForward). Forked as separate START/END events so
 * mods written against Fabric compile; both fire from the same
 * {@code ClientEvents.CLIENT_TICK} forwarder, with START running before
 * END within a single tick.
 *
 * <p>Wired through {@code FabricClientBridge.install()}.
 */
public final class ClientTickEvents {

    private ClientTickEvents() {}

    public static final Event<StartTick> START_CLIENT_TICK = Event.create(
            () -> {},
            listeners -> () -> { for (StartTick l : listeners) l.onStartTick(); }
    );

    public static final Event<EndTick> END_CLIENT_TICK = Event.create(
            () -> {},
            listeners -> () -> { for (EndTick l : listeners) l.onEndTick(); }
    );

    @FunctionalInterface public interface StartTick { void onStartTick(); }
    @FunctionalInterface public interface EndTick { void onEndTick(); }
}
