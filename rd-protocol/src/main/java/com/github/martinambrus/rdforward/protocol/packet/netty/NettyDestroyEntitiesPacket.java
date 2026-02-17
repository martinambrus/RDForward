package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Play state, S2C packet 0x13: Destroy Entities.
 *
 * Wire format:
 *   [byte]     count
 *   [int[]]    entityIds (count entries)
 *
 * Note: despite the Netty rewrite, 1.7.2 still uses byte count + int[] here
 * (same as v39). VarInt entity IDs didn't arrive until 1.8.
 */
public class NettyDestroyEntitiesPacket implements Packet {

    private int[] entityIds;

    public NettyDestroyEntitiesPacket() {}

    public NettyDestroyEntitiesPacket(int entityId) {
        this.entityIds = new int[]{entityId};
    }

    @Override
    public int getPacketId() { return 0x13; }

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
