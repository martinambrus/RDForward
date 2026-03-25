package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 26.1 Configuration/Play state, S2C packet.
 * Configuration: 0x0D, Play: 0x84.
 *
 * Changes from V774:
 * - Block tags: -bamboo_plantable_on, -big_dripleaf_placeable, -dry_vegetation_may_place_on,
 *   -mushroom_grow_block, -small_dripleaf_placeable, -snow_layer_can_survive_on,
 *   -snow_layer_cannot_survive_on; +51 new tags (supports_* family, substrate_overworld,
 *   prevents_nearby_leaf_decay, grass_blocks, moss_blocks, mud, etc.)
 * - Item tags: -dyeable; +cat_collar_dyes, +cauldron_can_remove_dye, +dyes, +grass_blocks,
 *   +loom_dyes, +loom_patterns, +metal_nuggets, +moss_blocks, +mud, +wolf_collar_dyes
 * - Entity type tags: +cannot_be_age_locked
 * - Fluid tags: +bubble_column_can_occupy, +supports_frogspawn, +supports_lily_pad,
 *   +supports_sugar_cane_adjacently (6 total vs 2)
 * - Biome, game_event, damage_type, enchantment, dialog, timeline: unchanged from V774.
 */
public class UpdateTagsPacketV775 implements Packet {

    public static final UpdateTagsPacketV775 INSTANCE = new UpdateTagsPacketV775();

    private static final String[] BLOCK_TAGS;
    static {
        String[] base = UpdateTagsPacketV774.getBlockTags();
        List<String> list = new ArrayList<>(Arrays.asList(base));
        // Remove 7 tags replaced by supports_* equivalents
        list.remove("minecraft:bamboo_plantable_on");
        list.remove("minecraft:big_dripleaf_placeable");
        list.remove("minecraft:dry_vegetation_may_place_on");
        list.remove("minecraft:mushroom_grow_block");
        list.remove("minecraft:small_dripleaf_placeable");
        list.remove("minecraft:snow_layer_can_survive_on");
        list.remove("minecraft:snow_layer_cannot_survive_on");
        // Add 51 new tags
        String[] additions = {
            "minecraft:beneath_bamboo_podzol_replaceable",
            "minecraft:beneath_tree_podzol_replaceable",
            "minecraft:cannot_replace_below_tree_trunk",
            "minecraft:cannot_support_kelp",
            "minecraft:cannot_support_seagrass",
            "minecraft:cannot_support_snow_layer",
            "minecraft:enables_bubble_column_drag_down",
            "minecraft:enables_bubble_column_push_up",
            "minecraft:forest_rock_can_place_on",
            "minecraft:grass_blocks",
            "minecraft:grows_crops",
            "minecraft:huge_brown_mushroom_can_place_on",
            "minecraft:huge_red_mushroom_can_place_on",
            "minecraft:ice_spike_replaceable",
            "minecraft:moss_blocks",
            "minecraft:mud",
            "minecraft:overrides_mushroom_light_requirement",
            "minecraft:prevents_nearby_leaf_decay",
            "minecraft:substrate_overworld",
            "minecraft:support_override_cactus_flower",
            "minecraft:support_override_snow_layer",
            "minecraft:supports_azalea",
            "minecraft:supports_bamboo",
            "minecraft:supports_big_dripleaf",
            "minecraft:supports_cactus",
            "minecraft:supports_chorus_flower",
            "minecraft:supports_chorus_plant",
            "minecraft:supports_cocoa",
            "minecraft:supports_crimson_fungus",
            "minecraft:supports_crimson_roots",
            "minecraft:supports_crops",
            "minecraft:supports_dry_vegetation",
            "minecraft:supports_frogspawn",
            "minecraft:supports_hanging_mangrove_propagule",
            "minecraft:supports_lily_pad",
            "minecraft:supports_mangrove_propagule",
            "minecraft:supports_melon_stem",
            "minecraft:supports_melon_stem_fruit",
            "minecraft:supports_nether_sprouts",
            "minecraft:supports_nether_wart",
            "minecraft:supports_pumpkin_stem",
            "minecraft:supports_pumpkin_stem_fruit",
            "minecraft:supports_small_dripleaf",
            "minecraft:supports_stem_crops",
            "minecraft:supports_stem_fruit",
            "minecraft:supports_sugar_cane",
            "minecraft:supports_sugar_cane_adjacently",
            "minecraft:supports_vegetation",
            "minecraft:supports_warped_fungus",
            "minecraft:supports_warped_roots",
            "minecraft:supports_wither_rose"
        };
        for (String tag : additions) {
            list.add(tag);
        }
        BLOCK_TAGS = list.toArray(new String[0]);
    }

