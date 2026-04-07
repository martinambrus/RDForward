package com.github.martinambrus.rdforward.server.hytale;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.classic.*;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.github.martinambrus.rdforward.server.hytale.HytaleProtocolConstants.*;

/**
 * Translates outbound Classic broadcast packets to Hytale format.
 *
 * Follows the same pattern as ClassicToBedrockTranslator.
 * Classic packets (SpawnPlayer, SetBlock, Position updates, etc.) are
 * converted to their Hytale equivalents for delivery to Hytale clients.
 *
 * Coordinate conversion:
 *   Classic: fixed-point (x32), eye-level Y, yaw byte (0=North, 0-255)
 *   Hytale: float/double, feet-level Y, yaw in radians (0=North)
 */
public class ClassicToHytaleTranslator {

    private static final double PLAYER_EYE_HEIGHT = (double) 1.62f;

    private final HytaleBlockMapper blockMapper;

    /** Tracked entity positions for delta-to-absolute reconstruction. */
    private final Map<Integer, EntityPosition> entityPositions = new HashMap<>();

    public ClassicToHytaleTranslator(HytaleBlockMapper blockMapper) {
        this.blockMapper = blockMapper;
    }

    /**
     * Translate a Classic packet to Hytale packet(s).
     * Returns null if the packet has no Hytale equivalent.
     * May return multiple packets (e.g., SpawnPlayer needs EntityUpdates).
     */
    public HytalePacketBuffer[] translate(Packet packet, ByteBufAllocator alloc) {
        if (packet instanceof SetBlockServerPacket) {
            return translateSetBlock((SetBlockServerPacket) packet, alloc);
        }
        if (packet instanceof SpawnPlayerPacket) {
            return translateSpawnPlayer((SpawnPlayerPacket) packet, alloc);
        }
        if (packet instanceof DespawnPlayerPacket) {
            return translateDespawnPlayer((DespawnPlayerPacket) packet, alloc);
        }
        if (packet instanceof PlayerTeleportPacket) {
            return translateTeleport((PlayerTeleportPacket) packet, alloc);
        }
        if (packet instanceof PositionOrientationUpdatePacket) {
            return translatePosRotUpdate((PositionOrientationUpdatePacket) packet, alloc);
        }
        if (packet instanceof PositionUpdatePacket) {
            return translatePosUpdate((PositionUpdatePacket) packet, alloc);
        }
        if (packet instanceof OrientationUpdatePacket) {
            return translateRotUpdate((OrientationUpdatePacket) packet, alloc);
        }
        if (packet instanceof MessagePacket) {
            return translateMessage((MessagePacket) packet, alloc);
        }
        // Silently drop packets with no Hytale equivalent
        return null;
    }

    /**
     * Translate SetBlockServerPacket -> ServerSetBlock (ID 140).
     * Fixed: x(4) + y(4) + z(4) + blockId(4) + filler(2) + rotation(1) = 19 bytes.
     */
    private HytalePacketBuffer[] translateSetBlock(SetBlockServerPacket pkt, ByteBufAllocator alloc) {
        HytalePacketBuffer out = HytalePacketBuffer.create(PACKET_SERVER_SET_BLOCK, alloc, 19);
        out.writeIntLE(pkt.getX());
        out.writeIntLE(pkt.getY());
        out.writeIntLE(pkt.getZ());
        out.writeIntLE(blockMapper.toHytaleId(pkt.getBlockType() & 0xFF));
        out.writeShortLE(0); // filler
        out.writeByte(0);    // rotation
        return new HytalePacketBuffer[] { out };
    }

    /**
     * Translate SpawnPlayerPacket -> EntityUpdates (ID 161) with spawn data.
     *
     * EntityUpdates has a complex ECS component structure. For spawning a player,
     * we need TransformUpdate (type 9) with position and orientation.
     */
    private HytalePacketBuffer[] translateSpawnPlayer(SpawnPlayerPacket pkt, ByteBufAllocator alloc) {
        int entityId = pkt.getPlayerId() & 0xFF;

        // Track position
        double x = pkt.getX() / 32.0;
        double y = (pkt.getY() / 32.0) - PLAYER_EYE_HEIGHT; // eye -> feet
        double z = pkt.getZ() / 32.0;
        float yaw = classicYawToRadians(pkt.getYaw());
        float pitch = classicPitchToRadians(pkt.getPitch());
        entityPositions.put(entityId, new EntityPosition(x, y, z, yaw, pitch));

        return buildEntityUpdateWithTransform(entityId, x, y, z, yaw, pitch, alloc);
    }

