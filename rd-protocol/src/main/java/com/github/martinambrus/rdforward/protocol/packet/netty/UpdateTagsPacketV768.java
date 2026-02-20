package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.21.2 Configuration/Play state, S2C packet.
 * Configuration: 0x0D, Play: 0x7F.
 *
 * Changes from V767:
 * - NEW registry: minecraft:worldgen/biome (36 tags, all empty)
 *   Required because enchantment built-in data references biome tags
 *   (is_badlands, is_jungle, is_savanna, etc.).
 * - Item tags: +18 enchantable/* sub-tags (96 total). In 1.21.2, the client
 *   validates item tag references eagerly during enchantment built-in data
 *   parsing, before merging its own core pack tags. Without these, all
 *   enchantments except mace enchantments fail to parse.
 * - Block, fluid, entity_type, game_event, damage_type, enchantment:
 *   unchanged from V767.
 */
public class UpdateTagsPacketV768 implements Packet {

    // Reuse V767's tag arrays for the 7 unchanged registries
    private static final String[] BLOCK_TAGS = {
        "minecraft:acacia_logs", "minecraft:all_hanging_signs", "minecraft:all_signs",
        "minecraft:ancient_city_replaceable",
        "minecraft:animals_spawnable_on",
        "minecraft:anvil", "minecraft:axolotls_spawnable_on",
        "minecraft:azalea_grows_on", "minecraft:azalea_root_replaceable",
        "minecraft:badlands_terracotta",
        "minecraft:bamboo_blocks",
        "minecraft:bamboo_plantable_on",
        "minecraft:banners", "minecraft:base_stone_nether", "minecraft:base_stone_overworld",
        "minecraft:beacon_base_blocks", "minecraft:beds",
        "minecraft:bee_growables", "minecraft:beehives",
        "minecraft:big_dripleaf_placeable", "minecraft:birch_logs",
        "minecraft:blocks_wind_charge_explosions",
        "minecraft:buttons",
        "minecraft:camel_sand_step_sound_blocks",
        "minecraft:campfires", "minecraft:candle_cakes",
        "minecraft:candles", "minecraft:cauldrons", "minecraft:cave_vines",
        "minecraft:ceiling_hanging_signs",
        "minecraft:cherry_logs",
        "minecraft:climbable", "minecraft:coal_ores",
        "minecraft:combination_step_sound_blocks",
        "minecraft:concrete_powder",
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
        "minecraft:emerald_ores",
        "minecraft:enchantment_power_provider", "minecraft:enchantment_power_transmitter",
        "minecraft:enderman_holdable",
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
        "minecraft:invalid_spawn_inside",
        "minecraft:iron_ores", "minecraft:jungle_logs",
        "minecraft:lapis_ores",
        "minecraft:lava_pool_stone_cannot_replace",
        "minecraft:leaves",
        "minecraft:logs", "minecraft:logs_that_burn",
        "minecraft:lush_ground_replaceable",
        "minecraft:maintains_farmland",
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
        "minecraft:replaceable",
        "minecraft:replaceable_by_trees",
        "minecraft:sand",
        "minecraft:saplings",
        "minecraft:sculk_replaceable",
        "minecraft:sculk_replaceable_world_gen",
        "minecraft:shulker_boxes", "minecraft:signs",
        "minecraft:slabs", "minecraft:small_dripleaf_placeable",
        "minecraft:small_flowers",
        "minecraft:smelts_to_glass",
        "minecraft:snaps_goat_horn",
        "minecraft:sniffer_diggable_block",
        "minecraft:sniffer_egg_hatch_boost",
        "minecraft:snow",
        "minecraft:soul_fire_base_blocks",
        "minecraft:soul_speed_blocks", "minecraft:spruce_logs", "minecraft:stairs",
        "minecraft:standing_signs", "minecraft:stone_bricks",
        "minecraft:stone_buttons",
        "minecraft:stone_ore_replaceables", "minecraft:stone_pressure_plates",
        "minecraft:strider_warm_blocks",
        "minecraft:sword_efficient",
        "minecraft:tall_flowers", "minecraft:terracotta",
        "minecraft:trail_ruins_replaceable",
        "minecraft:trapdoors", "minecraft:underwater_bonemeals",
        "minecraft:unstable_bottom_center", "minecraft:valid_spawn",
        "minecraft:vibration_resonators",
        "minecraft:wall_corals", "minecraft:wall_hanging_signs",
        "minecraft:wall_post_override", "minecraft:wall_signs",
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
        "minecraft:axes",
        "minecraft:axolotl_food",
        "minecraft:bamboo_blocks",
        "minecraft:banners", "minecraft:beacon_payment_items", "minecraft:beds",
        "minecraft:birch_logs", "minecraft:boats",
        "minecraft:breaks_decorated_pots",
        "minecraft:buttons",
        "minecraft:candles",
        "minecraft:cherry_logs",
        "minecraft:chest_boats",
        "minecraft:cluster_max_harvestables",
        "minecraft:coals", "minecraft:coal_ores", "minecraft:copper_ores",
        "minecraft:creeper_drop_music_discs",
        "minecraft:creeper_igniters",
        "minecraft:crimson_stems",
        "minecraft:dampens_vibrations",
        "minecraft:dark_oak_logs",
        "minecraft:decorated_pot_ingredients",
        "minecraft:decorated_pot_sherds",
        "minecraft:diamond_ores",
        "minecraft:dirt",
        "minecraft:doors",
        "minecraft:emerald_ores",
        "minecraft:enchantable/armor",
        "minecraft:enchantable/bow",
        "minecraft:enchantable/chest_armor",
        "minecraft:enchantable/crossbow",
        "minecraft:enchantable/durability",
        "minecraft:enchantable/equippable",
        "minecraft:enchantable/fire_aspect",
        "minecraft:enchantable/fishing",
        "minecraft:enchantable/foot_armor",
        "minecraft:enchantable/head_armor",
        "minecraft:enchantable/leg_armor",
        "minecraft:enchantable/mace",
        "minecraft:enchantable/mining",
        "minecraft:enchantable/mining_loot",
        "minecraft:enchantable/sharp_weapon",
        "minecraft:enchantable/sword",
        "minecraft:enchantable/trident",
        "minecraft:enchantable/vanishing",
        "minecraft:enchantable/weapon",
        "minecraft:fence_gates",
        "minecraft:fences", "minecraft:fishes", "minecraft:flowers",
        "minecraft:fox_food",
        "minecraft:freeze_immune_wearables",
        "minecraft:gold_ores",
        "minecraft:hoes",
        "minecraft:ignored_by_piglin_babies",
        "minecraft:iron_ores",
        "minecraft:jungle_logs",
        "minecraft:lapis_ores",
        "minecraft:leaves", "minecraft:lectern_books", "minecraft:logs",
        "minecraft:logs_that_burn",
        "minecraft:mangrove_logs",
        "minecraft:music_discs",
        "minecraft:non_flammable_wood",
        "minecraft:noteblock_top_instruments",
        "minecraft:oak_logs",
        "minecraft:pickaxes",
        "minecraft:piglin_food", "minecraft:piglin_loved",
        "minecraft:piglin_repellents", "minecraft:planks", "minecraft:rails",
        "minecraft:redstone_ores",
        "minecraft:sand", "minecraft:saplings",
        "minecraft:shovels",
        "minecraft:signs", "minecraft:slabs",
        "minecraft:small_flowers",
        "minecraft:smelts_to_glass",
        "minecraft:sniffer_food",
        "minecraft:soul_fire_base_blocks",
        "minecraft:spruce_logs", "minecraft:stairs", "minecraft:stone_bricks",
        "minecraft:stone_buttons",
        "minecraft:stone_crafting_materials", "minecraft:stone_tool_materials",
        "minecraft:swords",
        "minecraft:tall_flowers",
        "minecraft:terracotta",
        "minecraft:trapdoors",
        "minecraft:trim_materials",
        "minecraft:trim_templates",
        "minecraft:trimmable_armor",
        "minecraft:villager_plantable_seeds",
        "minecraft:walls", "minecraft:warped_stems",
        "minecraft:wooden_buttons", "minecraft:wooden_doors", "minecraft:wooden_fences",
        "minecraft:wooden_pressure_plates", "minecraft:wooden_slabs",
        "minecraft:wooden_stairs", "minecraft:wooden_trapdoors",
        "minecraft:wool", "minecraft:wool_carpets"
    };

