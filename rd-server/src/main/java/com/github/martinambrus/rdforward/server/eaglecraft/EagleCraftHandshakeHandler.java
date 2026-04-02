package com.github.martinambrus.rdforward.server.eaglecraft;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.codec.NettyPacketDecoder;
import com.github.martinambrus.rdforward.protocol.codec.NettyPacketEncoder;
import com.github.martinambrus.rdforward.protocol.packet.ConnectionState;
import com.github.martinambrus.rdforward.server.ClassicToNettyTranslator;
import com.github.martinambrus.rdforward.server.ChunkManager;
import com.github.martinambrus.rdforward.server.NettyConnectionHandler;
import com.github.martinambrus.rdforward.server.PlayerManager;
import com.github.martinambrus.rdforward.server.PrioritizingOutboundHandler;
import com.github.martinambrus.rdforward.server.ServerWorld;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.github.martinambrus.rdforward.server.eaglecraft.EagleCraftConstants.*;

/**
 * Handles the EagleCraft pre-Minecraft handshake that occurs over WebSocket
 * before the standard MC protocol 47 login sequence begins.
 *
 * State machine:
 *   STATE_OPENED -> receive CLIENT_VERSION (0x01), send SERVER_VERSION (0x02)
 *   STATE_CLIENT_VERSION -> receive CLIENT_REQUEST_LOGIN (0x04), send SERVER_ALLOW_LOGIN (0x05)
 *   STATE_CLIENT_LOGIN -> receive CLIENT_PROFILE_DATA (0x07) repeatable, then CLIENT_FINISH_LOGIN (0x08)
 *   STATE_CLIENT_COMPLETE -> send SERVER_FINISH_LOGIN (0x09), remove self, add MC pipeline
 *
 * After completion, the pipeline is reconfigured for standard MC protocol 47
 * and the NettyConnectionHandler takes over.
 */
public class EagleCraftHandshakeHandler extends ChannelInboundHandlerAdapter {

    private final ProtocolVersion serverVersion;
    private final ServerWorld world;
    private final PlayerManager playerManager;
    private final ChunkManager chunkManager;

    private int state = STATE_OPENED;
    private String username;
    private String uuid;
    private int selectedEaglerProtocol;
    private byte[] skinData;

    public EagleCraftHandshakeHandler(ProtocolVersion serverVersion, ServerWorld world,
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
        try {
            if (buf.readableBytes() < 1) return;
            int packetType = buf.readUnsignedByte();

            switch (state) {
                case STATE_OPENED:
                    handleClientVersion(ctx, buf, packetType);
                    break;
                case STATE_CLIENT_VERSION:
                    handleClientLogin(ctx, buf, packetType);
                    break;
                case STATE_CLIENT_LOGIN:
                    handleClientProfileOrFinish(ctx, buf, packetType);
                    break;
                default:
                    sendError(ctx, "Unexpected state");
                    break;
            }
        } finally {
            buf.release();
        }
    }

    /**
     * STATE_OPENED: Expect CLIENT_VERSION (0x01).
     *
     * Wire format:
     *   [byte 0x01] already consumed
     *   [byte 0x02] legacy version marker (EagleCraft 1.8 sends 0x01 0x02)
     *   [short] eagler protocol version count (1-16)
     *   [short...] eagler protocol versions
     *   [short] MC protocol version count (1-16)
     *   [short...] MC protocol versions
     *   [byte] brand length
     *   [bytes] brand (US-ASCII)
     *   [byte] version string length
     *   [bytes] version string (US-ASCII)
     *   [boolean] auth enabled
     *   [byte] auth username length
     *   [bytes] auth username
     */
    private void handleClientVersion(ChannelHandlerContext ctx, ByteBuf buf, int packetType) {
        if (packetType != PROTOCOL_CLIENT_VERSION) {
            sendError(ctx, "Expected CLIENT_VERSION");
            return;
        }

        // Legacy version marker
        if (buf.readableBytes() < 1) { sendError(ctx, "Incomplete CLIENT_VERSION"); return; }
        int legacyByte = buf.readUnsignedByte();

        if (legacyByte == 0x02 || legacyByte == 0x03) {
            // New-format CLIENT_VERSION (v2+ clients)
            parseNewFormatClientVersion(ctx, buf);
        } else {
            // Very old format (v1) or unknown — try v1 fallback
            selectedEaglerProtocol = EAGLER_PROTOCOL_V1;
            parseV1ClientVersion(ctx, buf, legacyByte);
        }
    }

