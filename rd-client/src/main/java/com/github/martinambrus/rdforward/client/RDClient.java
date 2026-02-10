package com.github.martinambrus.rdforward.client;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.codec.PacketDecoder;
import com.github.martinambrus.rdforward.protocol.codec.PacketEncoder;
import com.github.martinambrus.rdforward.protocol.packet.PacketDirection;
import com.github.martinambrus.rdforward.protocol.packet.classic.PlayerIdentificationPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * The RDForward multiplayer client.
 *
 * Connects to a server via Netty, performs the MC Classic login handshake,
 * and integrates with the RubyDung game loop to send player actions
 * and receive world state updates.
 *
 * Login sequence (follows MC Classic protocol):
 *   Client: PlayerIdentification (0x00) — protocol version + username
 *   Server: ServerIdentification (0x00) — server info
 *   Server: World data transfer (0x02 + 0x03... + 0x04)
 *   Server: SpawnPlayer (0x07) with ID -1
 *   Normal gameplay begins
 */
public class RDClient {

    private final ProtocolVersion clientVersion;
    private final String username;
    private EventLoopGroup group;
    private Channel channel;

    public RDClient(ProtocolVersion clientVersion, String username) {
        this.clientVersion = clientVersion;
        this.username = username;
    }

    /**
     * Connect to a server at the given host:port.
     */
    public void connect(String host, int port) throws InterruptedException {
        group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        // Client reads SERVER_TO_CLIENT packets
                        pipeline.addLast("decoder", new PacketDecoder(
                                PacketDirection.SERVER_TO_CLIENT, clientVersion));
                        pipeline.addLast("encoder", new PacketEncoder());
                        pipeline.addLast("handler", new ClientConnectionHandler(clientVersion));
                    }
                })
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true);

        channel = bootstrap.connect(host, port).sync().channel();

        // Send Player Identification (Classic 0x00 C->S)
        PlayerIdentificationPacket identification = new PlayerIdentificationPacket(
                clientVersion.getVersionNumber(),
                username,
                ""  // verification key (empty for offline mode)
        );
        channel.writeAndFlush(identification);

        System.out.println("Connected to " + host + ":" + port + " as " + username);
    }

    /**
     * Disconnect from the server.
     */
    public void disconnect() {
        if (channel != null) {
            channel.close();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
    }

    /**
     * Get the Netty channel for sending packets.
     */
    public Channel getChannel() {
        return channel;
    }
}
