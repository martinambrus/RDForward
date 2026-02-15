package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Beta protocol 0x1C (Server -> Client): Entity Velocity.
 *
 * Sets the velocity of an entity.
 *
 * Wire format (10 bytes payload):
 *   [int]   entity ID
 *   [short] velocity X
 *   [short] velocity Y
 *   [short] velocity Z
 */
public class EntityVelocityPacket implements Packet {

    private int entityId;
    private short vx;
    private short vy;
    private short vz;

    public EntityVelocityPacket() {}

    public EntityVelocityPacket(int entityId, int vx, int vy, int vz) {
        this.entityId = entityId;
        this.vx = (short) vx;
        this.vy = (short) vy;
        this.vz = (short) vz;
    }

    @Override
    public int getPacketId() {
        return 0x1C;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeShort(vx);
        buf.writeShort(vy);
        buf.writeShort(vz);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        vx = buf.readShort();
        vy = buf.readShort();
        vz = buf.readShort();
    }

    public int getEntityId() { return entityId; }
    public short getVx() { return vx; }
    public short getVy() { return vy; }
    public short getVz() { return vz; }
}