    private void parseNewFormatClientVersion(ChannelHandlerContext ctx, ByteBuf buf) {
        // Read eagler protocol versions
        int eaglerVersionCount = buf.readUnsignedShort();
        if (eaglerVersionCount < 1 || eaglerVersionCount > 16) {
            sendError(ctx, "Invalid eagler version count");
            return;
        }
        selectedEaglerProtocol = 0;
        for (int i = 0; i < eaglerVersionCount; i++) {
            int ver = buf.readUnsignedShort();
            // Pick the highest version we support (v2 or v3)
            if (ver >= EAGLER_PROTOCOL_V2 && ver <= EAGLER_PROTOCOL_V3 && ver > selectedEaglerProtocol) {
                selectedEaglerProtocol = ver;
            }
        }
        if (selectedEaglerProtocol == 0) {
            sendVersionMismatch(ctx);
            return;
        }

        // Read MC protocol versions
        int mcVersionCount = buf.readUnsignedShort();
        if (mcVersionCount < 1 || mcVersionCount > 16) {
            sendError(ctx, "Invalid MC version count");
            return;
        }
        boolean mc47Found = false;
        for (int i = 0; i < mcVersionCount; i++) {
            int mcVer = buf.readUnsignedShort();
            if (mcVer == MC_PROTOCOL_47) mc47Found = true;
        }
        if (!mc47Found) {
            sendDenyLogin(ctx, "Server requires MC protocol 47 (1.8)");
            return;
        }

        // Read brand
        int brandLen = buf.readUnsignedByte();
        String brand = buf.readCharSequence(brandLen, StandardCharsets.US_ASCII).toString();

        // Read version string
        int versionLen = buf.readUnsignedByte();
        String clientVersion = buf.readCharSequence(versionLen, StandardCharsets.US_ASCII).toString();

        // Read auth flag
        boolean authEnabled = buf.readBoolean();

        // Read auth username
        int authUserLen = buf.readUnsignedByte();
        if (authUserLen > 0) {
            buf.skipBytes(authUserLen);
        }

        System.out.println("[EagleCraft] Client connected: " + brand + " " + clientVersion
                + " (eagler v" + selectedEaglerProtocol + ", auth=" + authEnabled + ")");

        sendServerVersion(ctx);
        state = STATE_CLIENT_VERSION;
    }

    private void parseV1ClientVersion(ChannelHandlerContext ctx, ByteBuf buf, int firstDataByte) {
        // V1 format is simpler: the legacyByte we already read is the only version indicator
        // V1 clients send [0x01] [0x01] — the first 0x01 is type, second is protocol version
        selectedEaglerProtocol = EAGLER_PROTOCOL_V1;
        // Skip any remaining fields in the v1 CLIENT_VERSION
        System.out.println("[EagleCraft] V1 client connected (legacy protocol)");
        sendServerVersion(ctx);
        state = STATE_CLIENT_VERSION;
    }

    /**
     * Send SERVER_VERSION (0x02) response.
     *
     * V1/V2 wire format:
     *   [byte 0x02]
     *   [short] selected eagler protocol
     *   [short] selected MC protocol (47)
     *   [byte] server brand length
     *   [bytes] server brand (US-ASCII)
     *   [byte] server version length
     *   [bytes] server version (US-ASCII)
     *   [byte] 0x00 (auth method: none)
     *   [short] 0x0000 (auth data length: 0)
     */
    private void sendServerVersion(ChannelHandlerContext ctx) {
        byte[] brandBytes = SERVER_BRAND.getBytes(StandardCharsets.US_ASCII);
        byte[] versionBytes = SERVER_VERSION.getBytes(StandardCharsets.US_ASCII);
        int size = 1 + 2 + 2 + 1 + brandBytes.length + 1 + versionBytes.length + 1 + 2;

        ByteBuf out = ctx.alloc().buffer(size);
        out.writeByte(PROTOCOL_SERVER_VERSION);
        out.writeShort(selectedEaglerProtocol);
        out.writeShort(MC_PROTOCOL_47);
        out.writeByte(brandBytes.length);
        out.writeBytes(brandBytes);
        out.writeByte(versionBytes.length);
        out.writeBytes(versionBytes);
        out.writeByte(0x00); // no auth
        out.writeShort(0x0000); // no auth data
        ctx.writeAndFlush(out);
    }

