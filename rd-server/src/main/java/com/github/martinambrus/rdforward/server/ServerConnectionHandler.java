package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.codec.PacketDecoder;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.classic.*;
import com.github.martinambrus.rdforward.protocol.translation.VersionTranslator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Handles individual client connections on the server side.
 *
 * Responsibilities:
 * 1. Process the initial Player Identification (Classic 0x00) to determine
 *    the client's protocol version
 * 2. Send Server Identification (Classic 0x00) in response
 * 3. Insert the appropriate VersionTranslator into the pipeline if needed
 * 4. Route game packets to the world state manager
 *
 * The login sequence follows the MC Classic protocol:
 *   Client: PlayerIdentification (0x00) — protocol version, username, key
 *   Server: ServerIdentification (0x00) — protocol version, name, MOTD, user type
 *   Server: LevelInitialize (0x02) + LevelDataChunk (0x03)... + LevelFinalize (0x04)
 *   Server: SpawnPlayer (0x07) with ID -1 (self)
 *   Normal gameplay begins
 */
public class ServerConnectionHandler extends SimpleChannelInboundHandler<Packet> {

    private final ProtocolVersion serverVersion;
    private ProtocolVersion clientVersion;
    private String clientUsername;
    private boolean loginComplete = false;

    public ServerConnectionHandler(ProtocolVersion serverVersion) {
        this.serverVersion = serverVersion;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        // First packet must be PlayerIdentification (Classic 0x00)
        if (!loginComplete && packet instanceof PlayerIdentificationPacket) {
            handlePlayerIdentification(ctx, (PlayerIdentificationPacket) packet);
            return;
        }

        if (!loginComplete) {
            // Drop packets before login is complete
            return;
        }

        // Route game packets by type
        if (packet instanceof SetBlockClientPacket) {
            handleSetBlock(ctx, (SetBlockClientPacket) packet);
        } else if (packet instanceof PlayerTeleportPacket) {
            handlePlayerPosition(ctx, (PlayerTeleportPacket) packet);
        } else if (packet instanceof MessagePacket) {
            handleMessage(ctx, (MessagePacket) packet);
        }
    }

    private void handlePlayerIdentification(ChannelHandlerContext ctx, PlayerIdentificationPacket identification) {
        clientVersion = ProtocolVersion.fromNumber(identification.getProtocolVersion());
        clientUsername = identification.getUsername();

        if (clientVersion == null) {
            // Unknown protocol version — disconnect with reason
            ctx.writeAndFlush(new DisconnectPacket("Unknown protocol version: " + identification.getProtocolVersion()));
            ctx.close();
            return;
        }

        // If client is on a different version, insert version translator
        if (clientVersion != serverVersion) {
            ctx.pipeline().addBefore("handler", "translator",
                    new VersionTranslator(serverVersion, clientVersion));

            // Update the decoder's protocol version so it creates the right packets
            PacketDecoder decoder = ctx.pipeline().get(PacketDecoder.class);
            if (decoder != null) {
                decoder.setProtocolVersion(clientVersion);
            }

            System.out.println("Client '" + clientUsername + "' connected with "
                    + clientVersion.getDisplayName() + " protocol — version translator active");
        }

        // Send Server Identification response (Classic 0x00 S->C)
        ServerIdentificationPacket response = new ServerIdentificationPacket(
                serverVersion.getVersionNumber(),
                "RDForward Server",
                "Welcome to RDForward!",
                ServerIdentificationPacket.USER_TYPE_NORMAL
        );
        ctx.writeAndFlush(response);

        loginComplete = true;
        System.out.println("Login complete: " + clientUsername
                + " (protocol: " + clientVersion.getDisplayName()
                + ", version " + clientVersion.getVersionNumber() + ")");

        // TODO: Send world data (LevelInitialize + LevelDataChunks + LevelFinalize)
        // TODO: Send SpawnPlayer with ID -1 for self-spawn
        // TODO: Send SpawnPlayer for each existing player
    }

    private void handleSetBlock(ChannelHandlerContext ctx, SetBlockClientPacket packet) {
        // TODO: validate and apply to world state, then broadcast to all clients
    }

    private void handlePlayerPosition(ChannelHandlerContext ctx, PlayerTeleportPacket packet) {
        // TODO: validate and broadcast to other clients
    }

    private void handleMessage(ChannelHandlerContext ctx, MessagePacket packet) {
        System.out.println("[Chat] " + clientUsername + ": " + packet.getMessage());
        // TODO: broadcast to all clients
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
