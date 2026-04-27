// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory {@link PersistentDataContainer} stub. Real Paper persists
 * the container to the player's NBT on disk; RDForward has no NBT layer
 * for player data, so writes are kept only for the lifetime of the
 * proxy and dropped on quit. VanishNoPacket and similar plugins call
 * {@code getPersistentDataContainer().set(...)} during the join handler
 * to remember per-player state — without a non-null container the
 * listener NPEs immediately, blocking every downstream join hook.
 *
 * <p>The container shape is intentionally type-erased: the auto-generated
 * {@link PersistentDataType} stubs expose every type constant as
 * {@code null}, so plugins can only round-trip values they wrote
 * themselves within the same JVM lifetime. Cross-restart persistence,
 * type validation, and adapter-context conversions are all out of scope.
 */
public final class StubPersistentDataContainer implements PersistentDataContainer {

    private final ConcurrentHashMap<NamespacedKey, Object> values = new ConcurrentHashMap<>();

    private static final PersistentDataAdapterContext ADAPTER_CONTEXT =
            StubPersistentDataContainer::new;

    @Override
    public void set(NamespacedKey key, PersistentDataType type, Object value) {
        if (key == null || value == null) return;
        values.put(key, value);
    }

    @Override
    public void remove(NamespacedKey key) {
        if (key != null) values.remove(key);
    }

    @Override
    public boolean has(NamespacedKey key, PersistentDataType type) {
        return key != null && values.containsKey(key);
    }

    @Override
    public boolean has(NamespacedKey key) {
        return key != null && values.containsKey(key);
    }

    @Override
    public Object get(NamespacedKey key, PersistentDataType type) {
        return key == null ? null : values.get(key);
    }

    @Override
    public Object getOrDefault(NamespacedKey key, PersistentDataType type, Object fallback) {
        if (key == null) return fallback;
        Object v = values.get(key);
        return v == null ? fallback : v;
    }

    @Override
    public Set<NamespacedKey> getKeys() {
        return new HashSet<>(values.keySet());
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public void copyTo(PersistentDataContainer dest, boolean replace) {
        if (dest == null) return;
        values.forEach((k, v) -> {
            if (replace || !dest.has(k)) {
                dest.set(k, null, v);
            }
        });
    }

    @Override
    public PersistentDataAdapterContext getAdapterContext() {
        return ADAPTER_CONTEXT;
    }

    @Override
    public byte[] serializeToBytes() {
        // Real Paper serializes the backing NBT compound. RDForward has
        // no NBT layer for player data, so return an empty payload
        // rather than nulling — plugins that round-trip via bytes still
        // get a valid (if empty) blob.
        return new byte[0];
    }

    @Override
    public int getSize() {
        return values.size();
    }

    @Override
    public void readFromBytes(byte[] bytes, boolean clear) {
        if (clear) values.clear();
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(
                null,
                "org.bukkit.persistence.PersistentDataContainer.readFromBytes([BZ)V");
    }
}
