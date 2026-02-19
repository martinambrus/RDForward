package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * 1.20.2 Configuration state, S2C packet 0x05: Registry Data.
 *
 * In 1.20.2, the dimension codec moved from JoinGame to a separate
 * RegistryData packet sent during Configuration. The format is a single
 * CompoundTag in network NBT format (no root name) containing all
 * registries: dimension_type, worldgen/biome, chat_type, damage_type.
 *
 * This is the SAME structure as the old JoinGame dimension codec, but:
 *   1. Sent as a separate Configuration-phase packet
 *   2. Uses network NBT (0x0A + children + 0x00, NO root name)
 *
 * Note: The per-entry format (String registryId + VarInt count + entries)
 * was introduced in 1.20.5 (v766), NOT 1.20.2.
 */
public class RegistryDataPacketV764 implements Packet {

    private ByteBuf prebuiltData;

    public RegistryDataPacketV764() {}

    private RegistryDataPacketV764(ByteBuf prebuiltData) {
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

    /**
     * Creates the complete registry data packet containing all 4 registries.
     */
    public static RegistryDataPacketV764 create(ByteBuf alloc) {
        ByteBuf data = alloc.alloc().buffer();

        // Network NBT root compound (type byte only, no name)
        data.writeByte(0x0A);

        writeDimensionTypeRegistry(data);
        writeBiomeRegistry(data);
        writeChatTypeRegistry(data);
        writeDamageTypeRegistry(data);

        data.writeByte(0x00); // end root compound

        return new RegistryDataPacketV764(data);
    }

    // ========================================================================
    // Registry writers
    // ========================================================================

    private static void writeDimensionTypeRegistry(ByteBuf buf) {
        writeCompoundTagHeader(buf, "minecraft:dimension_type");
        writeStringTag(buf, "type", "minecraft:dimension_type");
        writeListTagHeader(buf, "value", 0x0A, 4);

        // Entry 0: overworld
        writeRegistryEntryHeader(buf, 0, "minecraft:overworld");
        writeDimensionElement(buf, false, true, false, 0.0f,
                "#minecraft:infiniburn_overworld", false, true, true, true,
                256, false, "minecraft:overworld", 0, 256, null);
        buf.writeByte(0x00); // end list entry

        // Entry 1: overworld_caves
        writeRegistryEntryHeader(buf, 1, "minecraft:overworld_caves");
        writeDimensionElement(buf, true, true, false, 0.0f,
                "#minecraft:infiniburn_overworld", false, true, true, true,
                256, false, "minecraft:overworld", 0, 256, null);
        buf.writeByte(0x00); // end list entry

        // Entry 2: the_nether
        writeRegistryEntryHeader(buf, 2, "minecraft:the_nether");
        writeDimensionElement(buf, true, false, true, 0.1f,
                "#minecraft:infiniburn_nether", true, false, false, false,
                128, true, "minecraft:the_nether", 0, 256, 18000L);
        buf.writeByte(0x00); // end list entry

        // Entry 3: the_end
        writeRegistryEntryHeader(buf, 3, "minecraft:the_end");
        writeDimensionElement(buf, false, false, false, 0.0f,
                "#minecraft:infiniburn_end", false, false, false, true,
                256, false, "minecraft:the_end", 0, 256, 6000L);
        buf.writeByte(0x00); // end list entry

        buf.writeByte(0x00); // end dimension_type registry
    }

    private static void writeBiomeRegistry(ByteBuf buf) {
        writeCompoundTagHeader(buf, "minecraft:worldgen/biome");
        writeStringTag(buf, "type", "minecraft:worldgen/biome");
        writeListTagHeader(buf, "value", 0x0A, 1);

        // Entry 0: plains (id=1 to match chunk serialization biome palette value)
        writeRegistryEntryHeader(buf, 1, "minecraft:plains");
        writeByteTag(buf, "has_precipitation", (byte) 1);
        writeFloatTag(buf, "temperature", 0.8f);
        writeFloatTag(buf, "downfall", 0.4f);
        writeCompoundTagHeader(buf, "effects");
        writeIntTag(buf, "sky_color", 7907327);
        writeIntTag(buf, "water_fog_color", 329011);
        writeIntTag(buf, "fog_color", 12638463);
        writeIntTag(buf, "water_color", 4159204);
        writeCompoundTagHeader(buf, "mood_sound");
        writeStringTag(buf, "sound", "minecraft:ambient.cave");
        writeIntTag(buf, "tick_delay", 6000);
        writeDoubleTag(buf, "offset", 2.0);
        writeIntTag(buf, "block_search_extent", 8);
        buf.writeByte(0x00); // end mood_sound
        buf.writeByte(0x00); // end effects
        buf.writeByte(0x00); // end element
        buf.writeByte(0x00); // end list entry

        buf.writeByte(0x00); // end biome registry
    }

    private static void writeChatTypeRegistry(ByteBuf buf) {
        writeCompoundTagHeader(buf, "minecraft:chat_type");
        writeStringTag(buf, "type", "minecraft:chat_type");
        writeListTagHeader(buf, "value", 0x0A, 7);

        writeChatTypeEntry(buf, 0, "minecraft:chat",
                "chat.type.text", new String[]{"sender", "content"},
                "chat.type.text.narrate", new String[]{"sender", "content"}, false);

        writeChatTypeEntry(buf, 1, "minecraft:say_command",
                "chat.type.announcement", new String[]{"sender", "content"},
                "chat.type.text.narrate", new String[]{"sender", "content"}, false);

        writeChatTypeEntry(buf, 2, "minecraft:msg_command_incoming",
                "commands.message.display.incoming", new String[]{"sender", "content"},
                "chat.type.text.narrate", new String[]{"sender", "content"}, true);

        writeChatTypeEntry(buf, 3, "minecraft:msg_command_outgoing",
                "commands.message.display.outgoing", new String[]{"sender", "content"},
                "chat.type.text.narrate", new String[]{"sender", "content"}, true);

        writeChatTypeEntry(buf, 4, "minecraft:team_msg_command_incoming",
                "chat.type.team.text", new String[]{"target", "sender", "content"},
                "chat.type.text.narrate", new String[]{"sender", "content"}, false);

        writeChatTypeEntry(buf, 5, "minecraft:team_msg_command_outgoing",
                "chat.type.team.sent", new String[]{"target", "content"},
                "chat.type.text.narrate", new String[]{"sender", "content"}, false);

        writeChatTypeEntry(buf, 6, "minecraft:emote_command",
                "chat.type.emote", new String[]{"sender", "content"},
                "chat.type.emote", new String[]{"sender", "content"}, false);

        buf.writeByte(0x00); // end chat_type registry
    }

    private static void writeDamageTypeRegistry(ByteBuf buf) {
        writeCompoundTagHeader(buf, "minecraft:damage_type");
        writeStringTag(buf, "type", "minecraft:damage_type");
        writeListTagHeader(buf, "value", 0x0A, 44);

        writeDamageTypeEntry(buf, 0, "minecraft:arrow", "arrow", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(buf, 1, "minecraft:bad_respawn_point", "badRespawnPoint", "always", 0.1f, "intentional_game_design");
        writeDamageTypeEntry(buf, 2, "minecraft:cactus", "cactus", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(buf, 3, "minecraft:cramming", "cramming", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(buf, 4, "minecraft:dragon_breath", "dragonBreath", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(buf, 5, "minecraft:drown", "drown", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(buf, 6, "minecraft:dry_out", "dryout", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(buf, 7, "minecraft:explosion", "explosion", "always", 0.1f, null);
        writeDamageTypeEntry(buf, 8, "minecraft:fall", "fall", "when_caused_by_living_non_player", 0.0f, "fall_variants");
        writeDamageTypeEntry(buf, 9, "minecraft:falling_anvil", "anvil", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(buf, 10, "minecraft:falling_block", "fallingBlock", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(buf, 11, "minecraft:falling_stalactite", "fallingStalactite", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(buf, 12, "minecraft:fireball", "fireball", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(buf, 13, "minecraft:fireworks", "fireworks", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(buf, 14, "minecraft:fly_into_wall", "flyIntoWall", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(buf, 15, "minecraft:freeze", "freeze", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(buf, 16, "minecraft:generic", "generic", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(buf, 17, "minecraft:generic_kill", "genericKill", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(buf, 18, "minecraft:hot_floor", "hotFloor", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(buf, 19, "minecraft:in_fire", "inFire", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(buf, 20, "minecraft:in_wall", "inWall", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(buf, 21, "minecraft:indirect_magic", "indirectMagic", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(buf, 22, "minecraft:lava", "lava", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(buf, 23, "minecraft:lightning_bolt", "lightningBolt", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(buf, 24, "minecraft:magic", "magic", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(buf, 25, "minecraft:mob_attack", "mob", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(buf, 26, "minecraft:mob_attack_no_aggro", "mob", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(buf, 27, "minecraft:mob_projectile", "mob", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(buf, 28, "minecraft:on_fire", "onFire", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(buf, 29, "minecraft:out_of_world", "outOfWorld", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(buf, 30, "minecraft:outside_border", "outsideBorder", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(buf, 31, "minecraft:player_attack", "player", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(buf, 32, "minecraft:player_explosion", "explosion.player", "always", 0.1f, null);
        writeDamageTypeEntry(buf, 33, "minecraft:sonic_boom", "sonic_boom", "always", 0.0f, null);
        writeDamageTypeEntry(buf, 34, "minecraft:stalagmite", "stalagmite", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(buf, 35, "minecraft:starve", "starve", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(buf, 36, "minecraft:sting", "sting", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(buf, 37, "minecraft:sweet_berry_bush", "sweetBerryBush", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(buf, 38, "minecraft:thorns", "thorns", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(buf, 39, "minecraft:thrown", "thrown", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(buf, 40, "minecraft:trident", "trident", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(buf, 41, "minecraft:unattributed_fireball", "onFire", "when_caused_by_living_non_player", 0.1f, null);
        writeDamageTypeEntry(buf, 42, "minecraft:wither", "wither", "when_caused_by_living_non_player", 0.0f, null);
        writeDamageTypeEntry(buf, 43, "minecraft:wither_skull", "witherSkull", "when_caused_by_living_non_player", 0.1f, null);

        buf.writeByte(0x00); // end damage_type registry
    }

    // ========================================================================
    // Entry writing helpers
    // ========================================================================

    /**
     * Writes registry entry header: id, name, and opens element compound.
     * Each entry in a registry list is a compound with id (Int), name (String),
     * and element (Compound containing the actual data).
     */
    private static void writeRegistryEntryHeader(ByteBuf buf, int id, String name) {
        writeIntTag(buf, "id", id);
        writeStringTag(buf, "name", name);
        writeCompoundTagHeader(buf, "element");
    }

    /**
     * Writes dimension element fields into the current compound.
     * The final TAG_End (0x00) closes the "element" compound.
     */
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
        writeIntTag(buf, "monster_spawn_block_light_limit",
                piglinSafe ? 15 : 0); // nether=15, others=0
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
        buf.writeByte(0x00); // end element compound
    }

    private static void writeChatTypeEntry(ByteBuf buf, int id, String name,
            String chatKey, String[] chatParams,
            String narrationKey, String[] narrationParams, boolean grayItalic) {
        writeRegistryEntryHeader(buf, id, name);

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

        buf.writeByte(0x00); // end element
        buf.writeByte(0x00); // end list entry
    }

    private static void writeDamageTypeEntry(ByteBuf buf, int id, String name,
            String messageId, String scaling, float exhaustion, String deathMessageType) {
        writeRegistryEntryHeader(buf, id, name);
        writeStringTag(buf, "message_id", messageId);
        writeStringTag(buf, "scaling", scaling);
        writeFloatTag(buf, "exhaustion", exhaustion);
        if (deathMessageType != null) {
            writeStringTag(buf, "death_message_type", deathMessageType);
        }
        buf.writeByte(0x00); // end element
        buf.writeByte(0x00); // end list entry
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
