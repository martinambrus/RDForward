package com.github.martinambrus.rdforward.api.server;

import com.github.martinambrus.rdforward.api.command.CommandRegistry;
import com.github.martinambrus.rdforward.api.mod.ModManager;
import com.github.martinambrus.rdforward.api.network.PluginChannel;
import com.github.martinambrus.rdforward.api.permission.PermissionManager;
import com.github.martinambrus.rdforward.api.player.Player;
import com.github.martinambrus.rdforward.api.registry.RegistryKey;
import com.github.martinambrus.rdforward.api.scheduler.Scheduler;
import com.github.martinambrus.rdforward.api.version.ProtocolVersion;
import com.github.martinambrus.rdforward.api.world.World;

import java.util.Collection;

/**
 * Server facade handed to mods. Obtained via {@code ServerMod.onEnable(Server)}
 * or {@code RDForward.getServer()} after startup.
 */
public interface Server {

    World getWorld();

    Collection<? extends Player> getOnlinePlayers();

    /** @return matching player, or null if offline. */
    Player getPlayer(String name);

    Scheduler getScheduler();

    CommandRegistry getCommandRegistry();

    PermissionManager getPermissionManager();

    /** Query the set of currently loaded mods. */
    ModManager getModManager();

    /** Every protocol version this server accepts connections for. */
    ProtocolVersion[] getSupportedVersions();

    /** Broadcast a chat message to every online player. */
    void broadcastMessage(String message);

    /**
     * Open (or retrieve an existing) custom-payload channel. Ownership is
     * attributed to the currently-initializing mod; channels are removed
     * automatically when the owning mod disables or reloads.
     */
    PluginChannel openPluginChannel(RegistryKey id);

    /**
     * Install a {@link PlayerVisibilityFilter} consulted on every
     * per-sender broadcast (spawn / despawn / position / tab list).
     * Pass {@code null} to clear back to the default
     * "always visible" behaviour. Used by the Bukkit bridge to wire
     * {@code Player.hidePlayer/showPlayer} without touching rd-server
     * internals.
     */
    default void setVisibilityFilter(PlayerVisibilityFilter filter) {
        // Default no-op so non-rd-server fixtures (test stubs, Forge/
        // PocketMine bridges that don't currently wire visibility) still
        // satisfy the interface without bespoke implementations.
    }

    /**
     * Send a player-spawn packet for {@code targetName} to the single
     * connected client identified by {@code recipientName}. Used by the
     * Bukkit bridge to make a hidden player IMMEDIATELY reappear when
     * {@code Player.showPlayer(target)} is called mid-session — the
     * {@link PlayerVisibilityFilter} alone only affects future broadcasts.
     */
    default void sendPlayerSpawnTo(String recipientName, String targetName) {}

    /**
     * Send a player-despawn packet for {@code targetName} to the single
     * connected client identified by {@code recipientName}. Mirror of
     * {@link #sendPlayerSpawnTo} for {@code Player.hidePlayer}.
     */
    default void sendPlayerDespawnTo(String recipientName, String targetName) {}

    /**
     * Send a tab-list "add" entry for {@code targetName} to a single
     * recipient. Used by the Bukkit bridge to restore a previously
     * hidden player's tab list entry on {@code Player.showPlayer}.
     * No-op for protocol versions that don't have a tab list.
     */
    default void sendPlayerListAddTo(String recipientName, String targetName) {}

    /**
     * Mirror of {@link #sendPlayerListAddTo} for {@code Player.hidePlayer}.
     * Removes a stale tab list entry that was added before the bridge
     * had a chance to apply visibility filtering — the join handler
     * broadcasts the add before {@code PLAYER_JOIN_ANNOUNCE} fires, so
     * the recipient already has the entry when Vanish's PJE listener
     * calls {@code hidePlayer}.
     */
    default void sendPlayerListRemoveTo(String recipientName, String targetName) {}
}
