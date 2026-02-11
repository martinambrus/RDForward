package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x1F (Server -> Client): Entity Relative Move.
 *
 * Sent when an entity moves less than 4 blocks. Deltas are fixed-point
 * bytes (position change * 32).
 *
 * Wire format (7 bytes payload):
 *   [int]  entity ID
 *   [byte] dx (fixed-point delta, change * 32)
 *   [byte] dy (fixed-point delta)
 *   [byte] dz (fixed-point delta)
 */
public class EntityRelativeMovePacket implements Packet {

    private int entityId;
    private int dx;
    private int dy;
    private int dz;

    public EntityRelativeMovePacket() {}

    public EntityRelativeMovePacket(int entityId, int dx, int dy, int dz) {
        this.entityId = entityId;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    @Override
    public int getPacketId() {
        return 0x1F;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeByte(dx);
        buf.writeByte(dy);
        buf.writeByte(dz);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        dx = buf.readByte();
        dy = buf.readByte();
        dz = buf.readByte();
    }

    public int getEntityId() { return entityId; }
    public int getDx() { return dx; }
    public int getDy() { return dy; }
    public int getDz() { return dz; }
}