    /** Translate DespawnPlayerPacket -> EntityUpdates with removed array. */
    private HytalePacketBuffer[] translateDespawnPlayer(DespawnPlayerPacket pkt, ByteBufAllocator alloc) {
        int entityId = pkt.getPlayerId() & 0xFF;
        entityPositions.remove(entityId);

        // EntityUpdates: nullBits(1) + removedOffset(4) + updatesOffset(4) = 9 bytes fixed
        HytalePacketBuffer out = HytalePacketBuffer.create(PACKET_ENTITY_UPDATES, alloc);
        io.netty.buffer.ByteBuf buf = out.getBuf();

        buf.writeByte(0x01); // nullBits: bit0 = removed present
        int removedOffsetSlot = buf.writerIndex();
        buf.writeIntLE(0);   // removed offset (will backpatch)
        buf.writeIntLE(-1);  // updates offset = -1 (null)
        int varBlockStart = buf.writerIndex(); // VARIABLE_BLOCK_START = byte 9

        // Backpatch removed offset relative to varBlockStart
        buf.setIntLE(removedOffsetSlot, buf.writerIndex() - varBlockStart);

        // Variable block: removed array (VarInt count + int32LE[] entityIds)
        out.writeVarInt(1);  // count = 1
        buf.writeIntLE(entityId);

        return new HytalePacketBuffer[] { out };
    }

    /** Translate PlayerTeleportPacket -> EntityUpdates with absolute position. */
    private HytalePacketBuffer[] translateTeleport(PlayerTeleportPacket pkt, ByteBufAllocator alloc) {
        int entityId = pkt.getPlayerId() & 0xFF;

        double x = pkt.getX() / 32.0;
        double y = (pkt.getY() / 32.0) - PLAYER_EYE_HEIGHT;
        double z = pkt.getZ() / 32.0;
        float yaw = classicYawToRadians(pkt.getYaw());
        float pitch = classicPitchToRadians(pkt.getPitch());
        entityPositions.put(entityId, new EntityPosition(x, y, z, yaw, pitch));

        return buildEntityUpdateWithTransform(entityId, x, y, z, yaw, pitch, alloc);
    }

    /** Translate PositionOrientationUpdatePacket (delta) -> EntityUpdates with absolute. */
    private HytalePacketBuffer[] translatePosRotUpdate(PositionOrientationUpdatePacket pkt,
                                                        ByteBufAllocator alloc) {
        int entityId = pkt.getPlayerId() & 0xFF;
        EntityPosition pos = entityPositions.get(entityId);
        if (pos == null) return null;

        pos.x += pkt.getChangeX() / 32.0;
        pos.y += pkt.getChangeY() / 32.0;
        pos.z += pkt.getChangeZ() / 32.0;
        pos.yaw = classicYawToRadians(pkt.getYaw());
        pos.pitch = classicPitchToRadians(pkt.getPitch());

        return buildEntityUpdateWithTransform(entityId, pos.x, pos.y, pos.z, pos.yaw, pos.pitch, alloc);
    }

    /** Translate PositionUpdatePacket (delta) -> EntityUpdates. */
    private HytalePacketBuffer[] translatePosUpdate(PositionUpdatePacket pkt, ByteBufAllocator alloc) {
        int entityId = pkt.getPlayerId() & 0xFF;
        EntityPosition pos = entityPositions.get(entityId);
        if (pos == null) return null;

        pos.x += pkt.getChangeX() / 32.0;
        pos.y += pkt.getChangeY() / 32.0;
        pos.z += pkt.getChangeZ() / 32.0;

        return buildEntityUpdateWithTransform(entityId, pos.x, pos.y, pos.z, pos.yaw, pos.pitch, alloc);
    }

    /** Translate OrientationUpdatePacket -> EntityUpdates. */
    private HytalePacketBuffer[] translateRotUpdate(OrientationUpdatePacket pkt, ByteBufAllocator alloc) {
        int entityId = pkt.getPlayerId() & 0xFF;
        EntityPosition pos = entityPositions.get(entityId);
        if (pos == null) return null;

        pos.yaw = classicYawToRadians(pkt.getYaw());
        pos.pitch = classicPitchToRadians(pkt.getPitch());

        return buildEntityUpdateWithTransform(entityId, pos.x, pos.y, pos.z, pos.yaw, pos.pitch, alloc);
    }

