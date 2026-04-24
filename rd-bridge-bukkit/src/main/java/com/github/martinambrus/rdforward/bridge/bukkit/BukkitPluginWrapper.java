package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.api.command.CommandRegistry;
import com.github.martinambrus.rdforward.api.mod.ServerMod;
import com.github.martinambrus.rdforward.api.server.Server;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Adapts a Bukkit {@link JavaPlugin} to RDForward's {@link ServerMod}
 * lifecycle. The plugin's {@code onLoad()} + {@code onEnable()} run during
 * the RDForward mod's {@code onEnable(Server)}, and any listeners the plugin
 * registered are wired into the API events via {@link BukkitEventAdapter}.
 * {@code onDisable()} mirrors back to the plugin.
 *
 * <p>Commands declared in {@code plugin.yml} with a {@link CommandExecutor}
 * attached via {@code plugin.getCommand(name).setExecutor(...)} are
 * registered with the rd-api {@link CommandRegistry} under the plugin's mod
 * id, so they dispatch through the real server command pipeline.
 *
 * <p>The mod loader already sweeps event/command/scheduler ownership on
 * disable, so the wrapper does not need to unregister listeners manually.
 */
public final class BukkitPluginWrapper implements ServerMod {

    private static final Logger LOG = Logger.getLogger("RDForward/BukkitBridge");

    private final JavaPlugin plugin;
    private final String pluginName;

    public BukkitPluginWrapper(JavaPlugin plugin) {
        this(plugin, null);
    }

    public BukkitPluginWrapper(JavaPlugin plugin, String pluginName) {
        this.plugin = plugin;
        this.pluginName = pluginName;
    }

    public JavaPlugin plugin() { return plugin; }

    @Override
    public void onEnable(Server server) {
        plugin.onLoad();
        plugin.onEnable();
        for (Listener listener : plugin.getRegisteredListeners()) {
            BukkitEventAdapter.register(listener, pluginName);
        }
        if (server != null && pluginName != null) {
            registerCommands(server.getCommandRegistry());
        }
    }

    @Override
    public void onDisable() {
        plugin.onDisable();
    }

    /** Forward every {@link PluginCommand} with an executor into the rd-api registry. */
    private void registerCommands(CommandRegistry registry) {
        if (registry == null) return;
        for (Map.Entry<String, PluginCommand> entry : plugin.getCommandMap().entrySet()) {
            PluginCommand cmd = entry.getValue();
            CommandExecutor exec = cmd.getExecutor();
            if (exec == null) continue;
            String name = cmd.getName();
            String description = cmd.getDescription() == null ? "" : cmd.getDescription();
            registry.register(pluginName, name, description, ctx -> {
                CommandSender sender = resolveSender(ctx.getSenderName(), ctx.isConsole());
                try {
                    boolean ok = exec.onCommand(sender, cmd, name, ctx.getArgs());
                    if (!ok && !cmd.getUsage().isBlank()) {
                        ctx.reply(cmd.getUsage());
                    }
                } catch (RuntimeException e) {
                    LOG.warning("[BukkitBridge] Command '" + name + "' threw: " + e);
                    ctx.reply("An internal error occurred while executing this command.");
                }
            });
        }
    }

    private static CommandSender resolveSender(String name, boolean console) {
        if (console || name == null) {
            return Bukkit.getServer() == null ? null : Bukkit.getServer().getConsoleSender();
        }
        if (Bukkit.getServer() != null) {
            CommandSender player = Bukkit.getServer().getPlayer(name);
            if (player != null) return player;
        }
        return new FallbackSender(name);
    }

    /** Plain sender used when the plugin receives a command before Bukkit is installed. */
    private record FallbackSender(String name) implements CommandSender {
        @Override public String getName() { return name; }
        @Override public void sendMessage(String message) { LOG.info("[" + name + "] " + message); }
        @Override public boolean isOp() { return false; }
    }
}
