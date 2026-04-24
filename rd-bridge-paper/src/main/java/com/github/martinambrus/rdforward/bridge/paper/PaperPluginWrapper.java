package com.github.martinambrus.rdforward.bridge.paper;

import com.github.martinambrus.rdforward.api.command.CommandRegistry;
import com.github.martinambrus.rdforward.api.mod.ServerMod;
import com.github.martinambrus.rdforward.api.server.Server;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitPluginWrapper;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * Adapts a Paper plugin (optionally with bootstrapper) to rd-api's
 * {@link ServerMod} lifecycle. The bootstrapper's
 * {@code bootstrap(BootstrapContext)} is invoked at plugin-load time
 * (inside {@link PaperPluginLoader}); this wrapper handles the
 * {@code onEnable}/{@code onDisable} side, including replay of any
 * {@code LifecycleEvents.COMMANDS} handlers into the rd-api
 * {@link CommandRegistry} via {@link BrigadierCommandBridge}.
 */
public final class PaperPluginWrapper implements ServerMod {

    private static final Logger LOG = Logger.getLogger("RDForward/PaperBridge");

    private final JavaPlugin plugin;
    private final String pluginName;
    private final PluginBootstrap bootstrapper;
    private final BootstrapLifecycleManager lifecycleManager;
    private final PaperPluginDescriptor descriptor;
    private final BukkitPluginWrapper bukkitInner;

    public PaperPluginWrapper(JavaPlugin plugin,
                              String pluginName,
                              PluginBootstrap bootstrapper,
                              BootstrapLifecycleManager lifecycleManager,
                              PaperPluginDescriptor descriptor) {
        this.plugin = plugin;
        this.pluginName = pluginName;
        this.bootstrapper = bootstrapper;
        this.lifecycleManager = lifecycleManager;
        this.descriptor = descriptor;
        this.bukkitInner = new BukkitPluginWrapper(plugin, pluginName);
    }

    public JavaPlugin plugin() { return plugin; }
    public PaperPluginDescriptor descriptor() { return descriptor; }

    @Override
    public void onEnable(Server server) {
        bukkitInner.onEnable(server);

        for (Listener listener : plugin.getRegisteredListeners()) {
            PaperEventAdapter.registerPaperOnly(listener, pluginName);
        }

        if (lifecycleManager != null && server != null) {
            CollectingCommandsRegistrar registrar = new CollectingCommandsRegistrar();
            lifecycleManager.fireCommands(registrar);
            if (!registrar.entries().isEmpty()) {
                BrigadierCommandBridge.registerWithRdApi(registrar, server.getCommandRegistry(), pluginName);
            } else if (bootstrapper != null && descriptor != null
                    && (descriptor.commands() == null || descriptor.commands().isEmpty())) {
                LOG.warning("[PaperBridge] Plugin '" + pluginName
                        + "' declared a bootstrapper but registered no COMMANDS and no legacy commands: block — "
                        + "no commands will be exposed.");
            }
        }
    }

    @Override
    public void onDisable() {
        bukkitInner.onDisable();
    }
}
