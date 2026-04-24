package com.github.martinambrus.rdforward.bridge.paper;

import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Minimal {@link PluginProviderContext} passed to
 * {@code PluginBootstrap.createPlugin}. Only carries the data directory
 * path; logger / configuration / source are stubbed.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
final class PluginProviderContextImpl implements PluginProviderContext {

    private final Path dataDirectory;
    private final Path pluginSource;

    PluginProviderContextImpl(String pluginName) {
        String n = pluginName == null ? "unknown" : pluginName;
        this.dataDirectory = Paths.get("plugins", n);
        this.pluginSource = Paths.get("plugins", n + ".jar");
    }

    @Override public Path getDataDirectory() { return dataDirectory; }
    @Override public Path getPluginSource() { return pluginSource; }
    @Override public ComponentLogger getLogger() { return null; }
    @Override public PluginMeta getConfiguration() { return null; }
}
