package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Pre-Netty protocol 0x23 (Server -> Client): Entity Head Rotation.
 *
 * Sets the head yaw of an entity independently of the body yaw.
 * Added in Release 1.2.1 (protocol version 28).
 *
 * Wire format (5 bytes payload):
 *   [int]  entity ID
 *   [byte] headYaw (angle/256 * 360)
 */
public class EntityHeadRotationPacket implements Packet {

    private int entityId;
    private int headYaw;

    public EntityHeadRotationPacket() {}

    public EntityHeadRotationPacket(int entityId, int headYaw) {
        this.entityId = entityId;
        this.headYaw = headYaw;
    }

    @Override
    public int getPacketId() {
        return 0x23;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeByte(headYaw);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        headYaw = buf.readUnsignedByte();
    }

    public int getEntityId() { return entityId; }
    public int getHeadYaw() { return headYaw; }
}
