package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Play state, C2S packet 0x0B: Entity Action.
 *
 * Wire format:
 *   [int]    entityId
 *   [byte]   actionId
 *   [int]    jumpBoost
 */
public class NettyEntityActionPacket implements Packet {

    private int entityId;
    private int actionId;
    private int jumpBoost;

    public NettyEntityActionPacket() {}

    @Override
    public int getPacketId() { return 0x0B; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeByte(actionId);
        buf.writeInt(jumpBoost);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        actionId = buf.readByte();
        jumpBoost = buf.readInt();
    }
}
