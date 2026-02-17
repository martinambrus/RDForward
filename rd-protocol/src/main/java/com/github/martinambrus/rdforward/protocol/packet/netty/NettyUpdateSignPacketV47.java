package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, C2S packet 0x12: Update Sign.
 *
 * 1.8 changed separate x/y/z to a packed Position long.
 * Sign lines are now JSON text components instead of plain strings.
 *
 * Wire format:
 *   [Position] location (packed long)
 *   [String]   line1 (JSON)
 *   [String]   line2 (JSON)
 *   [String]   line3 (JSON)
 *   [String]   line4 (JSON)
 */
public class NettyUpdateSignPacketV47 implements Packet {

    public NettyUpdateSignPacketV47() {}

    @Override
    public int getPacketId() { return 0x12; }

    @Override
    public void write(ByteBuf buf) {}

    @Override
    public void read(ByteBuf buf) {
        buf.skipBytes(8); // Position long
        McDataTypes.readVarIntString(buf); // line1
        McDataTypes.readVarIntString(buf); // line2
        McDataTypes.readVarIntString(buf); // line3
        McDataTypes.readVarIntString(buf); // line4
    }
}
