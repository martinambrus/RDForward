package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Play state, C2S packet 0x02: Use Entity.
 *
 * Wire format:
 *   [int]  targetEntityId
 *   [byte] action (0=interact, 1=attack)
 */
public class NettyUseEntityPacket implements Packet {

    private int targetId;
    private byte action;

    public NettyUseEntityPacket() {}

    @Override
    public int getPacketId() { return 0x02; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(targetId);
        buf.writeByte(action);
    }

    @Override
    public void read(ByteBuf buf) {
        targetId = buf.readInt();
        action = buf.readByte();
    }
}
