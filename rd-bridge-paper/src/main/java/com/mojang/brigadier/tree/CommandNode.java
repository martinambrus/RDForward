package com.mojang.brigadier.tree;

import com.mojang.brigadier.Command;

import java.util.List;

/**
 * Base class for Brigadier command nodes (literal / argument). Holds the
 * executor and child nodes; subclasses carry the node-specific data
 * ({@code literal} for {@link LiteralCommandNode}, {@code name + type} for
 * {@link ArgumentCommandNode}).
 */
public abstract class CommandNode<S> {

    private final Command<S> command;
    private final List<CommandNode<S>> children;

    protected CommandNode(Command<S> command, List<CommandNode<S>> children) {
        this.command = command;
        this.children = children == null ? List.of() : List.copyOf(children);
    }

    public Command<S> getCommand() { return command; }

    public List<CommandNode<S>> getChildren() { return children; }
}
