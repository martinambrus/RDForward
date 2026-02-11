package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x21 (Server -> Client): Entity Look and Relative Move.
 *
 * Sent when an entity both moves (less than 4 blocks) and rotates.
 * Combines Entity Relative Move (0x1F) and Entity Look (0x20).
 *
 * Wire format (9 bytes payload):
 *   [int]  entity ID
 *   [byte] dx (fixed-point delta)
 *   [byte] dy (fixed-point delta)
 *   [byte] dz (fixed-point delta)
 *   [byte] yaw (rotation * 256/360)
 *   [byte] pitch (rotation * 256/360)
 */
public class EntityLookAndMovePacket implements Packet {

    private int entityId;
    private int dx;
    private int dy;
    private int dz;
    private int yaw;
    private int pitch;

    public EntityLookAndMovePacket() {}

    public EntityLookAndMovePacket(int entityId, int dx, int dy, int dz, int yaw, int pitch) {
        this.entityId = entityId;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public int getPacketId() {
        return 0x21;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeByte(dx);
        buf.writeByte(dy);
        buf.writeByte(dz);
        buf.writeByte(yaw);
        buf.writeByte(pitch);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        dx = buf.readByte();
        dy = buf.readByte();
        dz = buf.readByte();
        yaw = buf.readUnsignedByte();
        pitch = buf.readUnsignedByte();
    }

    public int getEntityId() { return entityId; }
    public int getDx() { return dx; }
    public int getDy() { return dy; }
    public int getDz() { return dz; }
    public int getYaw() { return yaw; }
    public int getPitch() { return pitch; }
}
