package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * 1.16.2 Play state, S2C packet 0x24: Join Game.
 *
 * Major rewrite from v735:
 * - isHardcore is a separate boolean (was gameMode bit 3)
 * - gameMode is a plain byte (no longer embeds hardcore flag)
 * - maxPlayers is VarInt (was unsigned byte)
 * - Dimension is an NBT compound (was String/Identifier)
 * - Dimension codec uses REGISTRY format with minecraft:dimension_type
 *   and minecraft:worldgen/biome registries
 * - Uses "coordinate_scale" instead of "shrunk"
 * - Dimension type compound has "effects" field
 *
 * Wire format (1.16.2, protocol 751):
 *   [int]        entityId
 *   [boolean]    isHardcore
 *   [byte]       gameMode
 *   [byte]       previousGameMode (-1 = none)
 *   [VarInt]     worldCount
 *   [Identifier[]] worldNames
 *   [NBT]        dimensionCodec (registry format)
 *   [NBT]        dimensionType (compound)
 *   [Identifier]  worldName ("minecraft:overworld")
 *   [long]       hashedSeed
 *   [VarInt]     maxPlayers
 *   [VarInt]     viewDistance
 *   [boolean]    reducedDebugInfo
 *   [boolean]    enableRespawnScreen
 *   [boolean]    isDebug
 *   [boolean]    isFlat
 */
public class JoinGamePacketV751 implements Packet {

    private int entityId;
    private int gameMode;
    private int maxPlayers;
    private int viewDistance;

    public JoinGamePacketV751() {}

    public JoinGamePacketV751(int entityId, int gameMode, int maxPlayers, int viewDistance) {
        this.entityId = entityId;
        this.gameMode = gameMode;
        this.maxPlayers = maxPlayers;
        this.viewDistance = viewDistance;
    }

    @Override
    public int getPacketId() { return 0x24; }

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

        // dimensionType (NBT compound — reuse the overworld entry from the codec)
        writeDimensionType(buf);

        // worldName (Identifier)
        McDataTypes.writeVarIntString(buf, "minecraft:overworld");