    private static final String[] ITEM_TAGS;
    static {
        String[] base = UpdateTagsPacketV774.getItemTags();
        List<String> list = new ArrayList<>(Arrays.asList(base));
        list.remove("minecraft:dyeable");
        list.add("minecraft:cat_collar_dyes");
        list.add("minecraft:cauldron_can_remove_dye");
        list.add("minecraft:dyes");
        list.add("minecraft:grass_blocks");
        list.add("minecraft:loom_dyes");
        list.add("minecraft:loom_patterns");
        list.add("minecraft:metal_nuggets");
        list.add("minecraft:moss_blocks");
        list.add("minecraft:mud");
        list.add("minecraft:wolf_collar_dyes");
        ITEM_TAGS = list.toArray(new String[0]);
    }

    private static final String[] ENTITY_TYPE_TAGS;
    static {
        String[] base = UpdateTagsPacketV774.getEntityTypeTags();
        String[] additions = {
            "minecraft:cannot_be_age_locked"
        };
        ENTITY_TYPE_TAGS = new String[base.length + additions.length];
        System.arraycopy(base, 0, ENTITY_TYPE_TAGS, 0, base.length);
        System.arraycopy(additions, 0, ENTITY_TYPE_TAGS, base.length, additions.length);
    }

    // Reuse V774's unchanged tag arrays
    private static final String[] BIOME_TAGS = UpdateTagsPacketV774.getBiomeTags();
    private static final String[] TIMELINE_TAGS = UpdateTagsPacketV774.getTimelineTags();
    private static final String[] DAMAGE_TYPE_TAGS = UpdateTagsPacketV768.getDamageTypeTags();
    private static final String[] ENCHANTMENT_TAGS = UpdateTagsPacketV768.getEnchantmentTags();

    @Override
    public int getPacketId() { return 0x84; }

    private static final String[] BANNER_PATTERN_TAGS = {
        "minecraft:no_item_required",
        "minecraft:pattern_item/bordure_indented",
        "minecraft:pattern_item/creeper",
        "minecraft:pattern_item/field_masoned",
        "minecraft:pattern_item/flow",
        "minecraft:pattern_item/flower",
        "minecraft:pattern_item/globe",
        "minecraft:pattern_item/guster",
        "minecraft:pattern_item/mojang",
        "minecraft:pattern_item/piglin",
        "minecraft:pattern_item/skull"
    };

    private static final String[] INSTRUMENT_TAGS = {
        "minecraft:goat_horns",
        "minecraft:regular_goat_horns",
        "minecraft:screaming_goat_horns"
    };

    private static final String[] PAINTING_VARIANT_TAGS = {
        "minecraft:placeable"
    };

    private static final byte[] SERIALIZED;
    static {
        ByteBuf tmp = Unpooled.buffer();
        serializePayload(tmp);
        SERIALIZED = new byte[tmp.readableBytes()];
        tmp.readBytes(SERIALIZED);
        tmp.release();
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeBytes(SERIALIZED);
    }

