// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.configuration;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stub of Bukkit's {@code MemorySection} that actually carries a
 * {@code Map<String,Object>} backing store. Plugin code that calls
 * {@link YamlConfiguration#loadConfiguration} expects subsequent
 * {@code getString(path)} / {@code getInt(path)} reads to return the
 * parsed values; a no-op stub silently dropped them, which broke any
 * plugin that relied on its on-disk {@code config.yml} (LoginSecurity's
 * language picker, LuckPerms's storage backend toggle, …).
 *
 * <p>Paths are dot-separated; nested YAML structures are flattened into
 * single dot-keyed entries on load (see
 * {@link MemorySection#flattenInto}). {@code addDefault} stores into a
 * separate defaults map that {@code get*} falls back to when the primary
 * map has no entry — same precedence as paper-api.
 *
 * <p>Hierarchical sections are real: {@link #getConfigurationSection}
 * returns a view over the root's flat-keyed {@code values} map with a
 * path prefix, and {@link #getKeys}/{@link #getValues} on that view
 * filter the root keys to what is in scope (immediate children when
 * {@code deep=false}, full descendants when {@code deep=true}).
 * Subsections share the root's backing maps, so writes round-trip in
 * either direction.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class MemorySection implements ConfigurationSection {

    /** Primary store; populated by {@link #set} and YAML loaders.
     *  Subsections share the root's map so reads/writes round-trip. */
    protected Map<String, Object> values = new LinkedHashMap<>();

    /** Defaults populated by {@link #addDefault}; queried as a fallback.
     *  Shared across the whole section tree, same as {@link #values}. */
    protected Map<String, Object> defaults = new LinkedHashMap<>();

    private final ConfigurationSection parent;
    private final String prefix;

    protected MemorySection() { this(null, ""); }

    protected MemorySection(ConfigurationSection parent, String path) {
        this.parent = parent;
        this.prefix = path == null ? "" : path;
        if (parent instanceof MemorySection) {
            MemorySection rootSection = (MemorySection) parent;
            this.values = rootSection.values;
            this.defaults = rootSection.defaults;
        }
    }

    /** Translate a section-relative path into a root-absolute key. The
     *  root's {@link #prefix} is empty so the input is returned as-is;
     *  subsections prepend their dot-joined prefix. */
    private String resolve(String path) {
        if (path == null || prefix.isEmpty()) return path;
        return path.isEmpty() ? prefix : prefix + "." + path;
    }

    public Set getKeys(boolean deep) {
        Set<String> out = new LinkedHashSet<>();
        addKeys(values.keySet(), deep, out);
        addKeys(defaults.keySet(), deep, out);
        return out;
    }

    private void addKeys(Set<String> source, boolean deep, Set<String> out) {
        if (prefix.isEmpty()) {
            for (String key : source) {
                if (deep) out.add(key);
                else {
                    int dot = key.indexOf('.');
                    out.add(dot < 0 ? key : key.substring(0, dot));
                }
            }
            return;
        }
        String pfx = prefix + ".";
        for (String key : source) {
            if (!key.startsWith(pfx)) continue;
            String rest = key.substring(pfx.length());
            if (rest.isEmpty()) continue;
            if (deep) out.add(rest);
            else {
                int dot = rest.indexOf('.');
                out.add(dot < 0 ? rest : rest.substring(0, dot));
            }
        }
    }

    public Map getValues(boolean deep) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (Object key : getKeys(deep)) {
            Object v = get((String) key);
            if (v != null) out.put((String) key, v);
        }
        return out;
    }

    public boolean contains(String path) {
        String key = resolve(path);
        return values.containsKey(key) || defaults.containsKey(key);
    }
    public boolean contains(String path, boolean ignoreDefault) {
        String key = resolve(path);
        return ignoreDefault ? values.containsKey(key) : (values.containsKey(key) || defaults.containsKey(key));
    }
    public boolean isSet(String path) { return values.containsKey(resolve(path)); }

    public String getCurrentPath() { return prefix; }
    public String getName() {
        int i = prefix.lastIndexOf('.');
        return i >= 0 ? prefix.substring(i + 1) : prefix;
    }
    public Configuration getRoot() {
        // Walk to the top of the parent chain. The root of any well-formed
        // tree is a Configuration (MemoryConfiguration / YamlConfiguration).
        // HomeSpawnPlus's BukkitYamlConfigFile.getRootConfigurationSection
        // calls yaml.getRoot() and falls back to a null path when this
        // returns null, so a top-level YamlConfiguration MUST report itself.
        ConfigurationSection cs = this;
        while (cs.getParent() != null) cs = cs.getParent();
        return (cs instanceof Configuration) ? (Configuration) cs : null;
    }
    public ConfigurationSection getParent() { return parent; }

    public void addDefault(String path, Object value) { defaults.put(resolve(path), value); }
    public ConfigurationSection getDefaultSection() { return null; }

    public void set(String path, Object value) {
        String key = resolve(path);
        if (value == null) values.remove(key);
        else values.put(key, value);
    }

    public Object get(String path) {
        String key = resolve(path);
        Object v = values.get(key);
        if (v != null) return maybeDeserialize(v);
        Object d = defaults.get(key);
        if (d != null) return maybeDeserialize(d);
        // No direct value at {@code key}, but the path may name a
        // sub-section (e.g. "homes" when the flat map carries
        // "homes.home.world" / "homes.home.x" / ...). Real Bukkit's
        // {@code MemorySection.get} returns the section in that case,
        // and Essentials's {@code UserData._getHomes} relies on
        // {@code getConfigurationSection("homes").getValues(false)}
        // returning a non-empty Map — without this, /home reports
        // "no homes set" even when the YAML has them.
        if (key == null || key.isEmpty()) return null;
        String dotPfx = key + ".";
        // Check if this sub-section represents a serialized object
        // (has a "==" key). If so, reconstruct the Map and deserialize.
        String typeKey = key + "." + ConfigurationSerialization.SERIALIZED_TYPE_KEY;
        Object typeVal = values.get(typeKey);
        if (typeVal == null) typeVal = defaults.get(typeKey);
        if (typeVal != null) {
            Map<String, Object> map = collectSubMap(key, values);
            if (map.isEmpty()) map = collectSubMap(key, defaults);
            ConfigurationSerializable cs = ConfigurationSerialization.deserializeObject(map);
            if (cs != null) return cs;
        }
        for (String k : values.keySet()) {
            if (k.startsWith(dotPfx)) return new MemorySection(this, key);
        }
        for (String k : defaults.keySet()) {
            if (k.startsWith(dotPfx)) return new MemorySection(this, key);
        }
        return null;
    }
    public Object get(String path, Object def) {
        Object v = get(path);
        return v != null ? v : def;
    }

    public ConfigurationSection createSection(String path) {
        String full = resolve(path);
        return new MemorySection(this, full);
    }
    public ConfigurationSection createSection(String path, Map map) {
        String full = resolve(path);
        String dotPfx = full + ".";
        for (Object entry : ((Map<?,?>) map).entrySet()) {
            Map.Entry<?,?> e = (Map.Entry<?,?>) entry;
            values.put(dotPfx + e.getKey(), e.getValue());
        }
        return new MemorySection(this, full);
    }

    public String getString(String path) {
        Object o = get(path);
        return o == null ? null : o.toString();
    }
    public String getString(String path, String def) {
        Object o = get(path);
        return o == null ? def : o.toString();
    }
    public boolean isString(String path) { return get(path) instanceof String; }

    public int getInt(String path) {
        Object o = get(path);
        return o instanceof Number ? ((Number) o).intValue() : 0;
    }
    public int getInt(String path, int def) {
        Object o = get(path);
        return o instanceof Number ? ((Number) o).intValue() : def;
    }
    public boolean isInt(String path) { return get(path) instanceof Integer; }

    public boolean getBoolean(String path) {
        Object o = get(path);
        return o instanceof Boolean && (Boolean) o;
    }
    public boolean getBoolean(String path, boolean def) {
        Object o = get(path);
        return o instanceof Boolean ? (Boolean) o : def;
    }
    public boolean isBoolean(String path) { return get(path) instanceof Boolean; }

    public double getDouble(String path) {
        Object o = get(path);
        return o instanceof Number ? ((Number) o).doubleValue() : 0.0;
    }
    public double getDouble(String path, double def) {
        Object o = get(path);
        return o instanceof Number ? ((Number) o).doubleValue() : def;
    }
    public boolean isDouble(String path) {
        Object o = get(path);
        return o instanceof Double || o instanceof Float;
    }

    public long getLong(String path) {
        Object o = get(path);
        return o instanceof Number ? ((Number) o).longValue() : 0L;
    }
    public long getLong(String path, long def) {
        Object o = get(path);
        return o instanceof Number ? ((Number) o).longValue() : def;
    }
    public boolean isLong(String path) { return get(path) instanceof Long; }

    public List getList(String path) {
        Object o = get(path);
        return o instanceof List ? (List) o : Collections.emptyList();
    }
    public List getList(String path, List def) {
        Object o = get(path);
        return o instanceof List ? (List) o : (def != null ? def : Collections.emptyList());
    }
    public boolean isList(String path) { return get(path) instanceof List; }

    public List getStringList(String path) {
        List<?> raw = getList(path);
        List<String> out = new ArrayList<>(raw.size());
        for (Object o : raw) if (o != null) out.add(o.toString());
        return out;
    }
    public List getIntegerList(String path) {
        List<?> raw = getList(path);
        List<Integer> out = new ArrayList<>(raw.size());
        for (Object o : raw) if (o instanceof Number) out.add(((Number) o).intValue());
        return out;
    }
    public List getBooleanList(String path) {
        List<?> raw = getList(path);
        List<Boolean> out = new ArrayList<>(raw.size());
        for (Object o : raw) if (o instanceof Boolean) out.add((Boolean) o);
        return out;
    }
    public List getDoubleList(String path) {
        List<?> raw = getList(path);
        List<Double> out = new ArrayList<>(raw.size());
        for (Object o : raw) if (o instanceof Number) out.add(((Number) o).doubleValue());
        return out;
    }
    public List getFloatList(String path) {
        List<?> raw = getList(path);
        List<Float> out = new ArrayList<>(raw.size());
        for (Object o : raw) if (o instanceof Number) out.add(((Number) o).floatValue());
        return out;
    }
    public List getLongList(String path) {
        List<?> raw = getList(path);
        List<Long> out = new ArrayList<>(raw.size());
        for (Object o : raw) if (o instanceof Number) out.add(((Number) o).longValue());
        return out;
    }
    public List getByteList(String path) {
        List<?> raw = getList(path);
        List<Byte> out = new ArrayList<>(raw.size());
        for (Object o : raw) if (o instanceof Number) out.add(((Number) o).byteValue());
        return out;
    }
    public List getCharacterList(String path) {
        List<?> raw = getList(path);
        List<Character> out = new ArrayList<>(raw.size());
        for (Object o : raw) {
            if (o instanceof Character) out.add((Character) o);
            else if (o instanceof String && !((String) o).isEmpty()) out.add(((String) o).charAt(0));
        }
        return out;
    }
    public List getShortList(String path) {
        List<?> raw = getList(path);
        List<Short> out = new ArrayList<>(raw.size());
        for (Object o : raw) if (o instanceof Number) out.add(((Number) o).shortValue());
        return out;
    }
    public List getMapList(String path) {
        List<?> raw = getList(path);
        List<Map> out = new ArrayList<>(raw.size());
        for (Object o : raw) if (o instanceof Map) out.add((Map) o);
        return out;
    }

    public Object getObject(String path, Class clazz) {
        Object o = get(path);
        return clazz != null && clazz.isInstance(o) ? o : null;
    }
    public Object getObject(String path, Class clazz, Object def) {
        Object o = getObject(path, clazz);
        return o == null ? def : o;
    }

    public ConfigurationSerializable getSerializable(String path, Class clazz) { return null; }
    public ConfigurationSerializable getSerializable(String path, Class clazz, ConfigurationSerializable def) { return def; }

    public Vector getVector(String path) { return null; }
    public Vector getVector(String path, Vector def) { return def; }
    public boolean isVector(String path) { return false; }

    public OfflinePlayer getOfflinePlayer(String path) { return null; }
    public OfflinePlayer getOfflinePlayer(String path, OfflinePlayer def) { return def; }
    public boolean isOfflinePlayer(String path) { return false; }

    public ItemStack getItemStack(String path) { return null; }
    public ItemStack getItemStack(String path, ItemStack def) { return def; }
    public boolean isItemStack(String path) { return false; }

    public Color getColor(String path) { return null; }
    public Color getColor(String path, Color def) { return def; }
    public boolean isColor(String path) { return false; }

    public Location getLocation(String path) { return null; }
    public Location getLocation(String path, Location def) { return def; }
    public boolean isLocation(String path) { return false; }

    /** Returns a view of all flat keys nested under {@code path}. The
     *  view shares this section's backing {@link #values}/{@link
     *  #defaults} maps so reads and writes round-trip in either
     *  direction. Returns {@code null} when no key matches {@code path}
     *  or {@code path.*}, mirroring upstream behaviour — LogBlock 1.41
     *  iterates {@code getConfigurationSection("tools").getKeys(false)}
     *  to enumerate per-tool subtrees and NPE'd on the previous null
     *  stub. */
    public ConfigurationSection getConfigurationSection(String path) {
        if (path == null) return null;
        if (path.isEmpty()) return this;
        String full = resolve(path);
        String dotPfx = full + ".";
        for (String key : values.keySet()) {
            if (key.equals(full) || key.startsWith(dotPfx)) {
                return new MemorySection(this, full);
            }
        }
        for (String key : defaults.keySet()) {
            if (key.equals(full) || key.startsWith(dotPfx)) {
                return new MemorySection(this, full);
            }
        }
        return null;
    }
    public boolean isConfigurationSection(String path) {
        String key = resolve(path);
        if (values.containsKey(key) || defaults.containsKey(key)) return true;
        String dotPfx = key + ".";
        for (String k : values.keySet()) if (k.startsWith(dotPfx)) return true;
        for (String k : defaults.keySet()) if (k.startsWith(dotPfx)) return true;
        return false;
    }

    protected boolean isPrimitiveWrapper(Object value) {
        return value instanceof Integer || value instanceof Boolean
                || value instanceof Long || value instanceof Double
                || value instanceof Float || value instanceof Short
                || value instanceof Byte || value instanceof Character;
    }

    /** Collect all flat entries under {@code prefix} into a nested Map,
     *  reversing the dot-flattening done by {@link #flattenInto}.
     *  For example, prefix "homes.2" with entries "homes.2.x"=1.0,
     *  "homes.2.y"=2.0 produces {"x":1.0, "y":2.0}. */
    private static Map<String, Object> collectSubMap(String prefix, Map<String, Object> flat) {
        Map<String, Object> out = new LinkedHashMap<>();
        String dotPfx = prefix + ".";
        for (Map.Entry<String, Object> e : flat.entrySet()) {
            if (e.getKey().startsWith(dotPfx)) {
                String rest = e.getKey().substring(dotPfx.length());
                out.put(rest, e.getValue());
            }
        }
        return out;
    }

    /** If the value is a Map containing a {@code "=="} key, try to
     *  deserialize it via {@link ConfigurationSerialization}. */
    private static Object maybeDeserialize(Object o) {
        if (o instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) o;
            if (map.containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> typed = (Map<String, Object>) map;
                ConfigurationSerializable cs = ConfigurationSerialization.deserializeObject(typed);
                if (cs != null) return cs;
            }
        }
        return o;
    }
    protected Object getDefault(String path) { return defaults.get(resolve(path)); }

    protected void mapChildrenKeys(Set output, ConfigurationSection section, boolean deep) {}
    protected void mapChildrenValues(Map output, ConfigurationSection section, boolean deep) {}

    public static String createPath(ConfigurationSection section, String key) { return key; }
    public static String createPath(ConfigurationSection section, String key, ConfigurationSection relativeTo) { return key; }

    public List getComments(String path) { return Collections.emptyList(); }
    public List getInlineComments(String path) { return Collections.emptyList(); }
    public void setComments(String path, List comments) {}
    public void setInlineComments(String path, List comments) {}

    /**
     * Recursively flatten a parsed YAML node ({@code Map<String,Object>})
     * into a dot-keyed map suitable for {@link #values}. Lists and
     * scalars are stored as-is; nested maps prefix their keys with
     * {@code parent.}. Used by {@link YamlConfiguration#loadFromString}.
     */
    public static void flattenInto(Map<String, Object> source, String prefix, Map<String, Object> dest) {
        for (Map.Entry<String, Object> e : source.entrySet()) {
            String rawKey = String.valueOf(e.getKey());
            String key = prefix.isEmpty() ? rawKey : prefix + "." + rawKey;
            Object val = e.getValue();
            if (val instanceof Map) {
                flattenInto((Map<String, Object>) val, key, dest);
            } else {
                dest.put(key, val);
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[path='" + getCurrentPath() + "']";
    }
}
