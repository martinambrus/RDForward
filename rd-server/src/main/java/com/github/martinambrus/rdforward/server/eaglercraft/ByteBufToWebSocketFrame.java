package com.github.martinambrus.rdforward.server.eaglercraft;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.List;

/**
 * Wraps outbound {@link ByteBuf} into {@link BinaryWebSocketFrame}
 * so Minecraft packet data is sent as WebSocket binary frames.
 *
 * Sharable singleton — no per-connection state.
 */
@ChannelHandler.Sharable
public class ByteBufToWebSocketFrame extends MessageToMessageEncoder<ByteBuf> {

    public static final ByteBufToWebSocketFrame INSTANCE = new ByteBufToWebSocketFrame();

    private ByteBufToWebSocketFrame() {}

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        out.add(new BinaryWebSocketFrame(msg.retain()));
    }
}
