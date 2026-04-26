package com.github.martinambrus.rdforward.modloader.impl;

import com.github.martinambrus.rdforward.api.command.CommandRegistry;
import com.github.martinambrus.rdforward.api.event.EventOwnership;
import com.github.martinambrus.rdforward.api.mod.ModManager;
import com.github.martinambrus.rdforward.api.network.PluginChannel;
import com.github.martinambrus.rdforward.api.permission.PermissionManager;
import com.github.martinambrus.rdforward.api.player.Player;
import com.github.martinambrus.rdforward.api.registry.RegistryKey;
import com.github.martinambrus.rdforward.api.scheduler.Scheduler;
import com.github.martinambrus.rdforward.api.server.Server;
import com.github.martinambrus.rdforward.api.version.ProtocolVersion;
import com.github.martinambrus.rdforward.api.world.World;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyPluginMessageS2CPacketV393;
import com.github.martinambrus.rdforward.server.ChunkManager;
import com.github.martinambrus.rdforward.server.ConnectedPlayer;
import com.github.martinambrus.rdforward.server.PlayerManager;
import com.github.martinambrus.rdforward.server.ServerWorld;
import com.github.martinambrus.rdforward.server.network.DefaultPluginChannel;
import com.github.martinambrus.rdforward.server.network.PluginChannelManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Adapter exposing the running {@link com.github.martinambrus.rdforward.server.RDServer}
 * to mods through {@link Server}. One instance is constructed during server
 * startup and handed to every {@code ServerMod.onEnable(Server)} call.
 *
 * <p>Component wrappers ({@link RDWorld}, {@link RDPlayer},
 * {@link RDCommandRegistry}, {@link RDScheduler}, {@link RDPermissionManager})
 * are created lazily and reused — mods receive stable references across
 * their lifetime.
 */
public final class RDServer implements Server {

    private final com.github.martinambrus.rdforward.server.RDServer delegate;
    private final RDWorld world;
    private final RDCommandRegistry commandRegistry = new RDCommandRegistry();
    private final RDScheduler scheduler = new RDScheduler();
    private final RDPermissionManager permissionManager = new RDPermissionManager();
    private volatile ModManager modManager;

    public RDServer(com.github.martinambrus.rdforward.server.RDServer delegate) {
        this.delegate = delegate;
        this.world = new RDWorld(delegate.getWorld());
        PluginChannelManager.installPlayerFactory(cp -> new RDPlayer(cp, this));
    }

    public void setModManager(ModManager modManager) {
        this.modManager = modManager;
    }

    PlayerManager playerManager() { return delegate.getPlayerManager(); }
    ChunkManager chunkManager() { return delegate.getChunkManager(); }
    ServerWorld serverWorld() { return delegate.getWorld(); }

    @Override
    public World getWorld() { return world; }

    @Override
    public Collection<? extends Player> getOnlinePlayers() {
        PlayerManager pm = playerManager();
        Collection<ConnectedPlayer> all = pm.getAllPlayers();
        List<RDPlayer> out = new ArrayList<>(all.size());
        for (ConnectedPlayer cp : all) out.add(new RDPlayer(cp, this));
        return out;
    }

    @Override
    public Player getPlayer(String name) {
        ConnectedPlayer cp = playerManager().getPlayerByName(name);
        return cp == null ? null : new RDPlayer(cp, this);
    }

    @Override
    public Scheduler getScheduler() { return scheduler; }

    @Override
    public CommandRegistry getCommandRegistry() { return commandRegistry; }

    @Override
    public PermissionManager getPermissionManager() { return permissionManager; }

    @Override
    public ModManager getModManager() { return modManager; }

    @Override
    public ProtocolVersion[] getSupportedVersions() {
        return com.github.martinambrus.rdforward.protocol.ProtocolVersion.values();
    }

    @Override
    public void broadcastMessage(String message) {
        // Legacy clients (Beta 1.7.3, Alpha, early Release) cap chat at
        // PlayerManager.MAX_CHAT_CHARS UTF-16 chars and disconnect on
        // overflow. StubCallLog broadcasts can run ~190+ chars; without
        // splitting, the very first stub-call hit kills v22 clients.
        java.util.List<String> chunks =
                com.github.martinambrus.rdforward.server.PlayerManager.splitChatMessage(message);
        for (ConnectedPlayer cp : playerManager().getAllPlayers()) {
            for (String chunk : chunks) {
                ChatDispatch.send(cp, chunk);
            }
        }
    }

    @Override
    public PluginChannel openPluginChannel(RegistryKey id) {
        String owner = EventOwnership.currentOwner();
        if (owner == null) owner = "__server__";
        return PluginChannelManager.open(id, owner,
                (target, key, payload) -> sendPluginMessage(target, key, payload),
                (key, payload) -> broadcastPluginMessage(key, payload));
    }

    /** Send the payload to a specific rd-api Player. */
    private void sendPluginMessage(Player target, RegistryKey id, byte[] payload) {
        ConnectedPlayer cp = resolveConnectedPlayer(target);
        if (cp != null) sendPluginMessageTo(cp, id, payload);
    }

    /** Send the payload to every online Netty player. Returns delivery count. */
    private int broadcastPluginMessage(RegistryKey id, byte[] payload) {
        int delivered = 0;
        for (ConnectedPlayer cp : playerManager().getAllPlayers()) {
            if (sendPluginMessageTo(cp, id, payload)) delivered++;
        }
        return delivered;
    }

    private ConnectedPlayer resolveConnectedPlayer(Player p) {
        if (p instanceof RDPlayer rdp) return rdp.delegate();
        return playerManager().getPlayerByName(p.getName());
    }

    static boolean sendPluginMessageTo(ConnectedPlayer cp, RegistryKey id, byte[] payload) {
        if (cp == null || payload == null) return false;
        if (cp.getProtocolVersion() == null) return false;
        // 1.13+ uses namespaced identifiers; older Netty uses MC|<name>. Both tolerate a namespaced
        // key sent directly so we normalize to {@code namespace:name} on the wire.
        cp.sendPacket(new NettyPluginMessageS2CPacketV393(id.toString(), payload));
        return true;
    }
}
