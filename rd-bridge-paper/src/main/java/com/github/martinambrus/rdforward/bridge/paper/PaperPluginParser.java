package com.github.martinambrus.rdforward.bridge.paper;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitPluginDescriptor.CommandSpec;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads a {@code paper-plugin.yml} from an input stream and produces a
 * {@link PaperPluginDescriptor}. Handles both the flat dependency form
 * (plain list of plugin names) and Paper's nested form
 * ({@code dependencies: { bootstrap: { Name: { load: BEFORE, required: true } } }}).
 * Unknown fields are ignored so future Paper extensions don't break parsing.
 */
public final class PaperPluginParser {

    private PaperPluginParser() {}

    public static PaperPluginDescriptor parse(InputStream in) {
        Yaml yaml = new Yaml();
        Map<String, Object> root = yaml.load(in);
        if (root == null) throw new IllegalArgumentException("empty paper-plugin.yml");
        String name = requireString(root, "name");
        String version = requireString(root, "version");
        String main = requireString(root, "main");
        String bootstrapper = stringOr(root.get("bootstrapper"), null);
        String loader = stringOr(root.get("loader"), null);
        String description = stringOr(root.get("description"), "");
        String apiVersion = stringOr(root.get("api-version"), "");
        List<String> authors = parseStringList(root.get("authors"));
        if (authors.isEmpty()) {
            String single = stringOr(root.get("author"), null);
            if (single != null) authors = List.of(single);
        }
        List<String> bootstrapDeps;
        List<String> serverDeps;
        Object deps = root.get("dependencies");
        if (deps instanceof Map<?, ?> map) {
            bootstrapDeps = parseDependencySection(map.get("bootstrap"));
            serverDeps = parseDependencySection(map.get("server"));
        } else {
            bootstrapDeps = List.of();
            serverDeps = parseStringList(root.get("depend"));
        }
        Map<String, CommandSpec> commands = parseCommands(root.get("commands"));
        return new PaperPluginDescriptor(
                name, version, main,
                bootstrapper, loader,
                description, authors, apiVersion,
                bootstrapDeps, serverDeps,
                commands);
    }

    private static List<String> parseDependencySection(Object raw) {
        if (raw == null) return List.of();
        if (raw instanceof List<?> list) {
            List<String> out = new ArrayList<>();
            for (Object v : list) out.add(String.valueOf(v));
            return List.copyOf(out);
        }
        if (raw instanceof Map<?, ?> map) {
            List<String> out = new ArrayList<>();
            for (Object key : map.keySet()) out.add(String.valueOf(key));
            return List.copyOf(out);
        }
        return List.of();
    }

    private static List<String> parseStringList(Object raw) {
        if (!(raw instanceof List<?> list)) return List.of();
        List<String> out = new ArrayList<>();
        for (Object v : list) out.add(String.valueOf(v));
        return List.copyOf(out);
    }

    private static Map<String, CommandSpec> parseCommands(Object raw) {
        if (!(raw instanceof Map<?, ?> map)) return Map.of();
        Map<String, CommandSpec> out = new LinkedHashMap<>();
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
            out.put(cmdName, new CommandSpec(
                    cmdName, description, usage, List.copyOf(aliases), permission));
        }
        return Map.copyOf(out);
    }

    private static String stringOr(Object v, String fallback) {
        return v == null ? fallback : String.valueOf(v);
    }

    private static String requireString(Map<String, Object> root, String key) {
        Object v = root.get(key);
        if (!(v instanceof String s) || s.isBlank()) {
            throw new IllegalArgumentException("paper-plugin.yml missing required field: " + key);
        }
        return s;
    }
}