    /**
     * STATE_CLIENT_VERSION: Expect CLIENT_REQUEST_LOGIN (0x04).
     *
     * Wire format:
     *   [byte 0x04] already consumed
     *   [byte] username length
     *   [bytes] username (US-ASCII)
     *   [byte] requested server length
     *   [bytes] requested server (US-ASCII)
     *   [byte] password length
     *   [bytes] password (raw bytes)
     */
    private void handleClientLogin(ChannelHandlerContext ctx, ByteBuf buf, int packetType) {
        if (packetType != PROTOCOL_CLIENT_REQUEST_LOGIN) {
            sendError(ctx, "Expected CLIENT_REQUEST_LOGIN");
            return;
        }

        // Read username
        int usernameLen = buf.readUnsignedByte();
        username = buf.readCharSequence(usernameLen, StandardCharsets.US_ASCII).toString();

        // Validate username
        if (username.isEmpty() || username.length() > 16 || !username.matches("^[A-Za-z0-9_]+$")) {
            sendDenyLogin(ctx, "Invalid username");
            return;
        }

        // Read requested server (ignored)
        int serverLen = buf.readUnsignedByte();
        if (serverLen > 0) buf.skipBytes(serverLen);

        // Read password (ignored, we don't do auth)
        int passLen = buf.readUnsignedByte();
        if (passLen > 0) buf.skipBytes(passLen);

        // Generate offline UUID
        uuid = ClassicToNettyTranslator.generateOfflineUuid(username);

        System.out.println("[EagleCraft] Login request from: " + username);

        sendAllowLogin(ctx);
        state = STATE_CLIENT_LOGIN;
    }

    /**
     * Send SERVER_ALLOW_LOGIN (0x05).
     *
     * Wire format:
     *   [byte 0x05]
     *   [byte] username length
     *   [bytes] username (US-ASCII)
     *   [long] UUID most significant bits
     *   [long] UUID least significant bits
     */
    private void sendAllowLogin(ChannelHandlerContext ctx) {
        byte[] usernameBytes = username.getBytes(StandardCharsets.US_ASCII);
        UUID playerUuid = UUID.fromString(uuid);

        ByteBuf out = ctx.alloc().buffer(1 + 1 + usernameBytes.length + 16);
        out.writeByte(PROTOCOL_SERVER_ALLOW_LOGIN);
        out.writeByte(usernameBytes.length);
        out.writeBytes(usernameBytes);
        out.writeLong(playerUuid.getMostSignificantBits());
        out.writeLong(playerUuid.getLeastSignificantBits());
        ctx.writeAndFlush(out);
    }

    /**
     * STATE_CLIENT_LOGIN: Expect CLIENT_PROFILE_DATA (0x07) or CLIENT_FINISH_LOGIN (0x08).
     *
     * CLIENT_PROFILE_DATA wire format:
     *   [byte 0x07] already consumed
     *   [byte] type string length
     *   [bytes] type string (US-ASCII, e.g. "skin_v1")
     *   [short] data length
     *   [bytes] data
     *
     * CLIENT_FINISH_LOGIN wire format:
     *   [byte 0x08] already consumed (no payload)
     */
    private void handleClientProfileOrFinish(ChannelHandlerContext ctx, ByteBuf buf, int packetType) {
        if (packetType == PROTOCOL_CLIENT_PROFILE_DATA) {
            // Read profile data type
            int typeLen = buf.readUnsignedByte();
            String dataType = buf.readCharSequence(typeLen, StandardCharsets.US_ASCII).toString();

            // Read data
            int dataLen = buf.readUnsignedShort();
            byte[] data = new byte[dataLen];
            buf.readBytes(data);

            if (PROFILE_DATA_TYPE_SKIN.equals(dataType) || "skin".equals(dataType)) {
                skinData = data;
                System.out.println("[EagleCraft] Received skin data from " + username
                        + " (" + data.length + " bytes, type=" + getSkinTypeDescription(data) + ")");
            }
            // May receive more profile data packets — stay in STATE_CLIENT_LOGIN
        } else if (packetType == PROTOCOL_CLIENT_FINISH_LOGIN) {
            // Client done sending profile data — complete the handshake
            state = STATE_CLIENT_COMPLETE;
            completeHandshake(ctx);
        } else {
            sendError(ctx, "Expected PROFILE_DATA or FINISH_LOGIN");
        }
    }

    private String getSkinTypeDescription(byte[] data) {
        if (data == null || data.length == 0) return "empty";
        int type = data[0] & 0xFF;
        if (type == SKIN_TYPE_PRESET) return "preset";
        if (type == SKIN_TYPE_CUSTOM) return "custom (" + (data.length - 2) + "px)";
        return "unknown(0x" + Integer.toHexString(type) + ")";
    }

