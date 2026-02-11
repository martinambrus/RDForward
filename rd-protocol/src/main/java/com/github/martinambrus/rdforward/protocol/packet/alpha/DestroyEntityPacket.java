package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x1D (Server -> Client): Destroy Entity.
 *
 * Sent when an entity (player, mob, item, etc.) is removed.
 * Uses int entity IDs (vs Classic's signed-byte player IDs).
 *
 * Wire format (4 bytes payload):
 *   [int] entity ID
 */
public class DestroyEntityPacket implements Packet {

    private int entityId;

    public DestroyEntityPacket() {}

    public DestroyEntityPacket(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public int getPacketId() {
        return 0x1D;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
    }

    public int getEntityId() { return entityId; }
}
