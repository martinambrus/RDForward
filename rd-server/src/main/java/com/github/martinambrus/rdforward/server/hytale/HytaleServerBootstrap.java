package com.github.martinambrus.rdforward.server.hytale;

import com.github.martinambrus.rdforward.server.ChunkManager;
import com.github.martinambrus.rdforward.server.PlayerManager;
import com.github.martinambrus.rdforward.server.ServerWorld;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.bootstrap.Bootstrap;
import io.netty.incubator.codec.quic.InsecureQuicTokenHandler;
import io.netty.incubator.codec.quic.QuicChannel;
import io.netty.incubator.codec.quic.QuicServerCodecBuilder;
import io.netty.incubator.codec.quic.QuicSslContext;
import io.netty.incubator.codec.quic.QuicSslContextBuilder;
import io.netty.incubator.codec.quic.QuicStreamChannel;

import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * Bootstraps the QUIC server for Hytale client connections on port 5520.
 *
 * Uses netty-incubator-codec-quic with a self-signed certificate for TLS 1.3
 * (required by QUIC). In offline/insecure mode, the Hytale client does not
 * validate the server's certificate chain.
 *
 * Architecture:
 * - Parent QuicChannel = one QUIC connection (one Hytale client)
 * - Child QuicStreamChannel = one bidirectional stream (Default, Chunks, WorldMap)
 * - Each stream gets HytaleFrameCodec + HytaleLoginHandler in its pipeline
 * - The HytaleSession object is shared across all streams of the same connection
 */
public class HytaleServerBootstrap {

    private final EventLoopGroup workerGroup;
    private final ServerWorld world;
    private final PlayerManager playerManager;
    private final ChunkManager chunkManager;
    private final HytaleBlockMapper blockMapper;
    private final HytaleChunkConverter chunkConverter;

    private Channel channel;

    public HytaleServerBootstrap(EventLoopGroup workerGroup, ServerWorld world,
                                  PlayerManager playerManager, ChunkManager chunkManager,
                                  HytaleBlockMapper blockMapper, HytaleChunkConverter chunkConverter) {
        this.workerGroup = workerGroup;
        this.world = world;
        this.playerManager = playerManager;
        this.chunkManager = chunkManager;
        this.blockMapper = blockMapper;
        this.chunkConverter = chunkConverter;
    }

    /**
     * Start the QUIC server on the specified port.
     * @return the bound channel, or null if startup failed
     */
    public Channel start(int port) {
        try {
            // Use shared worker group (same as main TCP server)
            // Generate self-signed certificate for QUIC/TLS 1.3
            io.netty.handler.ssl.util.SelfSignedCertificate ssc =
                    new io.netty.handler.ssl.util.SelfSignedCertificate();

            // Compute SHA-256 fingerprint of the server cert for mTLS token binding
            try {
                java.security.cert.X509Certificate cert = (java.security.cert.X509Certificate)
                        java.security.cert.CertificateFactory.getInstance("X.509")
                        .generateCertificate(new java.io.FileInputStream(ssc.certificate()));
                byte[] certDer = cert.getEncoded();
                MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                byte[] hash = sha256.digest(certDer);
                String fingerprint = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
                HytaleAuthManager.getInstance().setServerCertFingerprint(fingerprint);
                System.out.println("[Hytale] Server certificate fingerprint: " + fingerprint);
            } catch (Exception e) {
                System.err.println("[Hytale] Warning: could not compute cert fingerprint: " + e.getMessage());
            }

            QuicSslContext sslContext = QuicSslContextBuilder.forServer(
                    ssc.privateKey(), null, ssc.certificate())
                    .applicationProtocols("hytale/2", "hytale/1")
                    .clientAuth(io.netty.handler.ssl.ClientAuth.REQUIRE)
                    .trustManager(io.netty.handler.ssl.util.InsecureTrustManagerFactory.INSTANCE)
                    .earlyData(false)
                    .build();

            ChannelHandler quicServerCodec = new QuicServerCodecBuilder()
                    .sslContext(sslContext)
                    .maxIdleTimeout(HytaleProtocolConstants.IDLE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .initialMaxData(HytaleProtocolConstants.MAX_PAYLOAD_SIZE)
                    .initialMaxStreamDataBidirectionalLocal(HytaleProtocolConstants.MAX_PAYLOAD_SIZE)
                    .initialMaxStreamDataBidirectionalRemote(HytaleProtocolConstants.MAX_PAYLOAD_SIZE)
                    .initialMaxStreamsBidirectional(8) // Default + Chunks + WorldMap + spare
                    .tokenHandler(InsecureQuicTokenHandler.INSTANCE)
                    // Parent handler: fires when a new QUIC connection is established
                    .handler(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            QuicChannel quicChannel = (QuicChannel) ctx.channel();
                            System.out.println("[Hytale] New QUIC connection from "
                                    + quicChannel.remoteAddress());

                            // Create a shared session for all streams of this connection
                            HytaleSession session = new HytaleSession(quicChannel);
                            quicChannel.attr(HytaleSession.SESSION_KEY).set(session);

                            super.channelActive(ctx);
                        }

                        @Override
                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                            try {
                                QuicChannel quicChannel = (QuicChannel) ctx.channel();
                                HytaleSession session = quicChannel.attr(HytaleSession.SESSION_KEY).get();
                                String name = session != null ? session.getUsername() : "unknown";
                                if (session != null) {
                                    session.onDisconnect();
                                }
                                System.out.println("[Hytale] QUIC connection closed from " + name);
                            } catch (Exception e) {
                                System.err.println("[Hytale] Error handling QUIC disconnect: " + e.getMessage());
                            }
                            super.channelInactive(ctx);
                        }

                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                            System.err.println("[Hytale] QUIC connection error: " + cause.getMessage());
                        }
                    })
                    // Stream handler: fires when a new QUIC stream is opened by client
                    .streamHandler(new ChannelInitializer<QuicStreamChannel>() {
                        @Override
                        protected void initChannel(QuicStreamChannel ch) {
                            // Get the shared session from the parent QuicChannel
                            QuicChannel parent = (QuicChannel) ch.parent();
                            HytaleSession session = parent.attr(HytaleSession.SESSION_KEY).get();

                            ch.pipeline().addLast("frameCodec", new HytaleFrameCodec());
                            ch.pipeline().addLast("handler", new HytaleLoginHandler(
                                    session, world, playerManager, chunkManager,
                                    blockMapper, chunkConverter));
                        }
                    })
                    .build();

            // Bind QUIC server to UDP port
            channel = new Bootstrap()
                    .group(workerGroup)
                    .channel(NioDatagramChannel.class)
                    .handler(quicServerCodec)
                    .bind(new InetSocketAddress(port))
                    .sync()
                    .channel();

            return channel;
        } catch (Exception e) {
            System.err.println("[Hytale] Failed to start QUIC server on port " + port
                    + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /** Stop the QUIC server. */
    public void stop() {
        if (channel != null && channel.isOpen()) {
            try {
                channel.close().syncUninterruptibly();
            } catch (Exception e) {
                // Ignore errors during shutdown
            }
        }
    }
}
