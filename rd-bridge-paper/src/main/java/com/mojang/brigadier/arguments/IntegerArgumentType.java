package com.mojang.brigadier.arguments;

import com.mojang.brigadier.context.CommandContext;

/**
 * Brigadier integer argument. The stub ignores the min/max bounds — the
 * bridge dispatcher takes the raw argument string and parses only when the
 * plugin calls {@link #getInteger}.
 */
public final class IntegerArgumentType implements ArgumentType<Integer> {

    private static final IntegerArgumentType INSTANCE = new IntegerArgumentType();

    private IntegerArgumentType() {}

    public static IntegerArgumentType integer() { return INSTANCE; }
    public static IntegerArgumentType integer(int min) { return INSTANCE; }
    public static IntegerArgumentType integer(int min, int max) { return INSTANCE; }

    public static int getInteger(CommandContext<?> ctx, String name) {
        Object v = ctx.getArgument(name, Object.class);
        if (v instanceof Integer i) return i;
        if (v == null) return 0;
        return Integer.parseInt(v.toString());
    }
}
