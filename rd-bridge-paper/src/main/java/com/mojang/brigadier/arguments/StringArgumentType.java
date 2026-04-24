package com.mojang.brigadier.arguments;

import com.mojang.brigadier.context.CommandContext;

/**
 * Brigadier string argument. The three factory styles ({@code string},
 * {@code word}, {@code greedyString}) all share the same stub instance —
 * the bridge doesn't distinguish parsing modes.
 */
public final class StringArgumentType implements ArgumentType<String> {

    private static final StringArgumentType INSTANCE = new StringArgumentType();

    private StringArgumentType() {}

    public static StringArgumentType string() { return INSTANCE; }
    public static StringArgumentType word() { return INSTANCE; }
    public static StringArgumentType greedyString() { return INSTANCE; }

    public static String getString(CommandContext<?> ctx, String name) {
        Object v = ctx.getArgument(name, Object.class);
        return v == null ? null : v.toString();
    }
}
