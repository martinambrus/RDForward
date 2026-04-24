package com.github.martinambrus.rdforward.bridge.pocketmine;

import org.yaml.snakeyaml.Yaml;
import pocketmine.plugin.PluginDescription;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads a PocketMine-style {@code plugin.yml} and produces a
 * {@link PluginDescription}. Schema matches PocketMine-MP: required
 * {@code name} + {@code version} + {@code main}; optional {@code api},
 * {@code depend}, {@code softdepend}, {@code loadbefore}, {@code authors},
 * {@code description}, and {@code commands}.
 */
public final class PocketMinePluginParser {

    private PocketMinePluginParser() {}

    public static PluginDescription parse(InputStream in) {
        Yaml yaml = new Yaml();
        Map<String, Object> root = yaml.load(in);
        if (root == null) throw new IllegalArgumentException("empty plugin.yml");
        String name = requireString(root, "name");
        String version = requireString(root, "version");
        String main = requireString(root, "main");
        String api = stringOr(root.get("api"), "");
        String description = stringOr(root.get("description"), "");
        List<String> depend = asStringList(root.get("depend"));
        List<String> softDepend = asStringList(root.get("softdepend"));
        List<String> loadBefore = asStringList(root.get("loadbefore"));
        List<String> authors = parseAuthors(root);
        Map<String, Map<String, Object>> commands = parseCommands(root.get("commands"));
        return new PluginDescription(
                name, version, main, api,
                depend, softDepend, loadBefore, authors, description, commands
        );
    }

    private static List<String> parseAuthors(Map<String, Object> root) {
        Object authors = root.get("authors");
        if (authors instanceof List<?>) return asStringList(authors);
        Object single = root.get("author");
        if (single instanceof String s && !s.isBlank()) return List.of(s);
        return List.of();
    }

    private static Map<String, Map<String, Object>> parseCommands(Object raw) {
        if (!(raw instanceof Map<?, ?> map)) return Map.of();
        Map<String, Map<String, Object>> out = new LinkedHashMap<>();
        for (Map.Entry<?, ?> e : map.entrySet()) {
            String cmdName = String.valueOf(e.getKey());
            Map<String, Object> body = new LinkedHashMap<>();
            if (e.getValue() instanceof Map<?, ?> m) {
                for (Map.Entry<?, ?> field : m.entrySet()) {
                    body.put(String.valueOf(field.getKey()), field.getValue());
                }
            }
            out.put(cmdName, Map.copyOf(body));
        }
        return Map.copyOf(out);
    }

    private static List<String> asStringList(Object raw) {
        if (raw instanceof List<?> list) {
            List<String> out = new ArrayList<>(list.size());
            for (Object v : list) out.add(String.valueOf(v));
            return List.copyOf(out);
        }
        if (raw instanceof String s && !s.isBlank()) return List.of(s);
        return List.of();
    }

    private static String stringOr(Object v, String fallback) {
        return v == null ? fallback : String.valueOf(v);
    }

    private static String requireString(Map<String, Object> root, String key) {
        Object v = root.get(key);
        if (!(v instanceof String s) || s.isBlank()) {
            throw new IllegalArgumentException("plugin.yml missing required field: " + key);
        }
        return s;
    }
}
