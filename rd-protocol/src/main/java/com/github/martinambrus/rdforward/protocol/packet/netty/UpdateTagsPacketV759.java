package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.19 Play state, S2C packet 0x68: Update Tags.
 *
 * Changes from V758:
 * - Block tags renamed: carpets -> wool_carpets, polar_bears_spawnable_on_in_frozen_ocean -> polar_bears_spawnable_on_alternate
 * - Block tags added (14): ancient_city_replaceable, convertable_to_mud, dampens_vibrations,
 *   frog_prefer_jump_to, frogs_spawnable_on, mangrove_logs, mangrove_logs_can_grow_through,
 *   mangrove_roots_can_grow_through, nether_carver_replaceables, overworld_carver_replaceables,
 *   overworld_natural_logs, sculk_replaceable, sculk_replaceable_world_gen, snaps_goat_horn
 * - Item tags: Remove occludes_vibration_signals. Rename carpets -> wool_carpets.
 *   Add: chest_boats, dampens_vibrations, mangrove_logs, overworld_natural_logs.
 * - Entity type tags added (1): frog_food
 * - Game event tags added (3): allay_can_listen, shrieker_can_listen, warden_can_listen
 * Total block tags: 127. Total item tags: 58. Total entity type tags: 11.
 */
public class UpdateTagsPacketV759 implements Packet {

    private static final String[] BLOCK_TAGS = {
        "minecraft:acacia_logs", "minecraft:ancient_city_replaceable",
        "minecraft:animals_spawnable_on",
        "minecraft:anvil", "minecraft:axolotls_spawnable_on",
        "minecraft:azalea_grows_on", "minecraft:azalea_root_replaceable",
        "minecraft:bamboo_plantable_on",
        "minecraft:banners", "minecraft:base_stone_nether", "minecraft:base_stone_overworld",
        "minecraft:beacon_base_blocks", "minecraft:beds",
        "minecraft:bee_growables", "minecraft:beehives",
        "minecraft:big_dripleaf_placeable", "minecraft:birch_logs",
        "minecraft:buttons", "minecraft:campfires", "minecraft:candle_cakes",
        "minecraft:candles", "minecraft:cauldrons", "minecraft:cave_vines",
        "minecraft:climbable", "minecraft:coal_ores",
        "minecraft:convertable_to_mud",
        "minecraft:copper_ores",
        "minecraft:coral_blocks", "minecraft:coral_plants",
        "minecraft:corals", "minecraft:crimson_stems", "minecraft:crops",
        "minecraft:crystal_sound_blocks",
        "minecraft:dampens_vibrations",
        "minecraft:dark_oak_logs", "minecraft:deepslate_ore_replaceables",
        "minecraft:diamond_ores", "minecraft:dirt",
        "minecraft:doors", "minecraft:dragon_immune",
        "minecraft:dripstone_replaceable_blocks",
        "minecraft:emerald_ores", "minecraft:enderman_holdable",
        "minecraft:fall_damage_resetting",
        "minecraft:features_cannot_replace",
        "minecraft:fence_gates", "minecraft:fences",
        "minecraft:fire", "minecraft:flower_pots", "minecraft:flowers",
        "minecraft:foxes_spawnable_on",
        "minecraft:frog_prefer_jump_to",
        "minecraft:frogs_spawnable_on",
        "minecraft:geode_invalid_blocks",
        "minecraft:goats_spawnable_on",
        "minecraft:gold_ores", "minecraft:guarded_by_piglins",
        "minecraft:hoglin_repellents", "minecraft:ice", "minecraft:impermeable",
        "minecraft:infiniburn_end", "minecraft:infiniburn_nether",
        "minecraft:infiniburn_overworld", "minecraft:inside_step_sound_blocks",
        "minecraft:iron_ores", "minecraft:jungle_logs",
        "minecraft:lapis_ores",
        "minecraft:lava_pool_stone_cannot_replace",
        "minecraft:leaves",
        "minecraft:logs", "minecraft:logs_that_burn",
        "minecraft:lush_ground_replaceable",
        "minecraft:mangrove_logs",
        "minecraft:mangrove_logs_can_grow_through",
        "minecraft:mangrove_roots_can_grow_through",
        "minecraft:mineable/axe", "minecraft:mineable/hoe",
        "minecraft:mineable/pickaxe", "minecraft:mineable/shovel",
        "minecraft:mooshrooms_spawnable_on",
        "minecraft:moss_replaceable",
        "minecraft:mushroom_grow_block",
        "minecraft:needs_diamond_tool", "minecraft:needs_iron_tool",
        "minecraft:needs_stone_tool",
        "minecraft:nether_carver_replaceables",
        "minecraft:non_flammable_wood",
        "minecraft:nylium", "minecraft:oak_logs",
        "minecraft:occludes_vibration_signals",
        "minecraft:overworld_carver_replaceables",
        "minecraft:overworld_natural_logs",
        "minecraft:parrots_spawnable_on",
        "minecraft:piglin_repellents",
        "minecraft:planks",
        "minecraft:polar_bears_spawnable_on_alternate",
        "minecraft:portals", "minecraft:pressure_plates",
        "minecraft:prevent_mob_spawning_inside",
        "minecraft:rabbits_spawnable_on",
        "minecraft:rails",
        "minecraft:redstone_ores",
        "minecraft:replaceable_plants",
        "minecraft:sand",
        "minecraft:saplings",
        "minecraft:sculk_replaceable",
        "minecraft:sculk_replaceable_world_gen",
        "minecraft:shulker_boxes", "minecraft:signs",
        "minecraft:slabs", "minecraft:small_dripleaf_placeable",
        "minecraft:small_flowers",
        "minecraft:snaps_goat_horn",
        "minecraft:snow",
        "minecraft:soul_fire_base_blocks",
        "minecraft:soul_speed_blocks", "minecraft:spruce_logs", "minecraft:stairs",
        "minecraft:standing_signs", "minecraft:stone_bricks",
        "minecraft:stone_ore_replaceables", "minecraft:stone_pressure_plates",
        "minecraft:strider_warm_blocks",
        "minecraft:tall_flowers", "minecraft:terracotta",
        "minecraft:trapdoors", "minecraft:underwater_bonemeals",
        "minecraft:unstable_bottom_center", "minecraft:valid_spawn",
        "minecraft:wall_corals", "minecraft:wall_post_override", "minecraft:wall_signs",
        "minecraft:walls", "minecraft:warped_stems", "minecraft:wart_blocks",
        "minecraft:wither_immune", "minecraft:wither_summon_base_blocks",
        "minecraft:wolves_spawnable_on",
        "minecraft:wooden_buttons", "minecraft:wooden_doors", "minecraft:wooden_fences",
        "minecraft:wooden_pressure_plates", "minecraft:wooden_slabs",
        "minecraft:wooden_stairs", "minecraft:wooden_trapdoors",
        "minecraft:wool", "minecraft:wool_carpets"
    };