        buf.writeLong(0L); // hashedSeed
        McDataTypes.writeVarInt(buf, maxPlayers); // VarInt (was unsigned byte in v735)
        McDataTypes.writeVarInt(buf, viewDistance);
        buf.writeBoolean(false); // reducedDebugInfo
        buf.writeBoolean(true);  // enableRespawnScreen
        buf.writeBoolean(false); // isDebug
        buf.writeBoolean(false); // isFlat
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
        skipNbtCompound(buf); // dimensionType
        McDataTypes.readVarIntString(buf); // worldName
        buf.readLong(); // hashedSeed
        maxPlayers = McDataTypes.readVarInt(buf);
        viewDistance = McDataTypes.readVarInt(buf);
        buf.readBoolean(); // reducedDebugInfo
        buf.readBoolean(); // enableRespawnScreen
        buf.readBoolean(); // isDebug
        buf.readBoolean(); // isFlat
    }

    public int getEntityId() { return entityId; }

    // ========================================================================
    // NBT writing helpers
    // ========================================================================

    /**
     * Write the dimension codec for 1.16.2 (v751) in REGISTRY format.
     *
     * 1.16.2 uses a registry wrapper: root compound with two registries:
     * "minecraft:dimension_type" and "minecraft:worldgen/biome".
     * Each registry has "type" (String) and "value" (List of entries).
     * Each entry has "name" (String), "id" (Int), and "element" (Compound).
     */
    private void writeDimensionCodec(ByteBuf buf) {
        // Root TAG_Compound("")
        buf.writeByte(0x0A); // TAG_Compound
        buf.writeShort(0);   // empty name

        // minecraft:dimension_type registry
        writeCompoundTagHeader(buf, "minecraft:dimension_type");
        writeStringTag(buf, "type", "minecraft:dimension_type");
        writeListTagHeader(buf, "value", 0x0A, 4);

        // Entry 0: overworld (id=0)
        writeDimensionRegistryEntry(buf, "minecraft:overworld", 0,
                false, true, false, 0.0f, "minecraft:infiniburn_overworld",
                false, true, true, true, 256, false, false, "minecraft:overworld");

        // Entry 1: overworld_caves (id=1)
        writeDimensionRegistryEntry(buf, "minecraft:overworld_caves", 1,
                true, true, false, 0.0f, "minecraft:infiniburn_overworld",
                false, true, true, true, 256, false, false, "minecraft:overworld");

        // Entry 2: the_nether (id=2)
        writeNetherRegistryEntry(buf);

        // Entry 3: the_end (id=3)
        writeEndRegistryEntry(buf);

        buf.writeByte(0x00); // end dimension_type compound

        // minecraft:worldgen/biome registry
        writeCompoundTagHeader(buf, "minecraft:worldgen/biome");
        writeStringTag(buf, "type", "minecraft:worldgen/biome");
        writeListTagHeader(buf, "value", 0x0A, 1);

        // Entry 0: plains (id=1) — minimal biome entry
        writeStringTag(buf, "name", "minecraft:plains");
        writeIntTag(buf, "id", 1);
        writeCompoundTagHeader(buf, "element");
        writeStringTag(buf, "precipitation", "rain");
        writeFloatTag(buf, "depth", 0.125f);
        writeFloatTag(buf, "temperature", 0.8f);
        writeFloatTag(buf, "scale", 0.05f);
        writeFloatTag(buf, "downfall", 0.4f);
        writeStringTag(buf, "category", "plains");
        writeCompoundTagHeader(buf, "effects");
        writeIntTag(buf, "sky_color", 7907327);
        writeIntTag(buf, "water_fog_color", 329011);
        writeIntTag(buf, "fog_color", 12638463);
        writeIntTag(buf, "water_color", 4159204);
        buf.writeByte(0x00); // end effects compound
        buf.writeByte(0x00); // end element compound
        buf.writeByte(0x00); // end biome entry compound

        buf.writeByte(0x00); // end worldgen/biome compound

        buf.writeByte(0x00); // end root compound
    }

    private void writeDimensionRegistryEntry(ByteBuf buf, String name, int id,
            boolean hasCeiling, boolean natural, boolean piglinSafe, float ambientLight,
            String infiniburn, boolean respawnAnchorWorks, boolean hasSkylight,
            boolean bedWorks, boolean hasRaids, int logicalHeight,
            boolean ultrawarm, boolean shrunk, String effects) {
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
        writeStringTag(buf, "infiniburn", "minecraft:infiniburn_nether");
        writeByteTag(buf, "respawn_anchor_works", (byte) 1);
        writeByteTag(buf, "has_skylight", (byte) 0);
        writeByteTag(buf, "bed_works", (byte) 0);
        writeStringTag(buf, "effects", "minecraft:the_nether");
        writeLongTag(buf, "fixed_time", 18000L);
        writeByteTag(buf, "has_raids", (byte) 0);
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
        writeStringTag(buf, "infiniburn", "minecraft:infiniburn_end");
        writeByteTag(buf, "respawn_anchor_works", (byte) 0);
        writeByteTag(buf, "has_skylight", (byte) 0);
        writeByteTag(buf, "bed_works", (byte) 0);
        writeStringTag(buf, "effects", "minecraft:the_end");
        writeLongTag(buf, "fixed_time", 6000L);
        writeByteTag(buf, "has_raids", (byte) 1);
        writeIntTag(buf, "logical_height", 256);
        writeDoubleTag(buf, "coordinate_scale", 1.0);
        writeByteTag(buf, "ultrawarm", (byte) 0);
        writeByteTag(buf, "has_ceiling", (byte) 0);
        buf.writeByte(0x00); // end element compound
        buf.writeByte(0x00); // end entry compound
    }

    /**
     * Write the dimension type NBT compound for "minecraft:overworld".
     * This is the same compound as the overworld element in the codec.
     */
    private void writeDimensionType(ByteBuf buf) {
        // Root TAG_Compound("")
        buf.writeByte(0x0A); // TAG_Compound
        buf.writeShort(0);   // empty name

        writeByteTag(buf, "piglin_safe", (byte) 0);
        writeByteTag(buf, "natural", (byte) 1);
        writeFloatTag(buf, "ambient_light", 0.0f);
        writeStringTag(buf, "infiniburn", "minecraft:infiniburn_overworld");
        writeByteTag(buf, "respawn_anchor_works", (byte) 0);
        writeByteTag(buf, "has_skylight", (byte) 1);
        writeByteTag(buf, "bed_works", (byte) 1);
        writeStringTag(buf, "effects", "minecraft:overworld");
        writeByteTag(buf, "has_raids", (byte) 1);
        writeIntTag(buf, "logical_height", 256);
        writeDoubleTag(buf, "coordinate_scale", 1.0);
        writeByteTag(buf, "ultrawarm", (byte) 0);
        writeByteTag(buf, "has_ceiling", (byte) 0);

        buf.writeByte(0x00); // end root compound
    }

    // ========================================================================
    // NBT tag writers
    // ========================================================================

    private static void writeCompoundTagHeader(ByteBuf buf, String name) {
        buf.writeByte(0x0A); // TAG_Compound
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(nameBytes.length);
        buf.writeBytes(nameBytes);
    }

    private static void writeStringTag(ByteBuf buf, String name, String value) {
        buf.writeByte(0x08); // TAG_String
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(nameBytes.length);
        buf.writeBytes(nameBytes);
        byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(valueBytes.length);
        buf.writeBytes(valueBytes);
    }

    private static void writeByteTag(ByteBuf buf, String name, byte value) {
        buf.writeByte(0x01); // TAG_Byte
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(nameBytes.length);
        buf.writeBytes(nameBytes);
        buf.writeByte(value);
    }

    private static void writeIntTag(ByteBuf buf, String name, int value) {
        buf.writeByte(0x03); // TAG_Int
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(nameBytes.length);
        buf.writeBytes(nameBytes);
        buf.writeInt(value);
    }

    private static void writeFloatTag(ByteBuf buf, String name, float value) {
        buf.writeByte(0x05); // TAG_Float
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(nameBytes.length);
        buf.writeBytes(nameBytes);
        buf.writeFloat(value);
    }

    private static void writeLongTag(ByteBuf buf, String name, long value) {
        buf.writeByte(0x04); // TAG_Long
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(nameBytes.length);
        buf.writeBytes(nameBytes);
        buf.writeLong(value);
    }

    private static void writeDoubleTag(ByteBuf buf, String name, double value) {
        buf.writeByte(0x06); // TAG_Double
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(nameBytes.length);
        buf.writeBytes(nameBytes);
        buf.writeDouble(value);
    }

    private static void writeListTagHeader(ByteBuf buf, String name, int elementType, int count) {
        buf.writeByte(0x09); // TAG_List
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(nameBytes.length);
        buf.writeBytes(nameBytes);
        buf.writeByte(elementType);
        buf.writeInt(count);
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
