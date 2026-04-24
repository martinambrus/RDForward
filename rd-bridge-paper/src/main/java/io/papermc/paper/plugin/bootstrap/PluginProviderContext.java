package io.papermc.paper.plugin.bootstrap;

import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Runtime context supplied to {@link PluginBootstrap#createPlugin}. The bridge
 * hands in a minimal implementation — data directory, logger, optional plugin
 * source path — enough for plugins that inspect the context before creating
 * their {@code JavaPlugin} subclass.
 */
public interface PluginProviderContext {

    Path getDataDirectory();

    Logger getLogger();
}
