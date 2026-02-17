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
import com.github.martinambrus.rdforward.protocol.packet.alpha.MapChunkPacketV39;
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
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

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
        } else if (packet instanceof NettyDestroyEntitiesPacketV47 deV47) {
            for (int id : deV47.getEntityIds()) {
                session.recordDespawn(id);
            }
        } else if (packet instanceof NettyDestroyEntitiesPacket de) {
            for (int id : de.getEntityIds()) {
                session.recordDespawn(id);
            }
        } else if (packet instanceof KeepAlivePacketV47 kaV47) {
            ctx.writeAndFlush(new KeepAlivePacketV47(kaV47.getKeepAliveId()));
        } else if (packet instanceof KeepAlivePacketV17 ka) {
            ctx.writeAndFlush(new KeepAlivePacketV17(ka.getKeepAliveId()));
        } else if (packet instanceof MapChunkPacketV47 mcV47) {
            processV47Chunk(mcV47);
        } else if (packet instanceof MapChunkPacketV39 mcV39) {
            processSectionedChunk(mcV39.getChunkX(), mcV39.getChunkZ(),
                    mcV39.getPrimaryBitMask(), mcV39.getCompressedData());
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

    /**
     * Parse V47 (1.8) chunk data: ushort blockStates per section,
     * extracting block IDs (state >> 4) into AlphaChunk YZX layout.
     */
    private void processV47Chunk(MapChunkPacketV47 packet) {
        byte[] data = packet.getData();
        int primaryBitMask = packet.getPrimaryBitMask();
        byte[] blockIds = new byte[32768];
        int offset = 0;

        for (int section = 0; section < 16; section++) {
            if ((primaryBitMask & (1 << section)) == 0) continue;
            // Each section has 4096 ushort block states (8192 bytes)
            if (offset + 8192 > data.length) break;

            int baseY = section * 16;
            for (int i = 0; i < 4096; i++) {
                // Section YZX: index = (y&15)<<8 | z<<4 | x
                int sy = (i >> 8) & 15;
                int sz = (i >> 4) & 15;
                int sx = i & 15;
                // Read ushort little-endian block state
                int lo = data[offset + i * 2] & 0xFF;
                int hi = data[offset + i * 2 + 1] & 0xFF;
                int blockState = lo | (hi << 8);
                int blockId = blockState >> 4;
                // AlphaChunk YZX: index = y + z*128 + x*2048
                int alphaIndex = (baseY + sy) + sz * 128 + sx * 2048;
                blockIds[alphaIndex] = (byte) blockId;
            }
            offset += 8192;
        }

        session.recordChunkBlocks(packet.getChunkX(), packet.getChunkZ(), blockIds);
    }

    /**
     * Decompress section-based chunk data (V39, used by 1.7.x Netty path)
     * and convert from section YZX layout to AlphaChunk YZX layout.
     */
    private void processSectionedChunk(int chunkX, int chunkZ, short primaryBitMask, byte[] compressedData) {
        byte[] decompressed = decompress(compressedData);
        if (decompressed == null) return;

        byte[] blockIds = new byte[32768];
        int offset = 0;

        for (int section = 0; section < 16; section++) {
            if ((primaryBitMask & (1 << section)) == 0) continue;
            if (offset + 4096 > decompressed.length) break;

            int baseY = section * 16;
            for (int i = 0; i < 4096; i++) {
                int sy = (i >> 8) & 15;
                int sz = (i >> 4) & 15;
                int sx = i & 15;
                int alphaIndex = (baseY + sy) + sz * 128 + sx * 2048;
                blockIds[alphaIndex] = decompressed[offset + i];
            }
            offset += 4096;
        }

        session.recordChunkBlocks(chunkX, chunkZ, blockIds);
    }

    private byte[] decompress(byte[] compressed) {
        try {
            Inflater inflater = new Inflater();
            inflater.setInput(compressed);
            byte[] buf = new byte[4096];
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream(compressed.length * 4);
            while (!inflater.finished()) {
                int count = inflater.inflate(buf);
                if (count == 0 && inflater.needsInput()) break;
                out.write(buf, 0, count);
            }
            inflater.end();
            return out.toByteArray();
        } catch (DataFormatException e) {
            return null;
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
