package com.github.martinambrus.rdforward.protocol;

/**
 * Maps legacy block IDs (0-255, meta=0) to global block state IDs for
 * MC 1.13 (v393) and MC 1.16 (v735), and legacy item IDs to flat item IDs.
 *
 * In pre-1.13, block states were encoded as {@code (blockId << 4) | metadata}.
 * In 1.13+, each unique block+property combination has a flat global state ID.
 * Since our server stores blocks without metadata, all mappings assume meta=0
 * (the default variant of each block).
 *
 * 1.16 introduced many new blocks (Nether Update), shifting state IDs for all
 * blocks at state >= 72 (nether_gold_ore inserted). Total states grew from
 * ~11,337 (1.15) to ~17,104 (1.16), requiring 15-bit global palette.
 *
 * State IDs sourced from PrismarineJS minecraft-data, ViaVersion mapping data,
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

    /**
     * Maps legacy block ID (index) to 1.16 global block state ID.
     * 1.16 shifted all state IDs >= 72 due to nether_gold_ore insertion and
     * many other new blocks (soul_fire, crimson/warped wood, blackstone, etc.).
     */
    private static final int[] LEGACY_TO_STATE_V735 = new int[256];

    /**
     * Maps legacy item/block ID (index) to 1.16 item ID.
     * Only covers the block-items we actually send (SetSlot for creative).
     */
    private static final int[] LEGACY_TO_ITEM_V735 = new int[256];

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

        // === 1.16 (v735) Block State Mappings ===
        // IDs sourced from ViaVersion mapping data (1.13→1.16 state mapping).
        // IDs 0-71 are identical to 1.13. IDs >= 72 shifted due to new blocks.
        for (int i = 0; i < 256; i++) {
            LEGACY_TO_STATE_V735[i] = 1; // stone (same default)
            LEGACY_TO_ITEM_V735[i] = -1;
        }

        // Core terrain blocks (unchanged from 1.13, IDs 0-71)
        LEGACY_TO_STATE_V735[0]  = 0;     // air
        LEGACY_TO_STATE_V735[1]  = 1;     // stone
        LEGACY_TO_STATE_V735[2]  = 9;     // grass_block (snowy=false)
        LEGACY_TO_STATE_V735[3]  = 10;    // dirt
        LEGACY_TO_STATE_V735[4]  = 14;    // cobblestone
        LEGACY_TO_STATE_V735[5]  = 15;    // oak_planks
        LEGACY_TO_STATE_V735[6]  = 21;    // oak_sapling (stage=0)
        LEGACY_TO_STATE_V735[7]  = 33;    // bedrock
        LEGACY_TO_STATE_V735[8]  = 34;    // flowing_water → water (level=0)
        LEGACY_TO_STATE_V735[9]  = 34;    // still_water → water (level=0)
        LEGACY_TO_STATE_V735[10] = 50;    // flowing_lava → lava (level=0)
        LEGACY_TO_STATE_V735[11] = 50;    // still_lava → lava (level=0)
        LEGACY_TO_STATE_V735[12] = 66;    // sand
        LEGACY_TO_STATE_V735[13] = 68;    // gravel
        LEGACY_TO_STATE_V735[14] = 69;    // gold_ore
        LEGACY_TO_STATE_V735[15] = 70;    // iron_ore
        LEGACY_TO_STATE_V735[16] = 71;    // coal_ore
        // nether_gold_ore inserted at state 72 in 1.16
        LEGACY_TO_STATE_V735[17] = 74;    // oak_log (axis=y)
        LEGACY_TO_STATE_V735[18] = 158;   // oak_leaves (distance=7, persistent=false)
        LEGACY_TO_STATE_V735[19] = 229;   // sponge
        LEGACY_TO_STATE_V735[20] = 231;   // glass
        LEGACY_TO_STATE_V735[21] = 232;   // lapis_ore
        LEGACY_TO_STATE_V735[22] = 233;   // lapis_block
        LEGACY_TO_STATE_V735[23] = 235;   // dispenser
        LEGACY_TO_STATE_V735[24] = 246;   // sandstone
        LEGACY_TO_STATE_V735[25] = 250;   // note_block
        LEGACY_TO_STATE_V735[35] = 1384;  // white_wool
        LEGACY_TO_STATE_V735[37] = 1412;  // dandelion
        LEGACY_TO_STATE_V735[38] = 1413;  // poppy
        LEGACY_TO_STATE_V735[39] = 1425;  // brown_mushroom
        LEGACY_TO_STATE_V735[40] = 1426;  // red_mushroom
        LEGACY_TO_STATE_V735[41] = 1427;  // gold_block
        LEGACY_TO_STATE_V735[42] = 1428;  // iron_block
        LEGACY_TO_STATE_V735[44] = 8339;  // stone_slab (type=bottom, waterlogged=false)
        LEGACY_TO_STATE_V735[45] = 1429;  // bricks
        LEGACY_TO_STATE_V735[46] = 1431;  // tnt (unstable=false)
        LEGACY_TO_STATE_V735[47] = 1432;  // bookshelf
        LEGACY_TO_STATE_V735[48] = 1433;  // mossy_cobblestone
        LEGACY_TO_STATE_V735[49] = 1434;  // obsidian
        LEGACY_TO_STATE_V735[50] = 1435;  // torch (floor)
        LEGACY_TO_STATE_V735[51] = 1471;  // fire (age=0)
        // soul_fire inserted at state 1952 in 1.16
        LEGACY_TO_STATE_V735[52] = 1953;  // spawner
        LEGACY_TO_STATE_V735[53] = 1965;  // oak_stairs
        LEGACY_TO_STATE_V735[54] = 2035;  // chest
        LEGACY_TO_STATE_V735[55] = 3218;  // redstone_wire
        LEGACY_TO_STATE_V735[56] = 3354;  // diamond_ore
        LEGACY_TO_STATE_V735[57] = 3355;  // diamond_block
        LEGACY_TO_STATE_V735[58] = 3356;  // crafting_table
        LEGACY_TO_STATE_V735[59] = 3357;  // wheat (age=0)
        LEGACY_TO_STATE_V735[60] = 3365;  // farmland (moisture=0)
        LEGACY_TO_STATE_V735[61] = 3374;  // furnace
        LEGACY_TO_STATE_V735[62] = 3374;  // lit_furnace → furnace
        LEGACY_TO_STATE_V735[63] = 3382;  // oak_sign
        LEGACY_TO_STATE_V735[64] = 3584;  // oak_door
        LEGACY_TO_STATE_V735[65] = 3638;  // ladder
        LEGACY_TO_STATE_V735[66] = 3645;  // rail
        LEGACY_TO_STATE_V735[67] = 3666;  // cobblestone_stairs
        LEGACY_TO_STATE_V735[68] = 3736;  // oak_wall_sign
        LEGACY_TO_STATE_V735[69] = 3792;  // lever
        LEGACY_TO_STATE_V735[70] = 3808;  // stone_pressure_plate (powered=false)
        LEGACY_TO_STATE_V735[71] = 3820;  // iron_door
        LEGACY_TO_STATE_V735[72] = 3874;  // oak_pressure_plate (powered=false)
        LEGACY_TO_STATE_V735[73] = 3886;  // redstone_ore
        LEGACY_TO_STATE_V735[74] = 3886;  // lit_redstone_ore → redstone_ore
        LEGACY_TO_STATE_V735[75] = 3889;  // redstone_wall_torch
        LEGACY_TO_STATE_V735[76] = 3887;  // redstone_torch
        LEGACY_TO_STATE_V735[77] = 3906;  // stone_button
        LEGACY_TO_STATE_V735[78] = 3921;  // snow (layers=1)
        LEGACY_TO_STATE_V735[79] = 3929;  // ice
        LEGACY_TO_STATE_V735[80] = 3930;  // snow_block
        LEGACY_TO_STATE_V735[81] = 3931;  // cactus (age=0)
        LEGACY_TO_STATE_V735[82] = 3947;  // clay
        LEGACY_TO_STATE_V735[83] = 3948;  // sugar_cane (age=0)
        LEGACY_TO_STATE_V735[84] = 3965;  // jukebox (has_record=false)
        LEGACY_TO_STATE_V735[85] = 3997;  // oak_fence
        LEGACY_TO_STATE_V735[86] = 3998;  // pumpkin
        LEGACY_TO_STATE_V735[87] = 3999;  // netherrack
        LEGACY_TO_STATE_V735[88] = 4000;  // soul_sand

        // 1.16 item IDs (shifted due to new items)
        LEGACY_TO_ITEM_V735[0]  = 0;    // air
        LEGACY_TO_ITEM_V735[1]  = 1;    // stone
        LEGACY_TO_ITEM_V735[2]  = 8;    // grass_block
        LEGACY_TO_ITEM_V735[3]  = 9;    // dirt
        LEGACY_TO_ITEM_V735[4]  = 14;   // cobblestone (was 12 in 1.13)
        LEGACY_TO_ITEM_V735[5]  = 15;   // oak_planks (was 13 in 1.13)
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

    /**
     * Convert a legacy block ID (meta=0) to its 1.16 global block state ID.
     * Used for chunk palette entries and BlockChange packets for v735+ clients.
     */
    public static int toV735BlockState(int legacyBlockId) {
        if (legacyBlockId < 0 || legacyBlockId >= 256) {
            return 1; // stone
        }
        return LEGACY_TO_STATE_V735[legacyBlockId];
    }

    /**
     * Convert a legacy item/block ID to its 1.16 item ID.
     * Used for SetSlot packets for v735+ clients. Returns -1 if unmapped.
     */
    public static int toV735ItemId(int legacyItemId) {
        if (legacyItemId < 0 || legacyItemId >= 256) {
            return -1;
        }
        return LEGACY_TO_ITEM_V735[legacyItemId];
    }
}
