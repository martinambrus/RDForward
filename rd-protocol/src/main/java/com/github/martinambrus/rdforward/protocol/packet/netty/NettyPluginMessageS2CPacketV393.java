package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.13 Play state, S2C packet 0x19: Plugin Message (Custom Payload).
 *
 * In 1.13+ the channel name uses namespaced identifiers (e.g. "minecraft:brand")
 * instead of the pre-1.13 format (e.g. "MC|Brand").
 *
 * Wire format:
 *   [String] channel (namespaced identifier)
 *   [byte[]] data (remaining bytes — content depends on channel)
 */
public class NettyPluginMessageS2CPacketV393 implements Packet {

    private final String channel;
    private final byte[] data;

    public NettyPluginMessageS2CPacketV393(String channel, byte[] data) {
        this.channel = channel;
        this.data = data;
    }

    @Override
    public int getPacketId() { return 0x19; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarIntString(buf, channel);
        buf.writeBytes(data);
    }

    @Override
    public void read(ByteBuf buf) {
        // S2C only — no server-side decoding needed
    }
}
