package com.github.martinambrus.rdforward.server.network;

import com.github.martinambrus.rdforward.api.network.PluginChannel;
import com.github.martinambrus.rdforward.api.player.Player;
import com.github.martinambrus.rdforward.api.registry.RegistryKey;
import com.github.martinambrus.rdforward.server.ConnectedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Static registry of plugin channels opened by mods. Tracks the owner mod
 * id so {@link com.github.martinambrus.rdforward.modloader.ModManager}
 * can sweep channels when a mod unloads, and dispatches inbound payloads
 * arriving on the wire (from
 * {@link com.github.martinambrus.rdforward.server.NettyConnectionHandler})
 * to the registered receiver.
 *
 * <p>The {@link #playerFactory} bridge lets this manager — which lives in
 * rd-server — build an rd-api {@link Player} for the inbound call without
 * having a compile-time dependency on the mod-loader's adapter layer. It
 * is installed once at server startup by
 * {@link com.github.martinambrus.rdforward.modloader.ModSystem#boot}.
 */
public final class PluginChannelManager {

    private static final Logger LOG = Logger.getLogger(PluginChannelManager.class.getName());

    private static final ConcurrentMap<RegistryKey, DefaultPluginChannel> channels = new ConcurrentHashMap<>();
    /** Bridge for wrapping {@link ConnectedPlayer} as an rd-api {@link Player}. */
    private static volatile Function<ConnectedPlayer, Player> playerFactory;

    private PluginChannelManager() {}

    /**
     * Install the {@link ConnectedPlayer} → rd-api {@link Player} adapter.
     * Called once from {@code ModSystem#boot} after the server facade is ready.
     */
    public static void installPlayerFactory(Function<ConnectedPlayer, Player> factory) {
        playerFactory = factory;
    }

    /**
     * Register (or return the existing) channel with the given id. Ownership
     * is recorded against {@code modId} so mod unload can sweep channels.
     */
    public static PluginChannel open(RegistryKey id, String modId,
                                     DefaultPluginChannel.PerPlayerSender perPlayerSender,
                                     DefaultPluginChannel.Broadcaster broadcaster) {
        return channels.computeIfAbsent(id, k -> new DefaultPluginChannel(k, modId, perPlayerSender, broadcaster));
    }

    /** Fetch a channel by id, or {@code null}. */
    public static DefaultPluginChannel get(RegistryKey id) {
        return channels.get(id);
    }

    /** Remove every channel owned by {@code modId}. @return number removed. */
    public static int removeChannelsOwnedBy(String modId) {
        int removed = 0;
        for (RegistryKey k : new ArrayList<>(channels.keySet())) {
            DefaultPluginChannel ch = channels.get(k);
            if (ch != null && modId.equals(ch.ownerModId())) {
                channels.remove(k);
                removed++;
            }
        }
        return removed;
    }

    /**
     * Dispatch an inbound payload from {@code source} on the given channel
     * identifier to the registered receiver. Silently dropped if the channel
     * has no receiver or the channel is not opened.
     */
    public static void dispatchInbound(ConnectedPlayer source, String channelIdString, byte[] data) {
        RegistryKey key = parseChannel(channelIdString);
        if (key == null) return;
        DefaultPluginChannel channel = channels.get(key);
        if (channel == null) return;
        Function<ConnectedPlayer, Player> factory = playerFactory;
        if (factory == null) {
            LOG.warning("[PluginChannel] no player factory installed; dropping payload on " + key);
            return;
        }
        Player p;
        try { p = factory.apply(source); } catch (Throwable t) {
            LOG.warning("[PluginChannel] player factory threw for " + key + ": " + t);
            return;
        }
        channel.dispatch(p, data);
    }

    /**
     * Pre-1.13 and 1.13+ use different channel name conventions. Older
     * clients send {@code "MC|<name>"} (no namespace); 1.13+ send
     * namespaced identifiers like {@code "minecraft:name"} or
     * {@code "<modid>:<name>"}. Map both into {@link RegistryKey} so
     * callers can match regardless.
     */
    private static RegistryKey parseChannel(String name) {
        if (name == null || name.isEmpty()) return null;
        try {
            if (name.contains(":")) return RegistryKey.parse(name);
            if (name.startsWith("MC|")) {
                return new RegistryKey("minecraft", name.substring(3).toLowerCase());
            }
            return new RegistryKey("minecraft", name.toLowerCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Snapshot of registered channel ids — for admin tooling. */
    public static List<RegistryKey> channelIds() {
        return new ArrayList<>(channels.keySet());
    }
}
