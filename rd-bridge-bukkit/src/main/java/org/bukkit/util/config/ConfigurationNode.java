// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.util.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pre-Bukkit-1.x {@code org.bukkit.util.config.ConfigurationNode}.
 * Replaced in modern paper-api by {@link ConfigurationSection}; legacy
 * plugins (OpenWarp v1.1) still compile against the original
 * {@code Configuration}/{@code ConfigurationNode} pair.
 *
 * <p>This stub wraps a {@link ConfigurationSection} so the two APIs
 * share a single backing store. Reads delegate straight through; the
 * {@code getNode}/{@code getNodes}/{@code getNodeList} accessors
 * surface child sections (or transient sections built from inline
 * maps) as further {@link ConfigurationNode} views.
 *
 * <p>Keeps the legacy method shape: {@code setProperty} / {@code
 * removeProperty} / {@code getProperty} mirror upstream semantics,
 * and {@code getKeys(String)} returns {@code null} (not an empty list)
 * when the path is absent so call sites like OpenWarp's
 * {@code if (keys != null) { ... }} branch correctly.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class ConfigurationNode {

    protected final ConfigurationSection section;

    public ConfigurationNode(ConfigurationSection section) {
        this.section = section;
    }

    public String getString(String path) { return section.getString(path); }
    public String getString(String path, String def) { return section.getString(path, def); }

    public int getInt(String path, int def) { return section.getInt(path, def); }
    public double getDouble(String path, double def) { return section.getDouble(path, def); }
    public boolean getBoolean(String path, boolean def) { return section.getBoolean(path, def); }

    public List<String> getStringList(String path, List<String> def) {
        if (!section.contains(path)) return def == null ? new ArrayList<>() : def;
        return section.getStringList(path);
    }

    public List getList(String path) { return section.getList(path); }
    public List getList(String path, List def) {
        return section.contains(path) ? section.getList(path) : def;
    }

    public Object getProperty(String path) { return section.get(path); }
    public void setProperty(String path, Object value) { section.set(path, value); }
    public void removeProperty(String path) { section.set(path, null); }

    /** @return immediate child names of this node. */
    public List<String> getKeys() { return new ArrayList<>(section.getKeys(false)); }

    /** @return immediate child names under {@code path}, or {@code null}
     *  if no section exists there. OpenWarp's
     *  {@code OWConfigurationManager.loadWarps} branches on null to
     *  decide whether to iterate. */
    public List<String> getKeys(String path) {
        ConfigurationSection sub = section.getConfigurationSection(path);
        if (sub == null) return null;
        return new ArrayList<>(sub.getKeys(false));
    }

    /** @return a node view over the child section at {@code path}, or
     *  {@code null} if no such section exists. */
    public ConfigurationNode getNode(String path) {
        ConfigurationSection sub = section.getConfigurationSection(path);
        return sub == null ? null : new ConfigurationNode(sub);
    }

    /** @return a name → {@link ConfigurationNode} map for every
     *  immediate child of {@code path}, or {@code null} if no section
     *  exists there. */
    public Map<String, ConfigurationNode> getNodes(String path) {
        ConfigurationSection sub = section.getConfigurationSection(path);
        if (sub == null) return null;
        Map<String, ConfigurationNode> out = new LinkedHashMap<>();
        for (Object keyObj : sub.getKeys(false)) {
            String key = (String) keyObj;
            ConfigurationSection child = sub.getConfigurationSection(key);
            if (child != null) out.put(key, new ConfigurationNode(child));
        }
        return out;
    }

    /** @return a list of nodes, one per element of the YAML list at
     *  {@code path}. Each list element must be a map; non-map entries
     *  are skipped. Returns {@code def} when the path is absent or not
     *  a list (matching pre-1.x semantics). */
    public List<ConfigurationNode> getNodeList(String path, List<ConfigurationNode> def) {
        Object raw = section.get(path);
        if (!(raw instanceof List<?> list)) return def;
        List<ConfigurationNode> out = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> m) {
                Map<String, Object> flat = new LinkedHashMap<>();
                MemorySection.flattenInto((Map<String, Object>) m, "", flat);
                YamlConfiguration tmp = new YamlConfiguration();
                for (Map.Entry<String, Object> e : flat.entrySet()) tmp.set(e.getKey(), e.getValue());
                out.add(new ConfigurationNode(tmp));
            }
        }
        return out;
    }
}
