// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.configuration;

import java.util.Map;

/**
 * Stub of Bukkit's {@code MemoryConfiguration}. Inherits the real
 * backing map from {@link MemorySection} and routes the upstream
 * {@code addDefault*} / {@code setDefaults} accessors into it so plugins
 * that prime defaults via {@code addDefault(path, value)} and read with
 * {@code getString(path)} round-trip cleanly.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class MemoryConfiguration extends org.bukkit.configuration.MemorySection
        implements org.bukkit.configuration.Configuration {

    /** The defaults Configuration reference set via {@link #setDefaults}.
     *  Real Bukkit returns this from {@link #getDefaults}; HomeSpawnPlus's
     *  {@code BukkitYamlConfigFile.addDefaultConfig} round-trips through
     *  {@code setDefaults(jarYaml) → getDefaults() → getKeys(true)} and
     *  NPEs if {@code getDefaults()} returns null. */
    private org.bukkit.configuration.Configuration defaultsConfig;

    public MemoryConfiguration() {}

    public MemoryConfiguration(org.bukkit.configuration.Configuration source) {
        if (source != null) {
            for (String key : (java.util.Collection<String>) source.getKeys(true)) {
                Object v = source.get(key);
                if (v != null) defaults.put(key, v);
            }
        }
    }

    @Override
    public void addDefault(String path, Object value) {
        if (value == null) defaults.remove(path);
        else defaults.put(path, value);
        if (defaultsConfig == null) defaultsConfig = lazyDefaultsView();
    }

    public void addDefaults(Map<String, Object> map) {
        if (map == null) return;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            addDefault(e.getKey(), e.getValue());
        }
    }

    public void addDefaults(org.bukkit.configuration.Configuration source) {
        if (source == null) return;
        for (String key : (java.util.Collection<String>) source.getKeys(true)) {
            addDefault(key, source.get(key));
        }
    }

    public void setDefaults(org.bukkit.configuration.Configuration source) {
        if (source == null) throw new IllegalArgumentException("Defaults may not be null");
        defaults.clear();
        addDefaults(source);
        defaultsConfig = source;
    }

    public org.bukkit.configuration.Configuration getDefaults() {
        if (defaultsConfig == null && !defaults.isEmpty()) {
            defaultsConfig = lazyDefaultsView();
        }
        return defaultsConfig;
    }

    /** Build a Configuration view backed by this section's {@code defaults}
     *  map so {@code getDefaults().getKeys(true)} works even when the
     *  caller primed defaults via {@link #addDefault} rather than
     *  {@link #setDefaults}. */
    private org.bukkit.configuration.Configuration lazyDefaultsView() {
        MemoryConfiguration view = new MemoryConfiguration();
        view.values.putAll(defaults);
        return view;
    }

    @Override
    public org.bukkit.configuration.ConfigurationSection getParent() { return null; }

    public org.bukkit.configuration.MemoryConfigurationOptions options() { return null; }
}
