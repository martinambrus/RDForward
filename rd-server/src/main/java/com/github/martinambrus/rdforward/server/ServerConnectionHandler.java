package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.Capability;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.*;
import com.github.martinambrus.rdforward.protocol.translation.VersionTranslator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles individual client connections on the server side.
 *
 * Responsibilities:
 * 1. Process the initial handshake to determine client protocol version
 * 2. Insert the appropriate VersionTranslator into the pipeline
 * 3. Route game packets to the world state manager
 */
public class ServerConnectionHandler extends SimpleChannelInboundHandler<Packet> {

    private final ProtocolVersion serverVersion;
    private ProtocolVersion clientVersion;
    private boolean handshakeComplete = false;

    public ServerConnectionHandler(ProtocolVersion serverVersion) {
        this.serverVersion = serverVersion;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        if (!handshakeComplete && packet.getType() == PacketType.HANDSHAKE) {
            handleHandshake(ctx, (HandshakePacket) packet);
            return;
        }

        if (!handshakeComplete) {
            // Drop packets before handshake
            return;
        }

        // Route game packets
        switch (packet.getType()) {
            case BLOCK_CHANGE:
                handleBlockChange(ctx, (BlockChangePacket) packet);
                break;
            case PLAYER_POSITION:
                handlePlayerPosition(ctx, (PlayerPositionPacket) packet);
                break;
            case CHAT_MESSAGE:
                handleChatMessage(ctx, (ChatMessagePacket) packet);
                break;
            default:
                break;
        }
    }

    private void handleHandshake(ChannelHandlerContext ctx, HandshakePacket handshake) {
        clientVersion = ProtocolVersion.fromNumber(handshake.getProtocolVersion());
        if (clientVersion == null) {
            // Unknown protocol version — disconnect
            ctx.close();
            return;
        }

        // Compute active capabilities (intersection)
        List<Integer> activeCapabilities = new ArrayList<Integer>();
        for (int capId : handshake.getCapabilityIds()) {
            for (Capability cap : Capability.values()) {
                if (cap.getId() == capId && cap.isAvailableIn(clientVersion)) {
                    activeCapabilities.add(capId);
                }
            }
        }

        // If client is on a different version, insert version translator
        if (clientVersion != serverVersion) {
            ctx.pipeline().addBefore("handler", "translator",
                    new VersionTranslator(serverVersion, clientVersion));

            System.out.println("Client connected with " + clientVersion.getDisplayName()
                    + " protocol — version translator active");
        }

        // Send handshake response
        HandshakeResponsePacket response = new HandshakeResponsePacket(
                serverVersion.getVersionNumber(), activeCapabilities
        );
        ctx.writeAndFlush(response);

        handshakeComplete = true;
        System.out.println("Handshake complete: " + handshake.getClientName()
                + " (protocol v" + clientVersion.getVersionNumber() + ")");
    }

    private void handleBlockChange(ChannelHandlerContext ctx, BlockChangePacket packet) {
        // TODO: validate and apply to world state, then broadcast to all clients
    }

    private void handlePlayerPosition(ChannelHandlerContext ctx, PlayerPositionPacket packet) {
        // TODO: validate and broadcast to other clients
    }

    private void handleChatMessage(ChannelHandlerContext ctx, ChatMessagePacket packet) {
        // TODO: broadcast to all clients
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
