package net.fabricmc.fabric.api.client.networking.v1;

import com.github.martinambrus.rdforward.api.registry.RegistryKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Fabric-compatible client-side custom payload networking helper. Mods
 * register inbound receivers keyed by channel identifier and send outbound
 * payloads via {@link #send(RegistryKey, byte[])}.
 *
 * <p>RDForward does not yet expose a first-class client-side
 * {@code PluginChannel} — the send path routes through a pluggable
 * {@link Sender} that the client host wires in via {@link #setSender(Sender)}.
 * In environments where the sender is not installed (unit tests, standalone
 * bridge use), {@code send} logs a one-time warning and drops the payload.
 *
 * <p>Registration always succeeds and the receiver map is retained so the
 * client host can look up and install handlers once its networking stack
 * is ready.
 */
public final class ClientPlayNetworking {

    private static final Logger LOG = Logger.getLogger(ClientPlayNetworking.class.getName());

    @FunctionalInterface
    public interface PlayPayloadHandler {
        void onPayload(byte[] payload);
    }

    @FunctionalInterface
    public interface Sender {
        void send(RegistryKey channel, byte[] payload);
    }

    private static final Map<RegistryKey, PlayPayloadHandler> receivers = new ConcurrentHashMap<>();
    private static volatile Sender sender;
    private static volatile boolean warnedMissingSender;

    private ClientPlayNetworking() {}

    /** Register a receiver for inbound payloads on {@code channel}. Replaces any previous receiver. */
    public static void registerGlobalReceiver(RegistryKey channel, PlayPayloadHandler handler) {
        receivers.put(channel, handler);
    }

    /** Remove a previously-registered receiver. @return the handler that was removed, or {@code null}. */
    public static PlayPayloadHandler unregisterGlobalReceiver(RegistryKey channel) {
        return receivers.remove(channel);
    }

    /** @return an unmodifiable snapshot of the current receivers keyed by channel id. */
    public static Map<RegistryKey, PlayPayloadHandler> getReceivers() {
        return Map.copyOf(receivers);
    }

    /** Host-side hook to wire the sender once the client networking stack is ready. */
    public static void setSender(Sender s) {
        sender = s;
        warnedMissingSender = false;
    }

    /** Deliver an inbound payload to the registered receiver, if any. Called by the host. */
    public static void dispatchInbound(RegistryKey channel, byte[] payload) {
        PlayPayloadHandler h = receivers.get(channel);
        if (h != null) h.onPayload(payload);
    }

    /** Send a payload to the connected server. No-op when no sender is installed. */
    public static void send(RegistryKey channel, byte[] payload) {
        Sender s = sender;
        if (s == null) {
            if (!warnedMissingSender) {
                warnedMissingSender = true;
                LOG.warning("[ClientPlayNetworking] send(" + channel + "): no Sender installed — payload dropped");
            }
            return;
        }
        s.send(channel, payload);
    }
}
