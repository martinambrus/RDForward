package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.21.9 Play state, S2C packet 0x01: Spawn Entity.
 *
 * Changes from V771:
 * - Entity type IDs shifted: copper_golem and mannequin added before player
 *   (player: 149 -> 151).
 * - Velocity field changed from 3 shorts to MOVEMENT_VECTOR encoding
 *   (zero = single byte 0x00).
 * - Velocity field moved: now between position (x,y,z) and rotation (pitch,yaw,headYaw),
 *   was previously after data at the end.
 */
public class NettySpawnEntityPacketV773 implements Packet {

    private static final int ENTITY_TYPE_PLAYER = 151;

    private int entityId;
    private long uuidMsb;
    private long uuidLsb;
    private double x;
    private double y;
    private double z;
    private int yaw;
    private int pitch;

    public NettySpawnEntityPacketV773() {}

    public NettySpawnEntityPacketV773(int entityId, String uuidStr,
                                      double x, double y, double z,
                                      int yaw, int pitch) {
        this.entityId = entityId;
        parseUuid(uuidStr);
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    private void parseUuid(String uuidStr) {
        String noDashes = uuidStr.replace("-", "");
        uuidMsb = Long.parseUnsignedLong(noDashes.substring(0, 16), 16);
        uuidLsb = Long.parseUnsignedLong(noDashes.substring(16, 32), 16);
    }

    @Override
    public int getPacketId() { return 0x01; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, entityId);
        buf.writeLong(uuidMsb);
        buf.writeLong(uuidLsb);
        McDataTypes.writeVarInt(buf, ENTITY_TYPE_PLAYER);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeByte(0); // MOVEMENT_VECTOR zero encoding (single byte 0x00 for zero velocity)
        buf.writeByte(pitch); // pitch BEFORE yaw
        buf.writeByte(yaw);
        buf.writeByte(yaw); // headYaw = yaw
        McDataTypes.writeVarInt(buf, 0); // data
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = McDataTypes.readVarInt(buf);
        uuidMsb = buf.readLong();
        uuidLsb = buf.readLong();
        McDataTypes.readVarInt(buf); // entityType
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        buf.readByte(); // MOVEMENT_VECTOR zero byte
        pitch = buf.readByte();
        yaw = buf.readByte();
        buf.readByte(); // headYaw
        McDataTypes.readVarInt(buf); // data
    }

    public int getEntityId() { return entityId; }
}