    private static final String[] ITEM_TAGS = {
        "minecraft:acacia_logs", "minecraft:anvil", "minecraft:arrows",
        "minecraft:axolotl_tempt_items",
        "minecraft:banners", "minecraft:beacon_payment_items", "minecraft:beds",
        "minecraft:birch_logs", "minecraft:boats", "minecraft:buttons",
        "minecraft:candles",
        "minecraft:chest_boats",
        "minecraft:cluster_max_harvestables",
        "minecraft:coals", "minecraft:coal_ores", "minecraft:copper_ores",
        "minecraft:creeper_drop_music_discs",
        "minecraft:crimson_stems",
        "minecraft:dampens_vibrations",
        "minecraft:dark_oak_logs",
        "minecraft:diamond_ores",
        "minecraft:dirt",
        "minecraft:doors",
        "minecraft:emerald_ores",
        "minecraft:fences", "minecraft:fishes", "minecraft:flowers",
        "minecraft:fox_food",
        "minecraft:freeze_immune_wearables",
        "minecraft:gold_ores",
        "minecraft:ignored_by_piglin_babies",
        "minecraft:iron_ores",
        "minecraft:jungle_logs",
        "minecraft:lapis_ores",
        "minecraft:leaves", "minecraft:lectern_books", "minecraft:logs",
        "minecraft:logs_that_burn",
        "minecraft:mangrove_logs",
        "minecraft:music_discs",
        "minecraft:non_flammable_wood", "minecraft:oak_logs",
        "minecraft:overworld_natural_logs",
        "minecraft:piglin_food", "minecraft:piglin_loved",
        "minecraft:piglin_repellents", "minecraft:planks", "minecraft:rails",
        "minecraft:redstone_ores",
        "minecraft:sand", "minecraft:saplings", "minecraft:signs", "minecraft:slabs",
        "minecraft:small_flowers", "minecraft:soul_fire_base_blocks",
        "minecraft:spruce_logs", "minecraft:stairs", "minecraft:stone_bricks",
        "minecraft:stone_crafting_materials", "minecraft:stone_tool_materials",
        "minecraft:tall_flowers",
        "minecraft:terracotta",
        "minecraft:trapdoors", "minecraft:walls", "minecraft:warped_stems",
        "minecraft:wooden_buttons", "minecraft:wooden_doors", "minecraft:wooden_fences",
        "minecraft:wooden_pressure_plates", "minecraft:wooden_slabs",
        "minecraft:wooden_stairs", "minecraft:wooden_trapdoors",
        "minecraft:wool", "minecraft:wool_carpets"
    };

