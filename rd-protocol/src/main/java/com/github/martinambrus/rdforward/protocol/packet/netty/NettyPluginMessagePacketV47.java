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

    private String channel;
    private byte[] data = new byte[0];

    public NettyPluginMessagePacketV47() {}

    @Override
    public int getPacketId() { return 0x17; }

    @Override
    public void write(ByteBuf buf) {}

    @Override
    public void read(ByteBuf buf) {
        this.channel = McDataTypes.readVarIntString(buf);
        int remaining = buf.readableBytes();
        this.data = new byte[remaining];
        buf.readBytes(this.data);
    }

    public String getChannel() { return channel; }
    public byte[] getData() { return data; }
}
