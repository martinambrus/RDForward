package com.github.martinambrus.rdforward.bridge.neoforge;

import com.github.martinambrus.rdforward.api.server.Server;
import com.github.martinambrus.rdforward.bridge.forge.ForgeBridge;

/**
 * Thin facade. NeoForge's {@code NeoForge.EVENT_BUS} is the same instance
 * as Forge's {@code MinecraftForge.EVENT_BUS}, and every NeoForge event
 * class extends its Forge counterpart, so the Forge bridge's rd-api
 * forwarders already feed NeoForge subscribers correctly. This wrapper
 * just delegates install/uninstall.
 */
public final class NeoForgeBridge {

    private NeoForgeBridge() {}

    public static synchronized void install(Server rdServer) {
        ForgeBridge.install(rdServer);
    }

    public static synchronized void uninstall() {
        ForgeBridge.uninstall();
    }

    public static boolean isInstalled() { return ForgeBridge.isInstalled(); }
}
