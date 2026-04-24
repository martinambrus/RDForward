package com.github.martinambrus.rdforward.api.registry;

import java.util.Collection;

/**
 * Typed key-value registry indexed by {@link RegistryKey}. Used for
 * mod-contributed entries (custom block behaviours, recipe types, etc.)
 * where entries need to be discoverable by namespaced id across mods.
 *
 * <p>Registries are write-once per key: registering the same key twice
 * throws. The owning mod id (derived from {@link RegistryKey#namespace()})
 * is tracked so the entry can be removed on hot-reload.
 *
 * @param <T> value type held by the registry
 */
public interface Registry<T> {

    /** @throws IllegalStateException if {@code key} is already registered. */
    void register(RegistryKey key, T value);

    /** @return registered value, or {@code null} if absent. */
    T get(RegistryKey key);

    boolean contains(RegistryKey key);

    Collection<RegistryKey> keys();

    Collection<T> values();
}
