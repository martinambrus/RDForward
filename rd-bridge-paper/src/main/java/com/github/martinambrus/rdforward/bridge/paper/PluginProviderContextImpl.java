package com.github.martinambrus.rdforward.bridge.paper;

import io.papermc.paper.plugin.bootstrap.PluginProviderContext;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * Minimal {@link PluginProviderContext} passed to
 * {@code PluginBootstrap.createPlugin}. Only carries the data directory
 * path and a logger — nothing else is consumed by the bridge.
 */
final class PluginProviderContextImpl implements PluginProviderContext {

    private final Path dataDirectory;
    private final Logger logger;

    PluginProviderContextImpl(String pluginName) {
        this.dataDirectory = Paths.get("plugins", pluginName == null ? "unknown" : pluginName);
        this.logger = Logger.getLogger("RDForward/PaperBridge/" + (pluginName == null ? "unknown" : pluginName));
    }

    @Override public Path getDataDirectory() { return dataDirectory; }
    @Override public Logger getLogger() { return logger; }
}
