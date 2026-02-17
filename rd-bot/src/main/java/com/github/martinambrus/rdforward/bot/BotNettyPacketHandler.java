package com.github.martinambrus.rdforward.bot;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.codec.NettyPacketDecoder;
import com.github.martinambrus.rdforward.protocol.codec.NettyPacketEncoder;
import com.github.martinambrus.rdforward.protocol.crypto.CipherDecoder;
import com.github.martinambrus.rdforward.protocol.crypto.CipherEncoder;
import com.github.martinambrus.rdforward.protocol.crypto.MinecraftCipher;
import com.github.martinambrus.rdforward.protocol.packet.ConnectionState;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.alpha.KeepAlivePacketV17;
import com.github.martinambrus.rdforward.protocol.packet.netty.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Netty protocol handler for bot clients (1.7.2+ / v4-v47).
 *
 * Manages the state machine: HANDSHAKING -> LOGIN -> PLAY.
 * Handles encryption handshake, state transitions, and routing
 * of S2C packets to BotSession. Supports both v4/v5 and v47 (1.8)
 * packet variants.
 */
public class BotNettyPacketHandler extends SimpleChannelInboundHandler<Packet> {

    private final ProtocolVersion version;
    private final String username;
    private final String host;
    private final int port;
    private volatile BotSession session;
    private final CountDownLatch sessionReady = new CountDownLatch(1);

    private byte[] sharedSecret;

    public BotNettyPacketHandler(ProtocolVersion version, String username, String host, int port) {
        this.version = version;
        this.username = username;
        this.host = host;
        this.port = port;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        session = new BotSession(ctx.channel(), version);
        sessionReady.countDown();

        // Send Handshake: nextState=2 (Login)
        ctx.writeAndFlush(new NettyHandshakePacket(
                version.getVersionNumber(), host, port, 2));

        // Transition decoder/encoder to LOGIN state
        setCodecState(ctx, ConnectionState.LOGIN);

        // Send LoginStart
        ctx.writeAndFlush(new LoginStartPacket(username));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        session.recordPacket(packet);

        // --- Login state packets ---
        // V47 EncryptionRequest must be checked BEFORE base (no inheritance)
        if (packet instanceof NettyEncryptionRequestPacketV47 encReqV47) {
            handleEncryptionRequest(ctx, encReqV47.getPublicKey(), encReqV47.getVerifyToken(), true);
        } else if (packet instanceof NettyEncryptionRequestPacket encReq) {
            handleEncryptionRequest(ctx, encReq.getPublicKey(), encReq.getVerifyToken(), false);
        } else if (packet instanceof LoginSuccessPacket) {
            // Transition to PLAY state
            setCodecState(ctx, ConnectionState.PLAY);
        }
        // --- Play state packets ---
        // V47 variants must be checked BEFORE base (no inheritance)
        else if (packet instanceof JoinGamePacketV47 jgV47) {
            session.recordLogin(jgV47.getEntityId());
        } else if (packet instanceof JoinGamePacket joinGame) {
            session.recordLogin(joinGame.getEntityId());
        } else if (packet instanceof NettyPlayerPositionS2CPacketV47 posV47) {
            session.recordPosition(posV47.getX(), posV47.getY(), posV47.getZ(),
                    posV47.getYaw(), posV47.getPitch());
            if (!session.isLoginComplete()) {
                session.markLoginComplete();
            }
        } else if (packet instanceof NettyPlayerPositionS2CPacket posLook) {
            session.recordPosition(posLook.getX(), posLook.getY(), posLook.getZ(),
                    posLook.getYaw(), posLook.getPitch());
            if (!session.isLoginComplete()) {
                session.markLoginComplete();
            }
        } else if (packet instanceof NettyBlockChangePacketV47 bcV47) {
            session.recordBlockChange(bcV47.getX(), bcV47.getY(), bcV47.getZ(), bcV47.getBlockId());
        } else if (packet instanceof NettyBlockChangePacket bc) {
            session.recordBlockChange(bc.getX(), bc.getY(), bc.getZ(), bc.getBlockId());
        } else if (packet instanceof NettyChatS2CPacketV47 chatV47) {
            recordChatFromJson(chatV47.getJsonMessage());
        } else if (packet instanceof NettyChatS2CPacket chat) {
            recordChatFromJson(chat.getJsonMessage());
        } else if (packet instanceof NettySpawnPlayerPacketV47 spV47) {
            session.recordSpawnPlayer(spV47.getEntityId(), "v47_player");
        } else if (packet instanceof NettySpawnPlayerPacketV5 spV5) {
            session.recordSpawnPlayer(spV5.getEntityId(), spV5.getPlayerName());
        } else if (packet instanceof NettySpawnPlayerPacket sp) {
            session.recordSpawnPlayer(sp.getEntityId(), sp.getPlayerName());
        } else if (packet instanceof KeepAlivePacketV47 kaV47) {
            ctx.writeAndFlush(new KeepAlivePacketV47(kaV47.getKeepAliveId()));
        } else if (packet instanceof KeepAlivePacketV17 ka) {
            ctx.writeAndFlush(new KeepAlivePacketV17(ka.getKeepAliveId()));
        } else if (packet instanceof NettyDisconnectPacket disconnect) {
            System.err.println("BotNetty disconnected: " + disconnect.getJsonReason());
            ctx.close();
        }
    }

