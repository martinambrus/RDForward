package com.github.martinambrus.rdforward.server.mcpe;

import com.github.martinambrus.rdforward.server.ChunkManager;
import com.github.martinambrus.rdforward.server.PlayerManager;
import com.github.martinambrus.rdforward.server.ServerWorld;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Lightweight RakNet v6 server for legacy MCPE 0.7.x clients.
 * Uses Netty NioDatagramChannel for raw UDP, implementing the RakNet
 * handshake, reliability layer, and encapsulation/decapsulation.
 */
public class LegacyRakNetServer extends SimpleChannelInboundHandler<DatagramPacket> {

    private final long serverGuid;
    private final String serverName;
    private final ServerWorld world;
    private final PlayerManager playerManager;
    private final ChunkManager chunkManager;
    private final Runnable pongUpdater;

    /** Active sessions keyed by client address. */
    private final Map<InetSocketAddress, LegacyRakNetSession> sessions = new ConcurrentHashMap<>();

    /** Own channel when running in standalone mode (null in front-end mode). */
    private Channel channel;

    /** Front-end channel when running behind UdpFrontEndHandler (null in standalone mode). */
    private Channel frontEndChannel;

    public LegacyRakNetServer(long serverGuid, String serverName,
                              ServerWorld world, PlayerManager playerManager,
                              ChunkManager chunkManager, Runnable pongUpdater) {
        this.serverGuid = serverGuid;
        this.serverName = serverName;
        this.world = world;
        this.playerManager = playerManager;
        this.chunkManager = chunkManager;
        this.pongUpdater = pongUpdater;
    }

    /**
     * Bind and start the UDP server on the given port using the provided EventLoopGroup.
     */
    public Channel start(EventLoopGroup group, int port) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(NioDatagramChannel.class)
                .handler(this);

        channel = bootstrap.bind(port).sync().channel();

        // Schedule periodic timeout check (every 5 seconds)
        channel.eventLoop().scheduleAtFixedRate(this::checkTimeouts,
                5, 5, java.util.concurrent.TimeUnit.SECONDS);

