package com.github.martinambrus.rdforward.client;

import com.github.martinambrus.rdforward.protocol.Capability;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.codec.PacketDecoder;
import com.github.martinambrus.rdforward.protocol.codec.PacketEncoder;
import com.github.martinambrus.rdforward.protocol.packet.HandshakePacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.ArrayList;
import java.util.List;

/**
 * The RDForward multiplayer client.
 *
 * Connects to a server via Netty, performs the protocol handshake,
 * and integrates with the RubyDung game loop to send player actions
 * and receive world state updates.
 */
public class RDClient {

    private final ProtocolVersion clientVersion;
    private EventLoopGroup group;
    private Channel channel;

    public RDClient(ProtocolVersion clientVersion) {
        this.clientVersion = clientVersion;
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
                        pipeline.addLast("decoder", new PacketDecoder());
                        pipeline.addLast("encoder", new PacketEncoder());
                        pipeline.addLast("handler", new ClientConnectionHandler(clientVersion));
                    }
                })
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true);

        channel = bootstrap.connect(host, port).sync().channel();

        // Send handshake
        List<Integer> capabilities = new ArrayList<Integer>();
        for (Capability cap : Capability.values()) {
            if (cap.isAvailableIn(clientVersion)) {
                capabilities.add(cap.getId());
            }
        }

        HandshakePacket handshake = new HandshakePacket(
                clientVersion.getVersionNumber(),
                "RDForward/" + clientVersion.getDisplayName(),
                capabilities
        );
        channel.writeAndFlush(handshake);

        System.out.println("Connected to " + host + ":" + port);
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
