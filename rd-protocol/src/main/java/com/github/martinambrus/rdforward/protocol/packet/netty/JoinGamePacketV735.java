package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * 1.16 Play state, S2C packet 0x25: Join Game.
 *
 * Complete rewrite from v573. Dimension is now an Identifier with a full
 * NBT dimension codec registry. levelType removed; isDebug and isFlat
 * booleans added instead. Hardcore flag remains in gameMode bit 3
 * (separate isHardcore boolean was added later in 1.16.2).
 *
 * Wire format (1.16.0, protocol 735):
 *   [ubyte]      gameMode (bit 3 = hardcore)
 *   [byte]       previousGameMode (-1 = none)
 *   [VarInt]     worldCount
 *   [Identifier[]] worldNames
 *   [NBT]        dimensionCodec (full registry)
 *   [Identifier]  dimension ("minecraft:overworld")
 *   [Identifier]  worldName ("minecraft:overworld")
 *   [long]       hashedSeed
 *   [ubyte]      maxPlayers
 *   [VarInt]     viewDistance
 *   [boolean]    reducedDebugInfo
 *   [boolean]    enableRespawnScreen
 *   [boolean]    isDebug
 *   [boolean]    isFlat
 */
public class JoinGamePacketV735 implements Packet {

    private int entityId;
    private int gameMode;
    private int maxPlayers;
    private int viewDistance;

    public JoinGamePacketV735() {}

    public JoinGamePacketV735(int entityId, int gameMode, int maxPlayers, int viewDistance) {
        this.entityId = entityId;
        this.gameMode = gameMode;
        this.maxPlayers = maxPlayers;
        this.viewDistance = viewDistance;
    }

    @Override
    public int getPacketId() { return 0x25; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeByte(gameMode); // bit 3 = hardcore (0 here)
        buf.writeByte(-1); // previousGameMode (none)

        // worldCount + worldNames
        McDataTypes.writeVarInt(buf, 1);
        McDataTypes.writeVarIntString(buf, "minecraft:overworld");

        // dimensionCodec (full registry NBT)
        writeDimensionCodec(buf);

        // dimension (Identifier — dimension type name)
        McDataTypes.writeVarIntString(buf, "minecraft:overworld");

        // worldName (Identifier)
        McDataTypes.writeVarIntString(buf, "minecraft:overworld");

        buf.writeLong(0L); // hashedSeed
        buf.writeByte(maxPlayers);
        McDataTypes.writeVarInt(buf, viewDistance);
        buf.writeBoolean(false); // reducedDebugInfo
        buf.writeBoolean(true);  // enableRespawnScreen
        buf.writeBoolean(false); // isDebug
        buf.writeBoolean(false); // isFlat
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        gameMode = buf.readUnsignedByte(); // bit 3 = hardcore
        buf.readByte(); // previousGameMode
        int worldCount = McDataTypes.readVarInt(buf);
        for (int i = 0; i < worldCount; i++) {
            McDataTypes.readVarIntString(buf); // worldName
        }
        skipNbtCompound(buf); // dimensionCodec
        McDataTypes.readVarIntString(buf); // dimension
        McDataTypes.readVarIntString(buf); // worldName
        buf.readLong(); // hashedSeed
        maxPlayers = buf.readUnsignedByte();
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
     * Write the dimension codec for 1.16.0 (v735).
     *
     * 1.16.0 uses a flat format: root compound with a single "dimension" key
     * mapping to a TAG_List of TAG_Compound entries. Each entry is a flat compound
     * with dimension properties + "name" StringTag. No registry wrapper (type/value/
     * name/id/element), no biome registry. Field "shrunk" (byte) is 1.16.0-specific
     * (replaced by "coordinate_scale" in 1.16.2).
     */
    private void writeDimensionCodec(ByteBuf buf) {
        // Root TAG_Compound("")
        buf.writeByte(0x0A); // TAG_Compound
        buf.writeShort(0);   // empty name

        // TAG_List "dimension" of TAG_Compound, 4 entries
        writeListTagHeader(buf, "dimension", 0x0A, 4);

        // Entry 0: overworld
        writeOverworldEntry(buf, "minecraft:overworld", (byte) 0);

        // Entry 1: overworld_caves
        writeOverworldEntry(buf, "minecraft:overworld_caves", (byte) 1);

        // Entry 2: the_nether
        writeNetherEntry(buf);

        // Entry 3: the_end
        writeEndEntry(buf);

        buf.writeByte(0x00); // end root compound
    }

    private void writeOverworldEntry(ByteBuf buf, String name, byte hasCeiling) {
        writeStringTag(buf, "name", name);
        writeByteTag(buf, "has_ceiling", hasCeiling);
        writeByteTag(buf, "piglin_safe", (byte) 0);
        writeByteTag(buf, "natural", (byte) 1);
        writeFloatTag(buf, "ambient_light", 0.0f);
        writeStringTag(buf, "infiniburn", "minecraft:infiniburn_overworld");
        writeByteTag(buf, "respawn_anchor_works", (byte) 0);
        writeByteTag(buf, "has_skylight", (byte) 1);
        writeByteTag(buf, "bed_works", (byte) 1);
        writeByteTag(buf, "has_raids", (byte) 1);
        writeIntTag(buf, "logical_height", 256);
        writeByteTag(buf, "shrunk", (byte) 0);
        writeByteTag(buf, "ultrawarm", (byte) 0);
        buf.writeByte(0x00); // end entry compound
    }

    private void writeNetherEntry(ByteBuf buf) {
        writeByteTag(buf, "piglin_safe", (byte) 1);
        writeByteTag(buf, "natural", (byte) 0);
        writeFloatTag(buf, "ambient_light", 0.1f);
        writeStringTag(buf, "infiniburn", "minecraft:infiniburn_nether");
        writeByteTag(buf, "respawn_anchor_works", (byte) 1);
        writeByteTag(buf, "has_skylight", (byte) 0);
        writeByteTag(buf, "bed_works", (byte) 0);
        writeLongTag(buf, "fixed_time", 18000L);
        writeByteTag(buf, "has_raids", (byte) 0);
        writeStringTag(buf, "name", "minecraft:the_nether");
        writeIntTag(buf, "logical_height", 128);
        writeByteTag(buf, "shrunk", (byte) 1);
        writeByteTag(buf, "ultrawarm", (byte) 1);
        writeByteTag(buf, "has_ceiling", (byte) 1);
        buf.writeByte(0x00); // end entry compound
    }

    private void writeEndEntry(ByteBuf buf) {
        writeByteTag(buf, "piglin_safe", (byte) 0);
        writeByteTag(buf, "natural", (byte) 0);
        writeFloatTag(buf, "ambient_light", 0.0f);
        writeStringTag(buf, "infiniburn", "minecraft:infiniburn_end");
        writeByteTag(buf, "respawn_anchor_works", (byte) 0);
        writeByteTag(buf, "has_skylight", (byte) 0);
        writeByteTag(buf, "bed_works", (byte) 0);
        writeLongTag(buf, "fixed_time", 6000L);
        writeByteTag(buf, "has_raids", (byte) 1);
        writeStringTag(buf, "name", "minecraft:the_end");
        writeIntTag(buf, "logical_height", 256);
        writeByteTag(buf, "shrunk", (byte) 0);
        writeByteTag(buf, "ultrawarm", (byte) 0);
        writeByteTag(buf, "has_ceiling", (byte) 0);
        buf.writeByte(0x00); // end entry compound
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
