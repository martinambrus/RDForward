// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.fabricmc.fabric.api.client.event.lifecycle.v1;

import com.github.martinambrus.rdforward.api.event.Event;

/**
 * Fabric-compatible client lifecycle events: CLIENT_STARTED and
 * CLIENT_STOPPING. Mapped onto {@code ClientEvents.CLIENT_READY} and
 * {@code ClientEvents.CLIENT_STOPPING} by {@code FabricClientBridge.install()}.
 *
 * <p>Upstream Fabric passes a {@code MinecraftClient} to each callback;
 * our shim passes no argument (we have no equivalent type). Mods needing
 * per-tick or per-world state should use {@code ClientTickEvents} instead.
 */
public final class ClientLifecycleEvents {

    private ClientLifecycleEvents() {}

    public static final Event<ClientStarted> CLIENT_STARTED = Event.create(
            () -> {},
            listeners -> () -> { for (ClientStarted l : listeners) l.onClientStarted(); }
    );

    public static final Event<ClientStopping> CLIENT_STOPPING = Event.create(
            () -> {},
            listeners -> () -> { for (ClientStopping l : listeners) l.onClientStopping(); }
    );

    @FunctionalInterface public interface ClientStarted { void onClientStarted(); }
    @FunctionalInterface public interface ClientStopping { void onClientStopping(); }
}
