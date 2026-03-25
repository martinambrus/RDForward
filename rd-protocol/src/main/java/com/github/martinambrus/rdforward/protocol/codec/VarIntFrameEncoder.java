package com.github.martinambrus.rdforward.protocol.codec;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * Encodes outgoing ByteBuf frames with a VarInt length prefix (1.7.2+ Netty protocol).
 *
 * Wire format: [VarInt length] [length bytes of packet data]
 *
 * Zero-copy optimization: outputs two messages (length ByteBuf + retained original
 * data) instead of copying the entire packet body into a new buffer. This is safe
 * because downstream handlers (cipher, flush consolidation) process each message
 * independently and AES/CFB8 stream cipher state carries over between calls.
 *
 * Sharable singleton since it has no per-connection state.
 * Derived from Velocity/Krypton's MinecraftVarintPrepender.
 */
@ChannelHandler.Sharable
public class VarIntFrameEncoder extends MessageToMessageEncoder<ByteBuf> {

    public static final VarIntFrameEncoder INSTANCE = new VarIntFrameEncoder();

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        int length = msg.readableBytes();
        int varintLength = McDataTypes.varIntSize(length);

        ByteBuf lenBuf = ctx.alloc().heapBuffer(varintLength);
        McDataTypes.writeVarInt(lenBuf, length);
        out.add(lenBuf);
        out.add(msg.retain());
    }
}
