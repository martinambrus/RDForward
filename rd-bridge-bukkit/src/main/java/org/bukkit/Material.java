// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit;

/**
 * Subset of Bukkit's {@code Material} enum covering the block types
 * RDForward surfaces. Entries beyond RDForward's block set map to
 * {@link #AIR} via {@link com.github.martinambrus.rdforward.bridge.bukkit.MaterialMapper}
 * at runtime. Plugins that switch on Material for unsupported blocks
 * receive AIR and silently noop on any related checks.
 */
public enum Material {
    AIR,
    STONE,
    GRASS_BLOCK,
    DIRT,
    COBBLESTONE,
    OAK_PLANKS,
    OAK_SAPLING,
    BEDROCK,
    WATER,
    LAVA,
    SAND,
    GRAVEL,
    GOLD_ORE,
    IRON_ORE,
    COAL_ORE,
    OAK_LOG,
    OAK_LEAVES,
    GLASS,
    TNT;

    /** True if this material represents empty space. Mirrors upstream helper. */
    public boolean isAir() { return this == AIR; }

    /** True if this material is a solid block. Stub treats every non-AIR/WATER/LAVA as solid. */
    public boolean isSolid() { return this != AIR && this != WATER && this != LAVA; }
}