    private static final String[] ENTITY_TYPE_TAGS = {
        "minecraft:aquatic",
        "minecraft:arrows", "minecraft:arthropod",
        "minecraft:axolotl_always_hostiles",
        "minecraft:axolotl_hunt_targets",
        "minecraft:beehive_inhabitors",
        "minecraft:boat",
        "minecraft:can_breathe_under_water",
        "minecraft:can_turn_in_boats",
        "minecraft:deflects_projectiles",
        "minecraft:dismounts_underwater",
        "minecraft:fall_damage_immune",
        "minecraft:freeze_hurts_extra_types",
        "minecraft:freeze_immune_entity_types",
        "minecraft:frog_food",
        "minecraft:ignores_poison_and_regen",
        "minecraft:illager",
        "minecraft:illager_friends",
        "minecraft:immune_to_infested",
        "minecraft:immune_to_oozing",
        "minecraft:impact_projectiles",
        "minecraft:inverted_healing_and_harm",
        "minecraft:no_anger_from_wind_charge",
        "minecraft:non_controlling_rider",
        "minecraft:not_scary_for_pufferfish",
        "minecraft:powder_snow_walkable_mobs",
        "minecraft:raiders",
        "minecraft:redirectable_projectile",
        "minecraft:sensitive_to_bane_of_arthropods",
        "minecraft:sensitive_to_impaling",
        "minecraft:sensitive_to_smite",
        "minecraft:skeletons",
        "minecraft:undead",
        "minecraft:wither_friends",
        "minecraft:zombies"
    };