    /**
     * Translate MessagePacket -> ServerMessage (ID 210).
     *
     * ServerMessage wire format (from decompiled ServerMessage.serialize):
     *   [0]  byte    nullBits (bit0 = message present)
     *   [1]  byte    ChatType enum value (0=Chat)
     *   Variable: FormattedMessage (inline, no offset table — directly follows fixed block)
     *
     * FormattedMessage wire format (from decompiled FormattedMessage.serialize):
     *   Fixed block (38 bytes):
     *     [0]  byte    nullBits (bit0=rawText, bit1=messageId, bit2=children, bit3=params,
     *                            bit4=messageParams, bit5=color, bit6=link, bit7=image)
     *     [1]  byte    bold (MaybeBool: 0=Null)
     *     [2]  byte    italic (MaybeBool: 0=Null)
     *     [3]  byte    monospace (MaybeBool: 0=Null)
     *     [4]  byte    underlined (MaybeBool: 0=Null)
     *     [5]  byte    markupEnabled
     *     [6]  int32LE rawText offset (relative to varBlockStart)
     *     [10] int32LE messageId offset
     *     [14] int32LE children offset
     *     [18] int32LE params offset
     *     [22] int32LE messageParams offset
     *     [26] int32LE color offset
     *     [30] int32LE link offset
     *     [34] int32LE image offset
     *   Variable block (byte 38+): variable-length data
     */
    private HytalePacketBuffer[] translateMessage(MessagePacket pkt, ByteBufAllocator alloc) {
        String message = pkt.getMessage().trim();
        if (message.isEmpty()) return null;

        HytalePacketBuffer out = HytalePacketBuffer.create(PACKET_SERVER_MESSAGE, alloc);
        io.netty.buffer.ByteBuf buf = out.getBuf();

        // -- ServerMessage fixed block --
        buf.writeByte(0x01); // nullBits: bit0 = message present
        buf.writeByte(0);    // type: Chat (0)

        // -- FormattedMessage (inline, follows ServerMessage fixed block) --
        buf.writeByte(0x01); // nullBits: bit0 = rawText present (only field we use)
        buf.writeByte(0);    // bold = Null (0)
        buf.writeByte(0);    // italic = Null (0)
        buf.writeByte(0);    // monospace = Null (0)
        buf.writeByte(0);    // underlined = Null (0)
        buf.writeByte(0);    // markupEnabled = false

        // 8 variable field offset slots (int32LE each)
        int rawTextOffsetSlot = buf.writerIndex();
        buf.writeIntLE(0);   // rawText offset (will backpatch)
        buf.writeIntLE(-1);  // messageId = null
        buf.writeIntLE(-1);  // children = null
        buf.writeIntLE(-1);  // params = null
        buf.writeIntLE(-1);  // messageParams = null
        buf.writeIntLE(-1);  // color = null
        buf.writeIntLE(-1);  // link = null
        buf.writeIntLE(-1);  // image = null
        int varBlockStart = buf.writerIndex();

        // Backpatch rawText offset relative to varBlockStart
        buf.setIntLE(rawTextOffsetSlot, buf.writerIndex() - varBlockStart);

        // Write rawText as VarInt-length-prefixed UTF-8 string
        byte[] textBytes = message.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        out.writeVarInt(textBytes.length);
        buf.writeBytes(textBytes);

        return new HytalePacketBuffer[] { out };
    }

    // -- Entity Update helpers --

