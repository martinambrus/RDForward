// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.plugin;

import org.bukkit.Server;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Bukkit-shaped Plugin interface. RDForward's bridge always hands plugins
 * {@link org.bukkit.plugin.java.JavaPlugin}; this interface exists so
 * plugins that type against {@code Plugin} directly compile.
 *
 * <p>The {@code default}-implemented accessors below cover the common
 * cases plugins reach for off the {@link Plugin} interface (notably
 * LoginSecurity's {@code plugin.getResource("language/en_us.yml")}).
 * Real implementations live in {@link org.bukkit.plugin.java.JavaPlugin};
 * the defaults exist so any other custom {@link Plugin} implementor
 * (test fixtures, alternate bridges) doesn't NPE / NSME plugin code.
 */
public interface Plugin {

    String getName();
    PluginDescriptionFile getDescription();
    Logger getLogger();
    Server getServer();

    boolean isEnabled();
    void onLoad();
    void onEnable();
    void onDisable();

    /** @return a stream over a classpath resource bundled in the plugin
     *  jar, or {@code null} if absent. JavaPlugin overrides; the default
     *  here keeps test stubs from breaking plugin code that asks for a
     *  bundled language file. */
    default InputStream getResource(String filename) { return null; }

    /** @return the plugin's data folder. JavaPlugin overrides. */
    default File getDataFolder() { return null; }
}
