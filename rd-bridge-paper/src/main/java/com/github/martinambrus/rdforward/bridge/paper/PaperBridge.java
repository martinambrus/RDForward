package com.github.martinambrus.rdforward.bridge.paper;

import com.github.martinambrus.rdforward.api.server.Server;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge;

/**
 * Facade entry point for the Paper bridge. Delegates the core Bukkit-shaped
 * server install to {@link BukkitBridge} — Paper plugins still expect
 * {@code Bukkit.getServer()} to return a live object — and tracks its own
 * installation flag so {@link #uninstall()} can idempotently revert.
 *
 * <p>Adventure stubs and Paper lifecycle stubs are surfaced to plugins via
 * the compiled bridge jar; no runtime wiring is required for those — they
 * are plain classes that resolve once the jar is on the classloader.
 */
public final class PaperBridge {

    private static volatile boolean installed;

    private PaperBridge() {}

    public static synchronized void install(Server rdServer) {
        if (installed) return;
        BukkitBridge.install(rdServer);
        installed = true;
    }

    public static synchronized void uninstall() {
        if (!installed) return;
        BukkitBridge.uninstall();
        installed = false;
    }

    public static boolean isInstalled() { return installed; }
}
