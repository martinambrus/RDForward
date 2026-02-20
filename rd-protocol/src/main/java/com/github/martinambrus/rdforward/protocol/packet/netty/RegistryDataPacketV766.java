package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * 1.20.5 Configuration state, S2C packet 0x07: Registry Data.
 *
 * Wire format:
 *   String      registryKey
 *   VarInt      entryCount
 *   Per entry:
 *     String    entryId
 *     Boolean   hasData
 *     [Tag]     element data if hasData (network NBT: tagType + content, no root name)
 *
 * The Optional uses ByteBufCodecs::optional — boolean prefix + unnamed Tag.
 * Absent = boolean(false). Present = boolean(true) + byte(0x0A) + compound + byte(0x00).
 */
public class RegistryDataPacketV766 implements Packet {

    private ByteBuf prebuiltData;

    public RegistryDataPacketV766() {}

    public RegistryDataPacketV766(ByteBuf prebuiltData) {
        this.prebuiltData = prebuiltData;
    }

    @Override
    public int getPacketId() { return 0x07; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeBytes(prebuiltData, prebuiltData.readerIndex(), prebuiltData.readableBytes());
        prebuiltData.release();
    }

    @Override
    public void read(ByteBuf buf) {
        // S2C only — no server-side decoding needed
    }

    // ========================================================================
    // Factory methods — one per registry
    // ========================================================================

    public static RegistryDataPacketV766 createDimensionType(ByteBuf alloc) {
        ByteBuf data = alloc.alloc().buffer();

        McDataTypes.writeVarIntString(data, "minecraft:dimension_type");
        McDataTypes.writeVarInt(data, 4); // 4 entries

        // Entry 0: overworld (has data — custom height/minY)
        McDataTypes.writeVarIntString(data, "minecraft:overworld");
        data.writeBoolean(true);
        data.writeByte(0x0A); // network NBT compound (no root name)
        writeDimensionElement(data, false, true, false, 0.0f,
                "#minecraft:infiniburn_overworld", false, true, true, true,
                256, false, "minecraft:overworld", 0, 256, null);
        data.writeByte(0x00); // end compound

        // Entry 1: overworld_caves (no data — use built-in)
        McDataTypes.writeVarIntString(data, "minecraft:overworld_caves");
        data.writeBoolean(false);

        // Entry 2: the_nether (no data — use built-in)
        McDataTypes.writeVarIntString(data, "minecraft:the_nether");
        data.writeBoolean(false);

        // Entry 3: the_end (no data — use built-in)
        McDataTypes.writeVarIntString(data, "minecraft:the_end");
        data.writeBoolean(false);

        return new RegistryDataPacketV766(data);
    }

    public static RegistryDataPacketV766 createBiome(ByteBuf alloc) {
        ByteBuf data = alloc.alloc().buffer();

        McDataTypes.writeVarIntString(data, "minecraft:worldgen/biome");
        McDataTypes.writeVarInt(data, 1); // 1 entry

        // Entry 0: plains (has data)
        McDataTypes.writeVarIntString(data, "minecraft:plains");
        data.writeBoolean(true);
        data.writeByte(0x0A); // network NBT compound
        writeByteTag(data, "has_precipitation", (byte) 1);
        writeFloatTag(data, "temperature", 0.8f);
        writeFloatTag(data, "downfall", 0.4f);
        writeCompoundTagHeader(data, "effects");
        writeIntTag(data, "sky_color", 7907327);
        writeIntTag(data, "water_fog_color", 329011);
        writeIntTag(data, "fog_color", 12638463);
        writeIntTag(data, "water_color", 4159204);
        writeCompoundTagHeader(data, "mood_sound");
        writeStringTag(data, "sound", "minecraft:ambient.cave");
        writeIntTag(data, "tick_delay", 6000);
        writeDoubleTag(data, "offset", 2.0);
        writeIntTag(data, "block_search_extent", 8);
        data.writeByte(0x00); // end mood_sound
        data.writeByte(0x00); // end effects
        data.writeByte(0x00); // end element compound

        return new RegistryDataPacketV766(data);
    }

