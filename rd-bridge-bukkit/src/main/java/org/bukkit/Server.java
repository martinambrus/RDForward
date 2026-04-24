// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.SimpleServicesManager;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Bukkit-shaped server facade. Methods defer to the rd-api
 * {@link com.github.martinambrus.rdforward.api.server.Server} via
 * {@link com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge}.
 * Plugins retrieve their concrete instance via {@link Bukkit#getServer()}.
 */
public interface Server {

    String getName();
    String getVersion();
    String getBukkitVersion();

    Logger getLogger();

    /** Broadcast a chat message to every online player. */
    int broadcastMessage(String message);

    PluginManager getPluginManager();
    BukkitScheduler getScheduler();
    ConsoleCommandSender getConsoleSender();

    /** @return the player with this name, or null if not online. */
    Player getPlayer(String name);

    /** @return every online player, never null. */
    Collection<Player> getOnlinePlayers();

    /** @return every world the server hosts. RDForward ships a single world. */
    List<World> getWorlds();

    /** @return the world with the given name, or null. */
    World getWorld(String name);

    /**
     * @return {@code false} by default — RDForward does not implement
     *         Mojang online-mode auth. Plugins that gate behaviour on
     *         this (e.g. LuckPerms's uuid-lookup fallback) will take
     *         the offline-mode code path.
     */
    default boolean getOnlineMode() { return false; }

    /**
     * @return a process-wide {@link SimpleServicesManager}. Plugins that
     *         publish services (LuckPerms, Vault) register them here and
     *         each other look them up. Since everyone sees the same
     *         instance, service lookup works across plugins even though
     *         RDForward itself never consumes from it.
     */
    default ServicesManager getServicesManager() { return ServerSupport.SERVICES; }
}
