package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.21.4 Play state, S2C packet 0x01: Spawn Entity.
 *
 * Same wire format as V768 but entityType IDs shifted due to creaking_transient
 * removal (entity at index 30 removed, all after shift -1).
 * Player entity type: 147 (was 148 in v768).
 */
public class NettySpawnEntityPacketV769 implements Packet {

    private static final int ENTITY_TYPE_PLAYER = 147;

    private int entityId;
    private long uuidMsb;
    private long uuidLsb;
    private double x;
    private double y;
    private double z;
    private int yaw;
    private int pitch;

    public NettySpawnEntityPacketV769() {}

    public NettySpawnEntityPacketV769(int entityId, String uuidStr,
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
        buf.writeByte(pitch); // pitch BEFORE yaw
        buf.writeByte(yaw);
        buf.writeByte(yaw); // headYaw = yaw
        McDataTypes.writeVarInt(buf, 0); // data
        buf.writeShort(0); // velocityX
        buf.writeShort(0); // velocityY
        buf.writeShort(0); // velocityZ
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
        pitch = buf.readByte();
        yaw = buf.readByte();
        buf.readByte(); // headYaw
        McDataTypes.readVarInt(buf); // data
        buf.readShort(); // velocityX
        buf.readShort(); // velocityY
        buf.readShort(); // velocityZ
    }

    public int getEntityId() { return entityId; }
}
