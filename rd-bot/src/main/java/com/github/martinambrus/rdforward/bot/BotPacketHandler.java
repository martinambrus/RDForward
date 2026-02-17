package com.github.martinambrus.rdforward.bot;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.crypto.CipherDecoder;
import com.github.martinambrus.rdforward.protocol.crypto.CipherEncoder;
import com.github.martinambrus.rdforward.protocol.crypto.MinecraftCipher;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.alpha.*;
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
 * Netty inbound handler for the bot client.
 * Routes received S2C packets to the BotSession for recording,
 * and drives the login handshake sequence.
 *
 * Supports:
 * - Alpha v1-v6 (direct handshake + login)
 * - Beta v7-v16 (String16 handshake + login)
 * - Beta v17-v29 (username;host:port handshake + login)
 * - Release v39-v78 (v39 handshake + encryption + ClientStatus login)
 */
public class BotPacketHandler extends SimpleChannelInboundHandler<Packet> {

    private final ProtocolVersion version;
    private final String username;
    private final int port;
    private volatile BotSession session;
    private final CountDownLatch sessionReady = new CountDownLatch(1);

    public BotPacketHandler(ProtocolVersion version, String username, int port) {
        this.version = version;
        this.username = username;
        this.port = port;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        session = new BotSession(ctx.channel(), version);
        sessionReady.countDown();

        if (version.isAtLeast(ProtocolVersion.RELEASE_1_3_1)) {
            // v39+ handshake: byte protocolVersion + String16 username + String16 host + int port
            // Server responds with EncryptionKeyRequest (no HandshakeS2C).
            ctx.writeAndFlush(new HandshakeC2SPacket(
                    version.getVersionNumber(), username, "localhost", port));
        } else if (version.isAtLeast(ProtocolVersion.BETA_1_8)) {
            // v17+ handshake: "username;host:port" format
            ctx.writeAndFlush(new HandshakeC2SPacket(username + ";localhost:" + port));
        } else {
            // Alpha/Beta pre-v17: plain username
            ctx.writeAndFlush(new HandshakeC2SPacket(username));
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        session.recordPacket(packet);

        // --- Login flow packets ---
        if (packet instanceof HandshakeS2CPacket) {
            handleHandshakeResponse(ctx);
        } else if (packet instanceof EncryptionKeyRequestPacket encReq) {
            handleEncryptionRequest(ctx, encReq);
        } else if (packet instanceof EncryptionKeyResponsePacket) {
            // S2C empty response = "encryption enabled on server side".
            // Install cipher handlers, then send ClientStatus to trigger login.
            handleEncryptionEnabled(ctx);
        }
        // --- Login S2C variants (most specific first) ---
        else if (packet instanceof LoginS2CPacketV39 loginV39) {
            session.recordLogin(loginV39.getEntityId());
        } else if (packet instanceof LoginS2CPacketV28 loginV28) {
            session.recordLogin(loginV28.getEntityId());
        } else if (packet instanceof LoginS2CPacketV23 loginV23) {
            session.recordLogin(loginV23.getEntityId());
        } else if (packet instanceof LoginS2CPacketV17 loginV17) {
            session.recordLogin(loginV17.getEntityId());
        } else if (packet instanceof LoginS2CPacket loginS2C) {
            session.recordLogin(loginS2C.getEntityId());
        } else if (packet instanceof LoginS2CPacketV2 loginV2) {
            session.recordLogin(loginV2.getEntityId());
        }
        // --- Game state packets ---
        else if (packet instanceof PlayerPositionAndLookS2CPacket posLook) {
            session.recordPosition(posLook.getX(), posLook.getY(), posLook.getZ(),
                    posLook.getYaw(), posLook.getPitch());
            if (!session.isLoginComplete()) {
                session.markLoginComplete();
            }
        } else if (packet instanceof BlockChangePacketV39 bcV39) {
            session.recordBlockChange(bcV39.getX(), bcV39.getY(),
                    bcV39.getZ(), bcV39.getBlockType());
        } else if (packet instanceof BlockChangePacket blockChange) {
            session.recordBlockChange(blockChange.getX(), blockChange.getY(),
                    blockChange.getZ(), blockChange.getBlockType());
        } else if (packet instanceof ChatPacket chat) {
            session.recordChat(chat.getMessage());
        } else if (packet instanceof SpawnPlayerPacketV39 spV39) {
            session.recordSpawnPlayer(spV39.getEntityId(), spV39.getPlayerName());
        } else if (packet instanceof SpawnPlayerPacket spawnPlayer) {
            session.recordSpawnPlayer(spawnPlayer.getEntityId(), spawnPlayer.getPlayerName());
        } else if (packet instanceof DestroyEntityPacketV39 deV39) {
            for (int id : deV39.getEntityIds()) {
                session.recordDespawn(id);
            }
        } else if (packet instanceof DestroyEntityPacket de) {
            session.recordDespawn(de.getEntityId());
        } else if (packet instanceof MapChunkPacket mc) {
            processAlphaChunk(mc);
        } else if (packet instanceof MapChunkPacketV39 mcV39) {
            processSectionedChunk(mcV39.getChunkX(), mcV39.getChunkZ(),
                    mcV39.getPrimaryBitMask(), mcV39.getCompressedData());
        } else if (packet instanceof MapChunkPacketV28 mcV28) {
            processSectionedChunk(mcV28.getChunkX(), mcV28.getChunkZ(),
                    mcV28.getPrimaryBitMask(), mcV28.getCompressedData());
        } else if (packet instanceof KeepAlivePacket) {
            if (packet instanceof KeepAlivePacketV17 ka17) {
                ctx.writeAndFlush(new KeepAlivePacketV17(ka17.getKeepAliveId()));
            } else {
                ctx.writeAndFlush(new KeepAlivePacket());
            }
        }
    }

    private void handleHandshakeResponse(ChannelHandlerContext ctx) {
        int pv = version.getVersionNumber();
        ctx.writeAndFlush(new LoginC2SPacket(pv, username));
    }

    /**
     * Handle EncryptionKeyRequest from v39+ server.
     * Generate shared secret, RSA-encrypt it and the verify token,
     * send EncryptionKeyResponse. The server will respond with an
     * empty EncryptionKeyResponse to signal cipher activation.
     */
    private byte[] sharedSecret;

    private void handleEncryptionRequest(ChannelHandlerContext ctx, EncryptionKeyRequestPacket packet) {
        try {
            // Reconstruct RSA public key from X.509 encoded bytes
            PublicKey publicKey = KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(packet.getPublicKey()));

            // Generate 16-byte random shared secret
            sharedSecret = new byte[16];
            new SecureRandom().nextBytes(sharedSecret);

            // RSA-encrypt shared secret
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedSecret = rsaCipher.doFinal(sharedSecret);

            // RSA-encrypt verify token
            rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedToken = rsaCipher.doFinal(packet.getVerifyToken());

            // Send encrypted response
            ctx.writeAndFlush(new EncryptionKeyResponsePacket(encryptedSecret, encryptedToken));
        } catch (Exception e) {
            System.err.println("BotPacketHandler encryption error: " + e.getMessage());
            ctx.close();
        }
    }

