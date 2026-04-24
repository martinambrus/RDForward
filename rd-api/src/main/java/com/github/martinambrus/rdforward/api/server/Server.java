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
}
