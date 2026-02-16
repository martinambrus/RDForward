package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.3.1+ protocol 0x1D (Server -> Client): Destroy Entity.
 *
 * Changed from single entity ID to variable-length array.
 *
 * Wire format:
 *   [byte]    count
 *   [int[]]   entity IDs (count entries)
 */
public class DestroyEntityPacketV39 implements Packet {

    private int[] entityIds;

    public DestroyEntityPacketV39() {}

    /** Convenience constructor for destroying a single entity. */
    public DestroyEntityPacketV39(int entityId) {
        this.entityIds = new int[]{entityId};
    }

    public DestroyEntityPacketV39(int[] entityIds) {
        this.entityIds = entityIds;
    }

    @Override
    public int getPacketId() {
        return 0x1D;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(entityIds.length);
        for (int id : entityIds) {
            buf.writeInt(id);
        }
    }

    @Override
    public void read(ByteBuf buf) {
        int count = buf.readUnsignedByte();
        entityIds = new int[count];
        for (int i = 0; i < count; i++) {
            entityIds[i] = buf.readInt();
        }
    }

    public int[] getEntityIds() { return entityIds; }
}
