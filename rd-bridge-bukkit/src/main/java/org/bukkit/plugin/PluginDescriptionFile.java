// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.plugin;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Parsed view of a {@code plugin.yml}. RDForward's Bukkit bridge already
 * reads plugin.yml via
 * {@link com.github.martinambrus.rdforward.bridge.bukkit.BukkitPluginParser}
 * into an rd-api {@code ModDescriptor}; this class exposes the same data
 * in the shape plugins expect when they call
 * {@code JavaPlugin.getDescription()}.
 */
public class PluginDescriptionFile {

    private final String name;
    private final String version;
    private final String main;
    private final String description;
    private final List<String> authors;
    private final List<String> depend;
    private final Map<String, Map<String, Object>> commands;
    private final boolean database;

    public PluginDescriptionFile(String name, String version, String main) {
        this(name, version, main, "", Collections.emptyList(), Collections.emptyList(), Collections.emptyMap(), false);
    }

    public PluginDescriptionFile(String name, String version, String main,
                                 String description, List<String> authors, List<String> depend) {
        this(name, version, main, description, authors, depend, Collections.emptyMap(), false);
    }

    public PluginDescriptionFile(String name, String version, String main,
                                 String description, List<String> authors, List<String> depend,
                                 Map<String, Map<String, Object>> commands) {
        this(name, version, main, description, authors, depend, commands, false);
    }

    public PluginDescriptionFile(String name, String version, String main,
                                 String description, List<String> authors, List<String> depend,
                                 Map<String, Map<String, Object>> commands, boolean database) {
        this.name = name;
        this.version = version;
        this.main = main;
        this.description = description == null ? "" : description;
        this.authors = authors == null ? Collections.emptyList() : List.copyOf(authors);
        this.depend = depend == null ? Collections.emptyList() : List.copyOf(depend);
        this.commands = commands == null ? Collections.emptyMap() : Map.copyOf(commands);
        this.database = database;
    }

    public String getName() { return name; }
    public String getVersion() { return version; }
    public String getMain() { return main; }
    public String getDescription() { return description; }
    public List<String> getAuthors() { return authors; }
    public List<String> getDepend() { return depend; }

    /** Per-command attribute map keyed by command name, in real Bukkit's
     *  shape: each entry's inner map holds the {@code description} /
     *  {@code usage} / {@code aliases} / {@code permission} / etc. fields
     *  parsed from {@code plugin.yml}. EssentialsX iterates this in
     *  {@code Essentials.reload} to register fallback aliases — without it
     *  the call throws {@link NoSuchMethodError}. */
    public Map<String, Map<String, Object>> getCommands() { return commands; }

    /** Bukkit exposes {@code getFullName()} as {@code name + " v" + version}. */
    public String getFullName() { return name + " v" + version; }

    /** Plugin's declared website URL from plugin.yml. The bridge does
     *  not currently parse the {@code website:} field so this stub
     *  returns the empty string — Citizens 2.x prints it in its
     *  {@code /citizens} info banner and an empty value renders as
     *  no extra line, which is acceptable. Returning {@code null}
     *  would NPE inside Citizens' formatter. */
    public String getWebsite() { return ""; }

    /** Plugin's declared author prefix (e.g. {@code [Essentials] }).
     *  Real Bukkit pulls this from {@code prefix:} in plugin.yml.
     *  Empty string is a safe default that keeps log formatters
     *  from NPE'ing. */
    public String getPrefix() { return ""; }

    /** Plugin's soft-depend list. Bridge surfaces hard {@code depend}
     *  via {@link #getDepend()}; soft depends are honoured by the
     *  loader for ordering but are not currently re-exposed on this
     *  description. Empty list keeps callers from NPE'ing. */
    public List<String> getSoftDepend() { return Collections.emptyList(); }

    /** Plugin's {@code load-before} list — same rationale as
     *  {@link #getSoftDepend()}: bridge does not currently expose it
     *  here, so an empty list keeps consumers safe. */
    public List<String> getLoadBefore() { return Collections.emptyList(); }

    /** Whether plugin.yml declared {@code database: true}, signalling that
     *  the plugin uses Bukkit's built-in Ebean ORM support. Plugins such as
     *  HomeSpawnPlus check this flag and call {@code getDatabase()} to obtain
     *  an EbeanServer for their entity storage. */
    public boolean isDatabaseEnabled() { return database; }
}
