package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x22 (Server -> Client): Entity Teleport.
 *
 * Sets the absolute position and rotation of an entity.
 * Coordinates are fixed-point (absolute position * 32).
 *
 * Wire format (18 bytes payload):
 *   [int]  entity ID
 *   [int]  x (fixed-point, position * 32)
 *   [int]  y (fixed-point, position * 32)
 *   [int]  z (fixed-point, position * 32)
 *   [byte] yaw (rotation * 256/360)
 *   [byte] pitch (rotation * 256/360)
 */
public class EntityTeleportPacket implements Packet {

    private int entityId;
    private int x;
    private int y;
    private int z;
    private int yaw;
    private int pitch;

    public EntityTeleportPacket() {}

    public EntityTeleportPacket(int entityId, int x, int y, int z, int yaw, int pitch) {
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public int getPacketId() {
        return 0x22;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(yaw);
        buf.writeByte(pitch);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        yaw = buf.readUnsignedByte();
        pitch = buf.readUnsignedByte();
    }

    public int getEntityId() { return entityId; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getYaw() { return yaw; }
    public int getPitch() { return pitch; }
}
