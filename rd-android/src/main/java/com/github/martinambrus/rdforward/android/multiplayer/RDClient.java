package com.github.martinambrus.rdforward.android.multiplayer;

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
 * Singleton multiplayer client for Android.
 * Mirrors the desktop rd-client RDClient but lives in the Android module.
 */
public class RDClient {

    private static final RDClient INSTANCE = new RDClient();
    public static RDClient getInstance() { return INSTANCE; }

    private EventLoopGroup group;
    private Channel channel;
    private String username;
    private volatile boolean connectionFailed = false;

    private RDClient() {}

    public void connect(String host, int port, String username) {
        if (isConnected()) disconnect();
        this.username = username;
        this.connectionFailed = false;
        MultiplayerState.getInstance().resetWorldData();

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
                    ProtocolVersion.RUBYDUNG.getVersionNumber(), username, ""));
            } else {
                System.err.println("Failed to connect to " + host + ":" + port
                    + ": " + future.cause().getMessage());
                connectionFailed = true;
                shutdown();
            }
        });
    }

    public void disconnect() {
        if (channel != null && channel.isActive()) channel.close();
        shutdown();
    }

    public void sendPosition(short x, short y, short z, int yaw, int pitch) {
        sendPacket(new PlayerTeleportPacket(0xFF, x, y, z, yaw, pitch));
    }

    public void sendBlockChange(int x, int y, int z, int mode, int blockType) {
        sendPacket(new SetBlockClientPacket(x, y, z, mode, blockType));
    }

    public void sendChat(String message) {
        sendPacket(new MessagePacket(0xFF, message));
    }

    public boolean isConnected() { return channel != null && channel.isActive(); }
    public boolean hasConnectionFailed() { return connectionFailed; }
    public String getUsername() { return username; }

    private void sendPacket(Packet packet) {
        if (channel != null && channel.isActive()) channel.writeAndFlush(packet);
    }

    private void shutdown() {
        if (group != null) { group.shutdownGracefully(); group = null; }
        channel = null;
        MultiplayerState.getInstance().setConnected(false);
    }
}
