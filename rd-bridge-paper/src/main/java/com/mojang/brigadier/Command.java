package com.mojang.brigadier;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

/**
 * Brigadier command executor. Implementations return an integer result —
 * {@link #SINGLE_SUCCESS} is the conventional success value; Brigadier
 * itself lets commands aggregate success counts, but the bridge treats any
 * non-zero as success.
 */
@FunctionalInterface
public interface Command<S> {
    int SINGLE_SUCCESS = 1;

    int run(CommandContext<S> context) throws CommandSyntaxException;
}
