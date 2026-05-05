// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads a Bukkit-style {@code plugin.yml} from an input stream and
 * produces a {@link BukkitPluginDescriptor}. Only the subset of fields
 * the bridge understands is required; unknown fields are ignored.
 */
public final class BukkitPluginParser {

    private BukkitPluginParser() {}

    public static BukkitPluginDescriptor parse(InputStream in) {
        Yaml yaml = new Yaml();
        Map<String, Object> root = yaml.load(in);
        if (root == null) throw new IllegalArgumentException("empty plugin.yml");
        String name = requireString(root, "name");
        String version = requireString(root, "version");
        String main = requireString(root, "main");
        List<String> depend = readStringList(root.get("depend"));
        List<String> softdepend = readStringList(root.get("softdepend"));
        Map<String, BukkitPluginDescriptor.CommandSpec> commands = parseCommands(root.get("commands"));
        boolean database = Boolean.TRUE.equals(root.get("database"));
        List<String> libraries = readStringList(root.get("libraries"));
        return new BukkitPluginDescriptor(name, version, main,
                List.copyOf(depend), List.copyOf(softdepend), commands, database, List.copyOf(libraries));
    }

    /** Pull a list-of-strings field, accepting both YAML list shape
     *  ({@code [a, b]}) and a single bare string. Returns an empty list
     *  for a missing or empty-shaped value. */
    private static List<String> readStringList(Object raw) {
        List<String> out = new ArrayList<>();
        if (raw instanceof List<?> list) {
            for (Object d : list) out.add(String.valueOf(d));
        } else if (raw instanceof String s && !s.isBlank()) {
            out.add(s);
        }
        return out;
    }

    private static Map<String, BukkitPluginDescriptor.CommandSpec> parseCommands(Object raw) {
        if (!(raw instanceof Map<?, ?> map)) return Map.of();
        Map<String, BukkitPluginDescriptor.CommandSpec> out = new LinkedHashMap<>();
        for (Map.Entry<?, ?> e : map.entrySet()) {
            String cmdName = String.valueOf(e.getKey());
            Map<?, ?> body = e.getValue() instanceof Map<?, ?> m ? m : Map.of();
            String description = stringOr(body.get("description"), "");
            String usage = stringOr(body.get("usage"), "");
            String permission = body.get("permission") == null ? null : String.valueOf(body.get("permission"));
            List<String> aliases = new ArrayList<>();
            Object rawAliases = body.get("aliases");
            if (rawAliases instanceof List<?> list) {
                for (Object a : list) aliases.add(String.valueOf(a));
            } else if (rawAliases instanceof String s && !s.isBlank()) {
                aliases.add(s);
            }
            out.put(cmdName, new BukkitPluginDescriptor.CommandSpec(
                    cmdName, description, usage, List.copyOf(aliases), permission));
        }
        return Map.copyOf(out);
    }

    private static String stringOr(Object v, String fallback) {
        return v == null ? fallback : String.valueOf(v);
    }

    /** Pull a required field, coercing non-String types via
     *  {@code String.valueOf}. SnakeYAML parses an unquoted
     *  {@code version: 0.02} as {@code Double}, which a strict
     *  {@code instanceof String} check would reject — even though
     *  every real Bukkit loader accepts it. LogBlockQuestioner's
     *  {@code plugin.yml} ships exactly this shape. */
    private static String requireString(Map<String, Object> root, String key) {
        Object v = root.get(key);
        if (v == null) {
            throw new IllegalArgumentException("plugin.yml missing required field: " + key);
        }
        String s = String.valueOf(v).trim();
        if (s.isEmpty()) {
            throw new IllegalArgumentException("plugin.yml missing required field: " + key);
        }
        return s;
    }
}
