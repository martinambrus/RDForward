package com.github.martinambrus.rdforward.protocol;

/**
 * Maps legacy block IDs (0-255, meta=0) to MC 1.13 (v393) global block state IDs,
 * and legacy item IDs to 1.13 item IDs.
 *
 * In pre-1.13, block states were encoded as {@code (blockId << 4) | metadata}.
 * In 1.13+, each unique block+property combination has a flat global state ID.
 * Since our server stores blocks without metadata, all mappings assume meta=0
 * (the default variant of each block).
 *
 * State IDs sourced from PrismarineJS minecraft-data/data/pc/1.13/blocks.json
 * and wiki.vg Block State Registry.
 */
public final class BlockStateMapper {

    /**
     * Maps legacy block ID (index) to 1.13 global block state ID.
     * Index range: 0-255. Unmapped blocks default to stone (state 1).
     */
    private static final int[] LEGACY_TO_STATE = new int[256];

    /**
     * Maps legacy item/block ID (index) to 1.13 item ID.
     * Only covers the block-items we actually send (SetSlot for creative).
     * Index range: 0-255. Unmapped items return -1.
     */
    private static final int[] LEGACY_TO_ITEM = new int[256];

    static {
        // Default all to stone (safe fallback — renders as a visible block)
        for (int i = 0; i < 256; i++) {
            LEGACY_TO_STATE[i] = 1; // stone
            LEGACY_TO_ITEM[i] = -1; // unmapped
        }

        // === Block State Mappings (legacy blockId meta=0 → 1.13 default state) ===
        // State IDs verified from PrismarineJS minecraft-data/data/pc/1.13/blocks.json
        // (defaultState field for each block).

        // Core terrain blocks (0-20)
        LEGACY_TO_STATE[0]  = 0;     // air
        LEGACY_TO_STATE[1]  = 1;     // stone
        LEGACY_TO_STATE[2]  = 9;     // grass_block (snowy=false)
        LEGACY_TO_STATE[3]  = 10;    // dirt
        LEGACY_TO_STATE[4]  = 14;    // cobblestone
        LEGACY_TO_STATE[5]  = 15;    // oak_planks
        LEGACY_TO_STATE[6]  = 21;    // oak_sapling (stage=0)
        LEGACY_TO_STATE[7]  = 33;    // bedrock
        LEGACY_TO_STATE[8]  = 34;    // flowing_water → water (level=0)
        LEGACY_TO_STATE[9]  = 34;    // still_water → water (level=0)
        LEGACY_TO_STATE[10] = 50;    // flowing_lava → lava (level=0)
        LEGACY_TO_STATE[11] = 50;    // still_lava → lava (level=0)
        LEGACY_TO_STATE[12] = 66;    // sand
        LEGACY_TO_STATE[13] = 68;    // gravel
        LEGACY_TO_STATE[14] = 69;    // gold_ore
        LEGACY_TO_STATE[15] = 70;    // iron_ore
        LEGACY_TO_STATE[16] = 71;    // coal_ore
        LEGACY_TO_STATE[17] = 73;    // oak_log (axis=y)
        LEGACY_TO_STATE[18] = 157;   // oak_leaves (distance=7, persistent=false)
        LEGACY_TO_STATE[19] = 228;   // sponge
        LEGACY_TO_STATE[20] = 230;   // glass

        // Ores and minerals (21-22)
        LEGACY_TO_STATE[21] = 231;   // lapis_ore
        LEGACY_TO_STATE[22] = 232;   // lapis_block

        // Redstone and utility (23-25)
        LEGACY_TO_STATE[23] = 234;   // dispenser
        LEGACY_TO_STATE[24] = 245;   // sandstone
        LEGACY_TO_STATE[25] = 249;   // note_block

        // Misc blocks (35-49)
        LEGACY_TO_STATE[35] = 1083;  // white_wool
        LEGACY_TO_STATE[37] = 1111;  // dandelion
        LEGACY_TO_STATE[38] = 1112;  // poppy
        LEGACY_TO_STATE[39] = 1121;  // brown_mushroom
        LEGACY_TO_STATE[40] = 1122;  // red_mushroom
        LEGACY_TO_STATE[41] = 1123;  // gold_block
        LEGACY_TO_STATE[42] = 1124;  // iron_block
        // 43 (double_stone_slab) → no direct 1.13 equivalent, default to stone
        LEGACY_TO_STATE[44] = 3473;  // stone_slab
        LEGACY_TO_STATE[45] = 1125;  // bricks
        LEGACY_TO_STATE[46] = 1126;  // tnt
        LEGACY_TO_STATE[47] = 1127;  // bookshelf
        LEGACY_TO_STATE[48] = 1128;  // mossy_cobblestone
        LEGACY_TO_STATE[49] = 1129;  // obsidian

        // Torches and fire (50-52)
        LEGACY_TO_STATE[50] = 1130;  // torch (floor)
        LEGACY_TO_STATE[51] = 1166;  // fire (age=0)
        LEGACY_TO_STATE[52] = 1647;  // spawner

        // Stairs and storage (53-54)
        LEGACY_TO_STATE[53] = 1659;  // oak_stairs
        LEGACY_TO_STATE[54] = 1729;  // chest

        // Redstone components (55-60)
        LEGACY_TO_STATE[55] = 2912;  // redstone_wire
        LEGACY_TO_STATE[56] = 3048;  // diamond_ore
        LEGACY_TO_STATE[57] = 3049;  // diamond_block
        LEGACY_TO_STATE[58] = 3050;  // crafting_table
        LEGACY_TO_STATE[59] = 3051;  // wheat (age=0)
        LEGACY_TO_STATE[60] = 3059;  // farmland (moisture=0)

        // Furnace and signs (61-68)
        LEGACY_TO_STATE[61] = 3068;  // furnace
        LEGACY_TO_STATE[62] = 3068;  // lit_furnace → furnace (same block in 1.13)
        LEGACY_TO_STATE[63] = 3076;  // oak_sign
        LEGACY_TO_STATE[64] = 3118;  // oak_door
        LEGACY_TO_STATE[65] = 3172;  // ladder
        LEGACY_TO_STATE[66] = 3179;  // rail
        LEGACY_TO_STATE[67] = 3200;  // cobblestone_stairs
        LEGACY_TO_STATE[68] = 3270;  // oak_wall_sign

        // Switches and plates (69-72)
        LEGACY_TO_STATE[69] = 3286;  // lever
        LEGACY_TO_STATE[70] = 3302;  // stone_pressure_plate (powered=false)
        LEGACY_TO_STATE[71] = 3314;  // iron_door
        LEGACY_TO_STATE[72] = 3368;  // oak_pressure_plate (powered=false)

        // Redstone ore and torches (73-77)
        LEGACY_TO_STATE[73] = 3380;  // redstone_ore
        LEGACY_TO_STATE[74] = 3380;  // lit_redstone_ore → redstone_ore (same block in 1.13)
        LEGACY_TO_STATE[75] = 3383;  // unlit_redstone_torch → redstone_wall_torch
        LEGACY_TO_STATE[76] = 3381;  // redstone_torch
        LEGACY_TO_STATE[77] = 3400;  // stone_button

        // Snow and ice (78-80)
        LEGACY_TO_STATE[78] = 3415;  // snow (layers=1)
        LEGACY_TO_STATE[79] = 3423;  // ice
        LEGACY_TO_STATE[80] = 3424;  // snow_block

        // Cactus through portal (81-91)
        LEGACY_TO_STATE[81] = 3425;  // cactus (age=0)
        LEGACY_TO_STATE[82] = 3441;  // clay
        LEGACY_TO_STATE[83] = 3442;  // sugar_cane (age=0)
        LEGACY_TO_STATE[84] = 3459;  // jukebox (has_record=false)
        LEGACY_TO_STATE[85] = 3491;  // oak_fence
        LEGACY_TO_STATE[86] = 3492;  // pumpkin
        LEGACY_TO_STATE[87] = 3493;  // netherrack
        LEGACY_TO_STATE[88] = 3494;  // soul_sand
        // 89-91: Nether blocks — not found in truncated PrismarineJS data,
        // defaulting to stone. Rarely appear in Classic-style terrain.

        // === Item ID Mappings (legacy blockId → 1.13 item ID) ===
        // Only items we actually send in SetSlot. 1.13 items are flat IDs.
        LEGACY_TO_ITEM[0]  = 0;    // air
        LEGACY_TO_ITEM[1]  = 1;    // stone
        LEGACY_TO_ITEM[2]  = 8;    // grass_block
        LEGACY_TO_ITEM[3]  = 9;    // dirt
        LEGACY_TO_ITEM[4]  = 12;   // cobblestone
        LEGACY_TO_ITEM[5]  = 13;   // oak_planks
    }

    private BlockStateMapper() {}

    /**
     * Convert a legacy block ID (meta=0) to its 1.13 global block state ID.
     * Used for chunk palette entries and BlockChange packets.
     */
    public static int toV393BlockState(int legacyBlockId) {
        if (legacyBlockId < 0 || legacyBlockId >= 256) {
            return 1; // stone
        }
        return LEGACY_TO_STATE[legacyBlockId];
    }

    /**
     * Convert a legacy item/block ID to its 1.13 item ID.
     * Used for SetSlot packets. Returns -1 if unmapped.
     */
    public static int toV393ItemId(int legacyItemId) {
        if (legacyItemId < 0 || legacyItemId >= 256) {
            return -1;
        }
        return LEGACY_TO_ITEM[legacyItemId];
    }
}