    private static final String[] DAMAGE_TYPE_TAGS = {
        "minecraft:always_hurts_ender_dragons",
        "minecraft:always_most_significant_fall",
        "minecraft:always_triggers_silverfish",
        "minecraft:avoids_guardian_thorns",
        "minecraft:burns_armor_stands",
        "minecraft:bypasses_armor",
        "minecraft:bypasses_effects",
        "minecraft:bypasses_enchantments",
        "minecraft:bypasses_invulnerability",
        "minecraft:bypasses_resistance",
        "minecraft:bypasses_shield",
        "minecraft:can_break_armor_stand",
        "minecraft:damages_helmet",
        "minecraft:ignites_armor_stands",
        "minecraft:is_drowning",
        "minecraft:is_explosion",
        "minecraft:is_fall",
        "minecraft:is_fire",
        "minecraft:is_freezing",
        "minecraft:is_lightning",
        "minecraft:is_projectile",
        "minecraft:no_anger",
        "minecraft:no_impact",
        "minecraft:witch_resistant_to",
        "minecraft:wind_charge",
        "minecraft:wither_immune_to"
    };

    private static final String[] ENCHANTMENT_TAGS = {
        "minecraft:curse",
        "minecraft:double_trade_price",
        "minecraft:exclusive_set/armor",
        "minecraft:exclusive_set/boots",
        "minecraft:exclusive_set/bow",
        "minecraft:exclusive_set/crossbow",
        "minecraft:exclusive_set/damage",
        "minecraft:exclusive_set/mining",
        "minecraft:exclusive_set/riptide",
        "minecraft:in_enchanting_table",
        "minecraft:non_treasure",
        "minecraft:on_mob_spawn_equipment",
        "minecraft:on_random_loot",
        "minecraft:on_traded_equipment",
        "minecraft:prevents_bee_spawns_when_mining",
        "minecraft:prevents_decorated_pot_shattering",
        "minecraft:prevents_ice_melting",
        "minecraft:prevents_infested_spawns",
        "minecraft:smelts_loot",
        "minecraft:tooltip_order",
        "minecraft:tradeable",
        "minecraft:treasure"
    };

