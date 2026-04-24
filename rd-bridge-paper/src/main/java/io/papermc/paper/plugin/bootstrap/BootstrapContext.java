package io.papermc.paper.plugin.bootstrap;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;

import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Runtime context supplied to {@link PluginBootstrap#bootstrap}. Exposes the
 * {@link LifecycleEventManager} used to register bootstrap-phase handlers
 * (notably {@code LifecycleEvents.COMMANDS}).
 */
public interface BootstrapContext extends LifecycleEventOwner {

    Path getDataDirectory();

    Logger getLogger();

    LifecycleEventManager<BootstrapContext> getLifecycleManager();
}
