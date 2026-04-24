package io.papermc.paper.plugin.bootstrap;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Plugin bootstrapper entry point. Paper instantiates the class named by
 * {@code bootstrapper:} in {@code paper-plugin.yml}, calls
 * {@link #bootstrap(BootstrapContext)} before the world loads, then
 * {@link #createPlugin(PluginProviderContext)} to produce the
 * {@link JavaPlugin} instance that goes through normal enable/disable.
 */
public interface PluginBootstrap {

    void bootstrap(BootstrapContext context);

    default JavaPlugin createPlugin(PluginProviderContext context) {
        return null;
    }
}
