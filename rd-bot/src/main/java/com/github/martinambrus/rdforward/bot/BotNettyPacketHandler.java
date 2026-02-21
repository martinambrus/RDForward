package com.github.martinambrus.rdforward.bot;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.codec.NettyPacketDecoder;
import com.github.martinambrus.rdforward.protocol.codec.NettyPacketEncoder;
import com.github.martinambrus.rdforward.protocol.crypto.CipherDecoder;
import com.github.martinambrus.rdforward.protocol.crypto.CipherEncoder;
import com.github.martinambrus.rdforward.protocol.crypto.MinecraftCipher;
import com.github.martinambrus.rdforward.protocol.packet.ConnectionState;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.alpha.KeepAlivePacketV17;
import com.github.martinambrus.rdforward.protocol.packet.alpha.MapChunkPacketV39;
import com.github.martinambrus.rdforward.protocol.packet.netty.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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
 * Netty protocol handler for bot clients (1.7.2+ / v4-v774).
 *
 * Manages the state machine: HANDSHAKING -> LOGIN -> [CONFIGURATION] -> PLAY.
 * Handles encryption handshake, state transitions, and routing
 * of S2C packets to BotSession. Supports v4 through v774 (1.21.11)
 * packet variants, including CONFIGURATION state for v764+.
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

        // === LOGIN state ===
        // V766 EncryptionRequest (has shouldAuthenticate boolean)
        if (packet instanceof NettyEncryptionRequestPacketV766 encReqV766) {
            handleEncryptionRequest(ctx, encReqV766.getPublicKey(), encReqV766.getVerifyToken(), true);
        }
        // V47 EncryptionRequest (VarInt-length byte arrays)
        else if (packet instanceof NettyEncryptionRequestPacketV47 encReqV47) {
            handleEncryptionRequest(ctx, encReqV47.getPublicKey(), encReqV47.getVerifyToken(), true);
        }
        // Base EncryptionRequest (short-length byte arrays)
        else if (packet instanceof NettyEncryptionRequestPacket encReq) {
            handleEncryptionRequest(ctx, encReq.getPublicKey(), encReq.getVerifyToken(), false);
        }
        // LoginSuccess — all variants (no inheritance, order irrelevant)
        else if (packet instanceof LoginSuccessPacketV768
                || packet instanceof LoginSuccessPacketV766
                || packet instanceof LoginSuccessPacketV759
                || packet instanceof LoginSuccessPacketV735
                || packet instanceof LoginSuccessPacket) {
            handleLoginSuccess(ctx);
        }

        // === CONFIGURATION state (v764+) ===
        else if (packet instanceof SelectKnownPacksS2CPacket) {
            ctx.writeAndFlush(new SelectKnownPacksC2SPacket());
        }
        else if (packet instanceof ConfigFinishS2CPacket) {
            ctx.writeAndFlush(new ConfigFinishC2SPacket());
            setCodecState(ctx, ConnectionState.PLAY);
        }

        // === PLAY state — JoinGame variants (newest first) ===
        else if (packet instanceof JoinGamePacketV768 jg) { session.recordLogin(jg.getEntityId()); }
        else if (packet instanceof JoinGamePacketV766 jg) { session.recordLogin(jg.getEntityId()); }
        else if (packet instanceof JoinGamePacketV764 jg) { session.recordLogin(jg.getEntityId()); }
        else if (packet instanceof JoinGamePacketV763 jg) { session.recordLogin(jg.getEntityId()); }
        else if (packet instanceof JoinGamePacketV762 jg) { session.recordLogin(jg.getEntityId()); }
        else if (packet instanceof JoinGamePacketV760 jg) { session.recordLogin(jg.getEntityId()); }
        else if (packet instanceof JoinGamePacketV759 jg) { session.recordLogin(jg.getEntityId()); }
        else if (packet instanceof JoinGamePacketV758 jg) { session.recordLogin(jg.getEntityId()); }
        else if (packet instanceof JoinGamePacketV757 jg) { session.recordLogin(jg.getEntityId()); }
        else if (packet instanceof JoinGamePacketV755 jg) { session.recordLogin(jg.getEntityId()); }
        else if (packet instanceof JoinGamePacketV751 jg) { session.recordLogin(jg.getEntityId()); }
        else if (packet instanceof JoinGamePacketV735 jg) { session.recordLogin(jg.getEntityId()); }
        else if (packet instanceof JoinGamePacketV573 jg) { session.recordLogin(jg.getEntityId()); }
        else if (packet instanceof JoinGamePacketV477 jg) { session.recordLogin(jg.getEntityId()); }
        else if (packet instanceof JoinGamePacketV108 jg) { session.recordLogin(jg.getEntityId()); }
        else if (packet instanceof JoinGamePacketV47 jg) { session.recordLogin(jg.getEntityId()); }
        else if (packet instanceof JoinGamePacket jg) { session.recordLogin(jg.getEntityId()); }

        // === PLAY state — PlayerPosition variants (newest first) ===
        else if (packet instanceof NettyPlayerPositionS2CPacketV768 pos) {
            session.recordPosition(pos.getX(), pos.getY(), pos.getZ(),
                    pos.getYaw(), pos.getPitch());
            ctx.writeAndFlush(new TeleportConfirmPacketV109(pos.getTeleportId()));
            if (!session.isLoginComplete()) session.markLoginComplete();
        }
        else if (packet instanceof NettyPlayerPositionS2CPacketV762 pos) {
            session.recordPosition(pos.getX(), pos.getY(), pos.getZ(),
                    pos.getYaw(), pos.getPitch());
            ctx.writeAndFlush(new TeleportConfirmPacketV109(pos.getTeleportId()));
            if (!session.isLoginComplete()) session.markLoginComplete();
        }
        else if (packet instanceof NettyPlayerPositionS2CPacketV755 pos) {
            session.recordPosition(pos.getX(), pos.getY(), pos.getZ(),
                    pos.getYaw(), pos.getPitch());
            ctx.writeAndFlush(new TeleportConfirmPacketV109(pos.getTeleportId()));
            if (!session.isLoginComplete()) session.markLoginComplete();
        }
        else if (packet instanceof NettyPlayerPositionS2CPacketV109 pos) {
            session.recordPosition(pos.getX(), pos.getY(), pos.getZ(),
                    pos.getYaw(), pos.getPitch());
            ctx.writeAndFlush(new TeleportConfirmPacketV109(pos.getTeleportId()));
            if (!session.isLoginComplete()) session.markLoginComplete();
        }
        else if (packet instanceof NettyPlayerPositionS2CPacketV47 pos) {
            session.recordPosition(pos.getX(), pos.getY(), pos.getZ(),
                    pos.getYaw(), pos.getPitch());
            if (!session.isLoginComplete()) session.markLoginComplete();
        }
        else if (packet instanceof NettyPlayerPositionS2CPacket pos) {
            session.recordPosition(pos.getX(), pos.getY(), pos.getZ(),
                    pos.getYaw(), pos.getPitch());
            if (!session.isLoginComplete()) session.markLoginComplete();
        }

        // === PLAY state — SpawnEntity (v764+ replaces SpawnPlayer) ===
        else if (packet instanceof NettySpawnEntityPacketV774 sp) { session.recordSpawnPlayer(sp.getEntityId(), "v764+_player"); }
        else if (packet instanceof NettySpawnEntityPacketV773 sp) { session.recordSpawnPlayer(sp.getEntityId(), "v764+_player"); }
        else if (packet instanceof NettySpawnEntityPacketV771 sp) { session.recordSpawnPlayer(sp.getEntityId(), "v764+_player"); }
        else if (packet instanceof NettySpawnEntityPacketV770 sp) { session.recordSpawnPlayer(sp.getEntityId(), "v764+_player"); }
        else if (packet instanceof NettySpawnEntityPacketV769 sp) { session.recordSpawnPlayer(sp.getEntityId(), "v764+_player"); }
        else if (packet instanceof NettySpawnEntityPacketV768 sp) { session.recordSpawnPlayer(sp.getEntityId(), "v764+_player"); }
        else if (packet instanceof NettySpawnEntityPacketV766 sp) { session.recordSpawnPlayer(sp.getEntityId(), "v764+_player"); }
        else if (packet instanceof NettySpawnEntityPacketV764 sp) { session.recordSpawnPlayer(sp.getEntityId(), "v764+_player"); }

        // === PLAY state — SpawnPlayer (pre-v764) ===
        else if (packet instanceof NettySpawnPlayerPacketV573 sp) { session.recordSpawnPlayer(sp.getEntityId(), "v573+_player"); }
        else if (packet instanceof NettySpawnPlayerPacketV109 sp) { session.recordSpawnPlayer(sp.getEntityId(), "v109_player"); }
        else if (packet instanceof NettySpawnPlayerPacketV47 sp) { session.recordSpawnPlayer(sp.getEntityId(), "v47_player"); }
        else if (packet instanceof NettySpawnPlayerPacketV5 sp) { session.recordSpawnPlayer(sp.getEntityId(), sp.getPlayerName()); }
        else if (packet instanceof NettySpawnPlayerPacket sp) { session.recordSpawnPlayer(sp.getEntityId(), sp.getPlayerName()); }

        // === PLAY state — Chat / SystemChat ===
        else if (packet instanceof SystemChatPacketV765 sc) { session.recordChat(sc.getPlainText()); }
        else if (packet instanceof SystemChatPacketV760 sc) { recordChatFromJson(sc.getJsonMessage()); }
        else if (packet instanceof SystemChatPacketV759 sc) { recordChatFromJson(sc.getJsonMessage()); }
        else if (packet instanceof NettyChatS2CPacketV735 chat) { recordChatFromJson(chat.getJsonMessage()); }
        else if (packet instanceof NettyChatS2CPacketV47 chat) { recordChatFromJson(chat.getJsonMessage()); }
        else if (packet instanceof NettyChatS2CPacket chat) { recordChatFromJson(chat.getJsonMessage()); }

        // === PLAY state — KeepAlive ===
        else if (packet instanceof KeepAlivePacketV340 ka) { ctx.writeAndFlush(new KeepAlivePacketV340(ka.getKeepAliveId())); }
        else if (packet instanceof KeepAlivePacketV47 ka) { ctx.writeAndFlush(new KeepAlivePacketV47(ka.getKeepAliveId())); }
        else if (packet instanceof KeepAlivePacketV17 ka) { ctx.writeAndFlush(new KeepAlivePacketV17(ka.getKeepAliveId())); }

        // === PLAY state — Chunks ===
        else if (packet instanceof MapChunkPacketV109 mc) { processV109Chunk(mc); }
        else if (packet instanceof MapChunkPacketV47 mc) { processV47Chunk(mc); }
        else if (packet instanceof MapChunkPacketV39 mc) {
            processSectionedChunk(mc.getChunkX(), mc.getChunkZ(),
                    mc.getPrimaryBitMask(), mc.getCompressedData());
        }

        // === PLAY state — Block changes ===
        else if (packet instanceof NettyBlockChangePacketV477 bc) {
            session.recordBlockChange(bc.getX(), bc.getY(), bc.getZ(), bc.getBlockStateId());
        }
        else if (packet instanceof NettyBlockChangePacketV393 bc) {
            session.recordBlockChange(bc.getX(), bc.getY(), bc.getZ(), bc.getBlockStateId());
        }
        else if (packet instanceof NettyBlockChangePacketV47 bc) {
            session.recordBlockChange(bc.getX(), bc.getY(), bc.getZ(), bc.getBlockId());
        }
        else if (packet instanceof NettyBlockChangePacket bc) {
            session.recordBlockChange(bc.getX(), bc.getY(), bc.getZ(), bc.getBlockId());
        }

        // === PLAY state — Entities ===
        else if (packet instanceof NettyDestroyEntitiesPacketV47 de) {
            for (int id : de.getEntityIds()) session.recordDespawn(id);
        }
        else if (packet instanceof NettyDestroyEntitiesPacket de) {
            for (int id : de.getEntityIds()) session.recordDespawn(id);
        }
        else if (packet instanceof NettySetSlotPacketV766 ss) {
            session.recordSetSlot(ss.getSlotIndex(), ss.getItemId(), ss.getCount());
        }
        else if (packet instanceof NettySetSlotPacketV756 ss) {
            session.recordSetSlot(ss.getSlotIndex(), ss.getItemId(), ss.getCount());
        }
        else if (packet instanceof NettySetSlotPacketV404 ss) {
            session.recordSetSlot(ss.getSlotIndex(), ss.getItemId(), ss.getCount());
        }
        else if (packet instanceof NettySetSlotPacketV393 ss) {
            session.recordSetSlot(ss.getSlotIndex(), ss.getItemId(), ss.getCount());
        }
        else if (packet instanceof NettySetSlotPacketV47 ss) {
            session.recordSetSlot(ss.getSlotIndex(), ss.getItemId(), ss.getCount());
        }

        // === PLAY state — World ===
        else if (packet instanceof NettyTimeUpdatePacket tu) { session.recordTimeUpdate(tu.getTimeOfDay()); }
        else if (packet instanceof NettyChangeGameStatePacket gs) { session.recordWeatherChange(gs.getReason()); }

        // === PLAY state — ChunkBatch (v764+) ===
        else if (packet instanceof ChunkBatchFinishedPacket) {
            ctx.writeAndFlush(new ChunkBatchReceivedPacket());
        }

        // === PLAY state — Disconnect ===
        else if (packet instanceof NettyDisconnectPacketV765 disc) {
            System.err.println("BotNetty disconnected: " + disc.getPlainText());
            ctx.close();
        }
        else if (packet instanceof NettyDisconnectPacket disc) {
            System.err.println("BotNetty disconnected: " + disc.getJsonReason());
            ctx.close();
        }
    }

    private void handleLoginSuccess(ChannelHandlerContext ctx) {
        if (version.isAtLeast(ProtocolVersion.RELEASE_1_20_2)) {
            // v764+: Send LoginAcknowledged (still in LOGIN state), then transition to CONFIGURATION
            ctx.writeAndFlush(new LoginAcknowledgedPacket());
            setCodecState(ctx, ConnectionState.CONFIGURATION);
        } else {
            // Pre-v764: transition directly to PLAY
            setCodecState(ctx, ConnectionState.PLAY);
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
     * Parse V109 (1.9-1.12) paletted chunk data.
     * Unpacks section palettes and data arrays into AlphaChunk YZX layout.
     */
    private void processV109Chunk(MapChunkPacketV109 packet) {
        byte[] data = packet.getData();
        int primaryBitMask = packet.getPrimaryBitMask();
        byte[] blockIds = new byte[32768];
        ByteBuf buf = Unpooled.wrappedBuffer(data);

        try {
            for (int section = 0; section < 16; section++) {
                if ((primaryBitMask & (1 << section)) == 0) continue;
                int baseY = section * 16;

                int bitsPerBlock = buf.readUnsignedByte();
                int paletteLength = McDataTypes.readVarInt(buf);

                int[] palette = null;
                if (paletteLength > 0) {
                    palette = new int[paletteLength];
                    for (int i = 0; i < paletteLength; i++) {
                        palette[i] = McDataTypes.readVarInt(buf);
                    }
                }

                int dataArrayLength = McDataTypes.readVarInt(buf);
                long[] dataArray = new long[dataArrayLength];
                for (int i = 0; i < dataArrayLength; i++) {
                    dataArray[i] = buf.readLong();
                }

                // Unpack block states (entries span long boundaries in 1.9-1.12)
                long mask = (1L << bitsPerBlock) - 1;
                for (int i = 0; i < 4096; i++) {
                    int bitIndex = i * bitsPerBlock;
                    int longIndex = bitIndex / 64;
                    int bitOffset = bitIndex % 64;

                    int stateId;
                    if (longIndex < dataArray.length) {
                        stateId = (int) ((dataArray[longIndex] >>> bitOffset) & mask);
                        if (bitOffset + bitsPerBlock > 64 && longIndex + 1 < dataArray.length) {
                            int bitsInFirst = 64 - bitOffset;
                            stateId |= (int) ((dataArray[longIndex + 1] << bitsInFirst) & mask);
                        }
                    } else {
                        stateId = 0;
                    }

                    int blockId;
                    if (palette != null && stateId < palette.length) {
                        blockId = palette[stateId] >> 4;
                    } else {
                        blockId = stateId >> 4;
                    }

                    // Section YZX → AlphaChunk YZX
                    int sy = (i >> 8) & 15;
                    int sz = (i >> 4) & 15;
                    int sx = i & 15;
                    int alphaIndex = (baseY + sy) + sz * 128 + sx * 2048;
                    blockIds[alphaIndex] = (byte) blockId;
                }

                // Skip blockLight (2048 bytes) + skyLight (2048 bytes)
                buf.skipBytes(4096);
            }
        } finally {
            buf.release();
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
