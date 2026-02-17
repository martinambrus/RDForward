package com.github.martinambrus.rdforward.bot;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.codec.NettyPacketDecoder;
import com.github.martinambrus.rdforward.protocol.codec.NettyPacketEncoder;
import com.github.martinambrus.rdforward.protocol.codec.RawPacketDecoder;
import com.github.martinambrus.rdforward.protocol.codec.RawPacketEncoder;
import com.github.martinambrus.rdforward.protocol.codec.VarIntFrameDecoder;
import com.github.martinambrus.rdforward.protocol.codec.VarIntFrameEncoder;
import com.github.martinambrus.rdforward.protocol.packet.ConnectionState;
import com.github.martinambrus.rdforward.protocol.packet.PacketDirection;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Headless bot client that connects to an RDServer instance.
 * Creates a Netty pipeline matching the server's expected codec for the
 * given protocol version family:
 * - Alpha/Beta/Release pre-Netty (raw TCP)
 * - Netty 1.7.2+ (VarInt-framed)
 */
public class BotClient {

    private final String host;
    private final int port;
    private final ProtocolVersion version;
    private final String username;
    private final EventLoopGroup group;

    private Channel channel;
    // Handler reference (pre-Netty or Netty)
    private BotPacketHandler preNettyHandler;
    private BotNettyPacketHandler nettyHandler;

    public BotClient(String host, int port, ProtocolVersion version,
                     String username, EventLoopGroup group) {
        this.host = host;
        this.port = port;
        this.version = version;
        this.username = username;
        this.group = group;
    }

    /**
     * Connect to the server, run the login handshake, and return the session.
     * Blocks until login completes or timeout is reached.
     *
     * @param timeoutMs maximum time to wait for login to complete
     * @return the BotSession with login state populated
     * @throws Exception if connection or login fails
     */
    public BotSession connectSync(long timeoutMs) throws Exception {
        boolean isNetty = version.isAtLeast(ProtocolVersion.RELEASE_1_7_2);

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) timeoutMs);

        if (isNetty) {
            nettyHandler = new BotNettyPacketHandler(version, username, host, port);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("frameDecoder", new VarIntFrameDecoder());
                    NettyPacketDecoder packetDecoder = new NettyPacketDecoder(
                            ConnectionState.HANDSHAKING, PacketDirection.SERVER_TO_CLIENT);
                    packetDecoder.setProtocolVersion(version.getVersionNumber());
                    pipeline.addLast("decoder", packetDecoder);
                    pipeline.addLast("frameEncoder", new VarIntFrameEncoder());
                    NettyPacketEncoder encoder = new NettyPacketEncoder(
                            ConnectionState.HANDSHAKING, PacketDirection.CLIENT_TO_SERVER);
                    encoder.setProtocolVersion(version.getVersionNumber());
                    pipeline.addLast("encoder", encoder);
                    pipeline.addLast("handler", nettyHandler);
                }
            });
        } else {
            preNettyHandler = new BotPacketHandler(version, username, port);
            boolean useString16 = version.isAtLeast(ProtocolVersion.BETA_1_5);

            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ChannelPipeline pipeline = ch.pipeline();
                    RawPacketDecoder decoder = new RawPacketDecoder(
                            PacketDirection.SERVER_TO_CLIENT, version);
                    decoder.setUseString16(useString16);
                    RawPacketEncoder encoder = new RawPacketEncoder();
                    encoder.setUseString16(useString16);
                    pipeline.addLast("decoder", decoder);
                    pipeline.addLast("encoder", encoder);
                    pipeline.addLast("handler", preNettyHandler);
                }
            });
        }

        ChannelFuture future = bootstrap.connect(host, port).sync();
        channel = future.channel();

        // Wait for channelActive to fire and create the session
        BotSession session;
        if (isNetty) {
            session = nettyHandler.awaitSession(timeoutMs);
        } else {
            session = preNettyHandler.awaitSession(timeoutMs);
        }
        if (session == null) {
            throw new RuntimeException("Session not created after " + timeoutMs + "ms for " + username);
        }
        if (!session.waitForLogin(timeoutMs)) {
            throw new RuntimeException("Login timed out after " + timeoutMs + "ms for " + username);
        }

        return session;
    }

    public void disconnect() {
        if (channel != null && channel.isActive()) {
            channel.close();
        }
    }

    public BotSession getSession() {
        if (nettyHandler != null) return nettyHandler.getSession();
        if (preNettyHandler != null) return preNettyHandler.getSession();
        return null;
    }

    public Channel getChannel() {
        return channel;
    }
}
