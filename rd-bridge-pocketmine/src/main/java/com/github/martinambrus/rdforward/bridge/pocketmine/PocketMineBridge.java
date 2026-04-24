package com.github.martinambrus.rdforward.bridge.pocketmine;

import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.api.server.Server;
import pocketmine.event.Listener;
import pocketmine.plugin.Plugin;
import pocketmine.plugin.PluginManager;

import java.util.logging.Logger;

/**
 * Installs the PocketMine bridge facade. Once installed,
 * {@code pocketmine.Server.getInstance()} returns a
 * {@link BridgeServer} pointing at the live rd-api {@link Server};
 * plugin calls like {@code Server.getInstance().getPluginManager().registerEvents}
 * forward to {@link PocketMineEventAdapter}.
 *
 * <p>Idempotent. Calling {@code install} twice without an intervening
 * {@code uninstall} is a no-op on the second call.
 */
public final class PocketMineBridge {

    private static final Logger LOG = Logger.getLogger("RDForward/PocketMineBridge");

    private PocketMineBridge() {}

    private static boolean installed;
    private static BridgeServer bridgeServer;

    public static synchronized void install(Server rdServer) {
        if (installed) return;
        bridgeServer = new BridgeServer(rdServer);
        pocketmine.Server.setInstance(bridgeServer);
        installed = true;
    }

    public static synchronized void uninstall() {
        if (!installed) return;
        pocketmine.Server.setInstance(null);
        ServerEvents.clearAll();
        bridgeServer = null;
        installed = false;
    }

    public static boolean isInstalled() { return installed; }

    /**
     * Narrow bridge-side implementation of {@link pocketmine.Server}.
     * Only the two methods the plan lists as in-scope are wired —
     * {@code getPluginManager()} and {@code broadcastMessage}.
     */
    static final class BridgeServer implements pocketmine.Server {

        private final Server rdServer;
        private final PluginManager pluginManager;

        BridgeServer(Server rdServer) {
            this.rdServer = rdServer;
            this.pluginManager = new BridgePluginManager();
        }

        @Override public PluginManager getPluginManager() { return pluginManager; }

        @Override
        public void broadcastMessage(String message) {
            if (rdServer != null) rdServer.broadcastMessage(message);
            else LOG.info("[PocketMineBridge] broadcast (no rd-api server bound): " + message);
        }
    }

    /**
     * Dispatches {@code registerEvents} to {@link PocketMineEventAdapter}
     * using the supplied plugin's name as the rd-api event-owner tag.
     */
    static final class BridgePluginManager implements PluginManager {
        @Override
        public void registerEvents(Listener listener, Plugin plugin) {
            PocketMineEventAdapter.register(listener,
                    plugin == null ? null : plugin.getName());
        }
    }
}
