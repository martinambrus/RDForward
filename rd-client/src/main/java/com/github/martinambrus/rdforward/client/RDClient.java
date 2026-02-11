package com.github.martinambrus.rdforward.client;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.codec.PacketDecoder;
import com.github.martinambrus.rdforward.protocol.codec.PacketEncoder;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.PacketDirection;
import com.github.martinambrus.rdforward.protocol.packet.classic.MessagePacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.PlayerIdentificationPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.PlayerTeleportPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockClientPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * Singleton multiplayer client that connects to an RDForward server.
 *
 * Provides a thread-safe API for the game loop (via Mixins) to:
 *   - Connect/disconnect from a server
 *   - Send position updates
 *   - Send block place/break requests
 *   - Send chat messages
 *
 * The Netty I/O runs on a separate thread. Received packets are
 * processed by {@link ClientConnectionHandler} which writes to
 * the shared {@link MultiplayerState} singleton.
 */
public class RDClient {

    private static final RDClient INSTANCE = new RDClient();
    public static RDClient getInstance() { return INSTANCE; }

    private EventLoopGroup group;
    private Channel channel;
    private String username;

    private RDClient() {}

    /**
     * Connect to a server and send the login packet.
     * Runs asynchronously — returns immediately.
     */
    public void connect(String host, int port, String username) {
        if (isConnected()) {
            disconnect();
        }
        this.username = username;

        group = new NioEventLoopGroup(1, new DefaultThreadFactory("rd-client", true));
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("decoder", new PacketDecoder(
                                PacketDirection.SERVER_TO_CLIENT, ProtocolVersion.RUBYDUNG));
                        pipeline.addLast("encoder", new PacketEncoder());
                        pipeline.addLast("handler", new ClientConnectionHandler());
                    }
                })
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);

        bootstrap.connect(host, port).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                channel = future.channel();
                System.out.println("Connected to " + host + ":" + port + " as " + username);

                sendPacket(new PlayerIdentificationPacket(
                    ProtocolVersion.RUBYDUNG.getVersionNumber(), username, ""
                ));
            } else {
                System.err.println("Failed to connect to " + host + ":" + port
                    + ": " + future.cause().getMessage());
                shutdown();
            }
        });
    }

    /**
     * Disconnect from the server.
     */
    public void disconnect() {
        if (channel != null && channel.isActive()) {
            channel.close();
        }
        shutdown();
    }

    /**
     * Send a position update to the server.
     * Positions are in MC Classic fixed-point units (blocks * 32).
     */
    public void sendPosition(short x, short y, short z, int yaw, int pitch) {
        sendPacket(new PlayerTeleportPacket(0xFF, x, y, z, yaw, pitch));
    }

    /**
     * Send a block place/destroy request to the server.
     * @param mode 0 = destroy, 1 = place
     * @param blockType block ID (only used when mode = 1)
     */
    public void sendBlockChange(int x, int y, int z, int mode, int blockType) {
        sendPacket(new SetBlockClientPacket(x, y, z, mode, blockType));
    }

    /**
     * Send a chat message to the server.
     */
    public void sendChat(String message) {
        sendPacket(new MessagePacket(0xFF, message));
    }

    public boolean isConnected() {
        return channel != null && channel.isActive();
    }

    public String getUsername() { return username; }

    private void sendPacket(Packet packet) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(packet);
        }
    }

    private void shutdown() {
        if (group != null) {
            group.shutdownGracefully();
            group = null;
        }
        channel = null;
        MultiplayerState.getInstance().setConnected(false);
    }
}
