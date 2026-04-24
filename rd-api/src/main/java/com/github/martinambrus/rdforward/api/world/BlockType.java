package com.github.martinambrus.rdforward.api.world;

/**
 * Abstract block type identifier. Implementations are provided by the mod
 * loader and wrap the server's internal block registry.
 *
 * <p>For common types, use {@link BlockTypes}.
 */
public interface BlockType {

    /** Numeric id used by the server's block registry. */
    int getId();

    /** Namespaced identifier, e.g. "minecraft:stone" or "modid:my_block". */
    String getName();

    /** True if this type represents empty space (air). */
    default boolean isAir() {
        return getId() == 0;
    }
}
