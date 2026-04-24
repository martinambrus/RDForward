package org.bukkit.plugin;

import org.bukkit.Server;

import java.util.logging.Logger;

/**
 * Bukkit-shaped Plugin interface. RDForward's bridge always hands plugins
 * {@link org.bukkit.plugin.java.JavaPlugin}; this interface exists so
 * plugins that type against {@code Plugin} directly compile.
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
}
