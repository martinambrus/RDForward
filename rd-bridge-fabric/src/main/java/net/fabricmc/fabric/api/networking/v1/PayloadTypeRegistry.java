// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.fabricmc.fabric.api.networking.v1;

import com.github.martinambrus.rdforward.api.registry.RegistryKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fabric-compatible stub for 1.20.5+ typed payload registration. Upstream
 * Fabric uses this registry to bind a typed {@code CustomPayload} class to
 * a channel id + codec so the networking stack knows how to (de)serialize
 * payloads. RDForward ships raw {@code byte[]} payloads through
 * {@link ServerPlayNetworking} and {@code ClientPlayNetworking}, so the
 * typed codec plumbing is retained as a bookkeeping map only.
 *
 * <p>Registration succeeds and the channel id is remembered; the payload
 * type class is accepted but never used. Mods that relied on upstream's
 * typed payload routing see their raw bytes flow through the raw-byte
 * APIs instead.
 */
public final class PayloadTypeRegistry<T> {

    private static final PayloadTypeRegistry<Object> PLAY_S2C = new PayloadTypeRegistry<>("play-s2c");
    private static final PayloadTypeRegistry<Object> PLAY_C2S = new PayloadTypeRegistry<>("play-c2s");

    private final String side;
    private final Map<RegistryKey, Class<?>> registered = new ConcurrentHashMap<>();

    private PayloadTypeRegistry(String side) {
        this.side = side;
    }

    /** Play server-to-client typed payload registry. */
    @SuppressWarnings("unchecked")
    public static <T> PayloadTypeRegistry<T> playS2C() {
        return (PayloadTypeRegistry<T>) PLAY_S2C;
    }

    /** Play client-to-server typed payload registry. */
    @SuppressWarnings("unchecked")
    public static <T> PayloadTypeRegistry<T> playC2S() {
        return (PayloadTypeRegistry<T>) PLAY_C2S;
    }

    /** Register a typed payload. Codec is stored but not invoked — RDForward uses raw bytes. */
    public void register(RegistryKey id, Class<T> payloadType, Object codec) {
        registered.put(id, payloadType);
    }

    /** Register without a codec — same noop recording. */
    public void register(RegistryKey id, Class<T> payloadType) {
        registered.put(id, payloadType);
    }

    /** @return the payload type class previously registered under {@code id}, or null. */
    public Class<?> getRegistered(RegistryKey id) {
        return registered.get(id);
    }

    /** @return the side name ({@code "play-s2c"} / {@code "play-c2s"}). */
    public String side() {
        return side;
    }
}