    private void recordChatFromJson(String msg) {
        if (msg.startsWith("{")) {
            int start = msg.indexOf("\"text\":\"");
            if (start >= 0) {
                start += 8;
                int end = msg.indexOf("\"", start);
                if (end >= 0) {
                    msg = msg.substring(start, end);
                }
            }
        }
        session.recordChat(msg);
    }

    private void handleEncryptionRequest(ChannelHandlerContext ctx,
                                          byte[] pubKeyBytes, byte[] tokenBytes, boolean isV47) {
        try {
            PublicKey publicKey = KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(pubKeyBytes));

            sharedSecret = new byte[16];
            new SecureRandom().nextBytes(sharedSecret);

            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedSecret = rsaCipher.doFinal(sharedSecret);

            rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedToken = rsaCipher.doFinal(tokenBytes);

            Packet response = isV47
                    ? new NettyEncryptionResponsePacketV47(encryptedSecret, encryptedToken)
                    : new NettyEncryptionResponsePacket(encryptedSecret, encryptedToken);

            ctx.writeAndFlush(response)
                    .addListener(future -> {
                        if (future.isSuccess()) {
                            // Install cipher handlers BEFORE the frame codecs:
                            // Inbound: cipherDecoder → frameDecoder → decoder
                            // Outbound: encoder → frameEncoder → cipherEncoder
                            ctx.pipeline().addBefore("frameDecoder", "cipherDecoder",
                                    new CipherDecoder(new MinecraftCipher(Cipher.DECRYPT_MODE, sharedSecret)));
                            ctx.pipeline().addBefore("frameEncoder", "cipherEncoder",
                                    new CipherEncoder(new MinecraftCipher(Cipher.ENCRYPT_MODE, sharedSecret)));
                        }
                    });
        } catch (Exception e) {
            System.err.println("BotNettyPacketHandler encryption error: " + e.getMessage());
            ctx.close();
        }
    }

    private void setCodecState(ChannelHandlerContext ctx, ConnectionState state) {
        NettyPacketDecoder decoder = ctx.pipeline().get(NettyPacketDecoder.class);
        if (decoder != null) {
            decoder.setConnectionState(state);
        }
        NettyPacketEncoder encoder = ctx.pipeline().get(NettyPacketEncoder.class);
        if (encoder != null) {
            encoder.setConnectionState(state);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("BotNettyPacketHandler error (" + version + "): " + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }

    public BotSession awaitSession(long timeoutMs) throws InterruptedException {
        sessionReady.await(timeoutMs, TimeUnit.MILLISECONDS);
        return session;
    }

    public BotSession getSession() {
        return session;
    }
}
