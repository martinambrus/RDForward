package com.github.martinambrus.rdforward.bridge.pocketmine;

import com.github.martinambrus.rdforward.api.command.CommandRegistry;
import com.github.martinambrus.rdforward.api.mod.ServerMod;
import com.github.martinambrus.rdforward.api.server.Server;
import pocketmine.command.Command;
import pocketmine.command.CommandSender;
import pocketmine.plugin.PluginBase;
import pocketmine.plugin.PluginDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Adapts a {@link PluginBase} to rd-api's {@link ServerMod} lifecycle.
 * {@code onEnable(Server)} triggers {@code onLoad()} + {@code onEnable()}
 * in PocketMine order and forwards every {@code commands:} entry into
 * rd-api's {@link CommandRegistry}. Event registration lives in the
 * {@code PluginManager} / {@code PocketMineEventAdapter} path and is not
 * driven from here.
 */
public final class PocketMinePluginWrapper implements ServerMod {

    private static final Logger LOG = Logger.getLogger("RDForward/PocketMineBridge");

    private final PluginBase plugin;
    private final PluginDescription descriptor;

    public PocketMinePluginWrapper(PluginBase plugin, PluginDescription descriptor) {
        this.plugin = plugin;
        this.descriptor = descriptor;
    }

    public PluginBase plugin() { return plugin; }

    @Override
    public void onEnable(Server server) {
        plugin.onLoad();
        plugin.setEnabled(true);
        plugin.onEnable();
        if (server != null && !descriptor.commands().isEmpty()) {
            registerCommands(server.getCommandRegistry());
        }
    }

    @Override
    public void onDisable() {
        plugin.onDisable();
        plugin.setEnabled(false);
        plugin.getScheduler().cancelAllTasks();
    }

    private void registerCommands(CommandRegistry registry) {
        if (registry == null) return;
        for (Map.Entry<String, Map<String, Object>> entry : descriptor.commands().entrySet()) {
            String name = entry.getKey();
            Map<String, Object> body = entry.getValue();
            String description = stringOr(body.get("description"), "");
            String usage = stringOr(body.get("usage"), "");
            String permission = body.get("permission") == null ? null : String.valueOf(body.get("permission"));
            List<String> aliases = parseAliases(body.get("aliases"));

            Command cmd = new Command(name);
            cmd.setDescription(description);
            cmd.setUsage(usage);
            cmd.setAliases(aliases);
            cmd.setPermission(permission);

            registry.register(descriptor.name(), name, description, ctx -> {
                CommandSender sender = resolveSender(ctx.getSenderName(), ctx.isConsole(), ctx::reply);
                try {
                    boolean ok = plugin.onCommand(sender, cmd, name, ctx.getArgs());
                    if (!ok && !usage.isBlank()) ctx.reply(usage);
                } catch (RuntimeException e) {
                    LOG.warning("[PocketMineBridge] Command '" + name + "' threw: " + e);
                    ctx.reply("An internal error occurred while executing this command.");
                }
            });
        }
    }

    private static List<String> parseAliases(Object raw) {
        if (raw instanceof List<?> list) {
            List<String> out = new ArrayList<>(list.size());
            for (Object a : list) out.add(String.valueOf(a));
            return List.copyOf(out);
        }
        if (raw instanceof String s && !s.isBlank()) return List.of(s);
        return List.of();
    }

    private static String stringOr(Object v, String fallback) {
        return v == null ? fallback : String.valueOf(v);
    }

    private static CommandSender resolveSender(String name, boolean console,
                                               java.util.function.Consumer<String> replier) {
        String effective = console ? "CONSOLE" : (name == null ? "anonymous" : name);
        return new BridgeSender(effective, console, replier);
    }

    private record BridgeSender(String name, boolean op, java.util.function.Consumer<String> replier)
            implements CommandSender {
        @Override public String getName() { return name; }
        @Override public boolean isOp() { return op; }
        @Override public void sendMessage(String message) { replier.accept(message); }
    }
}
