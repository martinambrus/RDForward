package com.github.martinambrus.rdforward.api;

import com.github.martinambrus.rdforward.api.server.Server;

/**
 * Static entry point for mods needing access to the server outside of
 * {@code ServerMod.onEnable(Server)}. The mod loader assigns the server
 * instance at startup before any mod runs.
 */
public final class RDForward {

    private RDForward() {}

    private static volatile Server server;

    /** Set by the mod loader during startup. Throws on double-init. */
    public static void initServer(Server instance) {
        if (server != null) {
            throw new IllegalStateException("Server already initialized");
        }
        server = instance;
    }

    /** @return the active server, or null if the mod loader has not finished startup. */
    public static Server getServer() {
        return server;
    }

    /** Clear the server reference. Called by the mod loader at shutdown. */
    public static void shutdown() {
        server = null;
    }
}
