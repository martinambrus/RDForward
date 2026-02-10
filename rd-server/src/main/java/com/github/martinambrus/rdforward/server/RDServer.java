package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.codec.PacketDecoder;
import com.github.martinambrus.rdforward.protocol.codec.PacketEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * The RDForward dedicated server.
 *
 * Manages the world state, accepts client connections via Netty,
 * runs the server tick loop, and dispatches events to mods.
 *
 * The server is authoritative: clients send requests (place block,
 * move), the server validates them, updates world state, and
 * broadcasts the result to all connected clients.
 */
public class RDServer {

    private final int port;
    private final ProtocolVersion protocolVersion;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    public RDServer(int port, ProtocolVersion protocolVersion) {
        this.port = port;
        this.protocolVersion = protocolVersion;
    }

    /**
     * Start the server and begin accepting connections.
     */
    public void start() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();

                        // Codec layer: bytes <-> Packet objects
                        pipeline.addLast("decoder", new PacketDecoder());
                        pipeline.addLast("encoder", new PacketEncoder());

                        // Version translation is added dynamically after
                        // the handshake reveals the client's protocol version.
                        // See ServerConnectionHandler for the handshake logic.

                        pipeline.addLast("handler", new ServerConnectionHandler(protocolVersion));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true);

        serverChannel = bootstrap.bind(port).sync().channel();
        System.out.println("RDForward server started on port " + port
                + " (protocol: " + protocolVersion.getDisplayName() + ")");
    }

    /**
     * Stop the server and release all resources.
     */
    public void stop() {
        if (serverChannel != null) {
            serverChannel.close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        System.out.println("RDForward server stopped.");
    }

    public int getPort() {
        return port;
    }

    public ProtocolVersion getProtocolVersion() {
        return protocolVersion;
    }
}
