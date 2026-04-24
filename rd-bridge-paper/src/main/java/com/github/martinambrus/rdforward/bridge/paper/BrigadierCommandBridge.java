package com.github.martinambrus.rdforward.bridge.paper;

import com.github.martinambrus.rdforward.api.command.CommandContext;
import com.github.martinambrus.rdforward.api.command.CommandRegistry;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Flushes {@link CollectingCommandsRegistrar} entries to the rd-api
 * {@link CommandRegistry}. Each top-level literal becomes a single rd-api
 * command that reconstructs a Brigadier {@link com.mojang.brigadier.context.CommandContext}
 * from the rd-api args, walks the literal/argument tree to find the
 * matching executor, and invokes it.
 */
public final class BrigadierCommandBridge {

    private static final Logger LOG = Logger.getLogger("RDForward/PaperBridge");

    private BrigadierCommandBridge() {}

    public static void registerWithRdApi(CollectingCommandsRegistrar collected,
                                         CommandRegistry registry,
                                         String pluginName) {
        if (registry == null) return;
        for (CollectingCommandsRegistrar.Entry e : collected.entries()) {
            registerNode(registry, pluginName, e.node(), e.description());
            for (String alias : e.aliases()) {
                LiteralCommandNode<CommandSourceStack> aliasNode = aliasOf(e.node(), alias);
                registerNode(registry, pluginName, aliasNode, e.description());
            }
        }
    }

    private static LiteralCommandNode<CommandSourceStack> aliasOf(LiteralCommandNode<CommandSourceStack> node, String alias) {
        return new LiteralCommandNode<>(alias, node.getCommand(), node.getChildren());
    }

    private static void registerNode(CommandRegistry registry,
                                     String pluginName,
                                     LiteralCommandNode<CommandSourceStack> node,
                                     String description) {
        registry.register(pluginName, node.getLiteral(), description, ctx -> {
            CommandSender sender = resolveSender(ctx.getSenderName(), ctx.isConsole());
            CommandSourceStack source = new BridgeCommandSourceStack(sender);
            int result = dispatch(node, source, ctx.getArgs());
            if (result <= 0) {
                LOG.fine("[PaperBridge] Command '" + node.getLiteral() + "' returned " + result);
            }
        });
    }

    private static int dispatch(LiteralCommandNode<CommandSourceStack> root, CommandSourceStack source, String[] args) {
        com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx = new com.mojang.brigadier.context.CommandContext<>(source);
        CommandNode<CommandSourceStack> node = root;
        int i = 0;
        while (true) {
            if (i == args.length || node.getChildren().isEmpty()) {
                Command<CommandSourceStack> exec = node.getCommand();
                if (exec == null) return 0;
                try {
                    return exec.run(ctx);
                } catch (CommandSyntaxException cse) {
                    source.getSender().sendMessage(cse.getMessage());
                    return 0;
                } catch (RuntimeException re) {
                    LOG.warning("[PaperBridge] Brigadier command threw: " + re);
                    return 0;
                }
            }
            CommandNode<CommandSourceStack> next = null;
            for (CommandNode<CommandSourceStack> child : node.getChildren()) {
                if (child instanceof LiteralCommandNode<CommandSourceStack> lit && lit.getLiteral().equals(args[i])) {
                    next = child;
                    break;
                }
            }
            if (next == null) {
                for (CommandNode<CommandSourceStack> child : node.getChildren()) {
                    if (child instanceof ArgumentCommandNode<CommandSourceStack, ?> arg) {
                        ctx.putArgument(arg.getName(), args[i]);
                        next = child;
                        break;
                    }
                }
            }
            if (next == null) return 0;
            node = next;
            i++;
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

    private record BridgeCommandSourceStack(CommandSender sender) implements CommandSourceStack {
        @Override public CommandSender getSender() { return sender; }
        @Override public Entity getExecutor() { return null; }
    }

    private record FallbackSender(String name) implements CommandSender {
        @Override public String getName() { return name; }
        @Override public void sendMessage(String message) { LOG.info("[" + name + "] " + message); }
        @Override public boolean isOp() { return false; }
    }
}