    /**
     * Creates a registry where ALL entries use built-in data (hasData=false).
     * Used when the client confirmed a known pack, so it already has the data.
     */
    public static RegistryDataPacketV766 createBuiltIn(ByteBuf alloc,
            String registryKey, String... entryIds) {
        ByteBuf data = alloc.alloc().buffer();
        McDataTypes.writeVarIntString(data, registryKey);
        McDataTypes.writeVarInt(data, entryIds.length);
        for (String id : entryIds) {
            McDataTypes.writeVarIntString(data, id);
            data.writeBoolean(false);
        }
        return new RegistryDataPacketV766(data);
    }

    public static RegistryDataPacketV766 createChatType(ByteBuf alloc) {
        return createBuiltIn(alloc, "minecraft:chat_type",
                "minecraft:chat", "minecraft:say_command",
                "minecraft:msg_command_incoming", "minecraft:msg_command_outgoing",
                "minecraft:team_msg_command_incoming", "minecraft:team_msg_command_outgoing",
                "minecraft:emote_command");
    }

    public static RegistryDataPacketV766 createDamageType(ByteBuf alloc) {
        return createBuiltIn(alloc, "minecraft:damage_type",
                "minecraft:arrow", "minecraft:bad_respawn_point", "minecraft:cactus",
                "minecraft:cramming", "minecraft:dragon_breath", "minecraft:drown",
                "minecraft:dry_out", "minecraft:explosion", "minecraft:fall",
                "minecraft:falling_anvil", "minecraft:falling_block",
                "minecraft:falling_stalactite", "minecraft:fireball",
                "minecraft:fireworks", "minecraft:fly_into_wall", "minecraft:freeze",
                "minecraft:generic", "minecraft:generic_kill", "minecraft:hot_floor",
                "minecraft:in_fire", "minecraft:in_wall", "minecraft:indirect_magic",
                "minecraft:lava", "minecraft:lightning_bolt", "minecraft:magic",
                "minecraft:mob_attack", "minecraft:mob_attack_no_aggro",
                "minecraft:mob_projectile", "minecraft:on_fire",
                "minecraft:out_of_world", "minecraft:outside_border",
                "minecraft:player_attack", "minecraft:player_explosion",
                "minecraft:sonic_boom", "minecraft:spit", "minecraft:stalagmite",
                "minecraft:starve", "minecraft:sting", "minecraft:sweet_berry_bush",
                "minecraft:thorns", "minecraft:thrown", "minecraft:trident",
                "minecraft:unattributed_fireball", "minecraft:wither",
                "minecraft:wither_skull");
    }

    public static RegistryDataPacketV766 createTrimPattern(ByteBuf alloc) {
        return createBuiltIn(alloc, "minecraft:trim_pattern",
                "minecraft:coast", "minecraft:dune", "minecraft:eye",
                "minecraft:host", "minecraft:raiser", "minecraft:rib",
                "minecraft:sentry", "minecraft:shaper", "minecraft:silence",
                "minecraft:snout", "minecraft:spire", "minecraft:tide",
                "minecraft:vex", "minecraft:ward", "minecraft:wayfinder",
                "minecraft:wild");
    }

    public static RegistryDataPacketV766 createTrimPatternV767(ByteBuf alloc) {
        return createBuiltIn(alloc, "minecraft:trim_pattern",
                "minecraft:coast", "minecraft:dune", "minecraft:eye",
                "minecraft:host", "minecraft:raiser", "minecraft:rib",
                "minecraft:sentry", "minecraft:shaper", "minecraft:silence",
                "minecraft:snout", "minecraft:spire", "minecraft:tide",
                "minecraft:vex", "minecraft:ward", "minecraft:wayfinder",
                "minecraft:wild",
                "minecraft:bolt", "minecraft:flow");
    }

    public static RegistryDataPacketV766 createTrimMaterial(ByteBuf alloc) {
        return createBuiltIn(alloc, "minecraft:trim_material",
                "minecraft:amethyst", "minecraft:copper", "minecraft:diamond",
                "minecraft:emerald", "minecraft:gold", "minecraft:iron",
                "minecraft:lapis", "minecraft:netherite", "minecraft:quartz",
                "minecraft:redstone");
    }

