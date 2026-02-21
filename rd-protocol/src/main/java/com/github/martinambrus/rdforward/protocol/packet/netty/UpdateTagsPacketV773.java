package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.21.9 Configuration/Play state, S2C packet.
 * Configuration: 0x0D, Play: 0x84.
 *
 * Changes from V771:
 * - Block tags: +bars, +chains, +copper, +copper_chests, +copper_golem_statues,
 *   +incorrect_for_copper_tool, +lanterns, +lightning_rods, +wooden_shelves (166 total)
 * - Item tags: +bars, +chains, +copper, +copper_chests, +copper_golem_statues,
 *   +copper_tool_materials, +lanterns, +lightning_rods, +repairs_copper_armor,
 *   +shearable_from_copper_golem, +wooden_shelves (115 total)
 * - Entity type tags: +accepts_iron_golem_gift, +candidate_for_iron_golem_gift,
 *   +cannot_be_pushed_onto_boats (42 total)
 * - Fluid, game_event, damage_type, enchantment, biome, dialog: unchanged from V771.
 */
public class UpdateTagsPacketV773 implements Packet {

    private static final String[] BLOCK_TAGS;
    static {
        String[] base = UpdateTagsPacketV771.getBlockTags();
        String[] additions = {
            "minecraft:bars",
            "minecraft:chains",
            "minecraft:copper",
            "minecraft:copper_chests",
            "minecraft:copper_golem_statues",
            "minecraft:incorrect_for_copper_tool",
            "minecraft:lanterns",
            "minecraft:lightning_rods",
            "minecraft:wooden_shelves"
        };
        BLOCK_TAGS = new String[base.length + additions.length];
        System.arraycopy(base, 0, BLOCK_TAGS, 0, base.length);
        System.arraycopy(additions, 0, BLOCK_TAGS, base.length, additions.length);
    }

    private static final String[] ITEM_TAGS;
    static {
        String[] base = UpdateTagsPacketV771.getItemTags();
        String[] additions = {
            "minecraft:bars",
            "minecraft:chains",
            "minecraft:copper",
            "minecraft:copper_chests",
            "minecraft:copper_golem_statues",
            "minecraft:copper_tool_materials",
            "minecraft:lanterns",
            "minecraft:lightning_rods",
            "minecraft:repairs_copper_armor",
            "minecraft:shearable_from_copper_golem",
            "minecraft:wooden_shelves"
        };
        ITEM_TAGS = new String[base.length + additions.length];
        System.arraycopy(base, 0, ITEM_TAGS, 0, base.length);
        System.arraycopy(additions, 0, ITEM_TAGS, base.length, additions.length);
    }

    private static final String[] ENTITY_TYPE_TAGS;
    static {
        String[] base = UpdateTagsPacketV771.getEntityTypeTags();
        String[] additions = {
            "minecraft:accepts_iron_golem_gift",
            "minecraft:candidate_for_iron_golem_gift",
            "minecraft:cannot_be_pushed_onto_boats"
        };
        ENTITY_TYPE_TAGS = new String[base.length + additions.length];
        System.arraycopy(base, 0, ENTITY_TYPE_TAGS, 0, base.length);
        System.arraycopy(additions, 0, ENTITY_TYPE_TAGS, base.length, additions.length);
    }

    // Reuse V771's unchanged tag arrays
    private static final String[] DAMAGE_TYPE_TAGS = UpdateTagsPacketV768.getDamageTypeTags();
    private static final String[] ENCHANTMENT_TAGS = UpdateTagsPacketV768.getEnchantmentTags();
    private static final String[] BIOME_TAGS = UpdateTagsPacketV770.getBiomeTags();

    @Override
    public int getPacketId() { return 0x84; }

    @Override
    public void write(ByteBuf buf) {
        // 9 registries: block, item, fluid, entity_type, game_event, damage_type, enchantment, worldgen/biome, dialog
        McDataTypes.writeVarInt(buf, 9);

        // 1. Block tags
        McDataTypes.writeVarIntString(buf, "minecraft:block");
        writeEmptyTags(buf, BLOCK_TAGS);

        // 2. Item tags
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

        // 4. Entity type tags
        McDataTypes.writeVarIntString(buf, "minecraft:entity_type");
        writeEmptyTags(buf, ENTITY_TYPE_TAGS);

        // 5. Game event tags (unchanged from V771)
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

        // 6. Damage type tags (unchanged from V771)
        McDataTypes.writeVarIntString(buf, "minecraft:damage_type");
        writeEmptyTags(buf, DAMAGE_TYPE_TAGS);

        // 7. Enchantment tags (unchanged from V771)
        McDataTypes.writeVarIntString(buf, "minecraft:enchantment");
        writeEmptyTags(buf, ENCHANTMENT_TAGS);

        // 8. Biome tags (unchanged from V771)
        McDataTypes.writeVarIntString(buf, "minecraft:worldgen/biome");
        writeEmptyTags(buf, BIOME_TAGS);

        // 9. Dialog tags (unchanged from V771)
        McDataTypes.writeVarIntString(buf, "minecraft:dialog");
        McDataTypes.writeVarInt(buf, 2);
        McDataTypes.writeVarIntString(buf, "minecraft:pause_screen_additions");
        McDataTypes.writeVarInt(buf, 0);
        McDataTypes.writeVarIntString(buf, "minecraft:quick_actions");
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
