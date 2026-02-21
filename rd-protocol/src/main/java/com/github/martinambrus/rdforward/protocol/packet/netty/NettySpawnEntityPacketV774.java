package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.21.11 Play state, S2C packet 0x01: Spawn Entity.
 *
 * Changes from V773:
 * - Entity type IDs shifted: camel_husk, nautilus, parched, zombie_nautilus added
 *   (player: 151 -> 155).
 * - Wire format unchanged from V773 (MOVEMENT_VECTOR velocity between position and rotation).
 */
public class NettySpawnEntityPacketV774 implements Packet {

    private static final int ENTITY_TYPE_PLAYER = 155;

    private int entityId;
    private long uuidMsb;
    private long uuidLsb;
    private double x;
    private double y;
    private double z;
    private int yaw;
    private int pitch;

    public NettySpawnEntityPacketV774() {}

    public NettySpawnEntityPacketV774(int entityId, String uuidStr,
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
