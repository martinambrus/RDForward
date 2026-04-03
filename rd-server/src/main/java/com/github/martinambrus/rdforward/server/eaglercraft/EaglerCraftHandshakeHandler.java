package com.github.martinambrus.rdforward.server.eaglercraft;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.codec.NettyPacketDecoder;
import com.github.martinambrus.rdforward.protocol.codec.NettyPacketEncoder;
import com.github.martinambrus.rdforward.protocol.codec.RawPacketDecoder;
import com.github.martinambrus.rdforward.protocol.codec.RawPacketEncoder;
import com.github.martinambrus.rdforward.protocol.packet.ConnectionState;
import com.github.martinambrus.rdforward.protocol.packet.PacketDirection;
import com.github.martinambrus.rdforward.server.AlphaConnectionHandler;
import com.github.martinambrus.rdforward.server.ClassicToAlphaTranslator;
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
import java.util.regex.Pattern;

import static com.github.martinambrus.rdforward.server.eaglercraft.EaglerCraftConstants.*;

/**
 * Handles the EaglerCraft pre-Minecraft handshake that occurs over WebSocket
 * before the standard MC protocol begins.
 *
 * <h2>Connection Path Disambiguation</h2>
 *
 * EaglerCraft clients connect via four distinct paths, distinguished by the
 * first two bytes of the first WebSocket binary frame:
 *
 * <pre>
 *   First byte of frame
 *   |
 *   +-- 0x02  → Raw MC Handshake (EaglerCraft 1.5.2 / Beta 1.7.3)
 *   |           Sends pre-Netty MC packets directly over WebSocket.
 *   |           Pipeline → AlphaConnectionHandler (version resolved from MC Handshake).
 *   |
 *   +-- 0x01 (CLIENT_VERSION) → Read second byte ("legacyByte")
 *       |
 *       +-- 0x02 or 0x03 → EaglerCraft v2/v3 handshake (1.8.8 / 1.12.2 / Beta 1.7.3 / Beta 1.3 / Alpha 1.2.6)
 *       |                   Full handshake: CLIENT_VERSION → LOGIN → FINISH_LOGIN.
 *       |                   MC protocol negotiated from version list (6, 9, 14, 47, 340).
 *       |                   Pipeline → AlphaConnectionHandler (protocol 6/9/14) or
 *       |                              NettyConnectionHandler (protocol 47/340).
 *       |
 *       +-- 0x00          → Raw MC Login Request (PeytonPlayz595 Beta 1.7.3)
 *       |                   0x00 is the first byte of a big-endian int protocol version.
 *       |                   Skips both EaglerCraft and MC Handshake, sends Login (0x01) directly.
 *       |                   Pipeline → AlphaConnectionHandler (Beta 1.7.3, direct-to-PLAY).
 *       |
 *       +-- other         → EaglerCraft v1 (legacy)
 *                           Compact 2-byte handshake. MC protocol may be explicit (appended
 *                           short) or ambiguous. If ambiguous, V1McProtocolDetector inspects
 *                           the first inbound MC frame or falls back to Netty 1.8 after 500ms.
 * </pre>
 *
 * <h2>State Machine (v2/v3 and v1 paths)</h2>
 *
 * For EaglerCraftX (1.8.8/1.12.2) and v1 clients:
 *   STATE_OPENED → receive CLIENT_VERSION (0x01), send SERVER_VERSION (0x02)
 *   STATE_CLIENT_VERSION → receive CLIENT_REQUEST_LOGIN (0x04), send SERVER_ALLOW_LOGIN (0x05)
 *   STATE_CLIENT_LOGIN → receive CLIENT_PROFILE_DATA (0x07) repeatable, then CLIENT_FINISH_LOGIN (0x08)
 *   STATE_CLIENT_COMPLETE → send SERVER_FINISH_LOGIN (0x09), remove self, add MC pipeline
 *
 * For EaglerCraft 1.5.2 / raw MC Login clients:
 *   The first binary frame is detected and the pipeline is reconfigured immediately
 *   without going through the EaglerCraft state machine.
 */
