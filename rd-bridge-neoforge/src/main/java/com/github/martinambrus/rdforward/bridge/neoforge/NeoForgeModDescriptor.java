package com.github.martinambrus.rdforward.bridge.neoforge;

import java.util.List;
import java.util.Map;

/**
 * Parsed view of {@code META-INF/neoforge.mods.toml}. Shape mirrors
 * {@link com.github.martinambrus.rdforward.bridge.forge.ForgeModDescriptor}
 * but adds the optional {@code mainClass} field per {@code [[mods]]} entry
 * — NeoForge lets the loader locate the entrypoint without an
 * {@code @Mod} annotation.
 */
public record NeoForgeModDescriptor(
        String modLoader,
        String loaderVersion,
        String license,
        List<Entry> mods,
        Map<String, String> dependencies
) {
    public NeoForgeModDescriptor {
        mods = mods == null ? List.of() : List.copyOf(mods);
        dependencies = dependencies == null ? Map.of() : Map.copyOf(dependencies);
    }

    public Entry primary() { return mods.isEmpty() ? null : mods.get(0); }

    public record Entry(
            String modId,
            String version,
            String displayName,
            String description,
            String authors,
            String mainClass
    ) {}
}
