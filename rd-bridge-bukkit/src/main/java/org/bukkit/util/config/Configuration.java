// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.util.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Pre-Bukkit-1.x {@code org.bukkit.util.config.Configuration}.
 * Replaced in modern paper-api by
 * {@link org.bukkit.configuration.file.YamlConfiguration}; legacy
 * plugins (OpenWarp v1.1) instantiate {@code new Configuration(file)}
 * and call {@code load()} / {@code save()} / {@code setProperty} on
 * it directly.
 *
 * <p>Implementation wraps a {@link YamlConfiguration} so reads round
 * trip through the same backing store as the modern API. {@link #save}
 * returns a {@code boolean} — {@code true} on success, {@code false}
 * on any I/O failure — matching upstream's contract that plugins like
 * OpenWarp branch on (it logs and continues rather than throwing).
 */
@SuppressWarnings("unused")
public class Configuration extends ConfigurationNode {

    private final File file;

    public Configuration(File file) {
        super(new YamlConfiguration());
        this.file = file;
    }

    /** Read {@link #file} into the backing {@link YamlConfiguration}.
     *  Missing files round-trip silently — the section stays empty,
     *  matching upstream behaviour. */
    public void load() {
        try {
            ((YamlConfiguration) section).load(file);
        } catch (IOException | InvalidConfigurationException ignored) {
            // legacy contract: load() is void and swallows; plugins
            // observe an empty config rather than an exception.
        }
    }

    /** Write the backing {@link YamlConfiguration} to {@link #file}.
     *  @return {@code true} on success, {@code false} on any I/O error. */
    public boolean save() {
        try {
            ((YamlConfiguration) section).save(file);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /** Pre-1.x header API. Modern paper-api routes this through
     *  {@code options().header}; the legacy method took a varargs
     *  string list. RDForward never composes the YAML preamble, so
     *  this is a no-op. */
    public void setHeader(String... lines) {}

    /** @return the backing file. */
    public File getFile() { return file; }
}
