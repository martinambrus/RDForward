package com.github.martinambrus.rdforward.bridge.fabric.server;

import com.github.martinambrus.rdforward.api.command.CommandRegistry;
import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.api.event.server.ServerStartedCallback;
import com.github.martinambrus.rdforward.api.event.server.ServerStoppingCallback;
import com.github.martinambrus.rdforward.api.event.server.ServerTickCallback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

/**
 * Installs forwarders from rd-api's server events to the Fabric server
 * callback classes. Mods register against the Fabric-shaped events
 * ({@link ServerLifecycleEvents#SERVER_STARTED},
 * {@link ServerTickEvents#END_SERVER_TICK}, etc.) and the forwarder pushes
 * each invocation on rd-api's dispatch down to every registered Fabric
 * listener.
 *
 * <p>Idempotent — {@link #install()} can be called multiple times; the
 * bridge only attaches listeners on the first call. {@link #uninstall()}
 * detaches them (tests need this so state does not bleed between cases).
 *
 * <p>{@link #fireCommandRegistration(CommandRegistry)} is a host entry
 * point: call once the real {@link CommandRegistry} is wired so listeners
 * registered against {@link CommandRegistrationCallback#EVENT} run against
 * a live registry.
 */
public final class FabricServerBridge {

    private FabricServerBridge() {}

    private static ServerStartedCallback startedForwarder;
    private static ServerStoppingCallback stoppingForwarder;
    private static ServerTickCallback tickForwarder;
    private static volatile boolean installed;

    public static synchronized void install() {
        if (installed) return;

        startedForwarder = () -> ServerLifecycleEvents.SERVER_STARTED.invoker().onServerStarted(null);
        stoppingForwarder = () -> ServerLifecycleEvents.SERVER_STOPPING.invoker().onServerStopping(null);
        tickForwarder = tickCount -> {
            ServerTickEvents.START_SERVER_TICK.invoker().onStartTick(null);
            ServerTickEvents.END_SERVER_TICK.invoker().onEndTick(null);
        };

        ServerEvents.SERVER_STARTED.register(startedForwarder);
        ServerEvents.SERVER_STOPPING.register(stoppingForwarder);
        ServerEvents.SERVER_TICK.register(tickForwarder);

        installed = true;
    }

    public static synchronized void uninstall() {
        if (!installed) return;
        ServerEvents.SERVER_STARTED.unregister(startedForwarder);
        ServerEvents.SERVER_STOPPING.unregister(stoppingForwarder);
        ServerEvents.SERVER_TICK.unregister(tickForwarder);
        startedForwarder = null;
        stoppingForwarder = null;
        tickForwarder = null;
        installed = false;
    }

    public static boolean isInstalled() { return installed; }

    /**
     * Fire {@link CommandRegistrationCallback#EVENT} against {@code registry}.
     * The server host calls this once the real command registry is ready so
     * mods registered at {@code onInitialize} time see a live registry.
     */
    public static void fireCommandRegistration(CommandRegistry registry) {
        CommandRegistrationCallback.EVENT.invoker().register(registry);
    }
}
