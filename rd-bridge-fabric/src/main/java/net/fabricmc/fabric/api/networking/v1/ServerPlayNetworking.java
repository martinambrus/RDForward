package net.fabricmc.fabric.api.networking.v1;

import com.github.martinambrus.rdforward.api.registry.RegistryKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Fabric-compatible server-side custom payload networking helper. Mirrors
 * the client-side {@link net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking}
 * shape: mods register inbound receivers keyed by channel id and send
 * outbound payloads via {@link #send(Object, RegistryKey, byte[])}.
 *
 * <p>RDForward's real server-side networking lives behind rd-api
 * {@code PluginChannel}. This shim is a bookkeeping layer — the server host
 * installs a {@link Sender} once its networking stack is ready. Where no
 * sender is present (unit tests, pre-boot registrations), {@code send}
 * logs a one-time warning and drops the payload.
 */
public final class ServerPlayNetworking {

    private static final Logger LOG = Logger.getLogger(ServerPlayNetworking.class.getName());

    @FunctionalInterface
    public interface PlayPayloadHandler {
        void onPayload(Object player, byte[] payload);
    }

    @FunctionalInterface
    public interface Sender {
        void send(Object player, RegistryKey channel, byte[] payload);
    }

    private static final Map<RegistryKey, PlayPayloadHandler> receivers = new ConcurrentHashMap<>();
    private static volatile Sender sender;
    private static volatile boolean warnedMissingSender;

    private ServerPlayNetworking() {}

    /** Register a receiver for inbound payloads on {@code channel}. Replaces any previous receiver. */
    public static void registerGlobalReceiver(RegistryKey channel, PlayPayloadHandler handler) {
        receivers.put(channel, handler);
    }

    /** Remove a previously-registered receiver. @return the handler removed, or null. */
    public static PlayPayloadHandler unregisterGlobalReceiver(RegistryKey channel) {
        return receivers.remove(channel);
    }

    /** @return an unmodifiable snapshot of the current receivers keyed by channel id. */
    public static Map<RegistryKey, PlayPayloadHandler> getReceivers() {
        return Map.copyOf(receivers);
    }

    /** Host-side hook to wire the sender once the server networking stack is ready. */
    public static void setSender(Sender s) {
        sender = s;
        warnedMissingSender = false;
    }

    /** Deliver an inbound payload to the registered receiver, if any. Called by the host. */
    public static void dispatchInbound(Object player, RegistryKey channel, byte[] payload) {
        PlayPayloadHandler h = receivers.get(channel);
        if (h != null) h.onPayload(player, payload);
    }

    /** Send a payload to {@code player}. No-op when no sender is installed. */
    public static void send(Object player, RegistryKey channel, byte[] payload) {
        Sender s = sender;
        if (s == null) {
            if (!warnedMissingSender) {
                warnedMissingSender = true;
                LOG.warning("[ServerPlayNetworking] send(" + channel + "): no Sender installed — payload dropped");
            }
            return;
        }
        s.send(player, channel, payload);
    }
}
