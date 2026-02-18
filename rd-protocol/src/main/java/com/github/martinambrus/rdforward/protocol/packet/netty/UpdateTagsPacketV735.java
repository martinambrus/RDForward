package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.16 Play state, S2C packet 0x5B: Update Tags.
 *
 * Same 4-category format as v477 (blocks, items, fluids, entity_types).
 * 1.16 requires ALL tag names to be registered ("bound") or the client
 * crashes with "Tag used before it was bound". Tags can be empty (0 entries)
 * but must be present.
 *
 * Complete tag lists sourced from Minecraft 1.16.0 JAR (83 block, 55 item,
 * 2 fluid, 5 entity type tags).
 */
public class UpdateTagsPacketV735 implements Packet {

    private static final String[] BLOCK_TAGS = {
        "minecraft:acacia_logs", "minecraft:anvil", "minecraft:bamboo_plantable_on",
        "minecraft:banners", "minecraft:beacon_base_blocks", "minecraft:beds",
        "minecraft:bee_growables", "minecraft:beehives", "minecraft:birch_logs",
        "minecraft:buttons", "minecraft:campfires", "minecraft:carpets",
        "minecraft:climbable", "minecraft:coral_blocks", "minecraft:coral_plants",
        "minecraft:corals", "minecraft:crimson_stems", "minecraft:crops",
        "minecraft:dark_oak_logs", "minecraft:doors", "minecraft:dragon_immune",
        "minecraft:enderman_holdable", "minecraft:fence_gates", "minecraft:fences",
        "minecraft:fire", "minecraft:flower_pots", "minecraft:flowers",
        "minecraft:gold_ores", "minecraft:guarded_by_piglins",
        "minecraft:hoglin_repellents", "minecraft:ice", "minecraft:impermeable",
        "minecraft:infiniburn_end", "minecraft:infiniburn_nether",
        "minecraft:infiniburn_overworld", "minecraft:jungle_logs", "minecraft:leaves",
        "minecraft:logs", "minecraft:logs_that_burn", "minecraft:non_flammable_wood",
        "minecraft:nylium", "minecraft:oak_logs", "minecraft:piglin_repellents",
        "minecraft:planks", "minecraft:portals", "minecraft:pressure_plates",
        "minecraft:prevent_mob_spawning_inside", "minecraft:rails", "minecraft:sand",
        "minecraft:saplings", "minecraft:shulker_boxes", "minecraft:signs",
        "minecraft:slabs", "minecraft:small_flowers", "minecraft:soul_fire_base_blocks",
        "minecraft:soul_speed_blocks", "minecraft:spruce_logs", "minecraft:stairs",
        "minecraft:standing_signs", "minecraft:stone_bricks",
        "minecraft:stone_pressure_plates", "minecraft:strider_warm_blocks",
        "minecraft:tall_flowers", "minecraft:trapdoors", "minecraft:underwater_bonemeals",
        "minecraft:unstable_bottom_center", "minecraft:valid_spawn",
        "minecraft:wall_corals", "minecraft:wall_post_override", "minecraft:wall_signs",
        "minecraft:walls", "minecraft:warped_stems", "minecraft:wart_blocks",
        "minecraft:wither_immune", "minecraft:wither_summon_base_blocks",
        "minecraft:wooden_buttons", "minecraft:wooden_doors", "minecraft:wooden_fences",
        "minecraft:wooden_pressure_plates", "minecraft:wooden_slabs",
        "minecraft:wooden_stairs", "minecraft:wooden_trapdoors", "minecraft:wool"
    };

    private static final String[] ITEM_TAGS = {
        "minecraft:acacia_logs", "minecraft:anvil", "minecraft:arrows",
        "minecraft:banners", "minecraft:beacon_payment_items", "minecraft:beds",
        "minecraft:birch_logs", "minecraft:boats", "minecraft:buttons",
        "minecraft:carpets", "minecraft:coals", "minecraft:creeper_drop_music_discs",
        "minecraft:crimson_stems", "minecraft:dark_oak_logs", "minecraft:doors",
        "minecraft:fences", "minecraft:fishes", "minecraft:flowers",
        "minecraft:furnace_materials", "minecraft:gold_ores", "minecraft:jungle_logs",
        "minecraft:leaves", "minecraft:lectern_books", "minecraft:logs",
        "minecraft:logs_that_burn", "minecraft:music_discs",
        "minecraft:non_flammable_wood", "minecraft:oak_logs", "minecraft:piglin_loved",
        "minecraft:piglin_repellents", "minecraft:planks", "minecraft:rails",
        "minecraft:sand", "minecraft:saplings", "minecraft:signs", "minecraft:slabs",
        "minecraft:small_flowers", "minecraft:soul_fire_base_blocks",
        "minecraft:spruce_logs", "minecraft:stairs", "minecraft:stone_bricks",
        "minecraft:stone_tool_materials", "minecraft:tall_flowers",
        "minecraft:trapdoors", "minecraft:walls", "minecraft:warped_stems",
        "minecraft:wooden_buttons", "minecraft:wooden_doors", "minecraft:wooden_fences",
        "minecraft:wooden_pressure_plates", "minecraft:wooden_slabs",
        "minecraft:wooden_stairs", "minecraft:wooden_trapdoors", "minecraft:wool"
    };

    private static final String[] ENTITY_TYPE_TAGS = {
        "minecraft:arrows", "minecraft:beehive_inhabitors",
        "minecraft:impact_projectiles", "minecraft:raiders", "minecraft:skeletons"
    };

    @Override
    public int getPacketId() { return 0x5B; }

    @Override
    public void write(ByteBuf buf) {
        // Block tags (all empty — 0 entries each)
        writeEmptyTags(buf, BLOCK_TAGS);

        // Item tags (all empty)
        writeEmptyTags(buf, ITEM_TAGS);

        // Fluid tags (with entries)
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

        // Entity type tags (all empty)
        writeEmptyTags(buf, ENTITY_TYPE_TAGS);
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
