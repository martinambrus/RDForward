package com.github.martinambrus.rdforward.bridge.forge;

import com.moandjiezana.toml.Toml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses {@code META-INF/mods.toml} into a {@link ForgeModDescriptor}.
 * Handles the canonical structure: top-level {@code modLoader},
 * {@code loaderVersion}, {@code license}; an array of {@code [[mods]]}
 * tables with {@code modId}, {@code version}, {@code displayName},
 * {@code description}, {@code authors}; a {@code [dependencies]} sub-table
 * per mod id with {@code [[dependencies.<modId>]]} arrays.
 */
public final class ForgeModsTomlParser {

    private ForgeModsTomlParser() {}

    public static ForgeModDescriptor parse(InputStream in) {
        Toml toml = new Toml().read(in);

        String modLoader = toml.getString("modLoader", "javafml");
        String loaderVersion = toml.getString("loaderVersion", "");
        String license = toml.getString("license", "");

        List<Toml> modTables = toml.getTables("mods");
        List<ForgeModDescriptor.Entry> mods = new ArrayList<>();
        if (modTables != null) {
            for (Toml t : modTables) {
                mods.add(new ForgeModDescriptor.Entry(
                        t.getString("modId", ""),
                        t.getString("version", "1.0.0"),
                        t.getString("displayName", t.getString("modId", "")),
                        t.getString("description", ""),
                        t.getString("authors")
                ));
            }
        }

        Map<String, String> deps = new LinkedHashMap<>();
        Toml depsTable = toml.getTable("dependencies");
        if (depsTable != null) {
            for (ForgeModDescriptor.Entry e : mods) {
                List<Toml> perMod = depsTable.getTables(e.modId());
                if (perMod == null) continue;
                for (Toml d : perMod) {
                    String depId = d.getString("modId", "");
                    if (depId.isEmpty() || "forge".equals(depId) || "minecraft".equals(depId)) continue;
                    boolean mandatory = d.getBoolean("mandatory", Boolean.TRUE);
                    if (!mandatory) continue;
                    deps.put(depId, d.getString("versionRange", "*"));
                }
            }
        }

        return new ForgeModDescriptor(modLoader, loaderVersion, license, mods, deps);
    }
}
