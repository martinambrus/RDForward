package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x20 (Server -> Client): Entity Look.
 *
 * Sent when an entity rotates without moving.
 *
 * Wire format (6 bytes payload):
 *   [int]  entity ID
 *   [byte] yaw (rotation * 256/360)
 *   [byte] pitch (rotation * 256/360)
 */
public class EntityLookPacket implements Packet {

    private int entityId;
    private int yaw;
    private int pitch;

    public EntityLookPacket() {}

    public EntityLookPacket(int entityId, int yaw, int pitch) {
        this.entityId = entityId;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public int getPacketId() {
        return 0x20;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeByte(yaw);
        buf.writeByte(pitch);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        yaw = buf.readUnsignedByte();
        pitch = buf.readUnsignedByte();
    }

    public int getEntityId() { return entityId; }
    public int getYaw() { return yaw; }
    public int getPitch() { return pitch; }
}
