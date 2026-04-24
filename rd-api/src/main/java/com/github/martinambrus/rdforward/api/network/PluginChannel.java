package com.github.martinambrus.rdforward.api.network;

import com.github.martinambrus.rdforward.api.player.Player;
import com.github.martinambrus.rdforward.api.registry.RegistryKey;

import java.util.function.BiConsumer;

/**
 * Custom payload channel between server and client, mirroring Fabric's
 * {@code ServerPlayNetworking} / {@code ClientPlayNetworking} and
 * Bukkit's plugin-messaging API.
 *
 * <p>On the server:
 * <ul>
 *   <li>Register a receiver once per channel to handle inbound client messages.</li>
 *   <li>Call {@link #sendToPlayer(Player, byte[])} or
 *       {@link #broadcast(byte[])} to push payloads out.</li>
 * </ul>
 *
 * <p>On the client the same channel object is retrieved and used to send to
 * and receive from the connected server; the inbound {@code Player}
 * argument to the receiver is always the local player on the client side.
 *
 * <p>Transport: channels ride the version-appropriate custom-payload
 * packet (vanilla {@code minecraft:<name>}, pre-1.13 {@code MC|<name>},
 * Alpha has no custom channels and silently drops the send).
 */
public interface PluginChannel {

    /** Channel identifier used on the wire. */
    RegistryKey id();

    /** Install the receiver for inbound messages on this channel. Replaces any previous receiver. */
    void setReceiver(String modId, BiConsumer<Player, byte[]> handler);

    /** Remove the installed receiver, if any. */
    void clearReceiver();

    /** Send a payload to a specific player. No-op if the client does not support custom channels. */
    void sendToPlayer(Player player, byte[] payload);

    /** Send a payload to every online player that supports custom channels. */
    void broadcast(byte[] payload);
}
