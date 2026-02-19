package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * 1.19.4 Play state, S2C packet 0x28: Join Game.
 *
 * Changes from V760:
 * - New minecraft:damage_type registry in dimension codec (42 entries).
 * - Biome "precipitation" (String) replaced by "has_precipitation" (Byte).
 * - Chat type registry, dimension type registry unchanged from V760.
 */
public class JoinGamePacketV762 implements Packet {

    private int entityId;
    private int gameMode;
    private int maxPlayers;
    private int viewDistance;
    private int simulationDistance;

    public JoinGamePacketV762() {}

    public JoinGamePacketV762(int entityId, int gameMode, int maxPlayers,
                               int viewDistance, int simulationDistance) {
        this.entityId = entityId;
        this.gameMode = gameMode;
        this.maxPlayers = maxPlayers;
        this.viewDistance = viewDistance;
        this.simulationDistance = simulationDistance;
    }

    @Override
    public int getPacketId() { return 0x28; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeBoolean(false); // isHardcore
        buf.writeByte(gameMode); // plain gameMode (no hardcore bit)
        buf.writeByte(-1); // previousGameMode (none)

        // worldCount + worldNames
        McDataTypes.writeVarInt(buf, 1);
        McDataTypes.writeVarIntString(buf, "minecraft:overworld");

        // dimensionCodec (registry format NBT)
        writeDimensionCodec(buf);

        // dimensionType: String identifier
        McDataTypes.writeVarIntString(buf, "minecraft:overworld");

        // worldName (Identifier)
        McDataTypes.writeVarIntString(buf, "minecraft:overworld");

        buf.writeLong(0L); // hashedSeed
        McDataTypes.writeVarInt(buf, maxPlayers);
        McDataTypes.writeVarInt(buf, viewDistance);
        McDataTypes.writeVarInt(buf, simulationDistance);
        buf.writeBoolean(false); // reducedDebugInfo
        buf.writeBoolean(true);  // enableRespawnScreen
        buf.writeBoolean(false); // isDebug
        buf.writeBoolean(false); // isFlat
        buf.writeBoolean(false); // lastDeathLocation (Optional absent)
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        buf.readBoolean(); // isHardcore
        gameMode = buf.readByte(); // plain gameMode
        buf.readByte(); // previousGameMode
        int worldCount = McDataTypes.readVarInt(buf);
        for (int i = 0; i < worldCount; i++) {
            McDataTypes.readVarIntString(buf); // worldName
        }
        skipNbtCompound(buf); // dimensionCodec
        McDataTypes.readVarIntString(buf); // dimensionType (String, not NBT)
        McDataTypes.readVarIntString(buf); // worldName
        buf.readLong(); // hashedSeed
        maxPlayers = McDataTypes.readVarInt(buf);
        viewDistance = McDataTypes.readVarInt(buf);
        simulationDistance = McDataTypes.readVarInt(buf);
        buf.readBoolean(); // reducedDebugInfo
        buf.readBoolean(); // enableRespawnScreen
        buf.readBoolean(); // isDebug
        buf.readBoolean(); // isFlat
        buf.readBoolean(); // lastDeathLocation present?
    }

    public int getEntityId() { return entityId; }

    // ========================================================================
    // NBT writing helpers
    // ========================================================================