    /**
     * After receiving empty S2C EncryptionKeyResponse, install AES cipher
     * handlers and send ClientStatus(INITIAL_SPAWN) to complete login.
     */
    private void handleEncryptionEnabled(ChannelHandlerContext ctx) {
        try {
            // Install cipher handlers (shared secret = both AES key and IV)
            ctx.pipeline().addBefore("decoder", "cipherDecoder",
                    new CipherDecoder(new MinecraftCipher(Cipher.DECRYPT_MODE, sharedSecret)));
            ctx.pipeline().addBefore("encoder", "cipherEncoder",
                    new CipherEncoder(new MinecraftCipher(Cipher.ENCRYPT_MODE, sharedSecret)));

            // Send ClientStatus (INITIAL_SPAWN) to trigger login
            ctx.writeAndFlush(new ClientStatusPacket(ClientStatusPacket.INITIAL_SPAWN));
        } catch (Exception e) {
            System.err.println("BotPacketHandler cipher setup error: " + e.getMessage());
            ctx.close();
        }
    }

    /**
     * Decompress Alpha chunk data and extract the first 32768 bytes (block IDs).
     * Only processes full 16x128x16 chunks (sizeX=15, sizeY=127, sizeZ=15).
     */
    private void processAlphaChunk(MapChunkPacket mc) {
        // Only process full chunk columns
        if (mc.getSizeX() != 15 || mc.getSizeY() != 127 || mc.getSizeZ() != 15) return;
        // Must be chunk-aligned origin
        if ((mc.getX() & 15) != 0 || (mc.getZ() & 15) != 0) return;

        byte[] decompressed = decompress(mc.getCompressedData());
        if (decompressed == null || decompressed.length < 32768) return;

        byte[] blockIds = new byte[32768];
        System.arraycopy(decompressed, 0, blockIds, 0, 32768);
        session.recordChunkBlocks(mc.getX() >> 4, mc.getZ() >> 4, blockIds);
    }

    /**
     * Decompress section-based chunk data (V28/V39) and convert from section
     * YZX layout to AlphaChunk YZX layout.
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
                // Section YZX: index = (y&15)<<8 | z<<4 | x
                int sy = (i >> 8) & 15;
                int sz = (i >> 4) & 15;
                int sx = i & 15;
                // AlphaChunk YZX: index = y + z*128 + x*2048
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("BotPacketHandler error (" + version + "): " + cause.getMessage());
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

    public ProtocolVersion getVersion() {
        return version;
    }
}
