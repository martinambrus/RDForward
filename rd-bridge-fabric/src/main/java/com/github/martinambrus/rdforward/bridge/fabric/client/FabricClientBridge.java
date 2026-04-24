package com.github.martinambrus.rdforward.bridge.fabric.client;

import com.github.martinambrus.rdforward.api.client.ClientEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;

/**
 * Installs forwarders from rd-client's {@code ClientEvents} to the Fabric
 * client callback classes. Mods register against the Fabric-shaped
 * callbacks ({@link HudRenderCallback#EVENT},
 * {@link ClientTickEvents#END_CLIENT_TICK}, {@link ClientLifecycleEvents#CLIENT_STARTED},
 * etc.) and the forwarder pushes each invocation on rd-client's dispatch
 * down to every registered Fabric listener.
 *
 * <p>Idempotent — {@link #install()} can be called multiple times; the
 * bridge only attaches listeners on the first call. {@link #uninstall()}
 * detaches them (tests need this so state does not bleed between cases).
 *
 * <p>Must be called on the client host before any Fabric {@code
 * ClientModInitializer#onInitializeClient()} runs so their registrations
 * see a fully-wired dispatch. The RDForward Fabric plugin loader handles
 * this in production; tests invoke it explicitly.
 */
public final class FabricClientBridge {

    private FabricClientBridge() {}

    private static ClientEvents.RenderHud hudForwarder;
    private static ClientEvents.ClientTick tickForwarder;
    private static ClientEvents.ClientReady readyForwarder;
    private static ClientEvents.ClientStopping stoppingForwarder;
    private static ClientEvents.RenderWorld worldForwarder;
    private static ClientEvents.ScreenOpen screenOpenForwarder;
    private static ClientEvents.ScreenClose screenCloseForwarder;
    private static volatile boolean installed;

    public static synchronized void install() {
        if (installed) return;

        hudForwarder = ctx -> HudRenderCallback.EVENT.invoker().onHudRender(ctx, 0f);
        tickForwarder = () -> {
            ClientTickEvents.START_CLIENT_TICK.invoker().onStartTick();
            ClientTickEvents.END_CLIENT_TICK.invoker().onEndTick();
        };
        readyForwarder = () -> ClientLifecycleEvents.CLIENT_STARTED.invoker().onClientStarted();
        stoppingForwarder = () -> ClientLifecycleEvents.CLIENT_STOPPING.invoker().onClientStopping();
        worldForwarder = partialTick -> {
            WorldRenderContext wrc = new WorldRenderContext(partialTick);
            WorldRenderEvents.LAST.invoker().onLast(wrc);
            WorldRenderEvents.END.invoker().onEnd(wrc);
        };
        screenOpenForwarder = screen -> ScreenEvents.OPEN.invoker().onOpen(screen);
        screenCloseForwarder = screen -> ScreenEvents.CLOSE.invoker().onClose(screen);

        ClientEvents.RENDER_HUD.register(hudForwarder);
        ClientEvents.CLIENT_TICK.register(tickForwarder);
        ClientEvents.CLIENT_READY.register(readyForwarder);
        ClientEvents.CLIENT_STOPPING.register(stoppingForwarder);
        ClientEvents.RENDER_WORLD.register(worldForwarder);
        ClientEvents.SCREEN_OPEN.register(screenOpenForwarder);
        ClientEvents.SCREEN_CLOSE.register(screenCloseForwarder);

        installed = true;
    }

    public static synchronized void uninstall() {
        if (!installed) return;
        ClientEvents.RENDER_HUD.unregister(hudForwarder);
        ClientEvents.CLIENT_TICK.unregister(tickForwarder);
        ClientEvents.CLIENT_READY.unregister(readyForwarder);
        ClientEvents.CLIENT_STOPPING.unregister(stoppingForwarder);
        ClientEvents.RENDER_WORLD.unregister(worldForwarder);
        ClientEvents.SCREEN_OPEN.unregister(screenOpenForwarder);
        ClientEvents.SCREEN_CLOSE.unregister(screenCloseForwarder);
        hudForwarder = null;
        tickForwarder = null;
        readyForwarder = null;
        stoppingForwarder = null;
        worldForwarder = null;
        screenOpenForwarder = null;
        screenCloseForwarder = null;
        installed = false;
    }

    public static boolean isInstalled() { return installed; }
}