    private void writeDimensionCodec(ByteBuf buf) {
        // Root TAG_Compound("")
        buf.writeByte(0x0A);
        buf.writeShort(0);

        // minecraft:dimension_type registry (identical to V760)
        writeCompoundTagHeader(buf, "minecraft:dimension_type");
        writeStringTag(buf, "type", "minecraft:dimension_type");
        writeListTagHeader(buf, "value", 0x0A, 4);

        writeDimensionRegistryEntry(buf, "minecraft:overworld", 0,
                false, true, false, 0.0f, "#minecraft:infiniburn_overworld",
                false, true, true, true, 256, false, false, "minecraft:overworld",
                0, 256);

        writeDimensionRegistryEntry(buf, "minecraft:overworld_caves", 1,
                true, true, false, 0.0f, "#minecraft:infiniburn_overworld",
                false, true, true, true, 256, false, false, "minecraft:overworld",
                0, 256);

        writeNetherRegistryEntry(buf);
        writeEndRegistryEntry(buf);

        buf.writeByte(0x00); // end dimension_type compound

        // minecraft:worldgen/biome registry — precipitation changed to has_precipitation (Byte)
        writeCompoundTagHeader(buf, "minecraft:worldgen/biome");
        writeStringTag(buf, "type", "minecraft:worldgen/biome");
        writeListTagHeader(buf, "value", 0x0A, 1);

        writeStringTag(buf, "name", "minecraft:plains");
        writeIntTag(buf, "id", 1);
        writeCompoundTagHeader(buf, "element");
        writeByteTag(buf, "has_precipitation", (byte) 1); // was: writeStringTag "precipitation" "rain"
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
        buf.writeByte(0x00); // end mood_sound compound
        buf.writeByte(0x00); // end effects compound
        buf.writeByte(0x00); // end element compound
        buf.writeByte(0x00); // end biome entry compound

        buf.writeByte(0x00); // end worldgen/biome compound

        // minecraft:chat_type registry (7 entries, same as V760)
        writeCompoundTagHeader(buf, "minecraft:chat_type");
        writeStringTag(buf, "type", "minecraft:chat_type");
        writeListTagHeader(buf, "value", 0x0A, 7);

        writeChatTypeEntry(buf, "minecraft:chat", 0,
                "chat.type.text", new String[]{"sender", "content"},
                "chat.type.text.narrate", new String[]{"sender", "content"},
                false);

        writeChatTypeEntry(buf, "minecraft:say_command", 1,
                "chat.type.announcement", new String[]{"sender", "content"},
                "chat.type.text.narrate", new String[]{"sender", "content"},
                false);

        writeChatTypeEntry(buf, "minecraft:msg_command_incoming", 2,
                "commands.message.display.incoming", new String[]{"sender", "content"},
                "chat.type.text.narrate", new String[]{"sender", "content"},
                true);

        writeChatTypeEntry(buf, "minecraft:msg_command_outgoing", 3,
                "commands.message.display.outgoing", new String[]{"sender", "content"},
                "chat.type.text.narrate", new String[]{"sender", "content"},
                true);

        writeChatTypeEntry(buf, "minecraft:team_msg_command_incoming", 4,
                "chat.type.team.text", new String[]{"target", "sender", "content"},
                "chat.type.text.narrate", new String[]{"sender", "content"},
                false);

        writeChatTypeEntry(buf, "minecraft:team_msg_command_outgoing", 5,
                "chat.type.team.sent", new String[]{"target", "content"},
                "chat.type.text.narrate", new String[]{"sender", "content"},
                false);

        writeChatTypeEntry(buf, "minecraft:emote_command", 6,
                "chat.type.emote", new String[]{"sender", "content"},
                "chat.type.emote", new String[]{"sender", "content"},
                false);

        buf.writeByte(0x00); // end chat_type compound

        // minecraft:damage_type registry (42 entries)
        writeCompoundTagHeader(buf, "minecraft:damage_type");
        writeStringTag(buf, "type", "minecraft:damage_type");
        writeListTagHeader(buf, "value", 0x0A, 44);

        writeDamageTypeEntry(buf, "minecraft:arrow", 0, "arrow", "when_caused_by_living_non_player", 0.1f);
        writeDamageTypeEntry(buf, "minecraft:bad_respawn_point", 1, "badRespawnPoint", "always", 0.1f, "intentional_game_design");
        writeDamageTypeEntry(buf, "minecraft:cactus", 2, "cactus", "when_caused_by_living_non_player", 0.1f);
        writeDamageTypeEntry(buf, "minecraft:cramming", 3, "cramming", "when_caused_by_living_non_player", 0.0f);
        writeDamageTypeEntry(buf, "minecraft:dragon_breath", 4, "dragonBreath", "when_caused_by_living_non_player", 0.0f);
        writeDamageTypeEntry(buf, "minecraft:drown", 5, "drown", "when_caused_by_living_non_player", 0.0f);
        writeDamageTypeEntry(buf, "minecraft:dry_out", 6, "dryout", "when_caused_by_living_non_player", 0.1f);
        writeDamageTypeEntry(buf, "minecraft:explosion", 7, "explosion", "always", 0.1f);
        writeDamageTypeEntry(buf, "minecraft:fall", 8, "fall", "when_caused_by_living_non_player", 0.0f, "fall_variants");
        writeDamageTypeEntry(buf, "minecraft:falling_anvil", 9, "anvil", "when_caused_by_living_non_player", 0.1f);
        writeDamageTypeEntry(buf, "minecraft:falling_block", 10, "fallingBlock", "when_caused_by_living_non_player", 0.1f);
        writeDamageTypeEntry(buf, "minecraft:falling_stalactite", 11, "fallingStalactite", "when_caused_by_living_non_player", 0.1f);
        writeDamageTypeEntry(buf, "minecraft:fireball", 12, "fireball", "when_caused_by_living_non_player", 0.1f);
        writeDamageTypeEntry(buf, "minecraft:fireworks", 13, "fireworks", "when_caused_by_living_non_player", 0.1f);
        writeDamageTypeEntry(buf, "minecraft:fly_into_wall", 14, "flyIntoWall", "when_caused_by_living_non_player", 0.0f);
        writeDamageTypeEntry(buf, "minecraft:freeze", 15, "freeze", "when_caused_by_living_non_player", 0.0f);
        writeDamageTypeEntry(buf, "minecraft:generic", 16, "generic", "when_caused_by_living_non_player", 0.0f);
        writeDamageTypeEntry(buf, "minecraft:generic_kill", 17, "genericKill", "when_caused_by_living_non_player", 0.0f);
        writeDamageTypeEntry(buf, "minecraft:hot_floor", 18, "hotFloor", "when_caused_by_living_non_player", 0.1f);
        writeDamageTypeEntry(buf, "minecraft:in_fire", 19, "inFire", "when_caused_by_living_non_player", 0.1f);
        writeDamageTypeEntry(buf, "minecraft:in_wall", 20, "inWall", "when_caused_by_living_non_player", 0.0f);
        writeDamageTypeEntry(buf, "minecraft:indirect_magic", 21, "indirectMagic", "when_caused_by_living_non_player", 0.0f);
        writeDamageTypeEntry(buf, "minecraft:lava", 22, "lava", "when_caused_by_living_non_player", 0.1f);
        writeDamageTypeEntry(buf, "minecraft:lightning_bolt", 23, "lightningBolt", "when_caused_by_living_non_player", 0.1f);
        writeDamageTypeEntry(buf, "minecraft:magic", 24, "magic", "when_caused_by_living_non_player", 0.0f);
        writeDamageTypeEntry(buf, "minecraft:mob_attack", 25, "mob", "when_caused_by_living_non_player", 0.1f);
        writeDamageTypeEntry(buf, "minecraft:mob_attack_no_aggro", 26, "mob", "when_caused_by_living_non_player", 0.1f);
        writeDamageTypeEntry(buf, "minecraft:mob_projectile", 27, "mob", "when_caused_by_living_non_player", 0.1f);
        writeDamageTypeEntry(buf, "minecraft:on_fire", 28, "onFire", "when_caused_by_living_non_player", 0.0f);
        writeDamageTypeEntry(buf, "minecraft:out_of_world", 29, "outOfWorld", "when_caused_by_living_non_player", 0.0f);
        writeDamageTypeEntry(buf, "minecraft:outside_border", 30, "outsideBorder", "when_caused_by_living_non_player", 0.0f);
        writeDamageTypeEntry(buf, "minecraft:player_attack", 31, "player", "when_caused_by_living_non_player", 0.1f);
        writeDamageTypeEntry(buf, "minecraft:player_explosion", 32, "explosion.player", "always", 0.1f);
        writeDamageTypeEntry(buf, "minecraft:sonic_boom", 33, "sonic_boom", "always", 0.0f);
        writeDamageTypeEntry(buf, "minecraft:stalagmite", 34, "stalagmite", "when_caused_by_living_non_player", 0.0f);
        writeDamageTypeEntry(buf, "minecraft:starve", 35, "starve", "when_caused_by_living_non_player", 0.0f);
        writeDamageTypeEntry(buf, "minecraft:sting", 36, "sting", "when_caused_by_living_non_player", 0.1f);
        writeDamageTypeEntry(buf, "minecraft:sweet_berry_bush", 37, "sweetBerryBush", "when_caused_by_living_non_player", 0.1f);
        writeDamageTypeEntry(buf, "minecraft:thorns", 38, "thorns", "when_caused_by_living_non_player", 0.1f);
        writeDamageTypeEntry(buf, "minecraft:thrown", 39, "thrown", "when_caused_by_living_non_player", 0.1f);
        writeDamageTypeEntry(buf, "minecraft:trident", 40, "trident", "when_caused_by_living_non_player", 0.1f);
        writeDamageTypeEntry(buf, "minecraft:unattributed_fireball", 41, "onFire", "when_caused_by_living_non_player", 0.1f);
        writeDamageTypeEntry(buf, "minecraft:wither", 42, "wither", "when_caused_by_living_non_player", 0.0f);
        writeDamageTypeEntry(buf, "minecraft:wither_skull", 43, "witherSkull", "when_caused_by_living_non_player", 0.1f);

        buf.writeByte(0x00); // end damage_type compound

        buf.writeByte(0x00); // end root compound
    }