    private static void serializePayload(ByteBuf buf) {
        // 13 registries: 10 from V774 + 3 new in 26.1 (banner_pattern, instrument,
        // painting_variant). Tags for point_of_interest_type, potion, villager_trade
        // omitted since those registries aren't sent via RegistryData.
        McDataTypes.writeVarInt(buf, 13);

        // 1. Block tags
        McDataTypes.writeVarIntString(buf, "minecraft:block");
        writeEmptyTags(buf, BLOCK_TAGS);

        // 2. Item tags
        McDataTypes.writeVarIntString(buf, "minecraft:item");
        writeEmptyTags(buf, ITEM_TAGS);

        // 3. Fluid tags (expanded in 26.1: 6 tags vs 2)
        McDataTypes.writeVarIntString(buf, "minecraft:fluid");
        McDataTypes.writeVarInt(buf, 6);
        McDataTypes.writeVarIntString(buf, "minecraft:water");
        McDataTypes.writeVarInt(buf, 2);
        McDataTypes.writeVarInt(buf, 1);
        McDataTypes.writeVarInt(buf, 2);
        McDataTypes.writeVarIntString(buf, "minecraft:lava");
        McDataTypes.writeVarInt(buf, 2);
        McDataTypes.writeVarInt(buf, 3);
        McDataTypes.writeVarInt(buf, 4);
        McDataTypes.writeVarIntString(buf, "minecraft:bubble_column_can_occupy");
        McDataTypes.writeVarInt(buf, 0);
        McDataTypes.writeVarIntString(buf, "minecraft:supports_frogspawn");
        McDataTypes.writeVarInt(buf, 0);
        McDataTypes.writeVarIntString(buf, "minecraft:supports_lily_pad");
        McDataTypes.writeVarInt(buf, 0);
        McDataTypes.writeVarIntString(buf, "minecraft:supports_sugar_cane_adjacently");
        McDataTypes.writeVarInt(buf, 0);

        // 4. Entity type tags
        McDataTypes.writeVarIntString(buf, "minecraft:entity_type");
        writeEmptyTags(buf, ENTITY_TYPE_TAGS);

        // 5. Game event tags (unchanged from V774)
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

        // 6. Damage type tags (unchanged from V774)
        McDataTypes.writeVarIntString(buf, "minecraft:damage_type");
        writeEmptyTags(buf, DAMAGE_TYPE_TAGS);

        // 7. Enchantment tags (unchanged from V774)
        McDataTypes.writeVarIntString(buf, "minecraft:enchantment");
        writeEmptyTags(buf, ENCHANTMENT_TAGS);

        // 8. Biome tags (unchanged from V774)
        McDataTypes.writeVarIntString(buf, "minecraft:worldgen/biome");
        writeEmptyTags(buf, BIOME_TAGS);

        // 9. Dialog tags (unchanged from V774)
        McDataTypes.writeVarIntString(buf, "minecraft:dialog");
        McDataTypes.writeVarInt(buf, 2);
        McDataTypes.writeVarIntString(buf, "minecraft:pause_screen_additions");
        McDataTypes.writeVarInt(buf, 0);
        McDataTypes.writeVarIntString(buf, "minecraft:quick_actions");
        McDataTypes.writeVarInt(buf, 0);

        // 10. Timeline tags (unchanged from V774)
        McDataTypes.writeVarIntString(buf, "minecraft:timeline");
        writeEmptyTags(buf, TIMELINE_TAGS);

        // 11-16. New tag registries in 26.1
        McDataTypes.writeVarIntString(buf, "minecraft:banner_pattern");
        writeEmptyTags(buf, BANNER_PATTERN_TAGS);

        McDataTypes.writeVarIntString(buf, "minecraft:instrument");
        writeEmptyTags(buf, INSTRUMENT_TAGS);

        McDataTypes.writeVarIntString(buf, "minecraft:painting_variant");
        writeEmptyTags(buf, PAINTING_VARIANT_TAGS);
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
