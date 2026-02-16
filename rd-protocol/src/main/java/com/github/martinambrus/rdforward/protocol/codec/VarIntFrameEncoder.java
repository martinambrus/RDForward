package com.github.martinambrus.rdforward.protocol.codec;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Encodes outgoing ByteBuf frames with a VarInt length prefix (1.7.2+ Netty protocol).
 *
 * Wire format: [VarInt length] [length bytes of packet data]
 * The input ByteBuf contains the raw packet data (VarInt packetId + fields).
 * This encoder prepends the VarInt length.
 */
public class VarIntFrameEncoder extends MessageToByteEncoder<ByteBuf> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) {
        int length = msg.readableBytes();
        McDataTypes.writeVarInt(out, length);
        out.writeBytes(msg);
    }
}
