package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Play state, C2S packet 0x14: Tab-Complete.
 *
 * Wire format: [String] text
 */
public class NettyTabCompletePacket implements Packet {

    public NettyTabCompletePacket() {}

    @Override
    public int getPacketId() { return 0x14; }

    @Override
    public void write(ByteBuf buf) {}

    @Override
    public void read(ByteBuf buf) {
        McDataTypes.readVarIntString(buf);
    }
}