    /** All worldgen/biome tags from 1.21.2 mcmeta data. All sent as empty (0 entries). */
    private static final String[] BIOME_TAGS = {
        "minecraft:allows_surface_slime_spawns",
        "minecraft:allows_tropical_fish_spawns_at_any_height",
        "minecraft:has_closer_water_fog",
        "minecraft:increased_fire_burnout",
        "minecraft:is_badlands",
        "minecraft:is_beach",
        "minecraft:is_deep_ocean",
        "minecraft:is_end",
        "minecraft:is_forest",
        "minecraft:is_hill",
        "minecraft:is_jungle",
        "minecraft:is_mountain",
        "minecraft:is_nether",
        "minecraft:is_ocean",
        "minecraft:is_overworld",
        "minecraft:is_river",
        "minecraft:is_savanna",
        "minecraft:is_taiga",
        "minecraft:mineshaft_blocking",
        "minecraft:more_frequent_drowned_spawns",
        "minecraft:plays_underwater_music",
        "minecraft:polar_bears_spawn_on_alternate_blocks",
        "minecraft:produces_corals_from_bonemeal",
        "minecraft:reduce_water_ambient_spawns",
        "minecraft:required_ocean_monument_surrounding",
        "minecraft:snow_golem_melts",
        "minecraft:spawns_cold_variant_frogs",
        "minecraft:spawns_gold_rabbits",
        "minecraft:spawns_snow_foxes",
        "minecraft:spawns_warm_variant_frogs",
        "minecraft:spawns_white_rabbits",
        "minecraft:stronghold_biased_to",
        "minecraft:water_on_map_outlines",
        "minecraft:without_patrol_spawns",
        "minecraft:without_wandering_trader_spawns",
        "minecraft:without_zombie_sieges"
    };

    @Override
    public int getPacketId() { return 0x7F; }

    @Override
    public void write(ByteBuf buf) {
        // 8 registries: block, item, fluid, entity_type, game_event, damage_type, enchantment, worldgen/biome
        McDataTypes.writeVarInt(buf, 8);

        // 1. Block tags (149, unchanged from V767)
        McDataTypes.writeVarIntString(buf, "minecraft:block");
        writeEmptyTags(buf, BLOCK_TAGS);

        // 2. Item tags (96 = V767's 78 + 18 enchantable/* tags required for enchantment parsing)
        McDataTypes.writeVarIntString(buf, "minecraft:item");
        writeEmptyTags(buf, ITEM_TAGS);

        // 3. Fluid tags (with entries, unchanged)
        McDataTypes.writeVarIntString(buf, "minecraft:fluid");
        McDataTypes.writeVarInt(buf, 2);
        McDataTypes.writeVarIntString(buf, "minecraft:water");
        McDataTypes.writeVarInt(buf, 2);
        McDataTypes.writeVarInt(buf, 1);
        McDataTypes.writeVarInt(buf, 2);
        McDataTypes.writeVarIntString(buf, "minecraft:lava");
        McDataTypes.writeVarInt(buf, 2);
        McDataTypes.writeVarInt(buf, 3);
        McDataTypes.writeVarInt(buf, 4);

        // 4. Entity type tags (35 = V767's 24 - 2 removed + 13 new for 1.21.2)
        McDataTypes.writeVarIntString(buf, "minecraft:entity_type");
        writeEmptyTags(buf, ENTITY_TYPE_TAGS);

        // 5. Game event tags (unchanged from V767)
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

        // 6. Damage type tags (26, unchanged from V767)
        McDataTypes.writeVarIntString(buf, "minecraft:damage_type");
        writeEmptyTags(buf, DAMAGE_TYPE_TAGS);

        // 7. Enchantment tags (22, unchanged from V767)
        McDataTypes.writeVarIntString(buf, "minecraft:enchantment");
        writeEmptyTags(buf, ENCHANTMENT_TAGS);

        // 8. Biome tags (36, NEW in V768 — required for enchantment built-in data parsing)
        McDataTypes.writeVarIntString(buf, "minecraft:worldgen/biome");
        writeEmptyTags(buf, BIOME_TAGS);
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
