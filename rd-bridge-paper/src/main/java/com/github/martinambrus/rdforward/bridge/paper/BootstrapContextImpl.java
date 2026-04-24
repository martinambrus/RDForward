package com.github.martinambrus.rdforward.bridge.paper;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * Minimal {@link BootstrapContext} used when the bridge calls
 * {@code PluginBootstrap.bootstrap}. The data directory defaults to
 * {@code plugins/<pluginName>/} but the bridge never reads or writes
 * anything under it — that is left to the plugin.
 */
final class BootstrapContextImpl implements BootstrapContext {

    private final LifecycleEventManager<BootstrapContext> lifecycleManager;
    private final Path dataDirectory;
    private final Logger logger;

    BootstrapContextImpl(LifecycleEventManager<BootstrapContext> lifecycleManager, String pluginName) {
        this.lifecycleManager = lifecycleManager;
        this.dataDirectory = Paths.get("plugins", pluginName == null ? "unknown" : pluginName);
        this.logger = Logger.getLogger("RDForward/PaperBridge/" + (pluginName == null ? "unknown" : pluginName));
    }

    @Override public Path getDataDirectory() { return dataDirectory; }
    @Override public Logger getLogger() { return logger; }
    @Override public LifecycleEventManager<BootstrapContext> getLifecycleManager() { return lifecycleManager; }
}
