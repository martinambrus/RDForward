package com.github.martinambrus.rdforward.client;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Handles packets received from the server on the client side.
 *
 * Routes incoming packets to the appropriate client-side systems
 * (world renderer, player list, chat display, etc.)
 */
public class ClientConnectionHandler extends SimpleChannelInboundHandler<Packet> {

    private final ProtocolVersion clientVersion;
    private boolean handshakeComplete = false;

    public ClientConnectionHandler(ProtocolVersion clientVersion) {
        this.clientVersion = clientVersion;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        switch (packet.getType()) {
            case HANDSHAKE_RESPONSE:
                handleHandshakeResponse((HandshakeResponsePacket) packet);
                break;
            case BLOCK_CHANGE:
                handleBlockChange((BlockChangePacket) packet);
                break;
            case PLAYER_POSITION:
                handlePlayerPosition((PlayerPositionPacket) packet);
                break;
            case CHAT_MESSAGE:
                handleChatMessage((ChatMessagePacket) packet);
                break;
            default:
                // Unknown packet types are silently ignored (forward compat)
                break;
        }
    }

    private void handleHandshakeResponse(HandshakeResponsePacket response) {
        handshakeComplete = true;
        ProtocolVersion serverVersion = ProtocolVersion.fromNumber(response.getServerProtocolVersion());
        String serverName = serverVersion != null ? serverVersion.getDisplayName() : "Unknown";
        System.out.println("Handshake complete — server protocol: " + serverName
                + ", active capabilities: " + response.getActiveCapabilityIds().size());
    }

    private void handleBlockChange(BlockChangePacket packet) {
        // TODO: update local world state and trigger re-render
    }

    private void handlePlayerPosition(PlayerPositionPacket packet) {
        // TODO: update remote player position in the local world
    }

    private void handleChatMessage(ChatMessagePacket packet) {
        // TODO: display chat message
        System.out.println("[Chat] Player " + packet.getSenderId() + ": " + packet.getMessage());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
