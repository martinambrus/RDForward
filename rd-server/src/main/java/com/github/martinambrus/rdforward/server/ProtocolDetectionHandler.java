package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.codec.RawPacketDecoder;
import com.github.martinambrus.rdforward.protocol.codec.RawPacketEncoder;
import com.github.martinambrus.rdforward.protocol.packet.PacketDirection;
import com.github.martinambrus.rdforward.protocol.McDataTypes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;

/**
 * Detects whether a connecting client uses the Nati-framed protocol
 * (our custom client) or the raw MC Alpha protocol (real MC client).
 *
 * Inserted first in the pipeline, before the decoder. On the first
 * {@code channelRead}, peeks at the first byte:
 * - 0x02 (Alpha Handshake): reconfigure pipeline for raw Alpha protocol
 * - Anything else: keep the existing Nati-framed pipeline
 *
 * After detection, removes itself from the pipeline and re-fires the
 * buffered ByteBuf so the correct decoder processes it.
 */
public class ProtocolDetectionHandler extends ChannelInboundHandlerAdapter {

    private final ProtocolVersion serverVersion;
    private final ServerWorld world;
    private final PlayerManager playerManager;
    private final ChunkManager chunkManager;

    public ProtocolDetectionHandler(ProtocolVersion serverVersion, ServerWorld world,
                                    PlayerManager playerManager, ChunkManager chunkManager) {
        this.serverVersion = serverVersion;
        this.world = world;
        this.playerManager = playerManager;
        this.chunkManager = chunkManager;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof ByteBuf)) {
            super.channelRead(ctx, msg);
            return;
        }

        ByteBuf buf = (ByteBuf) msg;
        if (buf.readableBytes() < 1) {
            super.channelRead(ctx, msg);
            return;
        }

        // Peek at the first byte without consuming it
        int firstByte = buf.getUnsignedByte(buf.readerIndex());

        if (firstByte == 0xFE) {
            // Server list ping. Two formats exist:
            // - Old (Beta 1.8 - 1.3.2): client sends 0xFE alone.
            //   Response: 0xFF + String16("motd§playerCount§maxPlayers")
            // - New (1.4.2+): client sends 0xFE 0x01 (+ optional 0xFA payload).
            //   Response: 0xFF + String16("§1\0protocol\0version\0motd\0players\0max")
            //   where \0 is the null character (U+0000).
            boolean newPing = buf.readableBytes() >= 2
                    && buf.getUnsignedByte(buf.readerIndex() + 1) == 0x01;
            buf.release();

            String response;
            if (newPing) {
                response = "\u00A71\u0000"
                        + "47\u0000"
                        + "1.4.2\u0000"
                        + "RDForward Server\u0000"
                        + playerManager.getPlayerCount() + "\u0000"
                        + PlayerManager.MAX_PLAYERS;
            } else {
                response = "RDForward Server\u00A7"
                        + playerManager.getPlayerCount() + "\u00A7"
                        + PlayerManager.MAX_PLAYERS;
            }

            ByteBuf out = ctx.alloc().buffer();
            out.writeByte(0xFF); // Disconnect packet ID
            McDataTypes.writeString16(out, response);
            ctx.channel().writeAndFlush(out)
                    .addListener(io.netty.channel.ChannelFutureListener.CLOSE);
            return;
        }

        if (firstByte == 0x02 || firstByte == 0x01) {
            // 0x02 = Alpha Handshake (v14+ / post-rewrite clients)
            // 0x01 = Alpha Login (v13 / pre-rewrite clients that skip Handshake)
            // A Nati-framed client's first 4 bytes are a length prefix; a frame
            // starting with 0x01 would imply 16+ MB, which is unreasonable.
            ChannelPipeline pipeline = ctx.pipeline();

            // Replace handlers FIRST so that this handler's context.next
            // pointers get updated to the new handlers. If we removed self
            // first, context.next would point to the old (removed) decoder.
            pipeline.replace("decoder", "decoder",
                    new RawPacketDecoder(PacketDirection.CLIENT_TO_SERVER, ProtocolVersion.ALPHA_1_2_5));

            pipeline.replace("encoder", "encoder", new RawPacketEncoder());

            // Add outbound translator AFTER encoder in head-to-tail order,
            // so in the outbound direction (tail-to-head) the translator
            // receives Packet objects BEFORE the encoder encodes them.
            pipeline.addAfter("encoder", "alphaTranslator", new ClassicToAlphaTranslator());

            pipeline.replace("handler", "handler",
                    new AlphaConnectionHandler(serverVersion, world, playerManager, chunkManager));

            // Remove self from pipeline
            pipeline.remove(this);

            System.out.println("Detected raw TCP client (Alpha/Beta/Release), pipeline reconfigured");

            // Forward the ByteBuf from the pipeline HEAD so it reaches the
            // new decoder without relying on the removed context's pointers.
            pipeline.fireChannelRead(buf);
        } else {
            // Nati client — remove self and forward from HEAD
            ChannelPipeline pipeline = ctx.pipeline();
            pipeline.remove(this);
            pipeline.fireChannelRead(buf);
        }
    }
}
