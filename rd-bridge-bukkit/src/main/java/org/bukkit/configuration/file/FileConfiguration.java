// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.configuration.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Stub of Bukkit's {@code FileConfiguration} that actually loads YAML
 * content into the {@link org.bukkit.configuration.MemorySection} backing
 * store. Plugin code expects round-trip from
 * {@code load(File)} → {@code getString(path)}; the previous no-op
 * silently dropped every plugin's persisted config.
 *
 * <p>Save paths remain no-ops — RDForward never writes plugin YAML to
 * disk, only reads what is already there. Plugins that depend on save
 * round-trips should be flagged as unsupported.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class FileConfiguration extends org.bukkit.configuration.MemoryConfiguration {

    private YamlConfigurationOptions options;

    public FileConfiguration() {}
    public FileConfiguration(org.bukkit.configuration.Configuration arg0) {}

    /** Serialise the in-memory configuration to {@code file} as UTF-8
     *  via {@link #saveToString}. Creates parent directories as needed.
     *  Plugins (SimpleLogin, LoginSecurity) call this from {@code
     *  saveConfig()} during {@code onEnable} to materialise their
     *  default config; round-trip means the next reload sees the
     *  values that were just written. */
    public void save(java.io.File file) throws java.io.IOException {
        if (file == null) return;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
        String body = saveToString();
        Files.write(file.toPath(), body.getBytes(StandardCharsets.UTF_8));
    }

    public void save(java.lang.String filename) throws java.io.IOException {
        if (filename == null) return;
        save(new File(filename));
    }

    public abstract java.lang.String saveToString();

    /** Read the full file contents as UTF-8 and hand them to
     *  {@link #loadFromString}. Missing files round-trip silently — a
     *  plugin that calls {@code load(file)} with no on-disk config gets
     *  an empty configuration, matching real Bukkit behaviour. */
    public void load(File file) throws java.io.FileNotFoundException, IOException, org.bukkit.configuration.InvalidConfigurationException {
        if (file == null || !file.exists()) return;
        String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        loadFromString(content);
    }

    public void load(Reader reader) throws IOException, org.bukkit.configuration.InvalidConfigurationException {
        if (reader == null) return;
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[4096];
        BufferedReader br = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
        int n;
        while ((n = br.read(buf)) >= 0) sb.append(buf, 0, n);
        loadFromString(sb.toString());
    }

    public void load(String filename) throws java.io.FileNotFoundException, IOException, org.bukkit.configuration.InvalidConfigurationException {
        if (filename == null) return;
        load(new File(filename));
    }

    public abstract void loadFromString(String contents) throws org.bukkit.configuration.InvalidConfigurationException;

    /** Returns an empty header — RDForward never composes the YAML
     *  preamble. Plugins that feed this into {@code new
     *  StringBuilder(buildHeader())} would NPE on {@code null}, hence
     *  the explicit empty string. */
    protected String buildHeader() { return ""; }

    public org.bukkit.configuration.file.FileConfigurationOptions options() {
        if (options == null) {
            options = (this instanceof YamlConfiguration)
                    ? new YamlConfigurationOptions((YamlConfiguration) this)
                    : new YamlConfigurationOptions();
        }
        return options;
    }
}
