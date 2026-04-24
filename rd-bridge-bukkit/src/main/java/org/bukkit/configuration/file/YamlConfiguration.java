// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.configuration.file;

import java.io.File;
import java.io.Reader;

/**
 * Stub of Bukkit's {@code YamlConfiguration}. RDForward does not parse
 * YAML on behalf of plugins; the loader-returned instance is an empty
 * configuration whose two-arg getters return the caller-supplied default
 * (see {@link org.bukkit.configuration.MemorySection}).
 *
 * <p>The {@link #loadConfiguration(File)} / {@link #loadConfiguration(Reader)}
 * static factories MUST NOT return {@code null}: real plugin code (e.g.
 * LuckPerms's {@code BukkitConfigAdapter}) stores the result in a field
 * and later dereferences it without a null-check. Returning an empty
 * {@code YamlConfiguration} lets the plugin read every path, get its
 * default, and continue bootstrapping.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class YamlConfiguration extends org.bukkit.configuration.file.FileConfiguration {

    public YamlConfiguration() {}

    @Override
    public String saveToString() {
        return "";
    }

    @Override
    public void loadFromString(String contents)
            throws org.bukkit.configuration.InvalidConfigurationException {
        // no-op: we do not parse YAML in the stub
    }

    public YamlConfigurationOptions options() {
        return null;
    }

    /** @return an empty {@link YamlConfiguration}; never {@code null}. */
    public static YamlConfiguration loadConfiguration(File file) {
        return new YamlConfiguration();
    }

    /** @return an empty {@link YamlConfiguration}; never {@code null}. */
    public static YamlConfiguration loadConfiguration(Reader reader) {
        return new YamlConfiguration();
    }
}
