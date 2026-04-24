package pocketmine;

import pocketmine.plugin.PluginManager;

/**
 * Static accessor mirroring PocketMine-MP's {@code \pocketmine\Server}.
 * The concrete {@code Server} implementation is installed by
 * {@code PocketMineBridge.install(rdServer)} and exposes just the pieces
 * the bridge actually services. Plugins reach it via
 * {@code Server.getInstance()} — same shape as PocketMine's static
 * helper except adapted to Java's no-singleton-statics convention.
 */
public interface Server {

    static Server getInstance() { return Holder.instance; }

    static void setInstance(Server server) { Holder.instance = server; }

    PluginManager getPluginManager();

    void broadcastMessage(String message);

    final class Holder {
        private Holder() {}
        private static Server instance;
    }
}
