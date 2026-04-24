// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.plugin;

import java.util.Collections;
import java.util.List;

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

    public PluginDescriptionFile(String name, String version, String main) {
        this(name, version, main, "", Collections.emptyList(), Collections.emptyList());
    }

    public PluginDescriptionFile(String name, String version, String main,
                                 String description, List<String> authors, List<String> depend) {
        this.name = name;
        this.version = version;
        this.main = main;
        this.description = description == null ? "" : description;
        this.authors = authors == null ? Collections.emptyList() : List.copyOf(authors);
        this.depend = depend == null ? Collections.emptyList() : List.copyOf(depend);
    }

    public String getName() { return name; }
    public String getVersion() { return version; }
    public String getMain() { return main; }
    public String getDescription() { return description; }
    public List<String> getAuthors() { return authors; }
    public List<String> getDepend() { return depend; }

    /** Bukkit exposes {@code getFullName()} as {@code name + " v" + version}. */
    public String getFullName() { return name + " v" + version; }
}
