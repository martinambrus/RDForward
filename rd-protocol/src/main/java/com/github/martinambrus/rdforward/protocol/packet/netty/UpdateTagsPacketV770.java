package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.21.5 Configuration/Play state, S2C packet.
 * Configuration: 0x0D, Play: 0x7F.
 *
 * Changes from V769:
 * - Block tags: +camels_spawnable_on, +edible_for_sheep, +plays_ambient_desert_block_sounds,
 *   +replaceable_by_mushrooms, +sword_instantly_mines (154 total, was 149)
 * - Item tags: +book_cloning_target, +eggs, +flowers (101 total, was 98)
 * - Entity type tags: +can_equip_saddle, +can_wear_horse_armor (37 total, was 35)
 * - Biome tags: +spawns_cold_variant_farm_animals, +spawns_warm_variant_farm_animals
 *   (38 total, was 36)
 * - Fluid, game_event, damage_type, enchantment: unchanged from V769.
 */
public class UpdateTagsPacketV770 implements Packet {

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
        "minecraft:bee_attractive",
        "minecraft:bee_growables", "minecraft:beehives",
        "minecraft:big_dripleaf_placeable", "minecraft:birch_logs",
        "minecraft:blocks_wind_charge_explosions",
        "minecraft:buttons",
        "minecraft:camel_sand_step_sound_blocks",
        "minecraft:camels_spawnable_on",
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
        "minecraft:edible_for_sheep",
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
        "minecraft:plays_ambient_desert_block_sounds",
        "minecraft:polar_bears_spawnable_on_alternate",
        "minecraft:portals", "minecraft:pressure_plates",
        "minecraft:prevent_mob_spawning_inside",
        "minecraft:rabbits_spawnable_on",
        "minecraft:rails",
        "minecraft:redstone_ores",
        "minecraft:replaceable",
        "minecraft:replaceable_by_mushrooms",
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
        "minecraft:sword_instantly_mines",
        "minecraft:terracotta",
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
        "minecraft:birch_logs",
        "minecraft:boats",
        "minecraft:book_cloning_target",
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
        "minecraft:drowned_preferred_weapons",
        "minecraft:eggs",
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
        "minecraft:fences", "minecraft:fishes",
        "minecraft:flowers",
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
        "minecraft:piglin_preferred_weapons",
        "minecraft:piglin_repellents", "minecraft:planks", "minecraft:rails",
        "minecraft:pillager_preferred_weapons",
        "minecraft:redstone_ores",
        "minecraft:sand", "minecraft:saplings",
        "minecraft:skeleton_preferred_weapons",
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
        "minecraft:terracotta",
        "minecraft:trapdoors",
        "minecraft:trim_materials",
        "minecraft:trimmable_armor",
        "minecraft:villager_plantable_seeds",
        "minecraft:walls", "minecraft:warped_stems",
        "minecraft:wither_skeleton_disliked_weapons",
        "minecraft:wooden_buttons", "minecraft:wooden_doors", "minecraft:wooden_fences",
        "minecraft:wooden_pressure_plates", "minecraft:wooden_slabs",
        "minecraft:wooden_stairs", "minecraft:wooden_trapdoors",
        "minecraft:wool", "minecraft:wool_carpets"
    };

    private static final String[] ENTITY_TYPE_TAGS;
    static {
        // Start with V768's entity type tags, add 2 new ones
        String[] base = UpdateTagsPacketV768.getEntityTypeTags();
        ENTITY_TYPE_TAGS = new String[base.length + 2];
        System.arraycopy(base, 0, ENTITY_TYPE_TAGS, 0, base.length);
        ENTITY_TYPE_TAGS[base.length] = "minecraft:can_equip_saddle";
        ENTITY_TYPE_TAGS[base.length + 1] = "minecraft:can_wear_horse_armor";
    }

    private static final String[] BIOME_TAGS;
    static {
        // Start with V768's biome tags, add 2 new ones
        String[] base = UpdateTagsPacketV768.getBiomeTags();
        BIOME_TAGS = new String[base.length + 2];
        System.arraycopy(base, 0, BIOME_TAGS, 0, base.length);
        BIOME_TAGS[base.length] = "minecraft:spawns_cold_variant_farm_animals";
        BIOME_TAGS[base.length + 1] = "minecraft:spawns_warm_variant_farm_animals";
    }

    // Reuse V768's unchanged tag arrays
    private static final String[] DAMAGE_TYPE_TAGS = UpdateTagsPacketV768.getDamageTypeTags();
    private static final String[] ENCHANTMENT_TAGS = UpdateTagsPacketV768.getEnchantmentTags();

    @Override
    public int getPacketId() { return 0x7F; }

    @Override
    public void write(ByteBuf buf) {
        // 8 registries: block, item, fluid, entity_type, game_event, damage_type, enchantment, worldgen/biome
        McDataTypes.writeVarInt(buf, 8);

        // 1. Block tags (154: +5 new vs V769)
        McDataTypes.writeVarIntString(buf, "minecraft:block");
        writeEmptyTags(buf, BLOCK_TAGS);

        // 2. Item tags (101: +3 new vs V769)
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

        // 4. Entity type tags (37: +2 new vs V769)
        McDataTypes.writeVarIntString(buf, "minecraft:entity_type");
        writeEmptyTags(buf, ENTITY_TYPE_TAGS);

        // 5. Game event tags (unchanged from V768)
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

        // 6. Damage type tags (26, unchanged from V769)
        McDataTypes.writeVarIntString(buf, "minecraft:damage_type");
        writeEmptyTags(buf, DAMAGE_TYPE_TAGS);

        // 7. Enchantment tags (22, unchanged from V769)
        McDataTypes.writeVarIntString(buf, "minecraft:enchantment");
        writeEmptyTags(buf, ENCHANTMENT_TAGS);

        // 8. Biome tags (38: +2 new vs V769)
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
