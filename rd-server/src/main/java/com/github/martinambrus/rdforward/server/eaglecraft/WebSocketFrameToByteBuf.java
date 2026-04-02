package com.github.martinambrus.rdforward.server.eaglecraft;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.List;

/**
 * Unwraps inbound {@link BinaryWebSocketFrame} to raw {@link ByteBuf}
 * so downstream Minecraft packet decoders see the same input as TCP clients.
 *
 * Sharable singleton — no per-connection state.
 */
@ChannelHandler.Sharable
public class WebSocketFrameToByteBuf extends MessageToMessageDecoder<BinaryWebSocketFrame> {

    public static final WebSocketFrameToByteBuf INSTANCE = new WebSocketFrameToByteBuf();

    private WebSocketFrameToByteBuf() {}

    @Override
    protected void decode(ChannelHandlerContext ctx, BinaryWebSocketFrame frame, List<Object> out) {
        out.add(frame.content().retain());
    }
}
