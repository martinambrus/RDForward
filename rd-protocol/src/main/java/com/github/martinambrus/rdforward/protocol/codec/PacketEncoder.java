package com.github.martinambrus.rdforward.protocol.codec;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Encodes a Packet object into bytes for transmission.
 *
 * Wire format:
 *   [4 bytes] total packet length (excluding this field)
 *   [1 byte]  packet type ID
 *   [N bytes] packet payload
 *
 * The length prefix enables the decoder to frame messages
 * correctly over the TCP stream.
 */
public class PacketEncoder extends MessageToByteEncoder<Packet> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) {
        // Write payload to a temporary buffer to measure length
        ByteBuf payload = ctx.alloc().buffer();
        try {
            payload.writeByte(packet.getType().getId());
            packet.write(payload);

            // Write length prefix + payload
            out.writeInt(payload.readableBytes());
            out.writeBytes(payload);
        } finally {
            payload.release();
        }
    }
}
