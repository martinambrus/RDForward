package com.mojang.brigadier.tree;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;

import java.util.List;

/**
 * Brigadier argument node — captures a positional arg named {@code name} of
 * type {@code type}. At dispatch time the bridge stores the raw argument
 * string under {@code name} on the {@code CommandContext}.
 */
public final class ArgumentCommandNode<S, T> extends CommandNode<S> {

    private final String name;
    private final ArgumentType<T> type;

    public ArgumentCommandNode(String name, ArgumentType<T> type,
                               Command<S> command, List<CommandNode<S>> children) {
        super(command, children);
        this.name = name;
        this.type = type;
    }

    public String getName() { return name; }

    public ArgumentType<T> getType() { return type; }
}
