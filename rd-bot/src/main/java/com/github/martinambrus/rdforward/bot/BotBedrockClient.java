package com.github.martinambrus.rdforward.bot;

import com.github.martinambrus.rdforward.server.bedrock.BedrockProtocolConstants;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.cloudburstmc.netty.channel.raknet.RakChannelFactory;
import org.cloudburstmc.netty.channel.raknet.config.RakChannelOption;
import org.cloudburstmc.protocol.bedrock.BedrockClientSession;
import org.cloudburstmc.protocol.bedrock.netty.initializer.BedrockClientInitializer;

/**
 * Headless Bedrock bot client that connects to an RDServer's Bedrock listener
 * via RakNet/UDP using CloudburstMC Protocol's client support.
 */
public class BotBedrockClient {

    private final String host;
    private final int port;
    private final String username;
    private final EventLoopGroup group;

    private Channel channel;
    private BotBedrockPacketHandler handler;

    public BotBedrockClient(String host, int port, String username, EventLoopGroup group) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.group = group;
    }

    /**
     * Connect to the server via RakNet, run the Bedrock login handshake,
     * and return the session. Blocks until login completes or timeout.
     */
    public BotSession connectSync(long timeoutMs) throws Exception {
        handler = new BotBedrockPacketHandler(username);

        Bootstrap bootstrap = new Bootstrap()
                .channelFactory(RakChannelFactory.client(NioDatagramChannel.class))
                .group(group)
                .option(RakChannelOption.RAK_PROTOCOL_VERSION, 11)
                .handler(new BedrockClientInitializer() {
                    @Override
                    protected void initSession(BedrockClientSession session) {
                        session.setCodec(BedrockProtocolConstants.CODEC);
                        handler.init(session);
                        session.setPacketHandler(handler);
                    }
                });

        ChannelFuture future = bootstrap.connect(host, port).sync();
        channel = future.channel();

        // Start the login handshake (send RequestNetworkSettings)
        handler.startLogin();

        BotSession session = handler.awaitSession(timeoutMs);
        if (session == null) {
            throw new RuntimeException("Bedrock session not created after " + timeoutMs + "ms for " + username);
        }
        if (!session.waitForLogin(timeoutMs)) {
            throw new RuntimeException("Bedrock login timed out after " + timeoutMs + "ms for " + username);
        }

        return session;
    }

    public void disconnect() {
        if (handler != null) {
            handler.disconnect();
        }
        if (channel != null && channel.isActive()) {
            channel.close();
        }
    }

    public BotSession getSession() {
        return handler != null ? handler.getSession() : null;
    }
}
