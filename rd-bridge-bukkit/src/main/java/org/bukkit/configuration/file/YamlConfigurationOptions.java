// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.configuration.file;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Bukkit-shaped {@code YamlConfigurationOptions} with real backing
 * fields. Plugins (LoginSecurity, SimpleLogin, LuckPerms) call
 * {@code options().header(...).copyDefaults(true)} during {@code
 * onEnable} and expect those flags to round-trip when the plugin later
 * saves its config; without backing fields the calls were no-ops and
 * the saved YAML lost any header / failed to copy defaults.
 *
 * <p>{@link #configuration()} echoes back the {@link YamlConfiguration}
 * that produced this instance so plugin code that walks
 * {@code options.configuration().something()} keeps working. The fluent
 * setters return {@code this} for chainability, matching paper-api.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class YamlConfigurationOptions extends org.bukkit.configuration.file.FileConfigurationOptions {

    private final YamlConfiguration owner;

    private List<String> header = Collections.emptyList();
    private List<String> footer = Collections.emptyList();
    private boolean copyDefaults;
    private boolean copyHeader = true;
    private boolean parseComments = true;
    private char pathSeparator = '.';
    private int indent = 2;
    private int width = 80;
    private int codePointLimit;

    protected YamlConfigurationOptions(org.bukkit.configuration.file.YamlConfiguration arg0) {
        super((org.bukkit.configuration.MemoryConfiguration) null);
        this.owner = arg0;
    }

    public YamlConfigurationOptions() {
        super((org.bukkit.configuration.MemoryConfiguration) null);
        this.owner = null;
    }

    @Override
    public org.bukkit.configuration.file.YamlConfiguration configuration() {
        return owner;
    }

    @Override
    public org.bukkit.configuration.file.YamlConfigurationOptions copyDefaults(boolean value) {
        this.copyDefaults = value;
        return this;
    }

    /** Real Bukkit exposes a {@code copyDefaults()} getter that mirrors
     *  the setter; YamlConfiguration.save uses this when copying. */
    public boolean copyDefaults() { return copyDefaults; }

    @Override
    public org.bukkit.configuration.file.YamlConfigurationOptions pathSeparator(char value) {
        this.pathSeparator = value;
        return this;
    }
    public char pathSeparator() { return pathSeparator; }

    @Override
    public org.bukkit.configuration.file.YamlConfigurationOptions setHeader(java.util.List value) {
        this.header = value == null ? Collections.emptyList() : new ArrayList<String>(value);
        return this;
    }

    @Override
    public java.util.List getHeader() { return Collections.unmodifiableList(header); }

    @Override
    public org.bukkit.configuration.file.YamlConfigurationOptions header(java.lang.String value) {
        if (value == null || value.isEmpty()) {
            this.header = Collections.emptyList();
        } else {
            this.header = new ArrayList<String>(Arrays.asList(value.split("\\r?\\n", -1)));
        }
        return this;
    }

    @Override
    public java.lang.String header() {
        return header.isEmpty() ? null : String.join("\n", header);
    }

    @Override
    public org.bukkit.configuration.file.YamlConfigurationOptions setFooter(java.util.List value) {
        this.footer = value == null ? Collections.emptyList() : new ArrayList<String>(value);
        return this;
    }

    @Override
    public java.util.List getFooter() { return Collections.unmodifiableList(footer); }

    @Override
    public org.bukkit.configuration.file.YamlConfigurationOptions parseComments(boolean value) {
        this.parseComments = value;
        return this;
    }

    @Override
    public boolean parseComments() { return parseComments; }

    @Override
    public org.bukkit.configuration.file.YamlConfigurationOptions copyHeader(boolean value) {
        this.copyHeader = value;
        return this;
    }

    @Override
    public boolean copyHeader() { return copyHeader; }

    public int indent() { return indent; }
    public org.bukkit.configuration.file.YamlConfigurationOptions indent(int value) {
        this.indent = value;
        return this;
    }

    public int width() { return width; }
    public org.bukkit.configuration.file.YamlConfigurationOptions width(int value) {
        this.width = value;
        return this;
    }

    public int codePointLimit() { return codePointLimit; }
    public org.bukkit.configuration.file.YamlConfigurationOptions codePointLimit(int value) {
        this.codePointLimit = value;
        return this;
    }
}
