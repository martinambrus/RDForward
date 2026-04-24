// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Bukkit-shaped static facade. Plugins call {@code Bukkit.getXxx()} to
 * reach global server services. The RDForward host installs the real
 * server via {@link #setServer(Server)} — before that point, lookups
 * return {@code null} or empty collections, matching pre-boot Bukkit.
 */
public final class Bukkit {

    private static volatile Server server;

    private Bukkit() {}

    /** Host-side hook — called once by the Bukkit bridge after boot. */
    public static void setServer(Server s) {
        server = s;
    }

    public static Server getServer() { return server; }

    public static String getName() {
        return server == null ? "RDForward" : server.getName();
    }

    public static String getVersion() {
        return server == null ? "unknown" : server.getVersion();
    }

    public static String getBukkitVersion() {
        return server == null ? "stub" : server.getBukkitVersion();
    }

    public static Logger getLogger() {
        return server == null ? Logger.getLogger("Bukkit") : server.getLogger();
    }

    public static int broadcastMessage(String message) {
        return server == null ? 0 : server.broadcastMessage(message);
    }

    public static PluginManager getPluginManager() {
        return server == null ? null : server.getPluginManager();
    }

    public static BukkitScheduler getScheduler() {
        return server == null ? null : server.getScheduler();
    }

    public static ConsoleCommandSender getConsoleSender() {
        return server == null ? null : server.getConsoleSender();
    }

    public static Player getPlayer(String name) {
        return server == null ? null : server.getPlayer(name);
    }

    public static Collection<Player> getOnlinePlayers() {
        return server == null ? List.of() : server.getOnlinePlayers();
    }

    public static List<World> getWorlds() {
        return server == null ? List.of() : server.getWorlds();
    }

    public static World getWorld(String name) {
        return server == null ? null : server.getWorld(name);
    }
}
