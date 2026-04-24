package com.mojang.brigadier.builder;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.CommandNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for Brigadier argument/literal builders. Stores child nodes
 * and the executor; concrete subclasses provide a {@link #build()} that
 * produces a {@link CommandNode} wrapping the collected data.
 */
public abstract class ArgumentBuilder<S, T extends ArgumentBuilder<S, T>> {

    private final List<CommandNode<S>> arguments = new ArrayList<>();
    private Command<S> command;

    @SuppressWarnings("unchecked")
    protected T getThis() { return (T) this; }

    public T then(ArgumentBuilder<S, ?> builder) {
        arguments.add(builder.build());
        return getThis();
    }

    public T then(CommandNode<S> node) {
        arguments.add(node);
        return getThis();
    }

    public T executes(Command<S> command) {
        this.command = command;
        return getThis();
    }

    public Command<S> getCommand() { return command; }

    public List<CommandNode<S>> getArguments() { return arguments; }

    public abstract CommandNode<S> build();
}
