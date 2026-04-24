package com.mojang.brigadier.builder;

import com.mojang.brigadier.tree.LiteralCommandNode;

/**
 * Brigadier builder for a literal node. {@link #literal(String)} is the
 * conventional factory (also exposed via Paper's {@code Commands.literal}).
 */
public final class LiteralArgumentBuilder<S> extends ArgumentBuilder<S, LiteralArgumentBuilder<S>> {

    private final String literal;

    private LiteralArgumentBuilder(String literal) {
        this.literal = literal;
    }

    public static <S> LiteralArgumentBuilder<S> literal(String name) {
        return new LiteralArgumentBuilder<>(name);
    }

    public String getLiteral() { return literal; }

    @Override
    public LiteralCommandNode<S> build() {
        return new LiteralCommandNode<>(literal, getCommand(), getArguments());
    }
}