    /**
     * Build an EntityUpdates packet (ID 161) with a single TransformUpdate (type 9).
     *
     * EntityUpdates wire format (from decompiled EntityUpdates.serialize):
     *   Fixed block (9 bytes):
     *     [0]  byte    nullBits (bit0=removed, bit1=updates)
     *     [1]  int32LE removed offset (relative to VARIABLE_BLOCK_START=9)
     *     [5]  int32LE updates offset (relative to VARIABLE_BLOCK_START=9)
     *   Variable block: removed(int32LE[]) + updates(EntityUpdate[])
     *
     * EntityUpdate wire format (from decompiled EntityUpdate.serialize):
     *   Fixed block (13 bytes):
     *     [0]  byte    nullBits (bit0=removed, bit1=updates)
     *     [1]  int32LE networkId
     *     [5]  int32LE removed offset (relative to VARIABLE_BLOCK_START=13)
     *     [9]  int32LE updates offset (relative to VARIABLE_BLOCK_START=13)
     *   Variable block: removed(ComponentUpdateType[]) + updates(ComponentUpdate[])
     *
     * ComponentUpdate serialized via serializeWithTypeId().
     */
    private HytalePacketBuffer[] buildEntityUpdateWithTransform(int entityId,
            double x, double y, double z, float yaw, float pitch, ByteBufAllocator alloc) {

        HytalePacketBuffer out = HytalePacketBuffer.create(PACKET_ENTITY_UPDATES, alloc);
        io.netty.buffer.ByteBuf buf = out.getBuf();

        // -- EntityUpdates fixed block (9 bytes) --
        buf.writeByte(0x02); // nullBits: bit1 = updates present, bit0 = removed absent
        int removedOffsetSlot = buf.writerIndex();
        buf.writeIntLE(-1);  // removed offset = -1 (null)
        int updatesOffsetSlot = buf.writerIndex();
        buf.writeIntLE(0);   // updates offset (will backpatch)
        int euVarBlockStart = buf.writerIndex(); // EntityUpdates VARIABLE_BLOCK_START = byte 9

        // Backpatch updates offset relative to euVarBlockStart
        buf.setIntLE(updatesOffsetSlot, buf.writerIndex() - euVarBlockStart);

        // Updates array: VarInt count + EntityUpdate[]
        out.writeVarInt(1);  // 1 entity update

        // -- EntityUpdate fixed block (13 bytes) --
        buf.writeByte(0x02); // nullBits: bit1 = updates present, bit0 = removed absent
        buf.writeIntLE(entityId); // networkId
        int euRemovedOffsetSlot = buf.writerIndex();
        buf.writeIntLE(-1);  // removed offset = -1 (null)
        int euUpdatesOffsetSlot = buf.writerIndex();
        buf.writeIntLE(0);   // updates offset (will backpatch)
        int entVarBlockStart = buf.writerIndex(); // EntityUpdate VARIABLE_BLOCK_START = byte 13

        // Backpatch EntityUpdate updates offset relative to entVarBlockStart
        buf.setIntLE(euUpdatesOffsetSlot, buf.writerIndex() - entVarBlockStart);

        // Updates array: VarInt count + ComponentUpdate[]
        out.writeVarInt(1);  // 1 component update

        // ComponentUpdate: serializeWithTypeId writes VarInt typeId then the component data
        out.writeVarInt(9);  // component type = TransformUpdate

        // ModelTransform (49 bytes): nullBits(1) + Position(24) + bodyOrientation(12) + lookOrientation(12)
        out.writeByte(0x07); // ModelTransform nullBits: position + bodyOrientation + lookOrientation all present

        // Position (3x double LE = 24 bytes)
        out.writeDoubleLE(x);
        out.writeDoubleLE(y);
        out.writeDoubleLE(z);

        // Direction: bodyOrientation (yaw, pitch, roll as 3x float LE = 12 bytes)
        out.writeFloatLE(yaw);
        out.writeFloatLE(pitch);
        out.writeFloatLE(0.0f); // roll

        // Direction: lookOrientation (3x float LE = 12 bytes)
        out.writeFloatLE(yaw);
        out.writeFloatLE(pitch);
        out.writeFloatLE(0.0f);

        return new HytalePacketBuffer[] { out };
    }

    // -- Coordinate conversion --

    /** Convert Classic yaw (0=North, 0-255) to Hytale radians (0=North). */
    private static float classicYawToRadians(int classicYaw) {
        float degrees = (classicYaw & 0xFF) * 360.0f / 256.0f;
        return (float) Math.toRadians(degrees);
    }

    /** Convert Classic pitch to Hytale radians (positive=up). */
    private static float classicPitchToRadians(int classicPitch) {
        float degrees = (classicPitch & 0xFF) * 360.0f / 256.0f;
        // Classic: 0=level, 64=down, 192=up
        // Hytale: positive=up
        return (float) Math.toRadians(-degrees);
    }

    /** Tracked entity position for delta-to-absolute reconstruction. */
    private static class EntityPosition {
        double x, y, z;
        float yaw, pitch;

        EntityPosition(double x, double y, double z, float yaw, float pitch) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }
}