    /**
     * Creates wolf_variant registry matching ViaVersion's approach:
     * single pale wolf entry with empty biomes list.
     */
    public static RegistryDataPacketV766 createWolfVariant(ByteBuf alloc) {
        ByteBuf data = alloc.alloc().buffer();
        McDataTypes.writeVarIntString(data, "minecraft:wolf_variant");
        McDataTypes.writeVarInt(data, 1); // 1 entry

        McDataTypes.writeVarIntString(data, "minecraft:pale");
        data.writeBoolean(true);
        data.writeByte(0x0A); // network NBT compound
        writeStringTag(data, "wild_texture", "entity/wolf/wolf");
        writeStringTag(data, "tame_texture", "entity/wolf/wolf_tame");
        writeStringTag(data, "angry_texture", "entity/wolf/wolf_angry");
        // biomes: empty ListTag (no biome restrictions)
        writeEmptyStringListTag(data, "biomes");
        data.writeByte(0x00); // end compound

        return new RegistryDataPacketV766(data);
    }

    /** Writes a named ListTag of type String with zero elements. */
    private static void writeEmptyStringListTag(ByteBuf buf, String name) {
        buf.writeByte(0x09); // ListTag type
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(nameBytes.length);
        buf.writeBytes(nameBytes);
        buf.writeByte(0x08); // element type = String
        buf.writeInt(0);     // 0 elements
    }

    public static RegistryDataPacketV766 createBannerPattern(ByteBuf alloc) {
        return createBuiltIn(alloc, "minecraft:banner_pattern",
                "minecraft:base", "minecraft:square_bottom_left",
                "minecraft:square_bottom_right", "minecraft:square_top_left",
                "minecraft:square_top_right", "minecraft:stripe_bottom",
                "minecraft:stripe_top", "minecraft:stripe_left",
                "minecraft:stripe_right", "minecraft:stripe_center",
                "minecraft:stripe_middle", "minecraft:stripe_downright",
                "minecraft:stripe_downleft", "minecraft:small_stripes",
                "minecraft:cross", "minecraft:straight_cross",
                "minecraft:triangle_bottom", "minecraft:triangle_top",
                "minecraft:triangles_bottom", "minecraft:triangles_top",
                "minecraft:diagonal_left", "minecraft:diagonal_up_right",
                "minecraft:diagonal_up_left", "minecraft:diagonal_right",
                "minecraft:circle", "minecraft:rhombus",
                "minecraft:half_vertical", "minecraft:half_horizontal",
                "minecraft:half_vertical_right", "minecraft:half_horizontal_bottom",
                "minecraft:border", "minecraft:curly_border",
                "minecraft:gradient", "minecraft:gradient_up",
                "minecraft:bricks", "minecraft:globe",
                "minecraft:creeper", "minecraft:skull",
                "minecraft:flower", "minecraft:mojang",
                "minecraft:piglin");
    }

    public static RegistryDataPacketV766 createDamageTypeV767(ByteBuf alloc) {
        return createBuiltIn(alloc, "minecraft:damage_type",
                "minecraft:arrow", "minecraft:bad_respawn_point", "minecraft:cactus",
                "minecraft:campfire",
                "minecraft:cramming", "minecraft:dragon_breath", "minecraft:drown",
                "minecraft:dry_out", "minecraft:explosion", "minecraft:fall",
                "minecraft:falling_anvil", "minecraft:falling_block",
                "minecraft:falling_stalactite", "minecraft:fireball",
                "minecraft:fireworks", "minecraft:fly_into_wall", "minecraft:freeze",
                "minecraft:generic", "minecraft:generic_kill", "minecraft:hot_floor",
                "minecraft:in_fire", "minecraft:in_wall", "minecraft:indirect_magic",
                "minecraft:lava", "minecraft:lightning_bolt", "minecraft:magic",
                "minecraft:mob_attack", "minecraft:mob_attack_no_aggro",
                "minecraft:mob_projectile", "minecraft:on_fire",
                "minecraft:out_of_world", "minecraft:outside_border",
                "minecraft:player_attack", "minecraft:player_explosion",
                "minecraft:sonic_boom", "minecraft:spit", "minecraft:stalagmite",
                "minecraft:starve", "minecraft:sting", "minecraft:sweet_berry_bush",
                "minecraft:thorns", "minecraft:thrown", "minecraft:trident",
                "minecraft:unattributed_fireball", "minecraft:wither",
                "minecraft:wither_skull");
    }

