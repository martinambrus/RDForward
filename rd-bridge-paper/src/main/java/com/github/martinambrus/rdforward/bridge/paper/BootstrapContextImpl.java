package com.github.martinambrus.rdforward.bridge.paper;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Minimal {@link BootstrapContext} used when the bridge calls
 * {@code PluginBootstrap.bootstrap}. The data directory defaults to
 * {@code plugins/<pluginName>/} but the bridge never reads or writes
 * anything under it — that is left to the plugin.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
final class BootstrapContextImpl implements BootstrapContext {

    private final LifecycleEventManager lifecycleManager;
    private final Path dataDirectory;
    private final Path pluginSource;
    private final String pluginName;

    BootstrapContextImpl(LifecycleEventManager lifecycleManager, String pluginName) {
        this.lifecycleManager = lifecycleManager;
        this.pluginName = pluginName == null ? "unknown" : pluginName;
        this.dataDirectory = Paths.get("plugins", this.pluginName);
        this.pluginSource = Paths.get("plugins", this.pluginName + ".jar");
    }

    @Override public Path getDataDirectory() { return dataDirectory; }
    @Override public Path getPluginSource() { return pluginSource; }
    @Override public ComponentLogger getLogger() { return null; }
    @Override public PluginMeta getConfiguration() { return null; }
    @Override public PluginMeta getPluginMeta() { return null; }
    @Override public LifecycleEventManager getLifecycleManager() { return lifecycleManager; }
}