    // ========================================================================
    // Damage type entry writer
    // ========================================================================

    private void writeDamageTypeEntry(ByteBuf buf, String name, int id,
            String messageId, String scaling, float exhaustion) {
        writeDamageTypeEntry(buf, name, id, messageId, scaling, exhaustion, null);
    }

    private void writeDamageTypeEntry(ByteBuf buf, String name, int id,
            String messageId, String scaling, float exhaustion, String deathMessageType) {
        writeStringTag(buf, "name", name);
        writeIntTag(buf, "id", id);
        writeCompoundTagHeader(buf, "element");
        writeStringTag(buf, "message_id", messageId);
        writeStringTag(buf, "scaling", scaling);
        writeFloatTag(buf, "exhaustion", exhaustion);
        if (deathMessageType != null) {
            writeStringTag(buf, "death_message_type", deathMessageType);
        }
        buf.writeByte(0x00); // end element compound
        buf.writeByte(0x00); // end entry compound
    }

    // ========================================================================
    // Chat type entry writer (same as V760)
    // ========================================================================

    private void writeChatTypeEntry(ByteBuf buf, String name, int id,
            String chatKey, String[] chatParams,
            String narrationKey, String[] narrationParams,
            boolean grayItalic) {
        writeStringTag(buf, "name", name);
        writeIntTag(buf, "id", id);
        writeCompoundTagHeader(buf, "element");

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
            buf.writeByte(0x00); // end style compound
        }
        buf.writeByte(0x00); // end chat compound