    public static RegistryDataPacketV766 createWolfVariantV767(ByteBuf alloc) {
        return createBuiltIn(alloc, "minecraft:wolf_variant",
                "minecraft:pale", "minecraft:ashen", "minecraft:black",
                "minecraft:chestnut", "minecraft:rusty", "minecraft:snowy",
                "minecraft:spotted", "minecraft:striped", "minecraft:woods");
    }

    public static RegistryDataPacketV766 createEnchantment(ByteBuf alloc) {
        return createBuiltIn(alloc, "minecraft:enchantment",
                "minecraft:protection", "minecraft:fire_protection",
                "minecraft:feather_falling", "minecraft:blast_protection",
                "minecraft:projectile_protection", "minecraft:respiration",
                "minecraft:aqua_affinity", "minecraft:thorns",
                "minecraft:depth_strider", "minecraft:frost_walker",
                "minecraft:binding_curse", "minecraft:soul_speed",
                "minecraft:swift_sneak", "minecraft:sharpness",
                "minecraft:smite", "minecraft:bane_of_arthropods",
                "minecraft:knockback", "minecraft:fire_aspect",
                "minecraft:looting", "minecraft:sweeping_edge",
                "minecraft:efficiency", "minecraft:silk_touch",
                "minecraft:unbreaking", "minecraft:fortune",
                "minecraft:power", "minecraft:punch",
                "minecraft:flame", "minecraft:infinity",
                "minecraft:luck_of_the_sea", "minecraft:lure",
                "minecraft:loyalty", "minecraft:impaling",
                "minecraft:riptide", "minecraft:channeling",
                "minecraft:multishot", "minecraft:quick_charge",
                "minecraft:piercing", "minecraft:density",
                "minecraft:breach", "minecraft:wind_burst",
                "minecraft:mending", "minecraft:vanishing_curse");
    }

    public static RegistryDataPacketV766 createPaintingVariant(ByteBuf alloc) {
        return createBuiltIn(alloc, "minecraft:painting_variant",
                "minecraft:kebab", "minecraft:aztec", "minecraft:alban",
                "minecraft:aztec2", "minecraft:bomb", "minecraft:plant",
                "minecraft:wasteland", "minecraft:pool", "minecraft:courbet",
                "minecraft:sea", "minecraft:sunset", "minecraft:creebet",
                "minecraft:wanderer", "minecraft:graham", "minecraft:match",
                "minecraft:bust", "minecraft:stage", "minecraft:void",
                "minecraft:skull_and_roses", "minecraft:wither",
                "minecraft:fighters", "minecraft:pointer", "minecraft:pigscene",
                "minecraft:burning_skull", "minecraft:skeleton",
                "minecraft:donkey_kong",
                "minecraft:earth", "minecraft:wind",
                "minecraft:water", "minecraft:fire");
    }

