// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.configuration.file;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bukkit-shaped {@code YamlConfiguration} with real load + save round
 * trip. Parsed content lands in the inherited
 * {@link MemorySection#values} map (dot-flattened) so subsequent
 * {@code getString} / {@code getInt} reads return real plugin
 * configuration data; {@link #saveToString} re-nests the flat map and
 * dumps via SnakeYAML so plugins (SimpleLogin, LoginSecurity) can
 * persist their settings back to disk.
 *
 * <p>The {@link #loadConfiguration(File)} / {@link #loadConfiguration(Reader)}
 * static factories MUST NOT return {@code null}: real plugin code (e.g.
 * LuckPerms's {@code BukkitConfigAdapter}) stores the result in a field
 * and later dereferences it without a null-check.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class YamlConfiguration extends org.bukkit.configuration.file.FileConfiguration {

    public YamlConfiguration() {}

    @Override
    public String saveToString() {
        Map<String, Object> nested = unflatten(values);
        if (options().copyDefaults() && !defaults.isEmpty()) {
            Map<String, Object> defaultsNested = unflatten(defaults);
            mergeMissing(defaultsNested, nested);
        }
        DumperOptions dumper = new DumperOptions();
        dumper.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumper.setIndent(Math.max(2, options().indent()));
        dumper.setWidth(options().width() > 0 ? options().width() : 80);
        dumper.setAllowUnicode(true);
        Yaml yaml = new Yaml(dumper);
        String body = nested.isEmpty() ? "" : yaml.dump(nested);
        String header = options().copyHeader() ? options().header() : null;
        if (header == null || header.isEmpty()) return body;
        StringBuilder out = new StringBuilder();
        for (String line : header.split("\\r?\\n", -1)) {
            out.append('#');
            if (!line.isEmpty()) out.append(' ').append(line);
            out.append('\n');
        }
        out.append('\n').append(body);
        return out.toString();
    }

    @Override
    public void loadFromString(String contents) throws InvalidConfigurationException {
        if (contents == null || contents.isEmpty()) return;
        Object loaded;
        try {
            loaded = new Yaml().load(contents);
        } catch (Exception e) {
            throw new InvalidConfigurationException("YAML parse failed: " + e.getMessage());
        }
        if (loaded instanceof Map) {
            values.clear();
            MemorySection.flattenInto((Map<String, Object>) loaded, "", values);
        }
    }

    @Override
    public YamlConfigurationOptions options() {
        return (YamlConfigurationOptions) super.options();
    }

    public static YamlConfiguration loadConfiguration(File file) {
        YamlConfiguration cfg = new YamlConfiguration();
        try { cfg.load(file); } catch (IOException | InvalidConfigurationException ignored) {}
        return cfg;
    }

    public static YamlConfiguration loadConfiguration(Reader reader) {
        YamlConfiguration cfg = new YamlConfiguration();
        try { cfg.load(reader); } catch (IOException | InvalidConfigurationException ignored) {}
        return cfg;
    }

    /** Reverse of {@link MemorySection#flattenInto}: walks dot-separated
     *  paths and rebuilds nested maps so SnakeYAML emits proper indented
     *  sections. Keys without dots stay at the top level. List/scalar
     *  values are inserted unchanged. */
    private static Map<String, Object> unflatten(Map<String, Object> flat) {
        Map<String, Object> root = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : flat.entrySet()) {
            String key = e.getKey();
            String[] parts = key.split("\\.");
            Map<String, Object> cursor = root;
            for (int i = 0; i < parts.length - 1; i++) {
                Object next = cursor.get(parts[i]);
                if (!(next instanceof Map)) {
                    Map<String, Object> child = new LinkedHashMap<>();
                    cursor.put(parts[i], child);
                    cursor = child;
                } else {
                    cursor = (Map<String, Object>) next;
                }
            }
            cursor.put(parts[parts.length - 1], e.getValue());
        }
        return root;
    }

    /** Recursively copy {@code defaults} entries into {@code target}
     *  where the target lacks them, mirroring real Bukkit's
     *  {@code copyDefaults} semantics. Existing values win. */
    private static void mergeMissing(Map<String, Object> defaults, Map<String, Object> target) {
        for (Map.Entry<String, Object> e : defaults.entrySet()) {
            Object existing = target.get(e.getKey());
            if (existing == null) {
                target.put(e.getKey(), e.getValue());
            } else if (existing instanceof Map && e.getValue() instanceof Map) {
                mergeMissing((Map<String, Object>) e.getValue(), (Map<String, Object>) existing);
            }
        }
    }
}
