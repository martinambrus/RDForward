package com.mojang.brigadier.tree;

import com.mojang.brigadier.Command;

import java.util.List;

/**
 * Brigadier literal node — matches an exact keyword in a command path. The
 * root literal is what Paper plugins name in {@code Commands.register}; the
 * bridge uses {@link #getLiteral()} as the rd-api {@code CommandRegistry}
 * command name.
 */
public final class LiteralCommandNode<S> extends CommandNode<S> {

    private final String literal;

    public LiteralCommandNode(String literal, Command<S> command, List<CommandNode<S>> children) {
        super(command, children);
        this.literal = literal;
    }

    public String getLiteral() { return literal; }
}
