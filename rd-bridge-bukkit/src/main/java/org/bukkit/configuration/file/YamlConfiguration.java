package org.bukkit.configuration.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Minimal YAML-ish file configuration. Uses Java's string-based parse of
 * {@code key: value} lines — plugins that rely on full YAML (nesting,
 * lists, anchors) need to supply their own parser. Values round-trip as
 * strings; typed getters coerce on read. Plugin that just stores a handful
 * of scalars works without surprise.
 *
 * <p>This is deliberately narrow: RDForward's plugin ecosystem is small
 * and Bukkit YAML full compatibility is out of scope. Plugins requiring
 * richer config can bundle snakeyaml themselves.
 */
public class YamlConfiguration extends FileConfiguration {

    private final Map<String, Object> data = new LinkedHashMap<>();

    /** Factory matching upstream Bukkit's usual entrypoint. */
    public static YamlConfiguration loadConfiguration(File file) {
        YamlConfiguration c = new YamlConfiguration();
        try { c.load(file); } catch (IOException e) { /* silent — upstream returns empty */ }
        return c;
    }

    @Override
    public void load(File file) throws IOException {
        data.clear();
        if (file == null || !file.exists()) return;
        for (String line : Files.readAllLines(file.toPath())) {
            int hash = line.indexOf('#');
            String trimmed = (hash >= 0 ? line.substring(0, hash) : line).trim();
            if (trimmed.isEmpty()) continue;
            int colon = trimmed.indexOf(':');
            if (colon <= 0) continue;
            String key = trimmed.substring(0, colon).trim();
            String raw = trimmed.substring(colon + 1).trim();
            data.put(key, stripQuotes(raw));
        }
    }

    @Override
    public void save(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> e : data.entrySet()) {
            sb.append(e.getKey()).append(": ").append(e.getValue()).append('\n');
        }
        Files.writeString(file.toPath(), sb.toString());
    }

    @Override public boolean contains(String path) { return data.containsKey(path); }

    @Override public Object get(String path) { return data.get(path); }

    @Override public Object get(String path, Object def) {
        Object v = data.get(path);
        return v == null ? def : v;
    }

    @Override public String getString(String path) {
        Object v = data.get(path);
        return v == null ? null : v.toString();
    }

    @Override public String getString(String path, String def) {
        String v = getString(path);
        return v == null ? def : v;
    }

    @Override public int getInt(String path) { return getInt(path, 0); }

    @Override public int getInt(String path, int def) {
        String v = getString(path);
        if (v == null) return def;
        try { return Integer.parseInt(v); } catch (NumberFormatException e) { return def; }
    }

    @Override public boolean getBoolean(String path) { return getBoolean(path, false); }

    @Override public boolean getBoolean(String path, boolean def) {
        String v = getString(path);
        if (v == null) return def;
        return Boolean.parseBoolean(v);
    }

    @Override public double getDouble(String path) { return getDouble(path, 0.0); }

    @Override public double getDouble(String path, double def) {
        String v = getString(path);
        if (v == null) return def;
        try { return Double.parseDouble(v); } catch (NumberFormatException e) { return def; }
    }

    @Override public void set(String path, Object value) {
        if (value == null) data.remove(path); else data.put(path, value);
    }

    @Override public Set<String> getKeys(boolean deep) {
        return new LinkedHashSet<>(data.keySet());
    }

    private static String stripQuotes(String v) {
        if (v.length() >= 2 && (v.startsWith("\"") && v.endsWith("\"")
                || v.startsWith("'") && v.endsWith("'"))) {
            return v.substring(1, v.length() - 1);
        }
        return v;
    }
}
