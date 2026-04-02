package com.github.martinambrus.rdforward.server.eaglecraft;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.server.ChunkManager;
import com.github.martinambrus.rdforward.server.PlayerManager;
import com.github.martinambrus.rdforward.server.ServerWorld;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.flush.FlushConsolidationHandler;

/**
 * Configures the Netty pipeline for an EagleCraft WebSocket client.
 *
 * This class is the lazy-loading boundary: all {@code io.netty.handler.codec.http.*}
 * imports live here and are NOT loaded by the JVM until a WebSocket client actually
 * connects (when {@link #configure} is first called from ProtocolDetectionHandler).
 *
 * After configuration, the pipeline looks like:
 * <pre>
 * Inbound:  flushConsolidation -> httpCodec -> httpAggregator -> wsProtocol -> wsFrameDecoder -> eaglerHandshake
 * Outbound: wsFrameEncoder -> httpCodec
 * </pre>
 *
 * Once the EagleCraft handshake completes, {@link EagleCraftHandshakeHandler} removes
 * itself and adds the standard MC protocol codecs (NettyPacketDecoder/Encoder etc.).
 */
public final class EagleCraftPipelineConfigurer {

    private EagleCraftPipelineConfigurer() {}

    /**
     * Reconfigure the pipeline for a WebSocket connection.
     *
     * Called from {@code ProtocolDetectionHandler} when the first byte is 0x47 ('G'),
     * indicating an HTTP request (WebSocket upgrade).
     *
     * @param ctx the channel handler context (from ProtocolDetectionHandler)
     * @param buf the ByteBuf containing the HTTP GET request (not consumed)
     * @param serverVersion the server's protocol version
     * @param world the server world
     * @param playerManager the player manager
     * @param chunkManager the chunk manager
     */
    public static void configure(ChannelHandlerContext ctx, ByteBuf buf,
                                  ProtocolVersion serverVersion, ServerWorld world,
                                  PlayerManager playerManager, ChunkManager chunkManager) {
        ChannelPipeline pipeline = ctx.pipeline();

        // Replace the initial frame codecs with HTTP codec.
        // HttpServerCodec is a combined HTTP request decoder + response encoder.
        pipeline.replace("decoder", "httpCodec", new HttpServerCodec());

        // Remove the initial encoder (HttpServerCodec handles outbound HTTP responses,
        // and after upgrade, WebSocket frames handle outbound data)
        pipeline.remove("encoder");

        // Aggregate HTTP request parts into a single FullHttpRequest for the upgrade
        pipeline.addAfter("httpCodec", "httpAggregator",
                new HttpObjectAggregator(65536));

        // WebSocket protocol handler: handles the HTTP 101 upgrade automatically.
        // Constructor: (path, subprotocols, allowExtensions, maxFrameSize, checkStartsWith)
        // checkStartsWith=true means any path starting with "/" is accepted
        // (i.e. all paths), matching EagleCraft's official server behavior.
        // After upgrade, inbound messages become BinaryWebSocketFrame/TextWebSocketFrame.
        pipeline.addAfter("httpAggregator", "wsProtocol",
                new WebSocketServerProtocolHandler("/", null, true, 65536, true));

        // Consolidate outbound flushes
        pipeline.addBefore("httpCodec", "flushConsolidation",
                new FlushConsolidationHandler(256, true));

        // EagleCraft MOTD query handler: intercepts TextWebSocketFrame "accept:motd"
        // for server list display. Binary frames pass through to the handshake handler.
        pipeline.addAfter("wsProtocol", "eaglerQuery",
                new EagleCraftQueryHandler(playerManager));

        // Inbound: unwrap BinaryWebSocketFrame -> ByteBuf
        pipeline.addAfter("eaglerQuery", "wsFrameDecoder",
                WebSocketFrameToByteBuf.INSTANCE);

        // Outbound: wrap ByteBuf -> BinaryWebSocketFrame
        // Placed after httpCodec in head-to-tail order so in outbound direction
        // (tail-to-head) it wraps ByteBuf BEFORE the HTTP/WS codec sees it.
        pipeline.addAfter("httpCodec", "wsFrameEncoder",
                ByteBufToWebSocketFrame.INSTANCE);

        // EagleCraft handshake handler: processes the pre-MC handshake,
        // then removes itself and adds NettyPacketDecoder/Encoder/handler.
        pipeline.addAfter("wsFrameDecoder", "eaglerHandshake",
                new EagleCraftHandshakeHandler(serverVersion, world, playerManager, chunkManager));

        // Remove the old ServerConnectionHandler (Nati/Classic handler)
        pipeline.remove("handler");

        // Remove ProtocolDetectionHandler
        pipeline.remove(ctx.handler());

        System.out.println("Detected WebSocket client (EagleCraft), pipeline reconfigured");

        // Re-fire the ByteBuf from pipeline HEAD so HttpServerCodec processes
        // the HTTP GET request for the WebSocket upgrade.
        pipeline.fireChannelRead(buf);
    }
}
