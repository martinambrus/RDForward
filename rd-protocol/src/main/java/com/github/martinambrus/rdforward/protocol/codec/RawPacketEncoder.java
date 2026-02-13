package com.github.martinambrus.rdforward.protocol.codec;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Encodes Packet objects into raw Minecraft wire format (no length prefix).
 *
 * Wire format: [1 byte packetId][payload]
 *
 * This is the format expected by real MC Classic/Alpha clients, which do
 * not use the 4-byte Nati length prefix that our custom client uses.
 */
public class RawPacketEncoder extends MessageToByteEncoder<Packet> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) {
        out.writeByte(packet.getPacketId());
        packet.write(out);
    }
}
