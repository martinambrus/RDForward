package org.bukkit.plugin;

import org.bukkit.event.Listener;

/**
 * Bukkit-shaped plugin manager. Only {@link #registerEvents(Listener, Plugin)}
 * is wired through to RDForward — events registered here flow through
 * {@link com.github.martinambrus.rdforward.bridge.bukkit.BukkitEventAdapter}
 * and land on rd-api {@code ServerEvents}. Other methods (plugin enable /
 * disable, plugin lookup) return sensible defaults because the RDForward
 * mod loader owns lifecycle, not the plugin manager.
 */
public interface PluginManager {

    /** Register every {@code @EventHandler} method on {@code listener}. */
    void registerEvents(Listener listener, Plugin plugin);

    /** Noop — RDForward does not support plugin disable from the plugin manager. */
    void disablePlugin(Plugin plugin);

    /** @return the plugin with the given name if loaded, or null. */
    Plugin getPlugin(String name);

    /** @return an array of currently-loaded plugins (empty by default). */
    Plugin[] getPlugins();
}