    private static final String[] ENTITY_TYPE_TAGS = {
        "minecraft:arrows", "minecraft:axolotl_always_hostiles",
        "minecraft:axolotl_hunt_targets",
        "minecraft:beehive_inhabitors",
        "minecraft:freeze_hurts_extra_types",
        "minecraft:freeze_immune_entity_types",
        "minecraft:frog_food",
        "minecraft:impact_projectiles",
        "minecraft:powder_snow_walkable_mobs",
        "minecraft:raiders", "minecraft:skeletons"
    };

    @Override
    public int getPacketId() { return 0x68; }

    @Override
    public void write(ByteBuf buf) {
        // 5 registries: block, item, fluid, entity_type, game_event
        McDataTypes.writeVarInt(buf, 5);

        // 1. Block tags
        McDataTypes.writeVarIntString(buf, "minecraft:block");
        writeEmptyTags(buf, BLOCK_TAGS);

        // 2. Item tags
        McDataTypes.writeVarIntString(buf, "minecraft:item");
        writeEmptyTags(buf, ITEM_TAGS);

        // 3. Fluid tags (with entries)
        McDataTypes.writeVarIntString(buf, "minecraft:fluid");
        McDataTypes.writeVarInt(buf, 2);
        // minecraft:water = [flowing_water(1), water(2)]
        McDataTypes.writeVarIntString(buf, "minecraft:water");
        McDataTypes.writeVarInt(buf, 2);
        McDataTypes.writeVarInt(buf, 1);
        McDataTypes.writeVarInt(buf, 2);
        // minecraft:lava = [flowing_lava(3), lava(4)]
        McDataTypes.writeVarIntString(buf, "minecraft:lava");
        McDataTypes.writeVarInt(buf, 2);
        McDataTypes.writeVarInt(buf, 3);
        McDataTypes.writeVarInt(buf, 4);

        // 4. Entity type tags
        McDataTypes.writeVarIntString(buf, "minecraft:entity_type");
        writeEmptyTags(buf, ENTITY_TYPE_TAGS);

        // 5. Game event tags
        McDataTypes.writeVarIntString(buf, "minecraft:game_event");
        McDataTypes.writeVarInt(buf, 5);
        McDataTypes.writeVarIntString(buf, "minecraft:allay_can_listen");
        McDataTypes.writeVarInt(buf, 0);
        McDataTypes.writeVarIntString(buf, "minecraft:ignore_vibrations_sneaking");
        McDataTypes.writeVarInt(buf, 0);
        McDataTypes.writeVarIntString(buf, "minecraft:shrieker_can_listen");
        McDataTypes.writeVarInt(buf, 0);
        McDataTypes.writeVarIntString(buf, "minecraft:vibrations");
        McDataTypes.writeVarInt(buf, 0);
        McDataTypes.writeVarIntString(buf, "minecraft:warden_can_listen");
        McDataTypes.writeVarInt(buf, 0);
    }

    private static void writeEmptyTags(ByteBuf buf, String[] tagNames) {
        McDataTypes.writeVarInt(buf, tagNames.length);
        for (String name : tagNames) {
            McDataTypes.writeVarIntString(buf, name);
            McDataTypes.writeVarInt(buf, 0); // 0 entries
        }
    }

    @Override
    public void read(ByteBuf buf) {
        // S2C only — no server-side decoding needed
    }
}
