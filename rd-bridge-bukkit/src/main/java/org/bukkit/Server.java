package org.bukkit;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
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
}
