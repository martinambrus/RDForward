package com.github.martinambrus.rdforward.api.world;

/**
 * Well-known block type constants mods can use without resolving from a registry.
 *
 * <p>Ids match the server's byte block-id convention (pre-1.13 numeric ids).
 * Modern protocol versions are translated by the server's block state mapper.
 */
public final class BlockTypes {

    private BlockTypes() {}

    public static final BlockType AIR        = of(0,  "minecraft:air");
    public static final BlockType STONE      = of(1,  "minecraft:stone");
    public static final BlockType GRASS      = of(2,  "minecraft:grass_block");
    public static final BlockType DIRT       = of(3,  "minecraft:dirt");
    public static final BlockType COBBLE     = of(4,  "minecraft:cobblestone");
    public static final BlockType PLANKS     = of(5,  "minecraft:oak_planks");
    public static final BlockType SAPLING    = of(6,  "minecraft:oak_sapling");
    public static final BlockType BEDROCK    = of(7,  "minecraft:bedrock");
    public static final BlockType WATER      = of(8,  "minecraft:water");
    public static final BlockType LAVA       = of(10, "minecraft:lava");
    public static final BlockType SAND       = of(12, "minecraft:sand");
    public static final BlockType GRAVEL     = of(13, "minecraft:gravel");
    public static final BlockType GOLD_ORE   = of(14, "minecraft:gold_ore");
    public static final BlockType IRON_ORE   = of(15, "minecraft:iron_ore");
    public static final BlockType COAL_ORE   = of(16, "minecraft:coal_ore");
    public static final BlockType WOOD       = of(17, "minecraft:oak_log");
    public static final BlockType LEAVES     = of(18, "minecraft:oak_leaves");
    public static final BlockType GLASS      = of(20, "minecraft:glass");
    public static final BlockType TNT        = of(46, "minecraft:tnt");

    /**
     * Resolve a block by its numeric id. Unknown ids become anonymous
     * types named {@code "minecraft:unknown_<id>"} so callers never get a
     * null back — simplifies downstream handling.
     */
    public static BlockType byId(int id) {
        return switch (id) {
            case 0 -> AIR;
            case 1 -> STONE;
            case 2 -> GRASS;
            case 3 -> DIRT;
            case 4 -> COBBLE;
            case 5 -> PLANKS;
            case 6 -> SAPLING;
            case 7 -> BEDROCK;
            case 8 -> WATER;
            case 10 -> LAVA;
            case 12 -> SAND;
            case 13 -> GRAVEL;
            case 14 -> GOLD_ORE;
            case 15 -> IRON_ORE;
            case 16 -> COAL_ORE;
            case 17 -> WOOD;
            case 18 -> LEAVES;
            case 20 -> GLASS;
            case 46 -> TNT;
            default -> new Simple(id, "minecraft:unknown_" + id);
        };
    }

    private static BlockType of(int id, String name) {
        return new Simple(id, name);
    }

    private record Simple(int id, String name) implements BlockType {
        @Override public int getId() { return id; }
        @Override public String getName() { return name; }
    }
}