    public static RegistryDataPacketV766 createDamageTypeV768(ByteBuf alloc) {
        return createBuiltIn(alloc, "minecraft:damage_type",
                "minecraft:arrow", "minecraft:bad_respawn_point", "minecraft:cactus",
                "minecraft:campfire",
                "minecraft:cramming", "minecraft:dragon_breath", "minecraft:drown",
                "minecraft:dry_out", "minecraft:ender_pearl",
                "minecraft:explosion", "minecraft:fall",
                "minecraft:falling_anvil", "minecraft:falling_block",
                "minecraft:falling_stalactite", "minecraft:fireball",
                "minecraft:fireworks", "minecraft:fly_into_wall", "minecraft:freeze",
                "minecraft:generic", "minecraft:generic_kill", "minecraft:hot_floor",
                "minecraft:in_fire", "minecraft:in_wall", "minecraft:indirect_magic",
                "minecraft:lava", "minecraft:lightning_bolt", "minecraft:mace_smash",
                "minecraft:magic",
                "minecraft:mob_attack", "minecraft:mob_attack_no_aggro",
                "minecraft:mob_projectile", "minecraft:on_fire",
                "minecraft:out_of_world", "minecraft:outside_border",
                "minecraft:player_attack", "minecraft:player_explosion",
                "minecraft:sonic_boom", "minecraft:spit", "minecraft:stalagmite",
                "minecraft:starve", "minecraft:sting", "minecraft:sweet_berry_bush",
                "minecraft:thorns", "minecraft:thrown", "minecraft:trident",
                "minecraft:unattributed_fireball", "minecraft:wither",
                "minecraft:wither_skull");
    }

    public static RegistryDataPacketV766 createInstrument(ByteBuf alloc) {
        return createBuiltIn(alloc, "minecraft:instrument",
                "minecraft:ponder_goat_horn", "minecraft:sing_goat_horn",
                "minecraft:seek_goat_horn", "minecraft:feel_goat_horn",
                "minecraft:admire_goat_horn", "minecraft:call_goat_horn",
                "minecraft:yearn_goat_horn", "minecraft:dream_goat_horn");
    }

    // ========================================================================
    // 1.21.5 registries
    // ========================================================================

    public static RegistryDataPacketV766 createPigVariant(ByteBuf alloc) {
        return createBuiltIn(alloc, "minecraft:pig_variant",
                "minecraft:cold", "minecraft:temperate", "minecraft:warm");
    }

    public static RegistryDataPacketV766 createCowVariant(ByteBuf alloc) {
        return createBuiltIn(alloc, "minecraft:cow_variant",
                "minecraft:cold", "minecraft:temperate", "minecraft:warm");
    }

    public static RegistryDataPacketV766 createChickenVariant(ByteBuf alloc) {
        return createBuiltIn(alloc, "minecraft:chicken_variant",
                "minecraft:cold", "minecraft:temperate", "minecraft:warm");
    }

    public static RegistryDataPacketV766 createFrogVariant(ByteBuf alloc) {
        return createBuiltIn(alloc, "minecraft:frog_variant",
                "minecraft:cold", "minecraft:temperate", "minecraft:warm");
    }

    public static RegistryDataPacketV766 createCatVariant(ByteBuf alloc) {
        return createBuiltIn(alloc, "minecraft:cat_variant",
                "minecraft:tabby", "minecraft:black", "minecraft:red",
                "minecraft:siamese", "minecraft:british_shorthair",
                "minecraft:calico", "minecraft:persian", "minecraft:ragdoll",
                "minecraft:white", "minecraft:jellie", "minecraft:all_black");
    }

    public static RegistryDataPacketV766 createWolfSoundVariant(ByteBuf alloc) {
        return createBuiltIn(alloc, "minecraft:wolf_sound_variant",
                "minecraft:classic", "minecraft:angry", "minecraft:big",
                "minecraft:cute", "minecraft:grumpy", "minecraft:puglin",
                "minecraft:sad");
    }

    public static RegistryDataPacketV766 createJukeboxSong(ByteBuf alloc) {
        return createBuiltIn(alloc, "minecraft:jukebox_song",
                "minecraft:11", "minecraft:13", "minecraft:5",
                "minecraft:blocks", "minecraft:cat", "minecraft:chirp",
                "minecraft:creator", "minecraft:creator_music_box",
                "minecraft:far", "minecraft:mall", "minecraft:mellohi",
                "minecraft:otherside", "minecraft:pigstep",
                "minecraft:precipice", "minecraft:relic",
                "minecraft:stal", "minecraft:strad",
                "minecraft:wait", "minecraft:ward");
    }

    public static RegistryDataPacketV766 createDialog(ByteBuf alloc) {
        return createBuiltIn(alloc, "minecraft:dialog",
                "minecraft:custom_options", "minecraft:quick_actions",
                "minecraft:server_links");
    }

