package com.github.martinambrus.rdforward.bridge.neoforge;

import com.moandjiezana.toml.Toml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses {@code META-INF/neoforge.mods.toml} into a
 * {@link NeoForgeModDescriptor}. Schema matches Forge's {@code mods.toml}
 * plus an optional {@code mainClass} string per {@code [[mods]]} entry
 * — when present, the loader skips the {@code @Mod} annotation scan and
 * instantiates the named class directly.
 */
public final class NeoForgeModsTomlParser {

    private NeoForgeModsTomlParser() {}

    public static NeoForgeModDescriptor parse(InputStream in) {
        Toml toml = new Toml().read(in);

        String modLoader = toml.getString("modLoader", "javafml");
        String loaderVersion = toml.getString("loaderVersion", "");
        String license = toml.getString("license", "");

        List<Toml> modTables = toml.getTables("mods");
        List<NeoForgeModDescriptor.Entry> mods = new ArrayList<>();
        if (modTables != null) {
            for (Toml t : modTables) {
                mods.add(new NeoForgeModDescriptor.Entry(
                        t.getString("modId", ""),
                        t.getString("version", "1.0.0"),
                        t.getString("displayName", t.getString("modId", "")),
                        t.getString("description", ""),
                        t.getString("authors"),
                        t.getString("mainClass")
                ));
            }
        }

        Map<String, String> deps = new LinkedHashMap<>();
        Toml depsTable = toml.getTable("dependencies");
        if (depsTable != null) {
            for (NeoForgeModDescriptor.Entry e : mods) {
                List<Toml> perMod = depsTable.getTables(e.modId());
                if (perMod == null) continue;
                for (Toml d : perMod) {
                    String depId = d.getString("modId", "");
                    if (depId.isEmpty() || "neoforge".equals(depId) || "minecraft".equals(depId)) continue;
                    boolean mandatory = d.getBoolean("mandatory", Boolean.TRUE);
                    if (!mandatory) continue;
                    deps.put(depId, d.getString("versionRange", "*"));
                }
            }
        }

        return new NeoForgeModDescriptor(modLoader, loaderVersion, license, mods, deps);
    }
}