        return channel;
    }

    /**
     * Configure this server to run behind a UdpFrontEndHandler.
     * In this mode, the server does not bind its own socket; all I/O
     * goes through the front-end channel.
     */
    public void setFrontEndChannel(Channel frontEnd) {
        this.frontEndChannel = frontEnd;
        // Schedule timeout check on the front-end's event loop
        frontEnd.eventLoop().scheduleAtFixedRate(this::checkTimeouts,
                5, 5, java.util.concurrent.TimeUnit.SECONDS);
    }

    /**
     * Handle an incoming datagram forwarded by the UdpFrontEndHandler.
     * Delegates to the same logic as channelRead0.
     */
    public void handleDatagram(ChannelHandlerContext ctx, DatagramPacket packet) {
        try {
            channelRead0(ctx, packet);
        } finally {
            packet.release();
        }
    }

    public Channel getChannel() { return channel; }
    public Channel getSendChannel() { return (frontEndChannel != null) ? frontEndChannel : channel; }
    public Map<InetSocketAddress, LegacyRakNetSession> getSessions() { return sessions; }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
        ByteBuf buf = packet.content();
        InetSocketAddress sender = packet.sender();

        if (!buf.isReadable()) return;
        int packetId = buf.readUnsignedByte();

        // Unconnected packets (no session required)
        if (packetId == (MCPEConstants.UNCONNECTED_PING & 0xFF)
                || packetId == (MCPEConstants.UNCONNECTED_PING_OPEN & 0xFF)) {
            System.out.println("[MCPE] Ping from " + sender + " (id=0x" + Integer.toHexString(packetId)
                    + ", remaining=" + buf.readableBytes() + " bytes)");
            handleUnconnectedPing(ctx, sender, buf, packetId);
            return;
        }
        if (packetId == (MCPEConstants.OPEN_CONNECTION_REQUEST_1 & 0xFF)) {
            handleOpenConnectionRequest1(ctx, sender, buf);
            return;
        }
        if (packetId == (MCPEConstants.OPEN_CONNECTION_REQUEST_2 & 0xFF)) {
            handleOpenConnectionRequest2(ctx, sender, buf);
            return;
        }

        // Connected packets (session required)
        LegacyRakNetSession session = sessions.get(sender);
        if (session == null) return;

        // Reset timeout timer on any received packet
        session.touch();

        if (packetId == (MCPEConstants.ACK & 0xFF)) {
            int ackCount = buf.readUnsignedShort();
            StringBuilder ackSeqs = new StringBuilder();
            for (int i = 0; i < ackCount && buf.isReadable(); i++) {
                boolean single = buf.readBoolean();
                int start = buf.readUnsignedByte() | (buf.readUnsignedByte() << 8) | (buf.readUnsignedByte() << 16);
                if (single) {
                    ackSeqs.append(start).append(" ");
                } else {
                    int end = buf.readUnsignedByte() | (buf.readUnsignedByte() << 8) | (buf.readUnsignedByte() << 16);
                    ackSeqs.append(start).append("-").append(end).append(" ");
                }
            }
            System.out.println("[MCPE ACK] from " + sender + ": " + ackSeqs);
            return;
        }
        if (packetId == (MCPEConstants.NACK & 0xFF)) {
            if (buf.readableBytes() >= 2) {
                int nackCount = buf.readUnsignedShort();
                StringBuilder nackSeqs = new StringBuilder();
                for (int i = 0; i < nackCount && buf.isReadable(); i++) {
                    boolean single = buf.readBoolean();
                    int start = buf.readUnsignedByte() | (buf.readUnsignedByte() << 8) | (buf.readUnsignedByte() << 16);
                    if (single) {
                        nackSeqs.append(start).append(" ");
                    } else {
                        int end = buf.readUnsignedByte() | (buf.readUnsignedByte() << 8) | (buf.readUnsignedByte() << 16);
                        nackSeqs.append(start).append("-").append(end).append(" ");
                    }
                }
                System.out.println("[MCPE NACK] from " + sender + ": " + nackSeqs);
            }
            return;
        }

        // Data packets (0x80-0x8F)
        if (packetId >= (MCPEConstants.DATA_PACKET_MIN & 0xFF)
                && packetId <= (MCPEConstants.DATA_PACKET_MAX & 0xFF)) {
            handleDataPacket(ctx, session, buf);
            return;
        }
    }

    /** Periodic check for timed-out sessions (client closed without disconnect packet). */
    private void checkTimeouts() {
        for (java.util.Iterator<java.util.Map.Entry<InetSocketAddress, LegacyRakNetSession>> it =
                sessions.entrySet().iterator(); it.hasNext(); ) {
            LegacyRakNetSession session = it.next().getValue();
            if (session.isTimedOut() && session.getState() != LegacyRakNetSession.State.DISCONNECTED) {
                if (session.getGameplayHandler() != null) {
                    session.getGameplayHandler().onDisconnect();
                }
                session.close();
                it.remove();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("[MCPE] Error: " + cause.getMessage());
    }

    // ========== Unconnected Packet Handlers ==========

    private void handleUnconnectedPing(ChannelHandlerContext ctx, InetSocketAddress sender,
                                       ByteBuf buf, int pingId) {
        long pingTime = buf.readLong();
        // Skip magic (16 bytes)
        buf.skipBytes(MCPEConstants.RAKNET_MAGIC_LENGTH);

        // Detect client era from remaining bytes:
        // 0.7.x pings have NO clientGUID (0 bytes remaining after magic).
        // 0.9.0+ pings have clientGUID (8 bytes remaining).
        boolean hasClientGuid = buf.readableBytes() >= 8;
        if (hasClientGuid) {
            buf.readLong(); // consume clientGUID
        }

        int playerCount = playerManager.getPlayerCount();
        int maxPlayers = com.github.martinambrus.rdforward.server.PlayerManager.MAX_PLAYERS;

        if (hasClientGuid) {
            // 0.9.0+ client — advertise latest supported version.
            // Older clients show "Outdated Client" but can still connect.
            String mcpePong = "MCPE;" + serverName + ";"
                    + MCPEConstants.MCPE_PROTOCOL_VERSION_MAX + ";"
                    + MCPEConstants.MCPE_VERSION_STRING + ";"
                    + playerCount + ";" + maxPlayers;
            sendPong(ctx, sender, pingTime, mcpePong);
        } else {
            // Could be 0.7.x or 0.11.0+ — send MCPE + MCCPP formats.
            String mcpePong = "MCPE;" + serverName + ";"
                    + MCPEConstants.MCPE_PROTOCOL_VERSION_MAX + ";"
                    + MCPEConstants.MCPE_VERSION_STRING + ";"
                    + playerCount + ";" + maxPlayers;
            sendPong(ctx, sender, pingTime, mcpePong);

            String mccppPong = MCPEConstants.PONG_PREFIX + serverName;
            sendPong(ctx, sender, pingTime, mccppPong);
        }
    }

    private void sendPong(ChannelHandlerContext ctx, InetSocketAddress sender, long pingTime, String pongData) {
        ByteBuf pong = Unpooled.buffer();
        pong.writeByte(MCPEConstants.UNCONNECTED_PONG);
        pong.writeLong(pingTime);
        pong.writeLong(serverGuid);
        pong.writeBytes(MCPEConstants.RAKNET_MAGIC);
        byte[] pongBytes = pongData.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        pong.writeShort(pongBytes.length);
        pong.writeBytes(pongBytes);
        ctx.writeAndFlush(new DatagramPacket(pong, sender));
    }

    private void handleOpenConnectionRequest1(ChannelHandlerContext ctx, InetSocketAddress sender, ByteBuf buf) {
        // Skip magic
        buf.skipBytes(MCPEConstants.RAKNET_MAGIC_LENGTH);
        int protocolVersion = buf.readUnsignedByte();
        // Remaining bytes = MTU padding; MTU = total packet size
        int mtu = buf.readableBytes() + 1 + MCPEConstants.RAKNET_MAGIC_LENGTH + 1 + 28; // +28 for UDP/IP headers

        // Send Open Connection Reply 1
        ByteBuf reply = Unpooled.buffer();
        reply.writeByte(MCPEConstants.OPEN_CONNECTION_REPLY_1);
        reply.writeBytes(MCPEConstants.RAKNET_MAGIC);
        reply.writeLong(serverGuid);
        reply.writeByte(0); // No security
        reply.writeShort(mtu);

        ctx.writeAndFlush(new DatagramPacket(reply, sender));
    }

    private void handleOpenConnectionRequest2(ChannelHandlerContext ctx, InetSocketAddress sender, ByteBuf buf) {
        // Skip magic
        buf.skipBytes(MCPEConstants.RAKNET_MAGIC_LENGTH);
        // Read server address (skip it — we know our address)
        MCPEPacketBuffer reader = new MCPEPacketBuffer(buf);
        reader.readAddress();
        int mtu = buf.readUnsignedShort();
        long clientGuid = buf.readLong();

        // Create session
        LegacyRakNetSession session = new LegacyRakNetSession(sender, clientGuid, mtu, serverGuid);
        sessions.put(sender, session);

        // Create login handler
        MCPELoginHandler loginHandler = new MCPELoginHandler(
                session, world, playerManager, chunkManager, this, pongUpdater);
        session.setLoginHandler(loginHandler);

        // Send Open Connection Reply 2
        ByteBuf reply = Unpooled.buffer();
        reply.writeByte(MCPEConstants.OPEN_CONNECTION_REPLY_2);
        reply.writeBytes(MCPEConstants.RAKNET_MAGIC);
        reply.writeLong(serverGuid);
        new MCPEPacketBuffer(reply).writeAddress(sender);
        reply.writeShort(mtu);
        reply.writeByte(0); // No security

        ctx.writeAndFlush(new DatagramPacket(reply, sender));
    }

    // ========== Data Packet Handling ==========

    private void handleDataPacket(ChannelHandlerContext ctx, LegacyRakNetSession session, ByteBuf buf) {
        // Cache ctx for outgoing writes (ensures same write path as handshake)
        session.setCachedCtx(ctx);

        // Read sequence number (3 bytes LE)
        int seqNum = buf.readUnsignedByte()
                   | (buf.readUnsignedByte() << 8)
                   | (buf.readUnsignedByte() << 16);

        // Send ACK for this sequence number
        sendAck(ctx, session, seqNum);
        session.acknowledgeSequence(seqNum);

        // Decode encapsulated packets
        while (buf.isReadable()) {
            decodeEncapsulated(ctx, session, buf);
        }
    }

    private void decodeEncapsulated(ChannelHandlerContext ctx, LegacyRakNetSession session, ByteBuf buf) {
        if (buf.readableBytes() < 3) return;

        int flags = buf.readUnsignedByte();
        int reliability = (flags >> 5) & 0x07;
        boolean hasSplit = (flags & 0x10) != 0;

        int bitLength = buf.readUnsignedShort();
        int byteLength = (bitLength + 7) / 8;

        // Reliable packets have a message index
        if (reliability == MCPEConstants.RELIABLE
                || reliability == MCPEConstants.RELIABLE_ORDERED
                || reliability == MCPEConstants.RELIABLE_SEQUENCED) {
            buf.skipBytes(3); // message index (triad LE)
        }

        // Ordered/sequenced packets have ordering index + channel
        if (reliability == MCPEConstants.RELIABLE_ORDERED
                || reliability == MCPEConstants.UNRELIABLE_SEQUENCED
                || reliability == MCPEConstants.RELIABLE_SEQUENCED) {
            buf.skipBytes(3); // ordering index (triad LE)
            buf.skipBytes(1); // ordering channel
        }

        // Split packet header
        int splitCount = 0, splitId = 0, splitIndex = 0;
        if (hasSplit) {
            splitCount = buf.readInt();
            splitId = buf.readUnsignedShort();
            splitIndex = buf.readInt();
        }

        if (buf.readableBytes() < byteLength) {
            System.err.println("[MCPE RakNet] Encapsulated underflow: need " + byteLength
                    + " but have " + buf.readableBytes()
                    + " (reliability=" + reliability + " split=" + hasSplit + ")");
            return;
        }
        ByteBuf payload = buf.readSlice(byteLength).retain();

        // Debug: log first bytes of encapsulated payload
        if (session.getGameplayHandler() == null) {
            int idx = payload.readerIndex();
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < Math.min(16, payload.readableBytes()); i++) {
                hex.append(String.format("%02x ", payload.getByte(idx + i)));
            }
            System.out.println("[MCPE RakNet DEBUG] Encap payload (" + byteLength + " bytes,"
                    + " split=" + hasSplit + " splitId=" + splitId
                    + " splitIdx=" + splitIndex + "/" + splitCount
                    + "): " + hex);
        }

        try {
            if (hasSplit) {
                ByteBuf reassembled = session.handleSplitPacket(splitId, splitIndex, splitCount, payload);
                if (reassembled != null) {
                    // Debug: log first bytes of reassembled packet
                    int ridx = reassembled.readerIndex();
                    StringBuilder rhex = new StringBuilder();
                    for (int i = 0; i < Math.min(16, reassembled.readableBytes()); i++) {
                        rhex.append(String.format("%02x ", reassembled.getByte(ridx + i)));
                    }
                    System.out.println("[MCPE RakNet DEBUG] Reassembled (" + reassembled.readableBytes()
                            + " bytes): " + rhex);
                    try {
                        handleGamePacket(ctx, session, reassembled);
                    } finally {
                        reassembled.release();
                    }
                }
            } else {
                handleGamePacket(ctx, session, payload);
            }
        } finally {
            payload.release();
        }
    }

    // ========== Game Packet Dispatch ==========

    private void handleGamePacket(ChannelHandlerContext ctx, LegacyRakNetSession session, ByteBuf payload) {
        if (!payload.isReadable()) return;
        int gamePacketId = payload.readUnsignedByte();

        // Connected Ping — handled for all versions and states
        if (gamePacketId == 0x00) {
            handleConnectedPing(ctx, session, payload);
            return;
        }

        // RakNet connected handshake packets — only during CONNECTING state
        if (session.getState() == LegacyRakNetSession.State.CONNECTING) {
            if (gamePacketId == (MCPEConstants.CLIENT_CONNECT & 0xFF)) {
                handleClientConnect(ctx, session, payload);
                return;
            }
            if (gamePacketId == (MCPEConstants.CLIENT_HANDSHAKE & 0xFF)) {
                handleClientHandshake(ctx, session);
                return;
            }
        }

        // CLIENT_DISCONNECT (0x15) — always a RakNet disconnect signal.
        // In v27, EntityEvent wire ID is 0x96 (internal 0x15 + 0x81), not 0x15.
        if (gamePacketId == (MCPEConstants.CLIENT_DISCONNECT & 0xFF)) {
            handleDisconnect(session);
            return;
        }

        // v27 DisconnectPacket (0x03) / v34 DisconnectPacket (0x91)
        if (gamePacketId == (MCPEConstants.V27_DISCONNECT & 0xFF)
                && session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27
                && session.getMcpeProtocolVersion() < MCPEConstants.MCPE_PROTOCOL_VERSION_34) {
            handleDisconnect(session);
            return;
        }
        if (gamePacketId == (MCPEConstants.V34_DISCONNECT & 0xFF)
                && session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_34) {
            handleDisconnect(session);
            return;
        }

        // BatchPacket — decompress and dispatch inner packets.
        // v27: 0xB1, v34: 0x92. Skip for v11-v20 sessions where 0xB1 collides with
        // ContainerClose wire ID (v14+ shift). Allow for unknown (0, pre-login) since
        // v34 sends Login inside a batch.
        {
            int pv = session.getMcpeProtocolVersion();
            boolean couldBeBatch = pv == 0 || pv >= MCPEConstants.MCPE_PROTOCOL_VERSION_27;
            if (couldBeBatch && (gamePacketId == (MCPEConstants.V27_BATCH & 0xFF)
                    || gamePacketId == (MCPEConstants.V34_BATCH & 0xFF))) {
                handleBatchPacket(ctx, session, payload);
                return;
            }
        }

        // Log game packets at RakNet level for debugging (skip frequent MovePlayer)
        int canonicalId = MCPEConstants.toCanonicalId(gamePacketId, session.getMcpeProtocolVersion());
        if (canonicalId != 0x94) { // skip MovePlayer
            System.out.println("[MCPE RakNet] Game packet 0x" + Integer.toHexString(gamePacketId)
                    + " (" + payload.readableBytes() + " bytes)");
        }

        // Game packets — delegate to login or gameplay handler
        if (session.getGameplayHandler() != null) {
            session.getGameplayHandler().handlePacket(ctx, gamePacketId, payload);
        } else if (session.getLoginHandler() != null) {
            session.getLoginHandler().handlePacket(ctx, gamePacketId, payload);
        }
    }

    /** Decompress a v27 BatchPacket and dispatch each inner game packet. */
    private void handleBatchPacket(ChannelHandlerContext ctx, LegacyRakNetSession session, ByteBuf payload) {
        int compressedLength = payload.readInt();
        if (compressedLength <= 0 || compressedLength > payload.readableBytes()) {
            System.err.println("[MCPE] BatchPacket invalid length: " + compressedLength);
            return;
        }
        byte[] compressed = new byte[compressedLength];
        payload.readBytes(compressed);

        byte[] decompressed;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
            InflaterInputStream iis = new InflaterInputStream(bais);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] tmp = new byte[4096];
            int n;
            while ((n = iis.read(tmp)) != -1) {
                baos.write(tmp, 0, n);
            }
            decompressed = baos.toByteArray();
        } catch (IOException e) {
            System.err.println("[MCPE] BatchPacket decompression failed: " + e.getMessage());
            return;
        }

        ByteBuf decompBuf = Unpooled.wrappedBuffer(decompressed);
        try {
            // Detect framing: if first 4 bytes form a valid length that fits,
            // use int-length-prefixed framing; otherwise treat as a raw single packet.
            boolean hasIntFraming = false;
            if (decompBuf.readableBytes() >= 5) { // at least 4 (len) + 1 (packet id)
                int possibleLen = decompBuf.getInt(decompBuf.readerIndex());
                hasIntFraming = possibleLen > 0 && possibleLen <= decompBuf.readableBytes() - 4;
            }

            if (hasIntFraming) {
                while (decompBuf.readableBytes() >= 4) {
                    int pktLen = decompBuf.readInt();
                    if (pktLen <= 0 || pktLen > decompBuf.readableBytes()) break;
                    ByteBuf innerPayload = decompBuf.readSlice(pktLen);
                    handleGamePacket(ctx, session, innerPayload);
                }
            } else {
                // No framing — entire decompressed content is a single game packet
                handleGamePacket(ctx, session, decompBuf);
            }
        } finally {
            decompBuf.release();
        }
    }

    // ========== Connected Handshake ==========

    private void handleClientConnect(ChannelHandlerContext ctx, LegacyRakNetSession session, ByteBuf payload) {
        long clientId = payload.readLong();
        long sessionId = payload.readLong();
        // unknown byte (if present)

        // Send ServerHandshake (0x10)
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(MCPEConstants.SERVER_HANDSHAKE);
        // Cookie + security
        pkt.writeBytes(new byte[] { 0x04, 0x3F, 0x57, (byte) 0xFE }); // cookie
        pkt.writeByte(0xCD); // security byte
        pkt.writeShort(MCPEConstants.DEFAULT_PORT); // server port

        // 10 system addresses (null addresses)
        for (int i = 0; i < 10; i++) {
            pkt.writeNullAddress();
        }

        // Padding
        pkt.writeShort(0);
        // Timestamps
        pkt.writeLong(0L); // ping time
        pkt.writeLong(System.currentTimeMillis()); // pong time

        sendEncapsulated(ctx, session, pkt.getBuf(), MCPEConstants.RELIABLE);
    }

    private void handleConnectedPing(ChannelHandlerContext ctx, LegacyRakNetSession session, ByteBuf payload) {
        if (payload.readableBytes() < 8) return;
        long pingTime = payload.readLong();
        if (session.getGameplayHandler() == null) {
            System.out.println("[MCPE PING] from " + session.getAddress() + " pingTime=" + pingTime);
        }

        // Respond with Connected Pong (0x03)
        MCPEPacketBuffer pkt = new MCPEPacketBuffer();
        pkt.writeByte(0x03); // Connected Pong
        pkt.writeLong(pingTime);
        pkt.writeLong(System.currentTimeMillis());
        sendEncapsulated(ctx, session, pkt.getBuf(), MCPEConstants.UNRELIABLE);
    }

    private void handleClientHandshake(ChannelHandlerContext ctx, LegacyRakNetSession session) {
        // Client completed RakNet connected handshake — now connected
        session.setState(LegacyRakNetSession.State.CONNECTED);
    }

    private void handleDisconnect(LegacyRakNetSession session) {
        if (session.getGameplayHandler() != null) {
            session.getGameplayHandler().onDisconnect();
        }
        session.close();
        sessions.remove(session.getAddress());
    }

    // ========== Sending ==========

    /**
     * Send a game packet to a session with RELIABLE_ORDERED encapsulation.
     * The payload should NOT include the data packet header (0x84) or sequence number;
     * this method wraps it in the RakNet reliability layer.
     */
    public void sendGamePacket(LegacyRakNetSession session, ByteBuf gamePayload) {
        // Use cached ctx from incoming data packets (same write path as handshake)
        ChannelHandlerContext cachedCtx = session.getCachedCtx();
        Channel sendCh;
        if (cachedCtx != null) {
            sendCh = cachedCtx.channel();
        } else {
            sendCh = (frontEndChannel != null) ? frontEndChannel : channel;
        }
        if (sendCh == null || !sendCh.isActive()) {
            System.err.println("[MCPE] sendGamePacket: channel null or inactive!");
            return;
        }

        int origId = gamePayload.getUnsignedByte(gamePayload.readerIndex());

        // Debug: log outgoing game packet IDs during login (before gameplay handler assigned)
        if (session.getGameplayHandler() == null && session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_34) {
            System.out.println("[MCPE OUT] Sending packet 0x" + Integer.toHexString(origId)
                    + " (" + gamePayload.readableBytes() + " bytes)"
                    + " to v" + session.getMcpeProtocolVersion());
        }

        // v27+ clients require all game packets to be batch-wrapped (except batch itself).
        if (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_27) {
            int batchId = (session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_34)
                    ? (MCPEConstants.V34_BATCH & 0xFF)
                    : (MCPEConstants.V27_BATCH & 0xFF);
            if (origId != batchId) {
                gamePayload = wrapInBatch(session, gamePayload);
            }
        }

        // Debug: log batch-wrapped size and MTU for v34 login
        if (session.getGameplayHandler() == null
                && session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_34) {
            System.out.println("[MCPE OUT BATCH] wrapped=" + gamePayload.readableBytes()
                    + " bytes, MTU=" + session.getMtu()
                    + ", maxPayload=" + (session.getMtu() - 60));
        }

        ChannelHandlerContext useCtx = (cachedCtx != null) ? cachedCtx : sendCh.pipeline().firstContext();
        sendEncapsulated(useCtx, session, gamePayload,
                MCPEConstants.RELIABLE_ORDERED);
    }

    /**
     * Wrap a game packet in a BatchPacket for v27+ clients.
     * v27: [0xB1][int compLen][zlib(raw packet bytes)]
     * v34: [0x92][int compLen][zlib(raw packet bytes)] — same as v27 but different batch ID
     */
    private ByteBuf wrapInBatch(LegacyRakNetSession session, ByteBuf gamePayload) {
        byte[] raw = new byte[gamePayload.readableBytes()];
        gamePayload.readBytes(raw);

        boolean isV34 = session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_34;

        // v34: inner packets are int-length-prefixed inside the compressed data
        byte[] toCompress;
        if (isV34) {
            toCompress = new byte[4 + raw.length];
            toCompress[0] = (byte) ((raw.length >> 24) & 0xFF);
            toCompress[1] = (byte) ((raw.length >> 16) & 0xFF);
            toCompress[2] = (byte) ((raw.length >> 8) & 0xFF);
            toCompress[3] = (byte) (raw.length & 0xFF);
            System.arraycopy(raw, 0, toCompress, 4, raw.length);
        } else {
            toCompress = raw;
        }

        byte[] compressed;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DeflaterOutputStream dos = new DeflaterOutputStream(baos,
                    new Deflater(Deflater.DEFAULT_COMPRESSION));
            dos.write(toCompress);
            dos.finish();
            dos.close();
            compressed = baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("batch compress failed", e);
        }

        byte batchId = isV34 ? MCPEConstants.V34_BATCH : MCPEConstants.V27_BATCH;
        ByteBuf batch = Unpooled.buffer();
        batch.writeByte(batchId);
        batch.writeInt(compressed.length);
        batch.writeBytes(compressed);
        return batch;
    }

    /**
     * Wrap a payload in a RakNet encapsulated frame and send it as a data packet.
     */
    private void sendEncapsulated(ChannelHandlerContext ctx, LegacyRakNetSession session,
                                  ByteBuf payload, int reliability) {
        int payloadLength = payload.readableBytes();

        // Check if we need to split (payload > MTU - overhead)
        int maxPayload = session.getMtu() - 60; // conservative overhead estimate
        if (payloadLength > maxPayload && maxPayload > 0) {
            sendSplit(ctx, session, payload, reliability, maxPayload);
            return;
        }

        int seqNum = session.nextSendSequenceNumber();
        int bitLength = payloadLength * 8;

        ByteBuf frame = Unpooled.buffer();
        // Data packet header
        frame.writeByte(MCPEConstants.DATA_PACKET_MIN);
        // Sequence number (3 bytes LE)
        frame.writeByte(seqNum & 0xFF);
        frame.writeByte((seqNum >> 8) & 0xFF);
        frame.writeByte((seqNum >> 16) & 0xFF);

        // Encapsulation header
        int flags = (reliability << 5);
        frame.writeByte(flags);
        frame.writeShort(bitLength);

        // Reliable index
        if (reliability == MCPEConstants.RELIABLE
                || reliability == MCPEConstants.RELIABLE_ORDERED
                || reliability == MCPEConstants.RELIABLE_SEQUENCED) {
            int relIdx = session.nextReliableIndex();
            frame.writeByte(relIdx & 0xFF);
            frame.writeByte((relIdx >> 8) & 0xFF);
            frame.writeByte((relIdx >> 16) & 0xFF);
        }

        // Ordering index + channel
        if (reliability == MCPEConstants.RELIABLE_ORDERED
                || reliability == MCPEConstants.UNRELIABLE_SEQUENCED
                || reliability == MCPEConstants.RELIABLE_SEQUENCED) {
            int ordIdx = session.nextOrderingIndex();
            frame.writeByte(ordIdx & 0xFF);
            frame.writeByte((ordIdx >> 8) & 0xFF);
            frame.writeByte((ordIdx >> 16) & 0xFF);
            frame.writeByte(0); // channel 0
        }

        frame.writeBytes(payload);

        // Debug: hex dump first few outgoing data packets during login
        if (session.getGameplayHandler() == null
                && session.getMcpeProtocolVersion() >= MCPEConstants.MCPE_PROTOCOL_VERSION_34) {
            int ridx = frame.readerIndex();
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < Math.min(32, frame.readableBytes()); i++) {
                hex.append(String.format("%02x ", frame.getByte(ridx + i)));
            }
            System.out.println("[MCPE OUT FRAME] " + frame.readableBytes() + " bytes → "
                    + session.getAddress() + ": " + hex);
        }

        io.netty.channel.ChannelFuture future = ctx.writeAndFlush(new DatagramPacket(frame, session.getAddress()));
        if (session.getGameplayHandler() == null) {
            final int sn = seqNum;
            future.addListener(f -> {
                if (!f.isSuccess()) {
                    System.err.println("[MCPE WRITE FAIL] seq=" + sn + " to " + session.getAddress()
                            + ": " + f.cause());
                }
            });
        }
    }

    /**
     * Split a large payload into multiple RakNet frames.
     * All fragments share the same splitId and ordering index.
     */
    private void sendSplit(ChannelHandlerContext ctx, LegacyRakNetSession session,
                           ByteBuf payload, int reliability, int maxPayload) {
        int totalLength = payload.readableBytes();
        int splitCount = (totalLength + maxPayload - 1) / maxPayload;
        int splitId = session.nextSplitId();

        // All fragments of a split packet share one ordering index
        int sharedOrdIdx = -1;
        if (reliability == MCPEConstants.RELIABLE_ORDERED
                || reliability == MCPEConstants.RELIABLE_SEQUENCED) {
            sharedOrdIdx = session.nextOrderingIndex();
        }

        for (int splitIndex = 0; splitIndex < splitCount; splitIndex++) {
            int start = splitIndex * maxPayload;
            int length = Math.min(maxPayload, totalLength - start);
            int bitLength = length * 8;

            int seqNum = session.nextSendSequenceNumber();

            ByteBuf frame = Unpooled.buffer();
            frame.writeByte(MCPEConstants.DATA_PACKET_MIN);
            // Sequence number
            frame.writeByte(seqNum & 0xFF);
            frame.writeByte((seqNum >> 8) & 0xFF);
            frame.writeByte((seqNum >> 16) & 0xFF);

            // Encapsulation: flags with split bit set
            int flags = (reliability << 5) | 0x10; // 0x10 = hasSplit
            frame.writeByte(flags);
            frame.writeShort(bitLength);

            // Reliable index (each fragment gets its own)
            if (reliability >= MCPEConstants.RELIABLE) {
                int relIdx = session.nextReliableIndex();
                frame.writeByte(relIdx & 0xFF);
                frame.writeByte((relIdx >> 8) & 0xFF);
                frame.writeByte((relIdx >> 16) & 0xFF);
            }

            // Ordering (shared across all fragments)
            if (sharedOrdIdx >= 0) {
                frame.writeByte(sharedOrdIdx & 0xFF);
                frame.writeByte((sharedOrdIdx >> 8) & 0xFF);
                frame.writeByte((sharedOrdIdx >> 16) & 0xFF);
                frame.writeByte(0);
            }

            // Split header
            frame.writeInt(splitCount);
            frame.writeShort(splitId);
            frame.writeInt(splitIndex);

            // Payload fragment
            frame.writeBytes(payload, start, length);

            ctx.writeAndFlush(new DatagramPacket(frame, session.getAddress()));
        }
    }

    /** Send an ACK for a received sequence number. */
    private void sendAck(ChannelHandlerContext ctx, LegacyRakNetSession session, int seqNum) {
        ByteBuf ack = Unpooled.buffer();
        ack.writeByte(MCPEConstants.ACK);
        ack.writeShort(1); // count = 1 range
        ack.writeByte(1);  // single (not range)
        // Sequence number (3 bytes LE)
        ack.writeByte(seqNum & 0xFF);
        ack.writeByte((seqNum >> 8) & 0xFF);
        ack.writeByte((seqNum >> 16) & 0xFF);

        ctx.writeAndFlush(new DatagramPacket(ack, session.getAddress()));
    }

    /** Remove a session (called on disconnect). */
    public void removeSession(LegacyRakNetSession session) {
        sessions.remove(session.getAddress());
        session.close();
    }
}
