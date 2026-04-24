// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.configuration;

import java.util.Set;

/**
 * Bukkit-shaped read/write configuration section. RDForward does not
 * expose a configuration pipeline, so implementations carry an in-memory
 * map that plugins can read/write at runtime but that is never persisted
 * unless the plugin explicitly reads/writes via
 * {@link org.bukkit.configuration.file.YamlConfiguration}.
 */
public interface Configuration {

    boolean contains(String path);

    Object get(String path);
    Object get(String path, Object def);

    String getString(String path);
    String getString(String path, String def);

    int getInt(String path);
    int getInt(String path, int def);

    boolean getBoolean(String path);
    boolean getBoolean(String path, boolean def);

    double getDouble(String path);
    double getDouble(String path, double def);

    void set(String path, Object value);

    Set<String> getKeys(boolean deep);
}
