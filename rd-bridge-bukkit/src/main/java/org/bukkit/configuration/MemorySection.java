// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.configuration;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
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
 * <p>Sections proper ({@code getConfigurationSection}) are not yet
 * implemented; plugins that walk hierarchical sections still observe
 * {@code null} from that accessor and need to fall back to flat-path
 * reads (which now actually work).
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class MemorySection implements ConfigurationSection {

    /** Primary store; populated by {@link #set} and YAML loaders. */
    protected final Map<String, Object> values = new LinkedHashMap<>();

    /** Defaults populated by {@link #addDefault}; queried as a fallback. */
    protected final Map<String, Object> defaults = new LinkedHashMap<>();

    private final ConfigurationSection parent;
    private final String prefix;

    protected MemorySection() { this(null, ""); }

    protected MemorySection(ConfigurationSection parent, String path) {
        this.parent = parent;
        this.prefix = path == null ? "" : path;
    }

    public Set getKeys(boolean deep) {
        Set<String> out = new LinkedHashSet<>();
        out.addAll(values.keySet());
        out.addAll(defaults.keySet());
        return out;
    }

    public Map getValues(boolean deep) {
        Map<String, Object> out = new LinkedHashMap<>(defaults);
        out.putAll(values);
        return out;
    }

    public boolean contains(String path) {
        return values.containsKey(path) || defaults.containsKey(path);
    }
    public boolean contains(String path, boolean ignoreDefault) {
        return ignoreDefault ? values.containsKey(path) : contains(path);
    }
    public boolean isSet(String path) { return values.containsKey(path); }

    public String getCurrentPath() { return prefix; }
    public String getName() {
        int i = prefix.lastIndexOf('.');
        return i >= 0 ? prefix.substring(i + 1) : prefix;
    }
    public Configuration getRoot() { return null; }
    public ConfigurationSection getParent() { return parent; }

    public void addDefault(String path, Object value) { defaults.put(path, value); }
    public ConfigurationSection getDefaultSection() { return null; }

    public void set(String path, Object value) {
        if (value == null) values.remove(path);
        else values.put(path, value);
    }

    public Object get(String path) {
        Object v = values.get(path);
        return v != null ? v : defaults.get(path);
    }
    public Object get(String path, Object def) {
        Object v = get(path);
        return v != null ? v : def;
    }

    public ConfigurationSection createSection(String path) { return null; }
    public ConfigurationSection createSection(String path, Map map) { return null; }

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

    public ConfigurationSection getConfigurationSection(String path) { return null; }
    public boolean isConfigurationSection(String path) { return false; }

    protected boolean isPrimitiveWrapper(Object value) {
        return value instanceof Integer || value instanceof Boolean
                || value instanceof Long || value instanceof Double
                || value instanceof Float || value instanceof Short
                || value instanceof Byte || value instanceof Character;
    }
    protected Object getDefault(String path) { return defaults.get(path); }

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
            String key = prefix.isEmpty() ? e.getKey() : prefix + "." + e.getKey();
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
