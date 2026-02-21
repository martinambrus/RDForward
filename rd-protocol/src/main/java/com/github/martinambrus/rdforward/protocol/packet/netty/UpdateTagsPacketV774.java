package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 1.21.11 Configuration/Play state, S2C packet.
 * Configuration: 0x0D, Play: 0x84.
 *
 * Changes from V773:
 * - Block tags: +can_glide_through (167 total)
 * - Item tags: -enchantable/sword, +camel_husk_food, +enchantable/lunge,
 *   +enchantable/melee_weapon, +enchantable/sweeping, +nautilus_bucket_food,
 *   +nautilus_food, +nautilus_taming_items, +spears, +zombie_horse_food (123 total)
 * - Entity type tags: +burn_in_daylight, +can_float_while_ridden,
 *   +can_wear_nautilus_armor, +nautilus_hostiles (46 total)
 * - Biome tags: -has_closer_water_fog, -increased_fire_burnout,
 *   -plays_underwater_music, -snow_golem_melts, -without_patrol_spawns,
 *   +spawns_coral_variant_zombie_nautilus (34 total)
 * - New timeline tag registry: universal, in_overworld, in_nether, in_end
 * - Fluid, game_event, damage_type, enchantment, dialog: unchanged from V773.
 */
public class UpdateTagsPacketV774 implements Packet {

    private static final String[] BLOCK_TAGS;
    static {
        String[] base = UpdateTagsPacketV773.getBlockTags();
        String[] additions = {
            "minecraft:can_glide_through"
        };
        BLOCK_TAGS = new String[base.length + additions.length];
        System.arraycopy(base, 0, BLOCK_TAGS, 0, base.length);
        System.arraycopy(additions, 0, BLOCK_TAGS, base.length, additions.length);
    }

    private static final String[] ITEM_TAGS;
    static {
        // Remove enchantable/sword, add 9 new tags
        String[] base = UpdateTagsPacketV773.getItemTags();
        List<String> list = new ArrayList<>(Arrays.asList(base));
        list.remove("minecraft:enchantable/sword");
        list.add("minecraft:camel_husk_food");
        list.add("minecraft:enchantable/lunge");
        list.add("minecraft:enchantable/melee_weapon");
        list.add("minecraft:enchantable/sweeping");
        list.add("minecraft:nautilus_bucket_food");
        list.add("minecraft:nautilus_food");
        list.add("minecraft:nautilus_taming_items");
        list.add("minecraft:spears");
        list.add("minecraft:zombie_horse_food");
        ITEM_TAGS = list.toArray(new String[0]);
    }

    private static final String[] ENTITY_TYPE_TAGS;
    static {
        String[] base = UpdateTagsPacketV773.getEntityTypeTags();
        String[] additions = {
            "minecraft:burn_in_daylight",
            "minecraft:can_float_while_ridden",
            "minecraft:can_wear_nautilus_armor",
            "minecraft:nautilus_hostiles"
        };
        ENTITY_TYPE_TAGS = new String[base.length + additions.length];
        System.arraycopy(base, 0, ENTITY_TYPE_TAGS, 0, base.length);
        System.arraycopy(additions, 0, ENTITY_TYPE_TAGS, base.length, additions.length);
    }

    private static final String[] BIOME_TAGS;
    static {
        // Remove 5 tags that moved to the Environment Attributes system, add 1 new
        String[] base = UpdateTagsPacketV770.getBiomeTags();
        List<String> list = new ArrayList<>(Arrays.asList(base));
        list.remove("minecraft:has_closer_water_fog");
        list.remove("minecraft:increased_fire_burnout");
        list.remove("minecraft:plays_underwater_music");
        list.remove("minecraft:snow_golem_melts");
        list.remove("minecraft:without_patrol_spawns");
        list.add("minecraft:spawns_coral_variant_zombie_nautilus");
        BIOME_TAGS = list.toArray(new String[0]);
    }

    private static final String[] TIMELINE_TAGS = {
        "minecraft:universal",
        "minecraft:in_overworld",
        "minecraft:in_nether",
        "minecraft:in_end"
    };

    // Reuse V773/V768's unchanged tag arrays
    private static final String[] DAMAGE_TYPE_TAGS = UpdateTagsPacketV768.getDamageTypeTags();
    private static final String[] ENCHANTMENT_TAGS = UpdateTagsPacketV768.getEnchantmentTags();

    @Override
    public int getPacketId() { return 0x84; }

    @Override
    public void write(ByteBuf buf) {
        // 10 registries: block, item, fluid, entity_type, game_event, damage_type, enchantment, worldgen/biome, dialog, timeline
        McDataTypes.writeVarInt(buf, 10);

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

        // 5. Game event tags (unchanged from V773)
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

        // 6. Damage type tags (unchanged from V773)
        McDataTypes.writeVarIntString(buf, "minecraft:damage_type");
        writeEmptyTags(buf, DAMAGE_TYPE_TAGS);

        // 7. Enchantment tags (unchanged from V773)
        McDataTypes.writeVarIntString(buf, "minecraft:enchantment");
        writeEmptyTags(buf, ENCHANTMENT_TAGS);

        // 8. Biome tags (5 removed, 1 added vs V773)
        McDataTypes.writeVarIntString(buf, "minecraft:worldgen/biome");
        writeEmptyTags(buf, BIOME_TAGS);

        // 9. Dialog tags (unchanged from V773)
        McDataTypes.writeVarIntString(buf, "minecraft:dialog");
        McDataTypes.writeVarInt(buf, 2);
        McDataTypes.writeVarIntString(buf, "minecraft:pause_screen_additions");
        McDataTypes.writeVarInt(buf, 0);
        McDataTypes.writeVarIntString(buf, "minecraft:quick_actions");
        McDataTypes.writeVarInt(buf, 0);

        // 10. Timeline tags (new in V774)
        McDataTypes.writeVarIntString(buf, "minecraft:timeline");
        writeEmptyTags(buf, TIMELINE_TAGS);
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
