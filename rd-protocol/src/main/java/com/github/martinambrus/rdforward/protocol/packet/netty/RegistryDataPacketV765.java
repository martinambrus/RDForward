package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * 1.20.3 Configuration state, S2C packet 0x05: Registry Data.
 *
 * In 1.20.3, RegistryData switched from a single CompoundTag containing
 * all registries (v764) to a per-registry format: one packet per registry,
 * sent 4 times (dimension_type, worldgen/biome, chat_type, damage_type).
 *
 * Wire format:
 *   String      registryKey (e.g., "minecraft:dimension_type")
 *   VarInt      entryCount
 *   Per entry:
 *     String    entryId (e.g., "minecraft:overworld")
 *     Boolean   hasData (true)
 *     [NBT]     element data (network NBT: 0x0A + children + 0x00, no root name)
 */
public class RegistryDataPacketV765 implements Packet {

    private ByteBuf prebuiltData;

    public RegistryDataPacketV765() {}

    private RegistryDataPacketV765(ByteBuf prebuiltData) {
        this.prebuiltData = prebuiltData;
    }

    @Override
    public int getPacketId() { return 0x05; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeBytes(prebuiltData, prebuiltData.readerIndex(), prebuiltData.readableBytes());
    }

    @Override
    public void read(ByteBuf buf) {
        // S2C only — no server-side decoding needed
    }

    // ========================================================================
    // Factory methods — one per registry
    // ========================================================================

    public static RegistryDataPacketV765 createDimensionType(ByteBuf alloc) {
        ByteBuf data = alloc.alloc().buffer();

        McDataTypes.writeVarIntString(data, "minecraft:dimension_type");
        McDataTypes.writeVarInt(data, 4); // 4 entries

        // Entry 0: overworld
        McDataTypes.writeVarIntString(data, "minecraft:overworld");
        data.writeByte(0x0A); // network NBT compound present (no root name)
        writeDimensionElement(data, false, true, false, 0.0f,
                "#minecraft:infiniburn_overworld", false, true, true, true,
                256, false, "minecraft:overworld", 0, 256, null);
        data.writeByte(0x00); // end compound

        // Entry 1: overworld_caves
        McDataTypes.writeVarIntString(data, "minecraft:overworld_caves");
        data.writeByte(0x0A);
        writeDimensionElement(data, true, true, false, 0.0f,
                "#minecraft:infiniburn_overworld", false, true, true, true,
                256, false, "minecraft:overworld", 0, 256, null);
        data.writeByte(0x00);

        // Entry 2: the_nether
        McDataTypes.writeVarIntString(data, "minecraft:the_nether");
        data.writeByte(0x0A);
        writeDimensionElement(data, true, false, true, 0.1f,
                "#minecraft:infiniburn_nether", true, false, false, false,
                128, true, "minecraft:the_nether", 0, 256, 18000L);
        data.writeByte(0x00);

        // Entry 3: the_end
        McDataTypes.writeVarIntString(data, "minecraft:the_end");
        data.writeByte(0x0A);
        writeDimensionElement(data, false, false, false, 0.0f,
                "#minecraft:infiniburn_end", false, false, false, true,
                256, false, "minecraft:the_end", 0, 256, 6000L);
        data.writeByte(0x00);

        return new RegistryDataPacketV765(data);
    }

    public static RegistryDataPacketV765 createBiome(ByteBuf alloc) {
        ByteBuf data = alloc.alloc().buffer();

        McDataTypes.writeVarIntString(data, "minecraft:worldgen/biome");
        McDataTypes.writeVarInt(data, 1); // 1 entry

        // Entry 0: plains
        McDataTypes.writeVarIntString(data, "minecraft:plains");
        data.writeByte(0x0A); // network NBT compound present
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

        return new RegistryDataPacketV765(data);
    }

    public static RegistryDataPacketV765 createChatType(ByteBuf alloc) {
        ByteBuf data = alloc.alloc().buffer();

        McDataTypes.writeVarIntString(data, "minecraft:chat_type");
        McDataTypes.writeVarInt(data, 7); // 7 entries

        writeChatTypeEntry(data, "minecraft:chat",
                "chat.type.text", new String[]{"sender", "content"},
                "chat.type.text.narrate", new String[]{"sender", "content"}, false);
        writeChatTypeEntry(data, "minecraft:say_command",
                "chat.type.announcement", new String[]{"sender", "content"},
                "chat.type.text.narrate", new String[]{"sender", "content"}, false);
        writeChatTypeEntry(data, "minecraft:msg_command_incoming",
                "commands.message.display.incoming", new String[]{"sender", "content"},
                "chat.type.text.narrate", new String[]{"sender", "content"}, true);
        writeChatTypeEntry(data, "minecraft:msg_command_outgoing",
                "commands.message.display.outgoing", new String[]{"sender", "content"},
                "chat.type.text.narrate", new String[]{"sender", "content"}, true);
        writeChatTypeEntry(data, "minecraft:team_msg_command_incoming",
                "chat.type.team.text", new String[]{"target", "sender", "content"},
                "chat.type.text.narrate", new String[]{"sender", "content"}, false);
        writeChatTypeEntry(data, "minecraft:team_msg_command_outgoing",
                "chat.type.team.sent", new String[]{"target", "content"},
                "chat.type.text.narrate", new String[]{"sender", "content"}, false);
        writeChatTypeEntry(data, "minecraft:emote_command",
                "chat.type.emote", new String[]{"sender", "content"},
                "chat.type.emote", new String[]{"sender", "content"}, false);

        return new RegistryDataPacketV765(data);
    }

