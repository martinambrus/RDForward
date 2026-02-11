package com.github.martinambrus.rdforward.protocol.codec;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Encodes a Packet object into bytes for transmission.
 *
 * Wire format (Nati framing — our Netty extension over raw MC protocol):
 *   [4 bytes] total packet length (excluding this field itself)
 *   [1 byte]  packet ID (matches real MC Classic/Alpha IDs)
 *   [N bytes] packet payload (MC-compatible field structure)
 *
 * The 4-byte length prefix is our addition for reliable Netty framing.
 * The packet ID and payload match the real Minecraft protocol structure,
 * so the actual content is compatible with MC clients/servers.
 *
 * Note: Real MC Classic/Alpha has NO length prefix (the parser must know
 * each packet's layout). Real MC 1.7+ uses VarInt length prefix + VarInt
 * packet ID. Our format sits in between for simplicity with Netty.
 */
public class PacketEncoder extends MessageToByteEncoder<Packet> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) {
        // Write payload to a temporary buffer to measure length
        ByteBuf payload = ctx.alloc().buffer();
        try {
            payload.writeByte(packet.getPacketId());
            packet.write(payload);

            // Write length prefix + payload
            out.writeInt(payload.readableBytes());
            out.writeBytes(payload);
        } finally {
            payload.release();
        }
    }
}
