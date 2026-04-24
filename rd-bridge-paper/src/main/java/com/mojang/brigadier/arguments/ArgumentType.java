package com.mojang.brigadier.arguments;

/**
 * Marker for Brigadier argument types. The stub does not implement parsing;
 * the bridge extracts argument strings from rd-api's command args array and
 * stores them raw in {@code CommandContext}. {@code StringArgumentType} /
 * {@code IntegerArgumentType} helpers read the raw value back, parsing on
 * demand.
 */
public interface ArgumentType<T> {
}
