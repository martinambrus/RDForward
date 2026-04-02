package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.codec.NettyPacketDecoder;
import com.github.martinambrus.rdforward.protocol.codec.NettyPacketEncoder;
import com.github.martinambrus.rdforward.protocol.codec.RawPacketDecoder;
import com.github.martinambrus.rdforward.protocol.codec.RawPacketEncoder;
import com.github.martinambrus.rdforward.protocol.codec.VarIntFrameDecoder;
import com.github.martinambrus.rdforward.protocol.codec.VarIntFrameEncoder;
import com.github.martinambrus.rdforward.protocol.packet.ConnectionState;
import com.github.martinambrus.rdforward.protocol.packet.PacketDirection;
import com.github.martinambrus.rdforward.protocol.packet.PacketRegistry;
import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.server.api.ServerProperties;
import com.github.martinambrus.rdforward.server.eaglercraft.EaglerCraftPipelineConfigurer;
import com.github.martinambrus.rdforward.server.lce.LCEConnectionHandler;
import com.github.martinambrus.rdforward.server.lce.ClassicToLCETranslator;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.flush.FlushConsolidationHandler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

    /** LCE small ID counter — assigns unique IDs to LCE clients (0-254). */
    private static final AtomicInteger lceSmallIdCounter = new AtomicInteger(0);

    /** How long to wait (ms) for client data before assuming LCE (server-sends-first). */
    private static final int LCE_DETECT_DELAY_MS = 300;

    private final ProtocolVersion serverVersion;
    private final ServerWorld world;
    private final PlayerManager playerManager;
    private final ChunkManager chunkManager;

    private volatile boolean dataReceived = false;
    private ScheduledFuture<?> lceDetectTask;

    public ProtocolDetectionHandler(ProtocolVersion serverVersion, ServerWorld world,
                                    PlayerManager playerManager, ChunkManager chunkManager) {
        this.serverVersion = serverVersion;
        this.world = world;
        this.playerManager = playerManager;
        this.chunkManager = chunkManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        // Schedule LCE detection: if no data arrives within the delay,
        // assume it's an LCE client (which waits for server to send a small ID first).
        lceDetectTask = ctx.executor().schedule(() -> {
            if (!dataReceived && ctx.channel().isActive()) {
                configureLCEPipeline(ctx);
            }
        }, LCE_DETECT_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    private void configureLCEPipeline(ChannelHandlerContext ctx) {
        PacketRegistry.ensureLCERegistered();
        ChannelPipeline pipeline = ctx.pipeline();

        // Send 1-byte small ID to the client (LCE expects this before any packets)
        int smallId = (lceSmallIdCounter.getAndIncrement() & 0x7FFFFFFF) % 255;
        ByteBuf idBuf = ctx.alloc().buffer(1);
        idBuf.writeByte(smallId);
        ctx.writeAndFlush(idBuf);

        // LCE uses 4-byte big-endian length-prefixed framing
        pipeline.replace("decoder", "decoder",
                new LengthFieldBasedFrameDecoder(4 * 1024 * 1024, 0, 4, 0, 4));
        pipeline.replace("encoder", "encoder", new LengthFieldPrepender(4));

        // Add packet-level codec after frame decoder (reads packet ID + fields from frame)
        RawPacketDecoder packetDecoder = new RawPacketDecoder(
                PacketDirection.CLIENT_TO_SERVER, ProtocolVersion.LCE_TU19);
        packetDecoder.setUseString16(true);
        packetDecoder.setSkipUnknownPackets(true);
        pipeline.addAfter("decoder", "packetDecoder", packetDecoder);

        // Add packet-level encoder after frame encoder
        RawPacketEncoder packetEncoder = new RawPacketEncoder();
        packetEncoder.setUseString16(true);
        pipeline.addAfter("encoder", "packetEncoder", packetEncoder);

        pipeline.addBefore("decoder", "flushConsolidation",
                new FlushConsolidationHandler(256, true));
        pipeline.addAfter("packetEncoder", "prioritizer", new PrioritizingOutboundHandler());
        pipeline.addAfter("prioritizer", "lceTranslator", new ClassicToLCETranslator());
        pipeline.replace("handler", "handler",
                new LCEConnectionHandler(serverVersion, world, playerManager, chunkManager));

        pipeline.remove(this);

        System.out.println("Detected LCE client (no initial data, sent small ID "
                + smallId + "), pipeline reconfigured");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // Cancel LCE timeout detection — client sent data first (not LCE)
        dataReceived = true;
        if (lceDetectTask != null) {
            lceDetectTask.cancel(false);
        }

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
            // Server list ping. Three formats exist:
            // - Old (Beta 1.8 - 1.3.2): client sends 0xFE alone.
            //   Response: 0xFF + String16("motd§playerCount§maxPlayers")
            // - New (1.4.2 - 1.5.2): client sends 0xFE 0x01 (no payload).
            //   Response: 0xFF + String16("§1\0protocol\0version\0motd\0players\0max")
            // - New with MC|PingHost (1.6+): client sends 0xFE 0x01 0xFA + payload
            //   containing the client's protocol version. Response same as above,
            //   but we mirror the client's version so every client sees "compatible".
            boolean newPing = buf.readableBytes() >= 2
                    && buf.getUnsignedByte(buf.readerIndex() + 1) == 0x01;

            // Try to parse client protocol version from MC|PingHost (1.6+ only).
            int clientProtocol = -1;
            if (newPing && buf.readableBytes() > 2
                    && buf.getUnsignedByte(buf.readerIndex() + 2) == 0xFA) {
                int savedIndex = buf.readerIndex();
                try {
                    buf.skipBytes(3); // FE 01 FA
                    int channelNameLen = buf.readUnsignedShort();
                    buf.skipBytes(channelNameLen * 2); // UTF-16BE channel name
                    buf.readShort(); // data length
                    clientProtocol = buf.readUnsignedByte();
                } catch (Exception e) {
                    // Buffer underflow — fall back to default
                }
                buf.readerIndex(savedIndex);
            }
            buf.release();

            String response;
            if (newPing) {
                // Mirror the client's protocol version so it sees "compatible".
                // 1.6+ sends MC|PingHost with the client's version.
                // 1.4.2-1.5.2 sends no MC|PingHost — we can't detect the exact
                // version, so default to 51 (1.4.7), the highest 1.4.x protocol.
                // Non-matching versions will show incompatible but can use
                // Direct Connect.
                int reportProtocol = clientProtocol > 0 ? clientProtocol : 51;
                String reportVersion = clientProtocol > 0
                        ? pingVersionString(clientProtocol) : "1.4.7";
                // For 1.4.2-1.5.2 (no MC|PingHost), show hint about Direct Connect
                // since some versions in this range will see "incompatible".
                String motd = clientProtocol > 0
                        ? ServerProperties.getMotd()
                        : "Incompatible? Use Direct Connect";
                // Strip null chars so MOTD can't break the \u0000-delimited response
                motd = motd.replace("\u0000", "");
                response = "\u00A71\u0000"
                        + reportProtocol + "\u0000"
                        + reportVersion + "\u0000"
                        + motd + "\u0000"
                        + playerManager.getPlayerCount() + "\u0000"
                        + PlayerManager.getMaxPlayers();
            } else {
                // Old ping (Beta 1.8 - 1.3.2): no version field.
                // Strip section signs so MOTD can't break the \u00A7-delimited response
                String motd = ServerProperties.getMotd().replace("\u00A7", "");
                response = motd + "\u00A7"
                        + playerManager.getPlayerCount() + "\u00A7"
                        + PlayerManager.getMaxPlayers();
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

            // Consolidate outbound flushes: placed before decoder (head of pipeline)
            // so in outbound direction it's the last handler before the socket,
            // catching ALL flushes including mid-tick writeAndFlush() calls.
            pipeline.addBefore("decoder", "flushConsolidation",
                    new FlushConsolidationHandler(256, true));

            // Add packet prioritizer AFTER encoder in head-to-tail order.
            // In the outbound direction (tail-to-head), the prioritizer
            // buffers writes from the translator and reorders by priority on flush.
            pipeline.addAfter("encoder", "prioritizer", new PrioritizingOutboundHandler());

            // Add outbound translator AFTER prioritizer in head-to-tail order,
            // so in the outbound direction (tail-to-head) the translator
            // receives Packet objects BEFORE the prioritizer buffers them.
            pipeline.addAfter("prioritizer", "alphaTranslator", new ClassicToAlphaTranslator());

            pipeline.replace("handler", "handler",
                    new AlphaConnectionHandler(serverVersion, world, playerManager, chunkManager));

            // Remove self from pipeline
            pipeline.remove(this);

            System.out.println("Detected raw TCP client (Alpha/Beta/Release), pipeline reconfigured");

            // Forward the ByteBuf from the pipeline HEAD so it reaches the
            // new decoder without relying on the removed context's pointers.
            pipeline.fireChannelRead(buf);
        } else if (firstByte > 0x02 && buf.readableBytes() >= 2
                && buf.getUnsignedByte(buf.readerIndex() + 1) == 0x00) {
            // 1.7.2+ Netty client: first byte is VarInt packet length (> 2 for any
            // reasonable Handshake), second byte is packet ID 0x00 (Handshake).
            // A Nati-framed first message with byte[0] > 0x02 would imply >50MB frame.
            ChannelPipeline pipeline = ctx.pipeline();

            // Replace frame-level codecs
            pipeline.replace("decoder", "decoder", new VarIntFrameDecoder());
            pipeline.replace("encoder", "encoder", VarIntFrameEncoder.INSTANCE);

            // Consolidate outbound flushes (same rationale as Alpha branch)
            pipeline.addBefore("decoder", "flushConsolidation",
                    new FlushConsolidationHandler(256, true));

            // Add packet-level codecs after frame codecs
            pipeline.addAfter("decoder", "packetDecoder",
                    new NettyPacketDecoder(ConnectionState.HANDSHAKING));
            pipeline.addAfter("encoder", "packetEncoder",
                    new NettyPacketEncoder(ConnectionState.HANDSHAKING));

            // Add packet prioritizer after packet encoder
            pipeline.addAfter("packetEncoder", "prioritizer",
                    new PrioritizingOutboundHandler());

            // Add outbound translator (Classic→Netty) after prioritizer
            pipeline.addAfter("prioritizer", "nettyTranslator",
                    new ClassicToNettyTranslator());

            // Replace handler
            pipeline.replace("handler", "handler",
                    new NettyConnectionHandler(serverVersion, world, playerManager, chunkManager));

            pipeline.remove(this);

            pipeline.fireChannelRead(buf);
        } else if (firstByte == 0x00 && buf.readableBytes() >= 2
                && buf.getUnsignedByte(buf.readerIndex() + 1) != 0x00) {
            // Real Classic client: first byte is packet ID 0x00 (PlayerIdentification).
            // Second byte is either a protocol version (0x01-0x07 for Classic v3-v7)
            // or the first character of a username (>= 0x20 for 0.0.15a, which has
            // no protocol version byte).
            // A Nati-framed client's first 4 bytes are a big-endian length prefix;
            // for a 131-byte identification, bytes are 0x00 0x00 0x00 0x83 — so
            // byte[1] == 0x00 distinguishes Nati from real Classic.
            int secondByte = buf.getUnsignedByte(buf.readerIndex() + 1);
            boolean isClassic015a = secondByte >= 0x20; // printable ASCII = username, not version

            ProtocolVersion classicVersion;
            if (isClassic015a) {
                classicVersion = ProtocolVersion.CLASSIC_0_0_15A;
            } else if (secondByte == 6) {
                // Protocol version 6: Classic 0.0.20a-0.27 (HAS unused/userType
                // trailing byte in identification, but no UpdateUserType packet)
                classicVersion = ProtocolVersion.CLASSIC_0_0_20A;
            } else if (secondByte < 7) {
                // Protocol version 3-5: Classic 0.0.16a-0.0.19a (no unused byte
                // in identification, no UpdateUserType packet)
                classicVersion = ProtocolVersion.CLASSIC_0_0_16A;
            } else {
                classicVersion = ProtocolVersion.CLASSIC;
            }

            ChannelPipeline pipeline = ctx.pipeline();

            pipeline.replace("decoder", "decoder",
                    new RawPacketDecoder(PacketDirection.CLIENT_TO_SERVER, classicVersion));
            pipeline.replace("encoder", "encoder", new RawPacketEncoder());

            pipeline.addBefore("decoder", "flushConsolidation",
                    new FlushConsolidationHandler(256, true));

            // No translator needed for Classic v7 (native internal protocol).
            // For 0.0.15a, ServerConnectionHandler inserts the VersionTranslator
            // after login when it detects the version mismatch.

            // Keep ServerConnectionHandler — it already handles Classic packets.
            // No handler replacement needed.

            pipeline.remove(this);

            String classicDesc = isClassic015a ? " (0.0.15a — no protocol version byte)"
                    : " (v" + secondByte + ")";
            System.out.println("Detected real Classic client" + classicDesc
                    + ", pipeline reconfigured");

            pipeline.fireChannelRead(buf);
        } else if (firstByte == 0x47) {
            // 0x47 = 'G' from HTTP "GET /" — EaglerCraft WebSocket upgrade request.
            // Delegate to EaglerCraftPipelineConfigurer (lazy-loading boundary:
            // HTTP/WebSocket classes are not loaded until this branch executes).
            EaglerCraftPipelineConfigurer.configure(
                    ctx, buf, serverVersion, world, playerManager, chunkManager);
            return;
        } else {
            // Nati client — remove self and forward from HEAD
            ChannelPipeline pipeline = ctx.pipeline();
            pipeline.remove(this);
            pipeline.fireChannelRead(buf);
        }
    }

    /**
     * Map a protocol version number to a game version string for server list display.
     * Returns the latest game version for a given protocol number.
     */
    private static String pingVersionString(int protocolVersion) {
        switch (protocolVersion) {
            case 78: return "1.6.4";
            case 74: return "1.6.2";
            case 73: return "1.6.2";
            case 61: return "1.5.2";
            case 60: return "1.5.1";
            case 51: return "1.4.7";
            case 49: return "1.4.5";
            case 47: return "1.4.2";
            case 39: return "1.3.2";
            case 4: return "1.7.5";
            case 5: return "1.7.10";
            default: return "RDForward";
        }
    }
}
