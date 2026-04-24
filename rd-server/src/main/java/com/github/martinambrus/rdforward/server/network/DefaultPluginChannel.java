package com.github.martinambrus.rdforward.server.network;

import com.github.martinambrus.rdforward.api.network.PluginChannel;
import com.github.martinambrus.rdforward.api.player.Player;
import com.github.martinambrus.rdforward.api.registry.RegistryKey;

import java.util.function.BiConsumer;
import java.util.logging.Logger;

/**
 * Server-side {@link PluginChannel} implementation. Sends ride through a
 * {@link PerPlayerSender}/{@link Broadcaster} pair that the server
 * initializer wires in — this keeps the channel object decoupled from
 * the concrete connection handler.
 */
public final class DefaultPluginChannel implements PluginChannel {

    private static final Logger LOG = Logger.getLogger(DefaultPluginChannel.class.getName());

    private final RegistryKey id;
    private final String ownerModId;
    private final PerPlayerSender perPlayerSender;
    private final Broadcaster broadcaster;

    private volatile String receiverModId;
    private volatile BiConsumer<Player, byte[]> receiver;

    DefaultPluginChannel(RegistryKey id, String ownerModId,
                         PerPlayerSender perPlayerSender,
                         Broadcaster broadcaster) {
        this.id = id;
        this.ownerModId = ownerModId;
        this.perPlayerSender = perPlayerSender;
        this.broadcaster = broadcaster;
    }

    public String ownerModId() { return ownerModId; }

    @Override
    public RegistryKey id() { return id; }

    @Override
    public void setReceiver(String modId, BiConsumer<Player, byte[]> handler) {
        this.receiverModId = modId;
        this.receiver = handler;
    }

    @Override
    public void clearReceiver() {
        this.receiverModId = null;
        this.receiver = null;
    }

    @Override
    public void sendToPlayer(Player player, byte[] payload) {
        PerPlayerSender sender = perPlayerSender;
        if (sender == null || player == null || payload == null) return;
        try { sender.send(player, id, payload); } catch (Throwable t) {
            LOG.warning("[PluginChannel] send on " + id + " threw: " + t);
        }
    }

    @Override
    public void broadcast(byte[] payload) {
        Broadcaster b = broadcaster;
        if (b == null || payload == null) return;
        try { b.broadcast(id, payload); } catch (Throwable t) {
            LOG.warning("[PluginChannel] broadcast on " + id + " threw: " + t);
        }
    }

    /** Called by {@link PluginChannelManager#dispatchInbound} when a client payload arrives. */
    void dispatch(Player source, byte[] data) {
        BiConsumer<Player, byte[]> handler = receiver;
        if (handler == null) return;
        try {
            handler.accept(source, data);
        } catch (Throwable t) {
            LOG.warning("[PluginChannel] receiver for " + id
                    + " (mod " + receiverModId + ") threw: " + t);
        }
    }

    /** Strategy that pushes a payload to one specific player. */
    @FunctionalInterface
    public interface PerPlayerSender {
        void send(Player target, RegistryKey channel, byte[] payload);
    }

    /** Strategy that pushes a payload to every online player. */
    @FunctionalInterface
    public interface Broadcaster {
        int broadcast(RegistryKey channel, byte[] payload);
    }
}
