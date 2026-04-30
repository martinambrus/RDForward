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

    public PluginDescriptionFile(String name, String version, String main) {
        this(name, version, main, "", Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());
    }

    public PluginDescriptionFile(String name, String version, String main,
                                 String description, List<String> authors, List<String> depend) {
        this(name, version, main, description, authors, depend, Collections.emptyMap());
    }

    public PluginDescriptionFile(String name, String version, String main,
                                 String description, List<String> authors, List<String> depend,
                                 Map<String, Map<String, Object>> commands) {
        this.name = name;
        this.version = version;
        this.main = main;
        this.description = description == null ? "" : description;
        this.authors = authors == null ? Collections.emptyList() : List.copyOf(authors);
        this.depend = depend == null ? Collections.emptyList() : List.copyOf(depend);
        this.commands = commands == null ? Collections.emptyMap() : Map.copyOf(commands);
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
}
