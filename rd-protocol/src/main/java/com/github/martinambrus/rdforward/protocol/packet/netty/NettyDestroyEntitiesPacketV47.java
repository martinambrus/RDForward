package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, S2C packet 0x13: Destroy Entities.
 *
 * 1.8 changed count from byte to VarInt and entityIds from int[] to VarInt[].
 *
 * Wire format:
 *   [VarInt]   count
 *   [VarInt[]] entityIds
 */
public class NettyDestroyEntitiesPacketV47 implements Packet {

    private int[] entityIds;

    public NettyDestroyEntitiesPacketV47() {}

    public NettyDestroyEntitiesPacketV47(int entityId) {
        this.entityIds = new int[]{entityId};
    }

    @Override
    public int getPacketId() { return 0x13; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, entityIds.length);
        for (int id : entityIds) {
            McDataTypes.writeVarInt(buf, id);
        }
    }

    @Override
    public void read(ByteBuf buf) {
        int count = McDataTypes.readVarInt(buf);
        entityIds = new int[count];
        for (int i = 0; i < count; i++) {
            entityIds[i] = McDataTypes.readVarInt(buf);
        }
    }
}
