package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.1+ protocol 0xFA (bidirectional): Custom Payload (Plugin Message).
 *
 * Wire format:
 *   [string16] channel name
 *   [short]    data length
 *   [byte[]]   data (length bytes)
 *
 * Silently consumed on the server side (no plugin channels implemented).
 */
public class CustomPayloadPacket implements Packet {

    private String channel;
    private byte[] data;

    public CustomPayloadPacket() {}

    @Override
    public int getPacketId() {
        return 0xFA;
    }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeStringAdaptive(buf, channel);
        buf.writeShort(data != null ? data.length : 0);
        if (data != null) {
            buf.writeBytes(data);
        }
    }

    @Override
    public void read(ByteBuf buf) {
        channel = McDataTypes.readStringAdaptive(buf);
        short length = buf.readShort();
        data = new byte[length];
        buf.readBytes(data);
    }

    public String getChannel() { return channel; }
    public byte[] getData() { return data; }
}