    public static RegistryDataPacketV765 createDamageType(ByteBuf alloc) {
        ByteBuf data = alloc.alloc().buffer();

        McDataTypes.writeVarIntString(data, "minecraft:damage_type");
        McDataTypes.writeVarInt(data, 44); // 44 entries

        writeDamageTypeEntry(data, "minecraft:arrow", "arrow", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(data, "minecraft:bad_respawn_point", "badRespawnPoint", "always", 0.1f, "intentional_game_design");
        writeDamageTypeEntry(data, "minecraft:cactus", "cactus", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(data, "minecraft:cramming", "cramming", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(data, "minecraft:dragon_breath", "dragonBreath", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(data, "minecraft:drown", "drown", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(data, "minecraft:dry_out", "dryout", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(data, "minecraft:explosion", "explosion", "always", 0.1f, null);
        writeDamageTypeEntry(data, "minecraft:fall", "fall", "when_caused_by_living_non_player", 0.0f, "fall_variants");
        writeDamageTypeEntry(data, "minecraft:falling_anvil", "anvil", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(data, "minecraft:falling_block", "fallingBlock", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(data, "minecraft:falling_stalactite", "fallingStalactite", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(data, "minecraft:fireball", "fireball", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(data, "minecraft:fireworks", "fireworks", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(data, "minecraft:fly_into_wall", "flyIntoWall", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(data, "minecraft:freeze", "freeze", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(data, "minecraft:generic", "generic", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(data, "minecraft:generic_kill", "genericKill", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(data, "minecraft:hot_floor", "hotFloor", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(data, "minecraft:in_fire", "inFire", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(data, "minecraft:in_wall", "inWall", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(data, "minecraft:indirect_magic", "indirectMagic", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(data, "minecraft:lava", "lava", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(data, "minecraft:lightning_bolt", "lightningBolt", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(data, "minecraft:magic", "magic", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(data, "minecraft:mob_attack", "mob", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(data, "minecraft:mob_attack_no_aggro", "mob", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(data, "minecraft:mob_projectile", "mob", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(data, "minecraft:on_fire", "onFire", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(data, "minecraft:out_of_world", "outOfWorld", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(data, "minecraft:outside_border", "outsideBorder", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(data, "minecraft:player_attack", "player", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(data, "minecraft:player_explosion", "explosion.player", "always", 0.1f, null);
        writeDamageTypeEntry(data, "minecraft:sonic_boom", "sonic_boom", "always", 0.0f, null);
        writeDamageTypeEntry(data, "minecraft:stalagmite", "stalagmite", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(data, "minecraft:starve", "starve", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(data, "minecraft:sting", "sting", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(data, "minecraft:sweet_berry_bush", "sweetBerryBush", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(data, "minecraft:thorns", "thorns", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(data, "minecraft:thrown", "thrown", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(data, "minecraft:trident", "trident", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(data, "minecraft:unattributed_fireball", "onFire", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(data, "minecraft:wither", "wither", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(data, "minecraft:wither_skull", "witherSkull", "when_caused_by_living_non_player", 0.1f, null);

        return new RegistryDataPacketV765(data);
    }

    // ========================================================================
    // Entry writing helpers
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

    private static void writeChatTypeEntry(ByteBuf buf, String name,
            String chatKey, String[] chatParams,
            String narrationKey, String[] narrationParams, boolean grayItalic) {
        McDataTypes.writeVarIntString(buf, name);
        buf.writeByte(0x0A); // network NBT compound present

        // chat decoration
        writeCompoundTagHeader(buf, "chat");
        writeStringTag(buf, "translation_key", chatKey);
        writeListTagHeader(buf, "parameters", 0x08, chatParams.length);
        for (String param : chatParams) {
            writeRawString(buf, param);
        }
        if (grayItalic) {
            writeCompoundTagHeader(buf, "style");
            writeStringTag(buf, "color", "gray");
            writeByteTag(buf, "italic", (byte) 1);
            buf.writeByte(0x00); // end style
        }
        buf.writeByte(0x00); // end chat

        // narration decoration
        writeCompoundTagHeader(buf, "narration");
        writeStringTag(buf, "translation_key", narrationKey);
        writeListTagHeader(buf, "parameters", 0x08, narrationParams.length);
        for (String param : narrationParams) {
            writeRawString(buf, param);
        }
        writeStringTag(buf, "priority", "chat");
        buf.writeByte(0x00); // end narration

        buf.writeByte(0x00); // end element compound
    }

    private static void writeDamageTypeEntry(ByteBuf buf, String name,
            String messageId, String scaling, float exhaustion, String deathMessageType) {
        McDataTypes.writeVarIntString(buf, name);
        buf.writeByte(0x0A); // network NBT compound present
        writeStringTag(buf, "message_id", messageId);
        writeStringTag(buf, "scaling", scaling);
        writeFloatTag(buf, "exhaustion", exhaustion);
        if (deathMessageType != null) {
            writeStringTag(buf, "death_message_type", deathMessageType);
        }
        buf.writeByte(0x00); // end element compound
    }

    // ========================================================================
    // NBT tag writers (same as V764)
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

    private static void writeListTagHeader(ByteBuf buf, String name, int elementType, int count) {
        buf.writeByte(0x09);
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(nameBytes.length);
        buf.writeBytes(nameBytes);
        buf.writeByte(elementType);
        buf.writeInt(count);
    }

    private static void writeRawString(ByteBuf buf, String value) {
        byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(valueBytes.length);
        buf.writeBytes(valueBytes);
    }
}
