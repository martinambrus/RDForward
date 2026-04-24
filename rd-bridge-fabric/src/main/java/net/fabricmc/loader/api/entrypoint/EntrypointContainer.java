package net.fabricmc.loader.api.entrypoint;

import net.fabricmc.loader.api.ModContainer;

/**
 * Fabric-compatible wrapper that pairs an entrypoint instance with the
 * {@link ModContainer} it came from. Upstream Fabric uses this for
 * reflection-heavy entrypoint traversal (plugins scanning for arbitrary
 * types). RDForward's loader only knows {@code ModInitializer},
 * {@code ClientModInitializer} and {@code DedicatedServerModInitializer},
 * so this wrapper is returned primarily from {@code FabricLoader.getEntrypointContainers}.
 */
public final class EntrypointContainer<T> {

    private final T entrypoint;
    private final ModContainer provider;

    public EntrypointContainer(T entrypoint, ModContainer provider) {
        this.entrypoint = entrypoint;
        this.provider = provider;
    }

    /** @return the instantiated entrypoint object. */
    public T getEntrypoint() {
        return entrypoint;
    }

    /** @return the mod container the entrypoint came from. */
    public ModContainer getProvider() {
        return provider;
    }
}
