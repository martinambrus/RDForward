package io.papermc.paper.command.brigadier;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import java.util.Collection;
import java.util.List;

/**
 * Brigadier command registrar handed to a bootstrap {@code
 * LifecycleEvents.COMMANDS} handler. Plugins build a
 * {@link LiteralCommandNode} via the static factories and hand it to
 * {@link #register}.
 */
public interface Commands {

    void register(LiteralCommandNode<CommandSourceStack> node);

    void register(LiteralCommandNode<CommandSourceStack> node, String description);

    void register(LiteralCommandNode<CommandSourceStack> node, String description, List<String> aliases);

    default void register(LiteralCommandNode<CommandSourceStack> node, String description, Collection<String> aliases) {
        register(node, description, aliases == null ? List.of() : List.copyOf(aliases));
    }

    static LiteralArgumentBuilder<CommandSourceStack> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    static <T> RequiredArgumentBuilder<CommandSourceStack, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }
}