        // narration decoration
        writeCompoundTagHeader(buf, "narration");
        writeStringTag(buf, "translation_key", narrationKey);
        writeListTagHeader(buf, "parameters", 0x08, narrationParams.length);
        for (String param : narrationParams) {
            writeRawString(buf, param);
        }
        writeStringTag(buf, "priority", "chat");
        buf.writeByte(0x00); // end narration compound

        buf.writeByte(0x00); // end element compound
        buf.writeByte(0x00); // end entry compound
    }

    private void writeDimensionRegistryEntry(ByteBuf buf, String name, int id,
            boolean hasCeiling, boolean natural, boolean piglinSafe, float ambientLight,
            String infiniburn, boolean respawnAnchorWorks, boolean hasSkylight,
            boolean bedWorks, boolean hasRaids, int logicalHeight,
            boolean ultrawarm, boolean shrunk, String effects,
            int minY, int height) {
        writeStringTag(buf, "name", name);
        writeIntTag(buf, "id", id);
        writeCompoundTagHeader(buf, "element");
        writeByteTag(buf, "piglin_safe", piglinSafe ? (byte) 1 : (byte) 0);
        writeByteTag(buf, "natural", natural ? (byte) 1 : (byte) 0);
        writeFloatTag(buf, "ambient_light", ambientLight);
        writeStringTag(buf, "infiniburn", infiniburn);
        writeByteTag(buf, "respawn_anchor_works", respawnAnchorWorks ? (byte) 1 : (byte) 0);
        writeByteTag(buf, "has_skylight", hasSkylight ? (byte) 1 : (byte) 0);
        writeByteTag(buf, "bed_works", bedWorks ? (byte) 1 : (byte) 0);
        writeStringTag(buf, "effects", effects);
        writeByteTag(buf, "has_raids", hasRaids ? (byte) 1 : (byte) 0);
        writeIntTag(buf, "monster_spawn_block_light_limit", 0);
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
        writeDoubleTag(buf, "coordinate_scale", 1.0);
        writeByteTag(buf, "ultrawarm", ultrawarm ? (byte) 1 : (byte) 0);
        writeByteTag(buf, "has_ceiling", hasCeiling ? (byte) 1 : (byte) 0);
        buf.writeByte(0x00); // end element compound
        buf.writeByte(0x00); // end entry compound
    }

    private void writeNetherRegistryEntry(ByteBuf buf) {
        writeStringTag(buf, "name", "minecraft:the_nether");
        writeIntTag(buf, "id", 2);
        writeCompoundTagHeader(buf, "element");
        writeByteTag(buf, "piglin_safe", (byte) 1);
        writeByteTag(buf, "natural", (byte) 0);
        writeFloatTag(buf, "ambient_light", 0.1f);
        writeStringTag(buf, "infiniburn", "#minecraft:infiniburn_nether");
        writeByteTag(buf, "respawn_anchor_works", (byte) 1);
        writeByteTag(buf, "has_skylight", (byte) 0);
        writeByteTag(buf, "bed_works", (byte) 0);
        writeStringTag(buf, "effects", "minecraft:the_nether");
        writeLongTag(buf, "fixed_time", 18000L);
        writeByteTag(buf, "has_raids", (byte) 0);
        writeIntTag(buf, "monster_spawn_block_light_limit", 15);
        writeCompoundTagHeader(buf, "monster_spawn_light_level");
        writeStringTag(buf, "type", "minecraft:uniform");
        writeCompoundTagHeader(buf, "value");
        writeIntTag(buf, "min_inclusive", 0);
        writeIntTag(buf, "max_inclusive", 7);
        buf.writeByte(0x00); // end value compound
        buf.writeByte(0x00); // end monster_spawn_light_level compound
        writeIntTag(buf, "min_y", 0);
        writeIntTag(buf, "height", 256);
        writeIntTag(buf, "logical_height", 128);
        writeDoubleTag(buf, "coordinate_scale", 8.0);
        writeByteTag(buf, "ultrawarm", (byte) 1);
        writeByteTag(buf, "has_ceiling", (byte) 1);
        buf.writeByte(0x00); // end element compound
        buf.writeByte(0x00); // end entry compound
    }

    private void writeEndRegistryEntry(ByteBuf buf) {
        writeStringTag(buf, "name", "minecraft:the_end");
        writeIntTag(buf, "id", 3);
        writeCompoundTagHeader(buf, "element");
        writeByteTag(buf, "piglin_safe", (byte) 0);
        writeByteTag(buf, "natural", (byte) 0);
        writeFloatTag(buf, "ambient_light", 0.0f);
        writeStringTag(buf, "infiniburn", "#minecraft:infiniburn_end");
        writeByteTag(buf, "respawn_anchor_works", (byte) 0);
        writeByteTag(buf, "has_skylight", (byte) 0);
        writeByteTag(buf, "bed_works", (byte) 0);
        writeStringTag(buf, "effects", "minecraft:the_end");
        writeLongTag(buf, "fixed_time", 6000L);
        writeByteTag(buf, "has_raids", (byte) 1);
        writeIntTag(buf, "monster_spawn_block_light_limit", 0);
        writeCompoundTagHeader(buf, "monster_spawn_light_level");
        writeStringTag(buf, "type", "minecraft:uniform");
        writeCompoundTagHeader(buf, "value");
        writeIntTag(buf, "min_inclusive", 0);
        writeIntTag(buf, "max_inclusive", 7);
        buf.writeByte(0x00); // end value compound
        buf.writeByte(0x00); // end monster_spawn_light_level compound
        writeIntTag(buf, "min_y", 0);
        writeIntTag(buf, "height", 256);
        writeIntTag(buf, "logical_height", 256);
        writeDoubleTag(buf, "coordinate_scale", 1.0);
        writeByteTag(buf, "ultrawarm", (byte) 0);
        writeByteTag(buf, "has_ceiling", (byte) 0);
        buf.writeByte(0x00); // end element compound
        buf.writeByte(0x00); // end entry compound
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

    // ========================================================================
    // NBT reading helpers (for bot decoder)
    // ========================================================================

    private void skipNbtCompound(ByteBuf buf) {
        byte type = buf.readByte();
        if (type == 0) return;
        int nameLen = buf.readUnsignedShort();
        buf.skipBytes(nameLen);
        skipNbtPayload(buf, type);
    }

    private void skipNbtPayload(ByteBuf buf, byte type) {
        switch (type) {
            case 1: buf.skipBytes(1); break;
            case 2: buf.skipBytes(2); break;
            case 3: buf.skipBytes(4); break;
            case 4: buf.skipBytes(8); break;
            case 5: buf.skipBytes(4); break;
            case 6: buf.skipBytes(8); break;
            case 7: { int len = buf.readInt(); buf.skipBytes(len); break; }
            case 8: { int len = buf.readUnsignedShort(); buf.skipBytes(len); break; }
            case 9: {
                byte listType = buf.readByte();
                int count = buf.readInt();
                for (int i = 0; i < count; i++) skipNbtPayload(buf, listType);
                break;
            }
            case 10: {
                while (true) {
                    byte childType = buf.readByte();
                    if (childType == 0) break;
                    int nameLen = buf.readUnsignedShort();
                    buf.skipBytes(nameLen);
                    skipNbtPayload(buf, childType);
                }
                break;
            }
            case 11: { int len = buf.readInt(); buf.skipBytes(len * 4); break; }
            case 12: { int len = buf.readInt(); buf.skipBytes(len * 8); break; }
        }
    }
}
