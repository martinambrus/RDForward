package com.github.martinambrus.rdforward.server.mcpe;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Front-end UDP handler that multiplexes legacy MCPE (RakNet v6) and
 * modern Bedrock (RakNet v10/v11) traffic on a single port (19132).
 *
 * Detection logic:
 * - Unconnected Pings: forwarded to legacy server; also injected into
 *   CloudburstMC pipeline if clientGUID is present (Bedrock/0.9.0).
 * - Open Connection Request 1: RakNet protocol version byte after magic
 *   determines routing (v6 = legacy, >= 10 = Bedrock).
 * - Subsequent packets: routed based on the mapping established at OCR1.
 */
public class UdpFrontEndHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    enum ClientType { LEGACY, BEDROCK }

    private final LegacyRakNetServer legacyServer;

    /** The NioDatagramChannel inside CloudburstMC's RakServerChannel. */
    private volatile Channel bedrockInternalChannel;

    /** This handler's own channel (the front-end NioDatagramChannel on port 19132). */
    private Channel frontEndChannel;

    /** Maps client addresses to their detected type for connected-phase routing. */
    private final Map<InetSocketAddress, ClientType> clientTypes = new ConcurrentHashMap<>();

    public UdpFrontEndHandler(LegacyRakNetServer legacyServer) {
        this.legacyServer = legacyServer;
    }

    /**
     * Set the internal DatagramChannel from CloudburstMC's RakServerChannel
     * (obtained via bedrockChannel.parent()). Must be called after Bedrock
     * server startup completes.
     */
    public void setBedrockInternalChannel(Channel ch) {
        this.bedrockInternalChannel = ch;
    }

    public Channel getFrontEndChannel() {
        return frontEndChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.frontEndChannel = ctx.channel();
        legacyServer.setFrontEndChannel(frontEndChannel);
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
        ByteBuf buf = packet.content();
        InetSocketAddress sender = packet.sender();

        if (!buf.isReadable()) return;
        buf.markReaderIndex();
        int packetId = buf.readUnsignedByte();
        buf.resetReaderIndex();

        // Unconnected Ping: forward to both backends as needed
        if (packetId == 0x01 || packetId == 0x02) {
            handlePing(ctx, packet);
            return;
        }

        // Open Connection Request 1: detect RakNet version and establish routing
        if (packetId == 0x05) {
            ClientType type = detectFromOCR1(buf);
            clientTypes.put(sender, type);
            routePacket(ctx, packet, type);
            return;
        }

        // All other packets: route based on established mapping
        ClientType type = clientTypes.get(sender);
        if (type == null) {
            // No OCR1 seen yet — try to detect from packet characteristics.
            // Default to legacy for safety (most packets will have been mapped already).
            type = ClientType.LEGACY;
        }
        routePacket(ctx, packet, type);
    }

    /**
     * Handle unconnected pings. Always forwarded to the legacy server (handles
     * 0.7.x and 0.9.0 pong formats). If the ping contains a clientGUID
     * (8 extra bytes after magic), also inject into CloudburstMC so it can
     * send a Bedrock-format pong.
     */
    private void handlePing(ChannelHandlerContext ctx, DatagramPacket packet) {
        ByteBuf buf = packet.content();
        buf.markReaderIndex();
        buf.skipBytes(1); // packet ID

        boolean hasClientGuid = false;
        if (buf.readableBytes() >= 24) { // 8 (pingTime) + 16 (magic)
            buf.skipBytes(24);
            hasClientGuid = buf.readableBytes() >= 8;
        }
        buf.resetReaderIndex();

        // Always forward to legacy server
        legacyServer.handleDatagram(ctx, packet.retain());

        // If has clientGUID, also forward to CloudburstMC for Bedrock pong
        if (hasClientGuid && bedrockInternalChannel != null) {
            injectIntoBedrock(packet);
        }
    }

    /**
     * Detect client type from Open Connection Request 1.
     * Format: [0x05][magic:16][rakNetVersion:1][MTU padding...]
     * RakNet v6 = legacy MCPE (0.7.x-0.9.0), v10+ = modern Bedrock.
     */
    private ClientType detectFromOCR1(ByteBuf buf) {
        buf.markReaderIndex();
        buf.skipBytes(1); // packet ID (0x05)
        if (buf.readableBytes() < 17) { // 16 (magic) + 1 (version)
            buf.resetReaderIndex();
            return ClientType.LEGACY;
        }
        buf.skipBytes(16); // magic
        int rakNetVersion = buf.readUnsignedByte();
        buf.resetReaderIndex();
        return rakNetVersion >= 10 ? ClientType.BEDROCK : ClientType.LEGACY;
    }

    private void routePacket(ChannelHandlerContext ctx, DatagramPacket packet, ClientType type) {
        if (type == ClientType.LEGACY) {
            legacyServer.handleDatagram(ctx, packet.retain());
        } else {
            injectIntoBedrock(packet);
        }
    }

    /**
     * Inject a DatagramPacket into CloudburstMC's internal pipeline as if it
     * arrived on the network. The sender address is preserved so CloudburstMC
     * creates/looks up the correct RakNet child channel.
     *
     * Uses retainedSlice(0, writerIndex) to get a fresh ByteBuf view starting
     * from byte 0, independent of the original buffer's reader position.
     * This is critical for the ping path where the legacy server reads from
     * the shared buffer before CloudburstMC gets it.
     */
    private void injectIntoBedrock(DatagramPacket packet) {
        if (bedrockInternalChannel == null) return;
        ByteBuf content = packet.content();
        DatagramPacket injected = new DatagramPacket(
                content.retainedSlice(0, content.writerIndex()),
                (InetSocketAddress) bedrockInternalChannel.localAddress(),
                packet.sender()
        );
        bedrockInternalChannel.pipeline().fireChannelRead(injected);
    }

    /**
     * Remove a client's routing entry (called on disconnect/timeout).
     */
    public void removeClient(InetSocketAddress address) {
        clientTypes.remove(address);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("[UdpFrontEnd] Error: " + cause.getMessage());
    }
}
