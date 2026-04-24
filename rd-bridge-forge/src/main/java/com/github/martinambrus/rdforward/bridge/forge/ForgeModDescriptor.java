package com.github.martinambrus.rdforward.bridge.forge;

import java.util.List;
import java.util.Map;

/**
 * Parsed view of {@code META-INF/mods.toml}. Forge supports multiple
 * {@code [[mods]]} entries in a single jar — RDForward carries them all in
 * {@link #mods()} but typical mods ship one. Dependencies are flattened
 * into a single mod-id -&gt; version-range map (rd-api's {@code ModDescriptor}
 * has no nested dependency model).
 *
 * @param modLoader     {@code javafml} for standard Forge mods
 * @param loaderVersion required Forge API version range
 * @param license       free-form licence string (required by Forge)
 * @param mods          one entry per {@code [[mods]]} table
 * @param dependencies  flat modid -&gt; version-range; unions every entry's
 *                      {@code [[dependencies.X]]} lists
 */
public record ForgeModDescriptor(
        String modLoader,
        String loaderVersion,
        String license,
        List<Entry> mods,
        Map<String, String> dependencies
) {
    public ForgeModDescriptor {
        mods = mods == null ? List.of() : List.copyOf(mods);
        dependencies = dependencies == null ? Map.of() : Map.copyOf(dependencies);
    }

    public Entry primary() {
        return mods.isEmpty() ? null : mods.get(0);
    }

    /**
     * One {@code [[mods]]} entry.
     *
     * @param modId       matches the {@code @Mod("modid")} annotation
     * @param version     mod version
     * @param displayName human-readable name
     * @param description short description ({@code ""} when omitted)
     * @param authors     author string as written ({@code null} when omitted)
     */
    public record Entry(
            String modId,
            String version,
            String displayName,
            String description,
            String authors
    ) {}
}
