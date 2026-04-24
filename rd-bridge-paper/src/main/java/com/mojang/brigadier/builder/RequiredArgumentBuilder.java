package com.mojang.brigadier.builder;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;

/**
 * Brigadier builder for a named argument node. {@link #argument} is the
 * conventional factory (also exposed via Paper's {@code Commands.argument}).
 */
public final class RequiredArgumentBuilder<S, T> extends ArgumentBuilder<S, RequiredArgumentBuilder<S, T>> {

    private final String name;
    private final ArgumentType<T> type;

    private RequiredArgumentBuilder(String name, ArgumentType<T> type) {
        this.name = name;
        this.type = type;
    }

    public static <S, T> RequiredArgumentBuilder<S, T> argument(String name, ArgumentType<T> type) {
        return new RequiredArgumentBuilder<>(name, type);
    }

    public String getName() { return name; }

    public ArgumentType<T> getType() { return type; }

    @Override
    public ArgumentCommandNode<S, T> build() {
        return new ArgumentCommandNode<>(name, type, getCommand(), getArguments());
    }
}