public class EaglerCraftHandshakeHandler extends ChannelInboundHandlerAdapter {

    private final ProtocolVersion serverVersion;
    private final ServerWorld world;
    private final PlayerManager playerManager;
    private final ChunkManager chunkManager;

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]+$");

    /** Pre-Netty MC protocols that use RawPacketDecoder + AlphaConnectionHandler. */
    private static boolean isPreNettyMcProtocol(int ver) {
        return ver == MC_PROTOCOL_6 || ver == MC_PROTOCOL_9 || ver == MC_PROTOCOL_14;
    }

    /** MC protocol versions this server supports for EaglerCraft clients. */
    private static boolean isSupportedMcProtocol(int ver) {
        return isPreNettyMcProtocol(ver) || ver == MC_PROTOCOL_47 || ver == MC_PROTOCOL_340;
    }

    private int state = STATE_OPENED;
    private String username;
    private String uuid;
    private int selectedEaglerProtocol;
    private int selectedMcProtocol;
    private boolean v1McProtocolExplicit;
    private byte[] skinData;

    public EaglerCraftHandshakeHandler(ProtocolVersion serverVersion, ServerWorld world,
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
     * STATE_OPENED: Expect CLIENT_VERSION (0x01) for EaglerCraftX,
     * or MC Handshake (0x02) for EaglerCraft 1.5.2.
     *
     * EaglerCraftX wire format:
     *   [byte 0x01] already consumed
     *   [byte 0x02] legacy version marker (EaglerCraft 1.8 sends 0x01 0x02)
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
     *
     * EaglerCraft 1.5.2 wire format:
     *   [byte 0x02] MC pre-Netty Handshake packet ID, already consumed
     *   Standard pre-Netty handshake fields follow
     */
    private void handleClientVersion(ChannelHandlerContext ctx, ByteBuf buf, int packetType) {
        if (packetType == 0x02) {
            // Pre-Netty MC Handshake (0x02) — EaglerCraft 1.5.2 client.
            // This is NOT an EaglerCraftX CLIENT_VERSION packet.
            // Reconfigure the pipeline for pre-Netty MC protocol.
            handleEaglerCraft152(ctx, buf);
            return;
        }

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
        } else if (legacyByte == 0x00 && buf.readableBytes() >= 3) {
            // legacyByte 0x00 is the first byte of a big-endian int (MC protocol version).
            // This is a raw MC Login Request (0x01) sent directly over WebSocket —
            // e.g. PeytonPlayz595 Beta 1.7.3 skips both EaglerCraft and MC Handshake.
            handleRawMcLogin(ctx, buf);
        } else {
            // Very old format (v1) or unknown — try v1 fallback
            selectedEaglerProtocol = EAGLER_PROTOCOL_V1;
            parseV1ClientVersion(ctx, buf, legacyByte);
        }
    }

    /**
     * Handle an EaglerCraft 1.5.2 client.
     *
     * EaglerCraft 1.5.2 sends standard MC pre-Netty packets directly over WebSocket
     * (no custom EaglerCraft handshake). The first byte (0x02) is the MC Handshake
     * packet ID which we've already consumed.
     *
     * We reconstitute the full packet, reconfigure the pipeline with pre-Netty codecs
     * (RawPacketDecoder/Encoder + AlphaConnectionHandler), and fire the packet through.
     */
    private void handleEaglerCraft152(ChannelHandlerContext ctx, ByteBuf buf) {
        // Reconstitute the full packet: prepend the 0x02 byte we already consumed
        ByteBuf fullPacket = ctx.alloc().buffer(1 + buf.readableBytes());
        fullPacket.writeByte(0x02);
        fullPacket.writeBytes(buf);

        try {
            ChannelPipeline pipeline = ctx.pipeline();

            // Mark as EaglerCraft connection
            ctx.channel().attr(ATTR_IS_EAGLECRAFT).set(Boolean.TRUE);

            // Remove MOTD query handler and handshake timeout (no longer needed)
            if (pipeline.get("eaglerQuery") != null) {
                pipeline.remove("eaglerQuery");
            }
            if (pipeline.get("eaglerTimeout") != null) {
                pipeline.remove("eaglerTimeout");
            }

            // EaglerCraft 1.5.2 version is resolved later by AlphaConnectionHandler
            // from the MC Handshake; skip-unknown handles 0xFA skin packets safely.
            addPreNettyPipeline(pipeline, ProtocolVersion.ALPHA_1_2_5, false,
                    serverVersion, world, playerManager, chunkManager);

            pipeline.remove(this);

            System.out.println("[EaglerCraft] Detected pre-Netty client (raw MC handshake over WebSocket), "
                    + "reconfiguring pipeline for AlphaConnectionHandler");

            // Fire the reconstituted packet directly into the RawPacketDecoder,
            // bypassing the WebSocket layer. pipeline.fireChannelRead() would fire
            // from HEAD, which passes through wsProtocol — the WebSocket codec would
            // misinterpret the raw MC bytes as a malformed WebSocket frame
            // ("fragmented control frame"). Instead, fire from wsFrameDecoder's context
            // so the next handler (RawPacketDecoder) receives it directly.
            pipeline.context("wsFrameDecoder").fireChannelRead(fullPacket);
        } catch (Exception e) {
            fullPacket.release();
            throw e;
        }
    }

    /**
     * Handle a raw MC Login Request (0x01) sent directly over WebSocket.
     *
     * Some EaglerCraft clients send MC Login (0x01) as their first WebSocket
     * binary frame, skipping both the EaglerCraft handshake and the MC Handshake (0x02).
     *
     * At this point, packetType (0x01) and legacyByte (0x00, first byte of
     * the protocol version int) have already been consumed.
     *
     * Login C2S wire format:
     *   [byte 0x01] packet ID — already consumed as packetType
     *   [int] protocol version — first byte (0x00) consumed as legacyByte
     *   [String16 or writeUTF] username (encoding depends on MC version)
     *   [long] map seed (ignored)
     *   [byte] dimension (ignored)
     */
    private void handleRawMcLogin(ChannelHandlerContext ctx, ByteBuf buf) {
        // Read remaining 3 bytes of protocol version (first byte was 0x00)
        int protocolVersion = buf.readUnsignedMedium();

        // Read username — auto-detect encoding (writeUTF for Alpha/early Beta,
        // String16 for Beta 1.5+). readStringAuto peeks at the first data byte:
        // 0x00 = String16 high byte (UTF-16BE), non-zero = writeUTF (raw UTF-8).
        if (buf.readableBytes() < 2) {
            sendError(ctx, "Incomplete MC Login (missing username)");
            return;
        }
        Object[] autoResult = McDataTypes.readStringAuto(buf);
        String loginUsername = (String) autoResult[0];
        boolean detectedString16 = (Boolean) autoResult[1];

        // Set username for logging (sendError uses this.username)
        username = loginUsername;

        System.out.println("[EaglerCraft] Detected raw MC Login Request over WebSocket from "
                + loginUsername + " (protocol version " + protocolVersion
                + ", encoding=" + (detectedString16 ? "String16" : "writeUTF") + ")");

        // Resolve the ProtocolVersion from the protocol number.
        // String16 detection disambiguates version clashes (e.g., v14 is both
        // Alpha 1.0.16 and Beta 1.7.3).
        ProtocolVersion mcVersion;
        if (detectedString16) {
            mcVersion = ProtocolVersion.fromNumber(protocolVersion,
                    ProtocolVersion.Family.BETA, ProtocolVersion.Family.RELEASE);
        } else {
            mcVersion = ProtocolVersion.fromNumber(protocolVersion,
                    ProtocolVersion.Family.ALPHA, ProtocolVersion.Family.BETA);
        }
        if (mcVersion == null) {
            // This client speaks raw MC protocol (not EaglerCraft handshake), so
            // sendError's EaglerCraft error frame would be unintelligible. Just log and close.
            System.out.println("[EaglerCraft] Rejected raw MC Login from " + loginUsername
                    + ": unsupported protocol version " + protocolVersion);
            ctx.close();
            return;
        }

        // Remaining fields (mapSeed, dimension) released with the buffer in channelRead.

        ChannelPipeline pipeline = ctx.pipeline();
        ctx.channel().attr(ATTR_IS_EAGLECRAFT).set(Boolean.TRUE);
        ctx.channel().attr(ATTR_EAGLER_USERNAME).set(loginUsername);

        if (pipeline.get("eaglerQuery") != null) pipeline.remove("eaglerQuery");
        if (pipeline.get("eaglerTimeout") != null) pipeline.remove("eaglerTimeout");

        AlphaConnectionHandler handler = addPreNettyPipeline(pipeline,
                mcVersion, detectedString16,
                serverVersion, world, playerManager, chunkManager);

        pipeline.remove(this);

        System.out.println("[EaglerCraft] Pipeline reconfigured for raw MC Login, "
                + "initiating direct-to-PLAY login (" + mcVersion.getDisplayName() + ")");

        ChannelHandlerContext handlerCtx = pipeline.context(handler);
        handler.initiateEaglecraftLogin(handlerCtx, loginUsername, mcVersion);
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

        // Read MC protocol versions — pick the highest we support (340 > 47)
        int mcVersionCount = buf.readUnsignedShort();
        if (mcVersionCount < 1 || mcVersionCount > 16) {
            sendError(ctx, "Invalid MC version count");
            return;
        }
        selectedMcProtocol = 0;
        for (int i = 0; i < mcVersionCount; i++) {
            int mcVer = buf.readUnsignedShort();
            if (isSupportedMcProtocol(mcVer) && mcVer > selectedMcProtocol) {
                selectedMcProtocol = mcVer;
            }
        }
        if (selectedMcProtocol == 0) {
            sendDenyLogin(ctx, "Server requires MC protocol 6 (Alpha 1.2.6), 9 (Beta 1.3), 14 (Beta 1.7.3), 47 (1.8), or 340 (1.12.2)");
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

        System.out.println("[EaglerCraft] Client connected: " + brand + " " + clientVersion
                + " (eagler v" + selectedEaglerProtocol + ", auth=" + authEnabled + ")");

        sendServerVersion(ctx);
        state = STATE_CLIENT_VERSION;
    }

    private void parseV1ClientVersion(ChannelHandlerContext ctx, ByteBuf buf, int firstDataByte) {
        // V1 format: [byte 0x01 type][byte version] — 2 bytes total.
        // Some Beta 1.7.3 forks (PeytonPlayz595) append a short MC protocol version.
        selectedEaglerProtocol = EAGLER_PROTOCOL_V1;

        // Check for optional MC protocol version in remaining bytes
        if (buf.readableBytes() >= 2) {
            int mcVer = buf.readUnsignedShort();
            if (isSupportedMcProtocol(mcVer)) {
                selectedMcProtocol = mcVer;
                v1McProtocolExplicit = true;
            } else {
                selectedMcProtocol = MC_PROTOCOL_47;
            }
        } else {
            // No MC protocol info — can't tell Beta 1.7.3 from 1.8 yet.
            // Default to 47 but mark as ambiguous; completeHandshake will install
            // a detection handler that inspects the first MC frame to decide.
            selectedMcProtocol = MC_PROTOCOL_47;
        }

        System.out.println("[EaglerCraft] V1 client connected (legacy protocol, MC protocol "
                + selectedMcProtocol + ")");
        sendServerVersion(ctx);
        state = STATE_CLIENT_VERSION;
    }

    /**
     * Send SERVER_VERSION (0x02) response.
     *
     * V1 wire format (2 bytes):
     *   [byte 0x02]
     *   [byte] selected eagler protocol version
     *
     * V2/V3 wire format:
     *   [byte 0x02]
     *   [short] selected eagler protocol
     *   [short] selected MC protocol (14, 47, or 340)
     *   [byte] server brand length
     *   [bytes] server brand (US-ASCII)
     *   [byte] server version length
     *   [bytes] server version (US-ASCII)
     *   [byte] 0x00 (auth method: none)
     *   [short] 0x0000 (auth data length: 0)
     */
    private void sendServerVersion(ChannelHandlerContext ctx) {
        if (selectedEaglerProtocol == EAGLER_PROTOCOL_V1) {
            // V1: compact 2-byte response
            ByteBuf out = ctx.alloc().buffer(2);
            out.writeByte(PROTOCOL_SERVER_VERSION);
            out.writeByte(selectedEaglerProtocol);
            ctx.writeAndFlush(out);
            return;
        }

        // V2/V3: full response with brand, version, auth info
        byte[] brandBytes = SERVER_BRAND.getBytes(StandardCharsets.US_ASCII);
        byte[] versionBytes = SERVER_VERSION.getBytes(StandardCharsets.US_ASCII);
        int size = 1 + 2 + 2 + 1 + brandBytes.length + 1 + versionBytes.length + 1 + 2;

        ByteBuf out = ctx.alloc().buffer(size);
        out.writeByte(PROTOCOL_SERVER_VERSION);
        out.writeShort(selectedEaglerProtocol);
        out.writeShort(selectedMcProtocol);
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
        if (username.isEmpty() || username.length() > 16 || !USERNAME_PATTERN.matcher(username).matches()) {
            sendDenyLogin(ctx, "Invalid username");
            return;
        }

        // Read requested server (v2/v3 only — v1 omits these fields)
        if (buf.readableBytes() > 0) {
            int serverLen = buf.readUnsignedByte();
            if (serverLen > 0) buf.skipBytes(serverLen);
        }

        // Read password (v2/v3 only, ignored — we don't do auth)
        if (buf.readableBytes() > 0) {
            int passLen = buf.readUnsignedByte();
            if (passLen > 0) buf.skipBytes(passLen);
        }

        // Generate offline UUID
        uuid = ClassicToNettyTranslator.generateOfflineUuid(username);

        System.out.println("[EaglerCraft] Login request from: " + username);

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
                System.out.println("[EaglerCraft] Received skin data from " + username
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
     * and add the appropriate MC protocol handlers.
     *
     * For protocol 47 (1.8) / 340 (1.12.2): Netty pipeline (NettyPacketDecoder + NettyConnectionHandler).
     * For protocol 14 (Beta 1.7.3): pre-Netty pipeline (RawPacketDecoder + AlphaConnectionHandler).
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

        // EaglerCraft clients go directly to PLAY after the EaglerCraft handshake —
        // no MC Handshake/Login packets, no LoginSuccess expected.
        ChannelPipeline pipeline = ctx.pipeline();

        // Remove MOTD query handler and handshake timeout (no longer needed)
        if (pipeline.get("eaglerQuery") != null) {
            pipeline.remove("eaglerQuery");
        }
        if (pipeline.get("eaglerTimeout") != null) {
            pipeline.remove("eaglerTimeout");
        }

        // Remove self before installing MC pipeline (sub-methods may be called
        // from the v1 detection handler too, so removal lives here, not in them)
        pipeline.remove(this);

        if (selectedEaglerProtocol == EAGLER_PROTOCOL_V1 && !v1McProtocolExplicit) {
            // V1 client didn't declare its MC protocol — we can't tell Beta 1.7.3
            // (pre-Netty) from 1.8 (Netty) until we see the first MC frame.
            completeHandshakeV1Detect(ctx, pipeline);
        } else if (isPreNettyMcProtocol(selectedMcProtocol)) {
            completeHandshakePreNetty(ctx, pipeline);
        } else {
            completeHandshakeNetty(ctx, pipeline);
        }
    }

    /**
     * V1 client without explicit MC protocol version — install a one-shot detection
     * handler that inspects the first inbound MC frame to decide pre-Netty vs Netty.
     *
     * Pre-Netty (Beta 1.7.3) clients send raw MC packets where the first byte is the
     * packet ID (0x01 = LoginRequest, 0x02 = Handshake, etc. — all <= 0x10 for the
     * initial handshake/login packets). Netty (1.8+) clients send VarInt-framed
     * packets where the first byte is a VarInt packet ID — but since WebSocket frame
     * boundaries delimit packets, the first byte would be 0x00 (Teleport Confirm in
     * PLAY) or a higher packet ID. In practice, 1.8 EaglerCraft clients that already
     * completed the EaglerCraft handshake go straight to PLAY state and the server
     * initiates the join sequence — so the client typically waits for the server.
     *
     * Detection logic:
     *   - If the first byte of the first inbound frame is a known pre-Netty packet ID
     *     (0x01 or 0x02), assume Beta 1.7.3 and install pre-Netty pipeline.
     *   - If no frame arrives within 500ms, assume Netty 1.8 and initiate the join.
     */
    private void completeHandshakeV1Detect(ChannelHandlerContext ctx, ChannelPipeline pipeline) {
        System.out.println("[EaglerCraft] V1 client without explicit MC protocol — "
                + "installing detection handler for " + username);

        pipeline.addAfter("wsFrameDecoder", "v1Detect",
                new V1McProtocolDetector(username, serverVersion, world, playerManager, chunkManager));
    }

    /**
     * One-shot handler that sits after wsFrameDecoder and inspects the first
     * inbound ByteBuf to determine pre-Netty vs Netty MC protocol for V1 clients.
     */
    private static class V1McProtocolDetector extends ChannelInboundHandlerAdapter {
        private final String username;
        private final ProtocolVersion serverVersion;
        private final ServerWorld world;
        private final PlayerManager playerManager;
        private final ChunkManager chunkManager;
        private java.util.concurrent.ScheduledFuture<?> timeoutFuture;
        private volatile boolean resolved;

        V1McProtocolDetector(String username, ProtocolVersion serverVersion,
                             ServerWorld world, PlayerManager playerManager, ChunkManager chunkManager) {
            this.username = username;
            this.serverVersion = serverVersion;
            this.world = world;
            this.playerManager = playerManager;
            this.chunkManager = chunkManager;
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) {
            // If no MC frame arrives in 500ms, assume Netty 1.8 (client waits for server).
            timeoutFuture = ctx.executor().schedule(() -> {
                if (!resolved) {
                    resolved = true;
                    System.out.println("[EaglerCraft] V1 detection timeout — assuming Netty 1.8 for " + username);
                    installNetty(ctx);
                }
            }, 500, java.util.concurrent.TimeUnit.MILLISECONDS);
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) {
            if (timeoutFuture != null) {
                timeoutFuture.cancel(false);
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (resolved || !(msg instanceof ByteBuf)) {
                super.channelRead(ctx, msg);
                return;
            }

            ByteBuf buf = (ByteBuf) msg;
            if (buf.readableBytes() < 1) {
                super.channelRead(ctx, msg);
                return;
            }

            resolved = true;
            if (timeoutFuture != null) {
                timeoutFuture.cancel(false);
            }

            int firstByte = buf.getUnsignedByte(buf.readerIndex());

            if (firstByte == 0x01 || firstByte == 0x02) {
                System.out.println("[EaglerCraft] V1 detection: first byte 0x"
                        + Integer.toHexString(firstByte) + " → pre-Netty (Beta 1.7.3) for " + username);
                // Login was already initiated by initiateEaglecraftLogin — discard the
                // triggering frame (it's a Login/Handshake the handler already handled).
                buf.release();
                installPreNetty(ctx);
            } else {
                System.out.println("[EaglerCraft] V1 detection: first byte 0x"
                        + Integer.toHexString(firstByte) + " → Netty (1.8) for " + username);
                buf.release();
                installNetty(ctx);
            }
        }

        private void installPreNetty(ChannelHandlerContext ctx) {
            ChannelPipeline pipeline = ctx.pipeline();
            AlphaConnectionHandler handler = addPreNettyPipeline(pipeline,
                    ProtocolVersion.BETA_1_7_3, true,
                    serverVersion, world, playerManager, chunkManager);
            pipeline.remove(this);

            System.out.println("[EaglerCraft] Handshake complete for " + username
                    + ", initiating direct-to-PLAY login (Beta 1.7.3, detected)");

            ChannelHandlerContext handlerCtx = pipeline.context(handler);
            handler.initiateEaglecraftLogin(handlerCtx, username);
        }

        private void installNetty(ChannelHandlerContext ctx) {
            ChannelPipeline pipeline = ctx.pipeline();
            NettyConnectionHandler handler = addNettyPipeline(pipeline,
                    serverVersion, world, playerManager, chunkManager);
            pipeline.remove(this);

            System.out.println("[EaglerCraft] Handshake complete for " + username
                    + ", initiating direct-to-PLAY login (1.8, detected)");

            ChannelHandlerContext handlerCtx = pipeline.context(handler);
            handler.initiateEaglecraftLogin(handlerCtx, username, MC_PROTOCOL_47);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            System.err.println("[EaglerCraft] V1 detection error for " + username + ": " + cause.getMessage());
            ctx.close();
        }
    }

    /**
     * Set up a pre-Netty pipeline for EaglerCraft Beta clients.
     *
     * Pre-Netty packets use fixed/self-describing layouts: [1 byte packetId][payload].
     * WebSocket frame boundaries delimit packets (no length prefix needed).
     * String16 encoding is used for Beta 1.5+ (v11+); earlier versions use writeUTF.
     */
    private void completeHandshakePreNetty(ChannelHandlerContext ctx, ChannelPipeline pipeline) {
        // Search BETA first: protocol 14 exists in both ALPHA (1.0.16) and BETA (1.7.3).
        // fromNumber returns the first enum-order match, and ALPHA_1_0_16 is declared
        // before BETA_1_7_3. EaglerCraft v2/v3 clients negotiating protocol 14 are always
        // Beta 1.7.3, so BETA must take priority. Protocol 6 has no BETA entry, so the
        // fallback to ALPHA correctly resolves it to ALPHA_1_2_5.
        ProtocolVersion mcVersion = ProtocolVersion.fromNumber(
                selectedMcProtocol, ProtocolVersion.Family.BETA);
        if (mcVersion == null) {
            mcVersion = ProtocolVersion.fromNumber(
                    selectedMcProtocol, ProtocolVersion.Family.ALPHA);
        }
        if (mcVersion == null) {
            System.err.println("[EaglerCraft] BUG: No ProtocolVersion for MC protocol "
                    + selectedMcProtocol + " in BETA/ALPHA families");
            ctx.close();
            return;
        }
        boolean useString16 = mcVersion.isAtLeast(ProtocolVersion.BETA_1_5);

        AlphaConnectionHandler handler = addPreNettyPipeline(pipeline,
                mcVersion, useString16,
                serverVersion, world, playerManager, chunkManager);

        System.out.println("[EaglerCraft] Handshake complete for " + username
                + ", initiating direct-to-PLAY login (" + mcVersion.getDisplayName() + ")");

        ChannelHandlerContext handlerCtx = pipeline.context(handler);
        handler.initiateEaglecraftLogin(handlerCtx, username, mcVersion);
    }

    /**
     * Set up a Netty pipeline for EaglerCraftX 1.8.8 (protocol 47) / 1.12.2 (protocol 340).
     *
     * Netty packets: [VarInt packetId][data]. WebSocket frame boundaries delimit packets.
     */
    private void completeHandshakeNetty(ChannelHandlerContext ctx, ChannelPipeline pipeline) {
        NettyConnectionHandler handler = addNettyPipeline(pipeline,
                serverVersion, world, playerManager, chunkManager);

        System.out.println("[EaglerCraft] Handshake complete for " + username
                + ", initiating direct-to-PLAY login");

        ChannelHandlerContext handlerCtx = pipeline.context(handler);
        handler.initiateEaglecraftLogin(handlerCtx, username, selectedMcProtocol);
    }

    // ---- Shared pipeline installation helpers ----

    /**
     * Install pre-Netty pipeline handlers (RawPacketDecoder/Encoder + AlphaConnectionHandler).
     * Used by all pre-Netty EaglerCraft paths: 1.5.2 raw handshake, raw MC login, v2/v3
     * handshake with protocol 14, and v1 detection.
     */
    private static AlphaConnectionHandler addPreNettyPipeline(
            ChannelPipeline pipeline, ProtocolVersion protocolVersion, boolean useString16,
            ProtocolVersion serverVersion, ServerWorld world,
            PlayerManager playerManager, ChunkManager chunkManager) {
        RawPacketDecoder decoder = new RawPacketDecoder(
                PacketDirection.CLIENT_TO_SERVER, protocolVersion);
        if (useString16) decoder.setUseString16(true);
        decoder.setSkipUnknownPackets(true);
        pipeline.addAfter("wsFrameDecoder", "decoder", decoder);

        RawPacketEncoder encoder = new RawPacketEncoder();
        if (useString16) encoder.setUseString16(true);
        pipeline.addAfter("wsFrameEncoder", "encoder", encoder);

        pipeline.addAfter("encoder", "prioritizer", new PrioritizingOutboundHandler());
        pipeline.addAfter("prioritizer", "alphaTranslator", new ClassicToAlphaTranslator());

        AlphaConnectionHandler handler = new AlphaConnectionHandler(
                serverVersion, world, playerManager, chunkManager);
        pipeline.addAfter("decoder", "handler", handler);
        return handler;
    }

    /**
     * Install Netty pipeline handlers (NettyPacketDecoder/Encoder + NettyConnectionHandler).
     * Used by all Netty EaglerCraft paths: v2/v3 handshake and v1 detection timeout.
     */
    private static NettyConnectionHandler addNettyPipeline(
            ChannelPipeline pipeline, ProtocolVersion serverVersion, ServerWorld world,
            PlayerManager playerManager, ChunkManager chunkManager) {
        pipeline.addAfter("wsFrameDecoder", "packetDecoder",
                new NettyPacketDecoder(ConnectionState.PLAY));
        pipeline.addAfter("wsFrameEncoder", "packetEncoder",
                new NettyPacketEncoder(ConnectionState.PLAY));
        pipeline.addAfter("packetEncoder", "prioritizer",
                new PrioritizingOutboundHandler());
        pipeline.addAfter("prioritizer", "nettyTranslator",
                new ClassicToNettyTranslator());

        NettyConnectionHandler handler = new NettyConnectionHandler(
                serverVersion, world, playerManager, chunkManager);
        pipeline.addAfter("packetDecoder", "handler", handler);
        return handler;
    }

    private void sendDenyLogin(ChannelHandlerContext ctx, String message) {
        System.out.println("[EaglerCraft] Login denied for "
                + (username != null ? username : "unknown") + ": " + message);
        byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);
        int len = Math.min(msgBytes.length, 255);

        ByteBuf out = ctx.alloc().buffer(1 + 2 + len);
        out.writeByte(PROTOCOL_SERVER_DENY_LOGIN);
        out.writeShort(len);
        out.writeBytes(msgBytes, 0, len);
        ctx.writeAndFlush(out).addListener(f -> ctx.close());
    }

    private void sendVersionMismatch(ChannelHandlerContext ctx) {
        System.out.println("[EaglerCraft] Version mismatch, no supported protocol version found");
        ByteBuf out = ctx.alloc().buffer(1);
        out.writeByte(PROTOCOL_VERSION_MISMATCH);
        ctx.writeAndFlush(out).addListener(f -> ctx.close());
    }

    private void sendError(ChannelHandlerContext ctx, String message) {
        System.out.println("[EaglerCraft] Error for "
                + (username != null ? username : "unknown") + ": " + message);
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
        System.err.println("[EaglerCraft] Handshake error for "
                + (username != null ? username : "unknown") + ": " + cause.getMessage());
        ctx.close();
    }
}
