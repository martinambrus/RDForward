package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Beta protocol 0x26 (Server -> Client): Entity Status.
 *
 * Sent to indicate a status change for an entity (e.g. hurt, death).
 *
 * Wire format (5 bytes payload):
 *   [int]  entity ID
 *   [byte] status (2 = hurt, 3 = dead, 6 = wolf taming, 7 = wolf tamed)
 */
public class EntityStatusPacket implements Packet {

    private int entityId;
    private byte status;

    public EntityStatusPacket() {}

    public EntityStatusPacket(int entityId, int status) {
        this.entityId = entityId;
        this.status = (byte) status;
    }

    @Override
    public int getPacketId() {
        return 0x26;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeByte(status);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        status = buf.readByte();
    }

    public int getEntityId() { return entityId; }
    public byte getStatus() { return status; }
}
