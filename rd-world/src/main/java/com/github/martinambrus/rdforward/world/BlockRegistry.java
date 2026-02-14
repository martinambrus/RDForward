package com.github.martinambrus.rdforward.world;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry of all block types across all supported protocol versions.
 *
 * Block IDs are the legacy numeric IDs from pre-flattening Minecraft.
 * These IDs are stable — block 1 is always Stone, block 2 is always
 * Grass, etc. — and are used in the Alpha world format's Blocks array.
 */
public final class BlockRegistry {

    // RubyDung blocks (only 3 types existed)
    public static final int AIR = 0;
    public static final int GRASS = 2;
    public static final int COBBLESTONE = 4;

    // Alpha blocks (complete set)
    public static final int STONE = 1;
    public static final int DIRT = 3;
    public static final int PLANKS = 5;
    public static final int SAPLING = 6;
    public static final int BEDROCK = 7;
    public static final int FLOWING_WATER = 8;
    public static final int STILL_WATER = 9;
    public static final int FLOWING_LAVA = 10;
    public static final int STILL_LAVA = 11;
    public static final int SAND = 12;
    public static final int GRAVEL = 13;
    public static final int GOLD_ORE = 14;
    public static final int IRON_ORE = 15;
    public static final int COAL_ORE = 16;
    public static final int LOG = 17;
    public static final int LEAVES = 18;
    public static final int SPONGE = 19;
    public static final int GLASS = 20;
    public static final int LAPIS_ORE = 21;
    public static final int LAPIS_BLOCK = 22;
    public static final int DISPENSER = 23;
    public static final int SANDSTONE = 24;
    public static final int NOTE_BLOCK = 25;
    public static final int WOOL = 35;
    public static final int DANDELION = 37;
    public static final int ROSE = 38;
    public static final int BROWN_MUSHROOM = 39;
    public static final int RED_MUSHROOM = 40;
    public static final int GOLD_BLOCK = 41;
    public static final int IRON_BLOCK = 42;
    public static final int DOUBLE_SLAB = 43;
    public static final int SLAB = 44;
    public static final int BRICKS = 45;
    public static final int TNT = 46;
    public static final int BOOKSHELF = 47;
    public static final int MOSSY_COBBLESTONE = 48;
    public static final int OBSIDIAN = 49;
    public static final int TORCH = 50;
    public static final int FIRE = 51;
    public static final int SPAWNER = 52;
    public static final int OAK_STAIRS = 53;
    public static final int CHEST = 54;
    public static final int REDSTONE_WIRE = 55;
    public static final int DIAMOND_ORE = 56;
    public static final int DIAMOND_BLOCK = 57;
    public static final int CRAFTING_TABLE = 58;
    public static final int WHEAT = 59;
    public static final int FARMLAND = 60;
    public static final int FURNACE = 61;
    public static final int LIT_FURNACE = 62;
    public static final int SIGN = 63;
    public static final int OAK_DOOR = 64;
    public static final int LADDER = 65;
    public static final int RAIL = 66;
    public static final int COBBLESTONE_STAIRS = 67;
    public static final int WALL_SIGN = 68;
    public static final int LEVER = 69;
    public static final int STONE_PRESSURE_PLATE = 70;
    public static final int IRON_DOOR = 71;
    public static final int WOOD_PRESSURE_PLATE = 72;
    public static final int REDSTONE_ORE = 73;
    public static final int LIT_REDSTONE_ORE = 74;
    public static final int UNLIT_REDSTONE_TORCH = 75;
    public static final int REDSTONE_TORCH = 76;
    public static final int STONE_BUTTON = 77;
    public static final int SNOW_LAYER = 78;
    public static final int ICE = 79;
    public static final int SNOW_BLOCK = 80;
    public static final int CACTUS = 81;
    public static final int CLAY = 82;
    public static final int SUGAR_CANE = 83;
    public static final int JUKEBOX = 84;
    public static final int FENCE = 85;
    public static final int PUMPKIN = 86;
    public static final int NETHERRACK = 87;
    public static final int SOUL_SAND = 88;
    public static final int GLOWSTONE = 89;
    public static final int NETHER_PORTAL = 90;
    public static final int JACK_O_LANTERN = 91;

    private static final Map<Integer, String> BLOCK_NAMES;

    static {
        Map<Integer, String> names = new HashMap<Integer, String>();
        names.put(AIR, "Air");
        names.put(STONE, "Stone");
        names.put(GRASS, "Grass Block");
        names.put(DIRT, "Dirt");
        names.put(COBBLESTONE, "Cobblestone");
        names.put(PLANKS, "Oak Planks");
        names.put(SAPLING, "Sapling");
        names.put(BEDROCK, "Bedrock");
        names.put(FLOWING_WATER, "Flowing Water");
        names.put(STILL_WATER, "Still Water");
        names.put(FLOWING_LAVA, "Flowing Lava");
        names.put(STILL_LAVA, "Still Lava");
        names.put(SAND, "Sand");
        names.put(GRAVEL, "Gravel");
        names.put(GOLD_ORE, "Gold Ore");
        names.put(IRON_ORE, "Iron Ore");
        names.put(COAL_ORE, "Coal Ore");
        names.put(LOG, "Oak Log");
        names.put(LEAVES, "Oak Leaves");
        names.put(SPONGE, "Sponge");
        names.put(GLASS, "Glass");
        names.put(WOOL, "Wool");
        names.put(TORCH, "Torch");
        names.put(CHEST, "Chest");
        names.put(CRAFTING_TABLE, "Crafting Table");
        names.put(FURNACE, "Furnace");
        names.put(OBSIDIAN, "Obsidian");
        names.put(DIAMOND_ORE, "Diamond Ore");
        names.put(DIAMOND_BLOCK, "Diamond Block");
        names.put(NETHERRACK, "Netherrack");
        names.put(GLOWSTONE, "Glowstone");
        BLOCK_NAMES = Collections.unmodifiableMap(names);
    }

    /**
     * Get the display name of a block by its ID.
     */
    public static String getName(int blockId) {
        String name = BLOCK_NAMES.get(blockId);
        return name != null ? name : "Unknown (" + blockId + ")";
    }

    /**
     * Check if a block ID is valid for the given protocol version.
     */
    public static boolean isValidBlock(int blockId, ProtocolVersion version) {
        switch (version) {
            case RUBYDUNG:
                return blockId == AIR || blockId == GRASS || blockId == COBBLESTONE;
            case CLASSIC:
                return blockId >= 0 && blockId <= 49;
            case ALPHA_1_0_15:
            case ALPHA_1_0_16:
            case ALPHA_1_2_5:
                return blockId >= 0 && blockId <= 91;
            default:
                return blockId >= 0;
        }
    }

    private BlockRegistry() {
        // Utility class
    }
}
