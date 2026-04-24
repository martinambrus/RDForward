package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Play state, C2S packet 0x17: Plugin Message.
 *
 * Wire format:
 *   [String] channel
 *   [short]  data length
 *   [byte[]] data
 */
public class NettyPluginMessagePacket implements Packet {

    private String channel;
    private byte[] data = new byte[0];

    public NettyPluginMessagePacket() {}

    @Override
    public int getPacketId() { return 0x17; }

    @Override
    public void write(ByteBuf buf) {}

    @Override
    public void read(ByteBuf buf) {
        this.channel = McDataTypes.readVarIntString(buf);
        int length = buf.readShort();
        this.data = new byte[length];
        buf.readBytes(this.data);
    }

    public String getChannel() { return channel; }
    public byte[] getData() { return data; }
}
