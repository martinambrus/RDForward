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

    /**
     * Maps legacy block ID (index) to 1.17 global block state ID.
     * 1.17 shifted all state IDs >= 70 due to deepslate ore variants inserted
     * (deepslate_gold_ore, deepslate_iron_ore, deepslate_coal_ore, etc.) and
     * many other new blocks (copper, amethyst, candles, dripstone, etc.).
     */
    private static final int[] LEGACY_TO_STATE_V755 = new int[256];

    /**
     * Maps legacy item/block ID (index) to 1.17 item ID.
     * Only covers the block-items we actually send (SetSlot for creative).
     */
    private static final int[] LEGACY_TO_ITEM_V755 = new int[256];

    /**
     * Maps legacy block ID (index) to 1.19 global block state ID.
     * 1.19 shifted most state IDs due to mangrove wood, mud, sculk, froglight,
     * and many other Wild Update blocks inserted throughout the registry.
     */
    private static final int[] LEGACY_TO_STATE_V759 = new int[256];

    /**
     * Maps legacy item/block ID (index) to 1.19 item ID.
     * Only covers the block-items we actually send (SetSlot for creative).
     */
    private static final int[] LEGACY_TO_ITEM_V759 = new int[256];

    /**
     * Maps legacy item/block ID (index) to 1.20.3 (v765) item ID.
     * Tuff variants (13 new items) inserted early in the registry shifted most IDs.
     */
    private static final int[] LEGACY_TO_ITEM_V765 = new int[256];

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

        // === 1.17 (v755) Block State Mappings ===
        // IDs sourced from PrismarineJS minecraft-data/data/pc/1.17/blocks.json (defaultState).
        // IDs 0-69 are identical to 1.16. IDs >= 70 shifted due to deepslate ores and
        // many other new blocks (copper, amethyst, candles, dripstone, etc.).
        for (int i = 0; i < 256; i++) {
            LEGACY_TO_STATE_V755[i] = 1; // stone (same default)
            LEGACY_TO_ITEM_V755[i] = -1;
        }

        // Core terrain blocks (unchanged from 1.16, IDs 0-69)
        LEGACY_TO_STATE_V755[0]  = 0;     // air
        LEGACY_TO_STATE_V755[1]  = 1;     // stone
        LEGACY_TO_STATE_V755[2]  = 9;     // grass_block (snowy=false)
        LEGACY_TO_STATE_V755[3]  = 10;    // dirt
        LEGACY_TO_STATE_V755[4]  = 14;    // cobblestone
        LEGACY_TO_STATE_V755[5]  = 15;    // oak_planks
        LEGACY_TO_STATE_V755[6]  = 21;    // oak_sapling (stage=0)
        LEGACY_TO_STATE_V755[7]  = 33;    // bedrock
        LEGACY_TO_STATE_V755[8]  = 34;    // flowing_water → water (level=0)
        LEGACY_TO_STATE_V755[9]  = 34;    // still_water → water (level=0)
        LEGACY_TO_STATE_V755[10] = 50;    // flowing_lava → lava (level=0)
        LEGACY_TO_STATE_V755[11] = 50;    // still_lava → lava (level=0)
        LEGACY_TO_STATE_V755[12] = 66;    // sand
        LEGACY_TO_STATE_V755[13] = 68;    // gravel
        LEGACY_TO_STATE_V755[14] = 69;    // gold_ore
        // deepslate_gold_ore inserted at state 70 in 1.17
        LEGACY_TO_STATE_V755[15] = 71;    // iron_ore (was 70 in 1.16)
        LEGACY_TO_STATE_V755[16] = 73;    // coal_ore (was 71 in 1.16)
        // nether_gold_ore at state 75 in 1.17
        LEGACY_TO_STATE_V755[17] = 77;    // oak_log (axis=y) (was 74 in 1.16)
        LEGACY_TO_STATE_V755[18] = 161;   // oak_leaves (distance=7, persistent=false)
        LEGACY_TO_STATE_V755[19] = 260;   // sponge
        LEGACY_TO_STATE_V755[20] = 262;   // glass
        LEGACY_TO_STATE_V755[21] = 263;   // lapis_ore
        LEGACY_TO_STATE_V755[22] = 265;   // lapis_block
        LEGACY_TO_STATE_V755[23] = 267;   // dispenser
        LEGACY_TO_STATE_V755[24] = 278;   // sandstone
        LEGACY_TO_STATE_V755[25] = 282;   // note_block
        LEGACY_TO_STATE_V755[31] = 1398;  // grass (tall_grass → short_grass/grass)
        LEGACY_TO_STATE_V755[35] = 1440;  // white_wool
        LEGACY_TO_STATE_V755[37] = 1468;  // dandelion
        LEGACY_TO_STATE_V755[38] = 1469;  // poppy
        LEGACY_TO_STATE_V755[39] = 1481;  // brown_mushroom
        LEGACY_TO_STATE_V755[40] = 1482;  // red_mushroom
        LEGACY_TO_STATE_V755[41] = 1483;  // gold_block
        LEGACY_TO_STATE_V755[42] = 1484;  // iron_block
        LEGACY_TO_STATE_V755[44] = 8589;  // stone_slab (type=bottom, waterlogged=false)
        LEGACY_TO_STATE_V755[45] = 1485;  // bricks
        LEGACY_TO_STATE_V755[46] = 1487;  // tnt (unstable=false)
        LEGACY_TO_STATE_V755[47] = 1488;  // bookshelf
        LEGACY_TO_STATE_V755[48] = 1489;  // mossy_cobblestone
        LEGACY_TO_STATE_V755[49] = 1490;  // obsidian
        LEGACY_TO_STATE_V755[50] = 1491;  // torch (floor)
        LEGACY_TO_STATE_V755[51] = 1527;  // fire (age=0)
        LEGACY_TO_STATE_V755[52] = 2009;  // spawner
        LEGACY_TO_STATE_V755[53] = 2021;  // oak_stairs
        LEGACY_TO_STATE_V755[54] = 2091;  // chest
        LEGACY_TO_STATE_V755[55] = 3274;  // redstone_wire
        LEGACY_TO_STATE_V755[56] = 3410;  // diamond_ore
        LEGACY_TO_STATE_V755[57] = 3412;  // diamond_block
        LEGACY_TO_STATE_V755[58] = 3413;  // crafting_table
        LEGACY_TO_STATE_V755[59] = 3414;  // wheat (age=0)
        LEGACY_TO_STATE_V755[60] = 3422;  // farmland (moisture=0)
        LEGACY_TO_STATE_V755[61] = 3431;  // furnace
        LEGACY_TO_STATE_V755[62] = 3431;  // lit_furnace → furnace
        LEGACY_TO_STATE_V755[63] = 3439;  // oak_sign
        LEGACY_TO_STATE_V755[64] = 3641;  // oak_door
        LEGACY_TO_STATE_V755[65] = 3695;  // ladder
        LEGACY_TO_STATE_V755[66] = 3703;  // rail
        LEGACY_TO_STATE_V755[67] = 3733;  // cobblestone_stairs
        LEGACY_TO_STATE_V755[68] = 3803;  // oak_wall_sign
        LEGACY_TO_STATE_V755[69] = 3859;  // lever
        LEGACY_TO_STATE_V755[70] = 3875;  // stone_pressure_plate (powered=false)
        LEGACY_TO_STATE_V755[71] = 3887;  // iron_door
        LEGACY_TO_STATE_V755[72] = 3941;  // oak_pressure_plate (powered=false)
        LEGACY_TO_STATE_V755[73] = 3953;  // redstone_ore
        LEGACY_TO_STATE_V755[74] = 3953;  // lit_redstone_ore → redstone_ore
        LEGACY_TO_STATE_V755[75] = 3958;  // unlit_redstone_torch → redstone_wall_torch
        LEGACY_TO_STATE_V755[76] = 3956;  // redstone_torch
        LEGACY_TO_STATE_V755[77] = 3975;  // stone_button
        LEGACY_TO_STATE_V755[78] = 3990;  // snow (layers=1)
        LEGACY_TO_STATE_V755[79] = 3998;  // ice
        LEGACY_TO_STATE_V755[80] = 3999;  // snow_block
        LEGACY_TO_STATE_V755[81] = 4000;  // cactus (age=0)
        LEGACY_TO_STATE_V755[82] = 4016;  // clay
        LEGACY_TO_STATE_V755[83] = 4017;  // sugar_cane (age=0)
        LEGACY_TO_STATE_V755[84] = 4034;  // jukebox (has_record=false)
        LEGACY_TO_STATE_V755[85] = 4066;  // oak_fence
        LEGACY_TO_STATE_V755[86] = 4067;  // pumpkin
        LEGACY_TO_STATE_V755[87] = 4068;  // netherrack
        LEGACY_TO_STATE_V755[88] = 4069;  // soul_sand
        LEGACY_TO_STATE_V755[89] = 4082;  // glowstone
        LEGACY_TO_STATE_V755[98] = 4564;  // stone_bricks

        // 1.17 item IDs (shifted due to new items)
        LEGACY_TO_ITEM_V755[0]  = 0;    // air
        LEGACY_TO_ITEM_V755[1]  = 1;    // stone
        LEGACY_TO_ITEM_V755[2]  = 14;   // grass_block
        LEGACY_TO_ITEM_V755[3]  = 15;   // dirt
        LEGACY_TO_ITEM_V755[4]  = 21;   // cobblestone
        LEGACY_TO_ITEM_V755[5]  = 22;   // oak_planks

        // === 1.19 (v759) Block State Mappings ===
        // IDs sourced from PrismarineJS minecraft-data/data/pc/1.19/blocks.json (defaultState).
        // Many new blocks (mangrove, mud, sculk, froglight, etc.) shift most IDs.
        for (int i = 0; i < 256; i++) {
            LEGACY_TO_STATE_V759[i] = 1; // stone (same default)
            LEGACY_TO_ITEM_V759[i] = -1;
        }

        // Core terrain blocks
        LEGACY_TO_STATE_V759[0]  = 0;     // air
        LEGACY_TO_STATE_V759[1]  = 1;     // stone
        LEGACY_TO_STATE_V759[2]  = 9;     // grass_block (snowy=false)
        LEGACY_TO_STATE_V759[3]  = 10;    // dirt
        LEGACY_TO_STATE_V759[4]  = 14;    // cobblestone
        LEGACY_TO_STATE_V759[5]  = 15;    // oak_planks
        LEGACY_TO_STATE_V759[6]  = 22;    // oak_sapling (stage=0)
        LEGACY_TO_STATE_V759[7]  = 74;    // bedrock
        LEGACY_TO_STATE_V759[8]  = 75;    // flowing_water → water (level=0)
        LEGACY_TO_STATE_V759[9]  = 75;    // still_water → water (level=0)
        LEGACY_TO_STATE_V759[10] = 91;    // flowing_lava → lava (level=0)
        LEGACY_TO_STATE_V759[11] = 91;    // still_lava → lava (level=0)
        LEGACY_TO_STATE_V759[12] = 107;   // sand
        LEGACY_TO_STATE_V759[13] = 109;   // gravel
        LEGACY_TO_STATE_V759[14] = 110;   // gold_ore
        LEGACY_TO_STATE_V759[15] = 112;   // iron_ore
        LEGACY_TO_STATE_V759[16] = 114;   // coal_ore
        LEGACY_TO_STATE_V759[17] = 118;   // oak_log (axis=y)
        LEGACY_TO_STATE_V759[18] = 233;   // oak_leaves (distance=7, persistent=false)
        LEGACY_TO_STATE_V759[19] = 458;   // sponge
        LEGACY_TO_STATE_V759[20] = 460;   // glass
        LEGACY_TO_STATE_V759[21] = 461;   // lapis_ore
        LEGACY_TO_STATE_V759[22] = 463;   // lapis_block
        LEGACY_TO_STATE_V759[23] = 465;   // dispenser
        LEGACY_TO_STATE_V759[24] = 476;   // sandstone
        LEGACY_TO_STATE_V759[25] = 480;   // note_block
        LEGACY_TO_STATE_V759[31] = 1596;  // grass (tall_grass → grass)
        LEGACY_TO_STATE_V759[35] = 1638;  // white_wool
        LEGACY_TO_STATE_V759[37] = 1666;  // dandelion
        LEGACY_TO_STATE_V759[38] = 1667;  // poppy
        LEGACY_TO_STATE_V759[39] = 1679;  // brown_mushroom
        LEGACY_TO_STATE_V759[40] = 1680;  // red_mushroom
        LEGACY_TO_STATE_V759[41] = 1681;  // gold_block
        LEGACY_TO_STATE_V759[42] = 1682;  // iron_block
        LEGACY_TO_STATE_V759[44] = 9086;  // stone_slab (type=bottom, waterlogged=false)
        LEGACY_TO_STATE_V759[45] = 1683;  // bricks
        LEGACY_TO_STATE_V759[46] = 1685;  // tnt (unstable=false)
        LEGACY_TO_STATE_V759[47] = 1686;  // bookshelf
        LEGACY_TO_STATE_V759[48] = 1687;  // mossy_cobblestone
        LEGACY_TO_STATE_V759[49] = 1688;  // obsidian
        LEGACY_TO_STATE_V759[50] = 1689;  // torch (floor)
        LEGACY_TO_STATE_V759[51] = 1725;  // fire (age=0)
        LEGACY_TO_STATE_V759[52] = 2207;  // spawner
        LEGACY_TO_STATE_V759[53] = 2219;  // oak_stairs
        LEGACY_TO_STATE_V759[54] = 2289;  // chest
        LEGACY_TO_STATE_V759[55] = 3472;  // redstone_wire
        LEGACY_TO_STATE_V759[56] = 3608;  // diamond_ore
        LEGACY_TO_STATE_V759[57] = 3610;  // diamond_block
        LEGACY_TO_STATE_V759[58] = 3611;  // crafting_table
        LEGACY_TO_STATE_V759[59] = 3612;  // wheat (age=0)
        LEGACY_TO_STATE_V759[60] = 3620;  // farmland (moisture=0)
        LEGACY_TO_STATE_V759[61] = 3629;  // furnace
        LEGACY_TO_STATE_V759[62] = 3629;  // lit_furnace → furnace
        LEGACY_TO_STATE_V759[63] = 3637;  // oak_sign
        LEGACY_TO_STATE_V759[64] = 3871;  // oak_door
        LEGACY_TO_STATE_V759[65] = 3925;  // ladder
        LEGACY_TO_STATE_V759[66] = 3933;  // rail
        LEGACY_TO_STATE_V759[67] = 3963;  // cobblestone_stairs
        LEGACY_TO_STATE_V759[68] = 4033;  // oak_wall_sign
        LEGACY_TO_STATE_V759[69] = 4097;  // lever
        LEGACY_TO_STATE_V759[70] = 4113;  // stone_pressure_plate (powered=false)
        LEGACY_TO_STATE_V759[71] = 4125;  // iron_door
        LEGACY_TO_STATE_V759[72] = 4179;  // oak_pressure_plate (powered=false)
        LEGACY_TO_STATE_V759[73] = 4193;  // redstone_ore
        LEGACY_TO_STATE_V759[74] = 4193;  // lit_redstone_ore → redstone_ore
        LEGACY_TO_STATE_V759[75] = 4198;  // unlit_redstone_torch → redstone_wall_torch
        LEGACY_TO_STATE_V759[76] = 4196;  // redstone_torch
        LEGACY_TO_STATE_V759[77] = 4215;  // stone_button
        LEGACY_TO_STATE_V759[78] = 4230;  // snow (layers=1)
        LEGACY_TO_STATE_V759[79] = 4238;  // ice
        LEGACY_TO_STATE_V759[80] = 4239;  // snow_block
        LEGACY_TO_STATE_V759[81] = 4240;  // cactus (age=0)
        LEGACY_TO_STATE_V759[82] = 4256;  // clay
        LEGACY_TO_STATE_V759[83] = 4257;  // sugar_cane (age=0)
        LEGACY_TO_STATE_V759[84] = 4274;  // jukebox (has_record=false)
        LEGACY_TO_STATE_V759[85] = 4306;  // oak_fence
        LEGACY_TO_STATE_V759[86] = 4307;  // pumpkin
        LEGACY_TO_STATE_V759[87] = 4308;  // netherrack
        LEGACY_TO_STATE_V759[88] = 4309;  // soul_sand
        LEGACY_TO_STATE_V759[89] = 4322;  // glowstone
        LEGACY_TO_STATE_V759[98] = 4868;  // stone_bricks

        // 1.19 item IDs (shifted due to new items)
        LEGACY_TO_ITEM_V759[0]  = 0;    // air
        LEGACY_TO_ITEM_V759[1]  = 1;    // stone
        LEGACY_TO_ITEM_V759[2]  = 14;   // grass_block
        LEGACY_TO_ITEM_V759[3]  = 15;   // dirt
        LEGACY_TO_ITEM_V759[4]  = 22;   // cobblestone
        LEGACY_TO_ITEM_V759[5]  = 23;   // oak_planks

        // 1.20.3 item IDs (tuff variants inserted, shifting most IDs)
        for (int i = 0; i < 256; i++) {
            LEGACY_TO_ITEM_V765[i] = -1;
        }
        LEGACY_TO_ITEM_V765[0]  = 0;    // air
        LEGACY_TO_ITEM_V765[1]  = 1;    // stone
        LEGACY_TO_ITEM_V765[2]  = 27;   // grass_block
        LEGACY_TO_ITEM_V765[3]  = 28;   // dirt
        LEGACY_TO_ITEM_V765[4]  = 35;   // cobblestone
        LEGACY_TO_ITEM_V765[5]  = 36;   // oak_planks
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

    /**
     * Convert a legacy block ID (meta=0) to its 1.17 global block state ID.
     * Used for chunk palette entries and BlockChange packets for v755+ clients.
     */
    public static int toV755BlockState(int legacyBlockId) {
        if (legacyBlockId < 0 || legacyBlockId >= 256) {
            return 1; // stone
        }
        return LEGACY_TO_STATE_V755[legacyBlockId];
    }

    /**
     * Convert a legacy item/block ID to its 1.17 item ID.
     * Used for SetSlot packets for v755+ clients. Returns -1 if unmapped.
     */
    public static int toV755ItemId(int legacyItemId) {
        if (legacyItemId < 0 || legacyItemId >= 256) {
            return -1;
        }
        return LEGACY_TO_ITEM_V755[legacyItemId];
    }

    /**
     * Convert a legacy block ID (meta=0) to its 1.19 global block state ID.
     * Used for chunk palette entries and BlockChange packets for v759+ clients.
     */
    public static int toV759BlockState(int legacyBlockId) {
        if (legacyBlockId < 0 || legacyBlockId >= 256) {
            return 1; // stone
        }
        return LEGACY_TO_STATE_V759[legacyBlockId];
    }

    /**
     * Convert a legacy item/block ID to its 1.19 item ID.
     * Used for SetSlot packets for v759+ clients. Returns -1 if unmapped.
     */
    public static int toV759ItemId(int legacyItemId) {
        if (legacyItemId < 0 || legacyItemId >= 256) {
            return -1;
        }
        return LEGACY_TO_ITEM_V759[legacyItemId];
    }

    /**
     * Convert a legacy item/block ID to its 1.20.3 (v765) item ID.
     * Used for SetSlot packets for v765+ clients. Returns -1 if unmapped.
     */
    public static int toV765ItemId(int legacyItemId) {
        if (legacyItemId < 0 || legacyItemId >= 256) {
            return -1;
        }
        return LEGACY_TO_ITEM_V765[legacyItemId];
    }
}
