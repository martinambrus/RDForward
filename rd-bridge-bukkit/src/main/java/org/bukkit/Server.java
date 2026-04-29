// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.help.HelpMap;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.SimpleServicesManager;
import org.bukkit.plugin.messaging.Messenger;
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

    /**
     * @return an {@link OfflinePlayer} for {@code name}. If a player by
     *         this name is currently online, returns that {@link Player}
     *         (which IS-A {@link OfflinePlayer}). Otherwise returns a
     *         minimal stub with name and a deterministic offline-mode
     *         UUID — Essentials's {@code OfflinePlayer.<init>} and
     *         similar legacy callers only read the name + UUID, so the
     *         stub satisfies the symbolic link without forcing us to
     *         model a full offline-player store.
     */
    default OfflinePlayer getOfflinePlayer(String name) {
        Player online = getPlayer(name);
        if (online != null) return online;
        return ServerSupport.offlinePlayerStub(name);
    }

    /**
     * @return the online player with the given offline-mode UUID, or
     *         {@code null} if none. LuckPerms's {@code
     *         LPBukkitBootstrap.isPlayerOnline} calls this every time
     *         a context invalidation fires; without it plugins error
     *         with {@link NoSuchMethodError} on every player join.
     */
    default Player getPlayer(java.util.UUID id) {
        if (id == null) return null;
        for (Player p : getOnlinePlayers()) {
            if (p != null && id.equals(p.getUniqueId())) return p;
        }
        return null;
    }

    /** @return every online player, never null. */
    Collection<Player> getOnlinePlayers();

    /**
     * @return every online player whose name starts with {@code partial}
     *         (case-insensitive). Real Bukkit returns an exact match
     *         alone if one is found; otherwise every prefix match.
     *         Essentials's {@code Commandmute} resolves the target name
     *         through this and {@link NoSuchMethodError}s without it.
     */
    default List<Player> matchPlayer(String partial) {
        List<Player> matches = new java.util.ArrayList<>();
        if (partial == null) return matches;
        String needle = partial.toLowerCase(java.util.Locale.ROOT);
        Player exact = null;
        for (Player p : getOnlinePlayers()) {
            if (p == null) continue;
            String n = p.getName();
            if (n == null) continue;
            String low = n.toLowerCase(java.util.Locale.ROOT);
            if (low.equals(needle)) { exact = p; break; }
            if (low.startsWith(needle)) matches.add(p);
        }
        if (exact != null) {
            matches.clear();
            matches.add(exact);
        }
        return matches;
    }

    /**
     * @return the configured max-player slot count. Bukkit plugins use
     *         this for capacity reporting (Essentials's {@code /list}
     *         output, MOTD slot fields). Default mirrors RDForward's
     *         {@code ServerProperties} default; concrete adapters
     *         override to read the live setting.
     */
    default int getMaxPlayers() { return 128; }

    /** @return the configured Java listener port. CoreProtect's
     *  {@code NetworkHandler} reads this at startup to phone home its
     *  server fingerprint; without the override the worker thread
     *  {@link NoSuchMethodError}s on every metrics tick. Default
     *  mirrors RDForward's {@code server.properties} default. */
    default int getPort() { return 25565; }

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

    /**
     * @return a non-null {@link HelpMap} stub. WorldEdit and similar
     *         plugins call this from {@code onEnable} to register their
     *         own help topics; the stub silently accepts every
     *         registration but never surfaces anything because RDForward
     *         routes help through its own command registry.
     */
    default HelpMap getHelpMap() { return ServerSupport.HELP_MAP; }

    /**
     * @return a non-null {@link Messenger} stub. WorldEdit's
     *         {@code onEnable} registers an outgoing
     *         {@code WECUI:datapack} plugin channel via this accessor;
     *         the stub accepts the registration but never delivers
     *         payloads because RDForward does not have a Bukkit-shaped
     *         plugin messaging pipeline.
     */
    default Messenger getMessenger() { return ServerSupport.MESSENGER; }

    /**
     * Look up a {@link org.bukkit.command.PluginCommand} by name or
     * alias across all loaded plugins. Essentials's {@code /sudo} routes
     * forced commands through this — if the lookup fails the sudo errors
     * out without dispatching.
     *
     * @return the matching {@link org.bukkit.command.PluginCommand}, or
     *         {@code null} when no plugin owns a command by that name.
     */
    default org.bukkit.command.PluginCommand getPluginCommand(String name) {
        return com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge.findPluginCommand(name);
    }
}
