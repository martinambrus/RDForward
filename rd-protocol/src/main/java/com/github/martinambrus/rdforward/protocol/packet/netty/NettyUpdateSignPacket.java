package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Play state, C2S packet 0x12: Update Sign.
 *
 * Wire format:
 *   [int]    x
 *   [short]  y
 *   [int]    z
 *   [String] line1
 *   [String] line2
 *   [String] line3
 *   [String] line4
 */
public class NettyUpdateSignPacket implements Packet {

    public NettyUpdateSignPacket() {}

    @Override
    public int getPacketId() { return 0x12; }

    @Override
    public void write(ByteBuf buf) {}

    @Override
    public void read(ByteBuf buf) {
        buf.skipBytes(4); // x
        buf.skipBytes(2); // y
        buf.skipBytes(4); // z
        McDataTypes.readVarIntString(buf); // line1
        McDataTypes.readVarIntString(buf); // line2
        McDataTypes.readVarIntString(buf); // line3
        McDataTypes.readVarIntString(buf); // line4
    }
}
