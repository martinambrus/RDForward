package com.github.martinambrus.rdforward.server.eaglecraft;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.List;

/**
 * Combined VarInt frame encoder + WebSocket frame wrapper for EagleCraft clients.
 *
 * Takes a {@link ByteBuf} from {@link com.github.martinambrus.rdforward.protocol.codec.NettyPacketEncoder}
 * (containing [VarInt packetId][data]), prepends the VarInt length prefix, and wraps the
 * result in a {@link BinaryWebSocketFrame}.
 *
 * This replaces both {@link com.github.martinambrus.rdforward.protocol.codec.VarIntFrameEncoder}
 * and {@link ByteBufToWebSocketFrame} in the EagleCraft pipeline. The regular VarIntFrameEncoder
 * outputs two separate ByteBufs (zero-copy optimization), which would produce two WebSocket
 * frames — this encoder combines them into a single frame.
 *
 * Sharable singleton — no per-connection state.
 */
@ChannelHandler.Sharable
public class WebSocketVarIntFrameEncoder extends MessageToMessageEncoder<ByteBuf> {

    public static final WebSocketVarIntFrameEncoder INSTANCE = new WebSocketVarIntFrameEncoder();

    private WebSocketVarIntFrameEncoder() {}

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        int dataLength = msg.readableBytes();
        int varintSize = McDataTypes.varIntSize(dataLength);
        ByteBuf combined = ctx.alloc().buffer(varintSize + dataLength);
        McDataTypes.writeVarInt(combined, dataLength);
        combined.writeBytes(msg);
        out.add(new BinaryWebSocketFrame(combined));
    }
}
