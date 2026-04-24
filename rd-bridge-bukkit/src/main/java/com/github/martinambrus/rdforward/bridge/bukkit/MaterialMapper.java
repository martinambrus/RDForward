package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.api.world.BlockType;
import com.github.martinambrus.rdforward.api.world.BlockTypes;
import org.bukkit.Material;

/**
 * Bridge between Bukkit's {@link Material} enum and rd-api
 * {@link BlockType}. RDForward exposes a narrow block registry; Material
 * entries beyond that registry fall back to {@link Material#AIR} /
 * {@link BlockTypes#AIR}, matching the stub contract.
 */
public final class MaterialMapper {

    private MaterialMapper() {}

    /** @return rd-api block type for {@code material}, or {@link BlockTypes#AIR} when unmapped. */
    public static BlockType toApi(Material material) {
        if (material == null) return BlockTypes.AIR;
        return switch (material) {
            case AIR          -> BlockTypes.AIR;
            case STONE        -> BlockTypes.STONE;
            case GRASS_BLOCK  -> BlockTypes.GRASS;
            case DIRT         -> BlockTypes.DIRT;
            case COBBLESTONE  -> BlockTypes.COBBLE;
            case OAK_PLANKS   -> BlockTypes.PLANKS;
            case OAK_SAPLING  -> BlockTypes.SAPLING;
            case BEDROCK      -> BlockTypes.BEDROCK;
            case WATER        -> BlockTypes.WATER;
            case LAVA         -> BlockTypes.LAVA;
            case SAND         -> BlockTypes.SAND;
            case GRAVEL       -> BlockTypes.GRAVEL;
            case GOLD_ORE     -> BlockTypes.GOLD_ORE;
            case IRON_ORE     -> BlockTypes.IRON_ORE;
            case COAL_ORE     -> BlockTypes.COAL_ORE;
            case OAK_LOG      -> BlockTypes.WOOD;
            case OAK_LEAVES   -> BlockTypes.LEAVES;
            case GLASS        -> BlockTypes.GLASS;
            case TNT          -> BlockTypes.TNT;
        };
    }

    /** @return Bukkit material for {@code type}, or {@link Material#AIR} when unmapped. */
    public static Material fromApi(BlockType type) {
        if (type == null) return Material.AIR;
        return switch (type.getId()) {
            case 0  -> Material.AIR;
            case 1  -> Material.STONE;
            case 2  -> Material.GRASS_BLOCK;
            case 3  -> Material.DIRT;
            case 4  -> Material.COBBLESTONE;
            case 5  -> Material.OAK_PLANKS;
            case 6  -> Material.OAK_SAPLING;
            case 7  -> Material.BEDROCK;
            case 8  -> Material.WATER;
            case 10 -> Material.LAVA;
            case 12 -> Material.SAND;
            case 13 -> Material.GRAVEL;
            case 14 -> Material.GOLD_ORE;
            case 15 -> Material.IRON_ORE;
            case 16 -> Material.COAL_ORE;
            case 17 -> Material.OAK_LOG;
            case 18 -> Material.OAK_LEAVES;
            case 20 -> Material.GLASS;
            case 46 -> Material.TNT;
            default -> Material.AIR;
        };
    }
}
