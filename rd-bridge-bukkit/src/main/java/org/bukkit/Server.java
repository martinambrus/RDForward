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
     * @return the online player whose name exactly matches {@code name}
     *         (case-insensitive), or {@code null} if none. Real Bukkit's
     *         {@code getPlayer(String)} is a fuzzy prefix match while
     *         {@code getPlayerExact} requires an exact name; rd-api's
     *         {@code getPlayer(String)} is already exact-match, so this
     *         delegates. Essentials's {@code /s} / {@code /sudo} target
     *         lookup throws {@link NoSuchMethodError} on every invocation
     *         without this method.
     */
    default Player getPlayerExact(String name) {
        return getPlayer(name);
    }

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
     * @return an {@link OfflinePlayer} for the given offline-mode UUID. If
     *         a player with that UUID is currently online, returns the
     *         live {@link Player}; otherwise a stub carrying that UUID
     *         and a best-effort name (null when the UUID has never been
     *         seen — matches real Bukkit behaviour for unknown UUIDs).
     *         Essentials's {@code com.earth2me.essentials.OfflinePlayer}
     *         constructor calls {@code Server.getOfflinePlayer(UUID)} for
     *         every cache miss in {@code UserMap}; without this overload
     *         {@code /balancetop} (and any path that walks {@code UserMap})
     *         throws {@link NoSuchMethodError}.
     */
    default OfflinePlayer getOfflinePlayer(java.util.UUID id) {
        if (id == null) return null;
        Player online = getPlayer(id);
        if (online != null) return online;
        return ServerSupport.offlinePlayerStubByUuid(id);
    }

    /**
     * @return the {@link BanList} for the requested type, backed by
     *         RDForward's {@link com.github.martinambrus.rdforward.server.api.BanManager}.
     *         Essentials's {@code /unbanip} (and the rest of its ban
     *         commands) reach the flat-file ban store through this
     *         method; without it every {@code /banip}/{@code /unbanip}
     *         throws {@link NoSuchMethodError}.
     */
    default BanList getBanList(BanList$Type type) {
        return new com.github.martinambrus.rdforward.bridge.bukkit.ban.BridgeBanList(type);
    }

    /**
     * @return list of crafting recipes whose result matches {@code stack}.
     *         RDForward does not model the crafting registry, so this
     *         returns an empty list — Essentials's {@code /recipes} just
     *         reports "no recipes found" instead of throwing
     *         {@link NoSuchMethodError}.
     */
    default java.util.List<org.bukkit.inventory.Recipe> getRecipesFor(org.bukkit.inventory.ItemStack stack) {
        return java.util.Collections.emptyList();
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
    /**
     * EssentialsX's {@code /disposal} hands the resulting {@link
     * org.bukkit.inventory.Inventory} to the player's
     * {@code openInventory} for a throwaway item-dump UI. RDForward has
     * no client-side container UI, but the call must NOT throw — a
     * {@link NoSuchMethodError} here aborts the command (and any other
     * plugin that builds custom GUIs) before its message reaches the
     * player. Returns a no-op array-backed
     * {@link com.github.martinambrus.rdforward.bridge.bukkit.StubInventory}
     * sized to {@code size}; the player never actually sees it.
     */
    default org.bukkit.inventory.Inventory createInventory(
            org.bukkit.inventory.InventoryHolder holder, int size, String title) {
        return new com.github.martinambrus.rdforward.bridge.bukkit.StubInventory(
                holder, size, title);
    }

    /** Title-less overload — defaults to empty string. */
    default org.bukkit.inventory.Inventory createInventory(
            org.bukkit.inventory.InventoryHolder holder, int size) {
        return createInventory(holder, size, "");
    }

    /** Type-only overload. Real Bukkit picks the slot count from the
     *  {@link org.bukkit.event.inventory.InventoryType}; we don't model
     *  type-specific layouts so size collapses to 9 (single row). */
    default org.bukkit.inventory.Inventory createInventory(
            org.bukkit.inventory.InventoryHolder holder,
            org.bukkit.event.inventory.InventoryType type) {
        return createInventory(holder, 9, "");
    }

    /** Type + title overload. */
    default org.bukkit.inventory.Inventory createInventory(
            org.bukkit.inventory.InventoryHolder holder,
            org.bukkit.event.inventory.InventoryType type, String title) {
        return createInventory(holder, 9, title);
    }

    /**
     * EssentialsX's {@code UserWarpEvent.<init>} (constructed for
     * {@code /warp}, {@code /sethome}, {@code /tpaccept} etc.) calls
     * {@code Bukkit.getServer().isPrimaryThread()} to decide whether
     * {@code isAsynchronous} on the event should be set. A
     * {@link NoSuchMethodError} here aborts the command before any
     * teleport runs. Delegates to {@link Bukkit#isPrimaryThread()} which
     * already handles the RDForward tick-thread + plugin-lifecycle
     * cases.
     */
    default boolean isPrimaryThread() {
        return Bukkit.isPrimaryThread();
    }

    default org.bukkit.command.PluginCommand getPluginCommand(String name) {
        return com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge.findPluginCommand(name);
    }
}