    /**
     * Send SERVER_FINISH_LOGIN (0x09), store attributes, remove self from pipeline,
     * and add the standard MC Netty protocol handlers.
     */
    private void completeHandshake(ChannelHandlerContext ctx) {
        // Send SERVER_FINISH_LOGIN
        ByteBuf out = ctx.alloc().buffer(1);
        out.writeByte(PROTOCOL_SERVER_FINISH_LOGIN);
        ctx.writeAndFlush(out);

        // Store handshake results in channel attributes
        ctx.channel().attr(ATTR_EAGLER_USERNAME).set(username);
        ctx.channel().attr(ATTR_IS_EAGLECRAFT).set(Boolean.TRUE);
        if (skinData != null) {
            ctx.channel().attr(ATTR_EAGLER_SKIN).set(skinData);
        }

        // Reconfigure pipeline for MC protocol 47 PLAY state.
        // EagleCraft clients go directly to PLAY after the EagleCraft handshake —
        // no MC Handshake/Login packets, no LoginSuccess expected.
        ChannelPipeline pipeline = ctx.pipeline();

        // Remove MOTD query handler (no longer needed after handshake)
        if (pipeline.get("eaglerQuery") != null) {
            pipeline.remove("eaglerQuery");
        }

        // Inbound: wsFrameDecoder -> packetDecoder
        // Each WebSocket binary frame = one MC packet: [VarInt packetId][data]
        // No VarInt length prefix — WebSocket frame boundaries delimit packets.
        pipeline.addAfter("wsFrameDecoder", "packetDecoder",
                new NettyPacketDecoder(ConnectionState.PLAY));

        // Outbound: packetEncoder -> wsFrameEncoder(ByteBufToWebSocketFrame)
        // NettyPacketEncoder outputs ByteBuf [VarInt packetId][data],
        // ByteBufToWebSocketFrame wraps it in a BinaryWebSocketFrame.
        // No VarInt length prefix needed — one frame = one packet.
        pipeline.addAfter("wsFrameEncoder", "packetEncoder",
                new NettyPacketEncoder(ConnectionState.PLAY));

        pipeline.addAfter("packetEncoder", "prioritizer",
                new PrioritizingOutboundHandler());

        pipeline.addAfter("prioritizer", "nettyTranslator",
                new ClassicToNettyTranslator());

        // Create the connection handler AFTER packetDecoder in the pipeline.
        // Inbound flow (head-to-tail): wsdecoder → wsProtocol → wsFrameDecoder → decoder → packetDecoder → handler
        // Outbound flow (tail-to-head): handler → nettyTranslator → prioritizer → packetEncoder → encoder
        NettyConnectionHandler handler = new NettyConnectionHandler(
                serverVersion, world, playerManager, chunkManager);
        pipeline.addAfter("packetDecoder", "handler", handler);

        // Remove self before triggering login (so pipeline is clean)
        pipeline.remove(this);

        System.out.println("[EagleCraft] Handshake complete for " + username
                + ", initiating direct-to-PLAY login");
        // Debug: dump pipeline
        for (java.util.Map.Entry<String, io.netty.channel.ChannelHandler> e : pipeline) {
            System.out.println("  pipeline: " + e.getKey() + " = " + e.getValue().getClass().getSimpleName());
        }

        // Trigger the MC join sequence directly (LoginSuccess + JoinGame + chunks).
        // Use the handler's own context, not ours (we've been removed from the pipeline).
        ChannelHandlerContext handlerCtx = pipeline.context(handler);
        handler.initiateEaglecraftLogin(handlerCtx, username);
    }

    private void sendDenyLogin(ChannelHandlerContext ctx, String message) {
        byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);
        int len = Math.min(msgBytes.length, 255);

        ByteBuf out = ctx.alloc().buffer(1 + 2 + len);
        out.writeByte(PROTOCOL_SERVER_DENY_LOGIN);
        out.writeShort(len);
        out.writeBytes(msgBytes, 0, len);
        ctx.writeAndFlush(out).addListener(f -> ctx.close());
    }

    private void sendVersionMismatch(ChannelHandlerContext ctx) {
        ByteBuf out = ctx.alloc().buffer(1);
        out.writeByte(PROTOCOL_VERSION_MISMATCH);
        ctx.writeAndFlush(out).addListener(f -> ctx.close());
    }

    private void sendError(ChannelHandlerContext ctx, String message) {
        byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);
        int len = Math.min(msgBytes.length, 255);

        ByteBuf out = ctx.alloc().buffer(1 + 1 + 2 + len);
        out.writeByte(PROTOCOL_SERVER_ERROR);
        out.writeByte(0x08); // SERVER_ERROR_CUSTOM_MESSAGE
        out.writeShort(len);
        out.writeBytes(msgBytes, 0, len);
        ctx.writeAndFlush(out).addListener(f -> ctx.close());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("[EagleCraft] Handshake error for "
                + (username != null ? username : "unknown") + ": " + cause.getMessage());
        ctx.close();
    }
}
