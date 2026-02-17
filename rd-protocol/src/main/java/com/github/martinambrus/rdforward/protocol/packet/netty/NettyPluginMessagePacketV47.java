package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, C2S packet 0x17: Plugin Message.
 *
 * 1.8 removed the short length prefix — data runs to end of packet.
 * Since we're inside a VarInt-framed packet, we just read remaining bytes.
 *
 * Wire format:
 *   [String] channel
 *   [byte[]] data (remaining bytes)
 */
public class NettyPluginMessagePacketV47 implements Packet {

    public NettyPluginMessagePacketV47() {}

    @Override
    public int getPacketId() { return 0x17; }

    @Override
    public void write(ByteBuf buf) {}

    @Override
    public void read(ByteBuf buf) {
        McDataTypes.readVarIntString(buf); // channel
        // Data runs to end of packet — skip all remaining bytes
        buf.skipBytes(buf.readableBytes());
    }
}
