package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.20.5 Play state, S2C packet 0x01: Spawn Entity.
 *
 * Same wire format as V764 but entityType IDs shifted due to new entities
 * (bogged, breeze, breeze_wind_charge, ominous_item_spawner, wind_charge).
 * Player entity type: 128 (was 122 in v764).
 */
public class NettySpawnEntityPacketV766 implements Packet {

    private static final int ENTITY_TYPE_PLAYER = 128;

    private int entityId;
    private long uuidMsb;
    private long uuidLsb;
    private double x;
    private double y;
    private double z;
    private int yaw;
    private int pitch;

    public NettySpawnEntityPacketV766() {}

    public NettySpawnEntityPacketV766(int entityId, String uuidStr,
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