    // ========================================================================
    // Dimension element helper
    // ========================================================================

    private static void writeDimensionElement(ByteBuf buf,
            boolean hasCeiling, boolean natural, boolean piglinSafe, float ambientLight,
            String infiniburn, boolean respawnAnchorWorks, boolean hasSkylight,
            boolean bedWorks, boolean hasRaids, int logicalHeight,
            boolean ultrawarm, String effects,
            int minY, int height, Long fixedTime) {
        writeByteTag(buf, "piglin_safe", piglinSafe ? (byte) 1 : (byte) 0);
        writeByteTag(buf, "natural", natural ? (byte) 1 : (byte) 0);
        writeFloatTag(buf, "ambient_light", ambientLight);
        writeStringTag(buf, "infiniburn", infiniburn);
        writeByteTag(buf, "respawn_anchor_works", respawnAnchorWorks ? (byte) 1 : (byte) 0);
        writeByteTag(buf, "has_skylight", hasSkylight ? (byte) 1 : (byte) 0);
        writeByteTag(buf, "bed_works", bedWorks ? (byte) 1 : (byte) 0);
        writeStringTag(buf, "effects", effects);
        if (fixedTime != null) {
            writeLongTag(buf, "fixed_time", fixedTime);
        }
        writeByteTag(buf, "has_raids", hasRaids ? (byte) 1 : (byte) 0);
        writeIntTag(buf, "monster_spawn_block_light_limit", piglinSafe ? 15 : 0);
        writeCompoundTagHeader(buf, "monster_spawn_light_level");
        writeStringTag(buf, "type", "minecraft:uniform");
        writeCompoundTagHeader(buf, "value");
        writeIntTag(buf, "min_inclusive", 0);
        writeIntTag(buf, "max_inclusive", 7);
        buf.writeByte(0x00); // end value compound
        buf.writeByte(0x00); // end monster_spawn_light_level compound
        writeIntTag(buf, "min_y", minY);
        writeIntTag(buf, "height", height);
        writeIntTag(buf, "logical_height", logicalHeight);
        writeDoubleTag(buf, "coordinate_scale", piglinSafe ? 8.0 : 1.0);
        writeByteTag(buf, "ultrawarm", ultrawarm ? (byte) 1 : (byte) 0);
        writeByteTag(buf, "has_ceiling", hasCeiling ? (byte) 1 : (byte) 0);
    }

    // ========================================================================
    // NBT tag writers
    // ========================================================================

    private static void writeCompoundTagHeader(ByteBuf buf, String name) {
        buf.writeByte(0x0A);
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(nameBytes.length);
        buf.writeBytes(nameBytes);
    }

    private static void writeStringTag(ByteBuf buf, String name, String value) {
        buf.writeByte(0x08);
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(nameBytes.length);
        buf.writeBytes(nameBytes);
        byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(valueBytes.length);
        buf.writeBytes(valueBytes);
    }

    private static void writeByteTag(ByteBuf buf, String name, byte value) {
        buf.writeByte(0x01);
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(nameBytes.length);
        buf.writeBytes(nameBytes);
        buf.writeByte(value);
    }

    private static void writeIntTag(ByteBuf buf, String name, int value) {
        buf.writeByte(0x03);
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(nameBytes.length);
        buf.writeBytes(nameBytes);
        buf.writeInt(value);
    }

    private static void writeFloatTag(ByteBuf buf, String name, float value) {
        buf.writeByte(0x05);
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(nameBytes.length);
        buf.writeBytes(nameBytes);
        buf.writeFloat(value);
    }

    private static void writeLongTag(ByteBuf buf, String name, long value) {
        buf.writeByte(0x04);
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(nameBytes.length);
        buf.writeBytes(nameBytes);
        buf.writeLong(value);
    }

    private static void writeDoubleTag(ByteBuf buf, String name, double value) {
        buf.writeByte(0x06);
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(nameBytes.length);
        buf.writeBytes(nameBytes);
        buf.writeDouble(value);
    }
}
