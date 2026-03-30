package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.codec.NettyPacketDecoder;
import com.github.martinambrus.rdforward.protocol.codec.NettyPacketEncoder;
import com.github.martinambrus.rdforward.protocol.codec.PacketCompressEncoder;
import com.github.martinambrus.rdforward.protocol.codec.PacketDecompressDecoder;
import com.github.martinambrus.rdforward.protocol.crypto.CipherDecoder;
import com.github.martinambrus.rdforward.protocol.crypto.CipherEncoder;
import com.github.martinambrus.rdforward.protocol.crypto.MinecraftCipher;
import com.github.martinambrus.rdforward.protocol.event.EventResult;
import com.github.martinambrus.rdforward.protocol.packet.ConnectionState;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.alpha.*;
import com.github.martinambrus.rdforward.protocol.packet.classic.PlayerTeleportPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockServerPacket;
import com.github.martinambrus.rdforward.protocol.packet.netty.*;
import com.github.martinambrus.rdforward.server.api.CommandRegistry;
import com.github.martinambrus.rdforward.server.api.ServerProperties;
import com.github.martinambrus.rdforward.server.auth.MojangSessionVerifier;
import com.github.martinambrus.rdforward.server.event.ServerEvents;
import com.github.martinambrus.rdforward.protocol.BlockStateMapper;
import com.github.martinambrus.rdforward.world.BlockRegistry;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Handles MC 1.7.2+ Netty protocol connections.
 *
 * Manages the connection lifecycle through states:
 * 1. HANDSHAKING: Receive NettyHandshakePacket, transition to STATUS or LOGIN
 * 2. STATUS: Handle server list ping (StatusRequest/StatusPing)
 * 3. LOGIN: Encryption handshake, then send LoginSuccess and transition to PLAY
 * 4. PLAY: Gameplay packets (position, placement, digging, chat)
 */
public class NettyConnectionHandler extends SimpleChannelInboundHandler<Packet> {

    private static final double PLAYER_EYE_HEIGHT = (double) 1.62f;
    private static final int PLAYER_EYE_HEIGHT_FIXED = (int) Math.ceil(PLAYER_EYE_HEIGHT * 32);

    private final ProtocolVersion serverVersion;
    private final ServerWorld world;
    private final PlayerManager playerManager;
    private final ChunkManager chunkManager;

    private ConnectionState state = ConnectionState.HANDSHAKING;
    private ProtocolVersion clientVersion = ProtocolVersion.RELEASE_1_7_2;
    private String pendingUsername;
    private ConnectedPlayer player;
    private boolean loginComplete = false;

    // Encryption state — RSA keypair shared across all connections (thread-safe: generated once)
    private static volatile KeyPair sharedRsaKeyPair;
    private static final Object RSA_LOCK = new Object();
    private static final java.security.SecureRandom SECURE_RANDOM = new java.security.SecureRandom();
    private byte[] verifyToken;
    private boolean awaitingEncryptionResponse = false;

    // Online-mode authentication result (null in offline mode)
    private String authenticatedUuid;

    /** Lazily generate a single RSA keypair shared by all connections. */
    private static KeyPair getOrCreateRsaKeyPair() throws java.security.NoSuchAlgorithmException {
        KeyPair kp = sharedRsaKeyPair;
        if (kp != null) return kp;
        synchronized (RSA_LOCK) {
            kp = sharedRsaKeyPair;
            if (kp != null) return kp;
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            kp = keyGen.generateKeyPair();
            sharedRsaKeyPair = kp;
            return kp;
        }
    }

    // KeepAlive
    private java.util.concurrent.ScheduledFuture<?> keepAliveTask;
    private int keepAliveCounter = 0;

    // Teleport ID counter for 1.9+ clients
    private int nextTeleportId = 0;

    // When true, C2S position updates are ignored until TeleportConfirm is received.
    // Prevents the client's pre-teleport position (at world spawn) from being applied
    // to the server-side player, which would cause updatePlayerChunks to unload the
    // correct chunks and reload wrong ones (race between PlayerPosition delivery and
    // the tick loop reading player.getX()/getZ()).
    private boolean awaitingTeleportConfirm = false;

    /** True while the player is stuck at an unloaded chunk boundary. */
    private boolean stuckAtUnloadedChunk = false;


    public NettyConnectionHandler(ProtocolVersion serverVersion, ServerWorld world,
                                   PlayerManager playerManager, ChunkManager chunkManager) {
        this.serverVersion = serverVersion;
        this.world = world;
        this.playerManager = playerManager;
        this.chunkManager = chunkManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        switch (state) {
            case HANDSHAKING:
                if (packet instanceof NettyHandshakePacket) {
                    handleHandshake(ctx, (NettyHandshakePacket) packet);
                }
                break;

            case STATUS:
                if (packet instanceof StatusRequestPacket) {
                    handleStatusRequest(ctx);
                } else if (packet instanceof StatusPingPacket) {
                    handleStatusPing(ctx, (StatusPingPacket) packet);
                }
                break;

            case LOGIN:
                if (packet instanceof LoginStartPacketV761) {
                    handleLoginStart(ctx, ((LoginStartPacketV761) packet).getUsername());
                } else if (packet instanceof LoginStartPacket) {
                    handleLoginStart(ctx, ((LoginStartPacket) packet).getUsername());
                } else if (packet instanceof LoginAcknowledgedPacket) {
                    handleLoginAcknowledged(ctx);
                } else if (packet instanceof NettyEncryptionResponsePacketV759) {
                    handleEncryptionResponseV759(ctx, (NettyEncryptionResponsePacketV759) packet);
                } else if (packet instanceof NettyEncryptionResponsePacketV47) {
                    handleEncryptionResponseV47(ctx, (NettyEncryptionResponsePacketV47) packet);
                } else if (packet instanceof NettyEncryptionResponsePacket) {
                    handleEncryptionResponse(ctx, (NettyEncryptionResponsePacket) packet);
                }
                break;

            case CONFIGURATION:
                if (packet instanceof ConfigFinishC2SPacket) {
                    handleConfigFinish(ctx);
                } else if (packet instanceof SelectKnownPacksC2SPacket) {
                    handleSelectKnownPacks(ctx);
                }
                break;

            case PLAY:
                handlePlayPacket(ctx, packet);
                break;
        }
    }

    // ========================================================================
    // Handshaking state
    // ========================================================================

    private void handleHandshake(ChannelHandlerContext ctx, NettyHandshakePacket packet) {
        int pv = packet.getProtocolVersion();
        // Protocol version 47 clashes: pre-Netty Release 1.4.2 and Netty 1.8 both use 47.
        // In the Netty handler, pv 47 always means 1.8 (pre-Netty clients never reach here).
        if (pv == 47) {
            clientVersion = ProtocolVersion.RELEASE_1_8;
        } else {
            ProtocolVersion resolved = ProtocolVersion.fromNumber(pv, ProtocolVersion.Family.RELEASE);
            if (resolved != null) {
                clientVersion = resolved;
            }
        }

        // Set codec protocol versions for version-aware packet encoding/decoding
        NettyPacketDecoder decoder = ctx.pipeline().get(NettyPacketDecoder.class);
        if (decoder != null) {
            decoder.setProtocolVersion(pv);
        }
        NettyPacketEncoder encoder = ctx.pipeline().get(NettyPacketEncoder.class);
        if (encoder != null) {
            encoder.setProtocolVersion(pv);
        }

        if (packet.getNextState() == 1) {
            // Status
            String pingVersion = ProtocolVersion.describeNettyProtocol(pv);
            System.out.println("Netty server list ping ("
                    + (pingVersion != null ? pingVersion : "v" + pv) + ")");
            state = ConnectionState.STATUS;
            setCodecState(ctx, ConnectionState.STATUS);
        } else if (packet.getNextState() == 2) {
            // Login
            state = ConnectionState.LOGIN;
            setCodecState(ctx, ConnectionState.LOGIN);
        } else {
            ctx.close();
        }
    }

    // ========================================================================
    // Status state (server list ping)
    // ========================================================================

    private void handleStatusRequest(ChannelHandlerContext ctx) {
        String versionName;
        if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_26_1)) {
            versionName = "26.1";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_21_11)) {
            versionName = "1.21.11";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_21_9)) {
            versionName = "1.21.9";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_21_7)) {
            versionName = "1.21.7";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_21_6)) {
            versionName = "1.21.6";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_21_5)) {
            versionName = "1.21.5";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_21_4)) {
            versionName = "1.21.4";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_21_2)) {
            versionName = "1.21.2";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_21)) {
            versionName = "1.21";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_20_5)) {
            versionName = "1.20.5";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_20_3)) {
            versionName = "1.20.3";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_20_2)) {
            versionName = "1.20.2";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_20)) {
            versionName = "1.20";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_19_4)) {
            versionName = "1.19.4";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_19_3)) {
            versionName = "1.19.3";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_19_1)) {
            versionName = "1.19.1";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_19)) {
            versionName = "1.19";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_18_2)) {
            versionName = "1.18.2";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_18)) {
            versionName = "1.18";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_17_1)) {
            versionName = "1.17.1";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_17)) {
            versionName = "1.17";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_16_4)) {
            versionName = "1.16.4";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_16_3)) {
            versionName = "1.16.3";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_16_2)) {
            versionName = "1.16.2";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_16)) {
            versionName = "1.16";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_15_2)) {
            versionName = "1.15.2";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_15_1)) {
            versionName = "1.15.1";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_15)) {
            versionName = "1.15";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_14_4)) {
            versionName = "1.14.4";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_14_3)) {
            versionName = "1.14.3";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_14_2)) {
            versionName = "1.14.2";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_14_1)) {
            versionName = "1.14.1";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_14)) {
            versionName = "1.14";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_13)) {
            versionName = "1.13.1";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_12)) {
            versionName = "1.12.2";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_11)) {
            versionName = "1.11.2";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_10)) {
            versionName = "1.10.2";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_9)) {
            versionName = "1.9.4";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_8)) {
            versionName = "1.8";
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_7_6)) {
            versionName = "1.7.10";
        } else {
            versionName = "1.7.5";
        }
        int protocol = clientVersion.getVersionNumber();
        String json = "{"
                + "\"version\":{\"name\":\"" + versionName + "\",\"protocol\":" + protocol + "},"
                + "\"players\":{\"max\":" + PlayerManager.getMaxPlayers()
                + ",\"online\":" + playerManager.getPlayerCount() + "},"
                + "\"description\":{\"text\":\"" + escapeJsonString(ServerProperties.getMotd()) + "\"}"
                + "}";
        ctx.writeAndFlush(new StatusResponsePacket(json));
    }

    private void handleStatusPing(ChannelHandlerContext ctx, StatusPingPacket packet) {
        ctx.writeAndFlush(new StatusPingPacket(packet.getTime()))
                .addListener(io.netty.channel.ChannelFutureListener.CLOSE);
    }

    // ========================================================================
    // Login state
    // ========================================================================

    private void handleLoginStart(ChannelHandlerContext ctx, String username) {
        pendingUsername = username;

        if (ServerProperties.isOnlineMode()) {
            // Online mode: send EncryptionRequest to initiate Mojang session authentication
            try {
                KeyPair rsaKeyPair = getOrCreateRsaKeyPair();
                verifyToken = new byte[4];
                SECURE_RANDOM.nextBytes(verifyToken);

                byte[] pubKey = rsaKeyPair.getPublic().getEncoded();
                if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_20_5)) {
                    ctx.writeAndFlush(new NettyEncryptionRequestPacketV766("", pubKey, verifyToken, true));
                } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_8)) {
                    ctx.writeAndFlush(new NettyEncryptionRequestPacketV47("", pubKey, verifyToken));
                } else {
                    ctx.writeAndFlush(new NettyEncryptionRequestPacket("", pubKey, verifyToken));
                }
                awaitingEncryptionResponse = true;
            } catch (Exception e) {
                System.err.println("Failed to generate RSA keypair: " + e.getMessage());
                sendLoginDisconnect(ctx, "Encryption error");
            }
        } else {
            // Offline mode: skip encryption, go directly to LoginSuccess
            completeLogin(ctx);
        }
    }

    /** Compression threshold in bytes. Packets smaller than this are sent uncompressed. */
    private static final int COMPRESSION_THRESHOLD = 256;

    /**
     * Send LoginSuccess and transition to PLAY (or CONFIGURATION for v764+).
     * Called after encryption completes or directly for offline-mode logins.
     *
     * For 1.8+ clients, sends Set Compression before LoginSuccess to enable
     * zlib packet compression. 1.7.x clients do not support compression.
     */
    private void completeLogin(ChannelHandlerContext ctx) {
        // Remove login timeout
        if (ctx.pipeline().get("loginTimeout") != null) {
            ctx.pipeline().remove("loginTimeout");
        }

        // Enable compression for 1.8+ clients (protocol 47+).
        // SetCompression MUST be sent before LoginSuccess.
        if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_8)) {
            ctx.writeAndFlush(new SetCompressionPacket(COMPRESSION_THRESHOLD));

            // Insert compress/decompress handlers into the pipeline.
            // Inbound (head-to-tail): decoder -> decompress -> packetDecoder
            // Outbound (tail-to-head): packetEncoder -> compress -> encoder
            // addBefore in head-to-tail means AFTER in outbound direction.
            ctx.pipeline().addAfter("decoder", "decompress",
                    new PacketDecompressDecoder(COMPRESSION_THRESHOLD));
            ctx.pipeline().addBefore("packetEncoder", "compress",
                    new PacketCompressEncoder(COMPRESSION_THRESHOLD));
            System.out.println("[Netty] Compression enabled (threshold=" + COMPRESSION_THRESHOLD
                    + ") for " + pendingUsername);
        }

        // Send LoginSuccess — use Mojang UUID in online mode, offline UUID otherwise
        String uuid = (authenticatedUuid != null)
                ? authenticatedUuid
                : ClassicToNettyTranslator.generateOfflineUuid(pendingUsername);
        if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_21_2)) {
            ctx.writeAndFlush(new LoginSuccessPacketV768(uuid, pendingUsername));
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_20_5)) {
            ctx.writeAndFlush(new LoginSuccessPacketV766(uuid, pendingUsername));
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_19)) {
            ctx.writeAndFlush(new LoginSuccessPacketV759(uuid, pendingUsername));
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_16)) {
            ctx.writeAndFlush(new LoginSuccessPacketV735(uuid, pendingUsername));
        } else {
            ctx.writeAndFlush(new LoginSuccessPacket(uuid, pendingUsername));
        }

        if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_20_2)) {
            // V764+: stay in LOGIN state, wait for LoginAcknowledged
            // (which will trigger CONFIGURATION -> PLAY transition)
        } else {
            // Pre-V764: transition directly to PLAY state
            state = ConnectionState.PLAY;
            setCodecState(ctx, ConnectionState.PLAY);
            handleJoinGame(ctx);
        }
    }

    private void handleEncryptionResponse(ChannelHandlerContext ctx,
                                           NettyEncryptionResponsePacket packet) {
        processEncryptionResponse(ctx, packet.getSharedSecret(), packet.getVerifyToken());
    }

    private void handleEncryptionResponseV47(ChannelHandlerContext ctx,
                                              NettyEncryptionResponsePacketV47 packet) {
        processEncryptionResponse(ctx, packet.getSharedSecret(), packet.getVerifyToken());
    }

    private void handleEncryptionResponseV759(ChannelHandlerContext ctx,
                                               NettyEncryptionResponsePacketV759 packet) {
        // V759: verify token may be null in chat signing mode
        processEncryptionResponse(ctx, packet.getSharedSecret(), packet.getVerifyToken());
    }

    /**
     * Common encryption response processing. Decrypts shared secret, verifies token
     * (unless null for V759 chat signing mode), installs ciphers, then proceeds to auth.
     */
    private void processEncryptionResponse(ChannelHandlerContext ctx,
                                            byte[] encryptedSharedSecret,
                                            byte[] encryptedVerifyToken) {
        KeyPair rsaKeyPair = sharedRsaKeyPair;
        if (!awaitingEncryptionResponse || rsaKeyPair == null) {
            sendLoginDisconnect(ctx, "Unexpected encryption response");
            return;
        }
        awaitingEncryptionResponse = false;

        try {
            Cipher rsaCipher = Cipher.getInstance("RSA");
            rsaCipher.init(Cipher.DECRYPT_MODE, rsaKeyPair.getPrivate());
            byte[] sharedSecret = rsaCipher.doFinal(encryptedSharedSecret);

            if (sharedSecret.length != 16) {
                System.err.println("Invalid shared secret length (" + sharedSecret.length
                        + ") from " + pendingUsername);
                sendLoginDisconnect(ctx, "Encryption verification failed");
                return;
            }

            if (encryptedVerifyToken != null) {
                rsaCipher.init(Cipher.DECRYPT_MODE, rsaKeyPair.getPrivate());
                byte[] decryptedToken = rsaCipher.doFinal(encryptedVerifyToken);

                if (!Arrays.equals(decryptedToken, verifyToken)) {
                    System.err.println("Encryption verify token mismatch for " + pendingUsername);
                    sendLoginDisconnect(ctx, "Encryption verification failed");
                    return;
                }
            }
            // null verify token: V759 chat signing mode — the RSA-encrypted shared
            // secret already proves the client received our encryption request.

            MinecraftCipher decryptCipher = new MinecraftCipher(Cipher.DECRYPT_MODE, sharedSecret);
            MinecraftCipher encryptCipher = new MinecraftCipher(Cipher.ENCRYPT_MODE, sharedSecret);
            ctx.pipeline().addBefore("decoder", "decrypt", new CipherDecoder(decryptCipher));
            ctx.pipeline().addBefore("encoder", "encrypt", new CipherEncoder(encryptCipher));

            verifyAndCompleteLogin(ctx, sharedSecret);

        } catch (Exception e) {
            System.err.println("Encryption handshake failed for " + pendingUsername
                    + ": " + e.getMessage());
            sendLoginDisconnect(ctx, "Encryption error");
        }
    }

    private void sendLoginDisconnect(ChannelHandlerContext ctx, String reason) {
        String json = "{\"text\":\"" + reason.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}";
        ctx.writeAndFlush(new LoginDisconnectPacket(json))
                .addListener(io.netty.channel.ChannelFutureListener.CLOSE);
    }

    /**
     * Verify player authentication with Mojang session server, then complete login.
     * In offline mode, completes login immediately.
     */
    private void verifyAndCompleteLogin(ChannelHandlerContext ctx, byte[] sharedSecret) {
        if (ServerProperties.isOnlineMode()) {
            MojangSessionVerifier.verifySession(pendingUsername, sharedSecret,
                            sharedRsaKeyPair.getPublic().getEncoded())
                    .thenAcceptAsync(result -> {
                        if (!ctx.channel().isActive()) return;
                        if (result.isSuccess()) {
                            authenticatedUuid = result.uuid;
                            pendingUsername = result.name; // use Mojang's canonical casing
                            System.out.println("[AUTH] Verified " + pendingUsername
                                    + " (UUID: " + authenticatedUuid + ")");
                            completeLogin(ctx);
                        } else {
                            System.err.println("[AUTH] Failed to verify " + pendingUsername
                                    + " (" + ctx.channel().remoteAddress() + ", "
                                    + clientVersion.getDisplayName() + "): " + result.failureMessage);
                            sendLoginDisconnect(ctx, result.failureMessage);
                        }
                    }, ctx.executor()); // run callback on Netty event loop
        } else {
            System.out.println("[AUTH] Offline mode, skipping verification for " + pendingUsername);
            completeLogin(ctx);
        }
    }

    // ========================================================================
    // Configuration state (v764+)
    // ========================================================================

    private void handleLoginAcknowledged(ChannelHandlerContext ctx) {
        // Transition from LOGIN to CONFIGURATION
        state = ConnectionState.CONFIGURATION;
        setCodecState(ctx, ConnectionState.CONFIGURATION);

        if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_20_5)) {
            // V766+: SelectKnownPacks round-trip before sending registry data.
            // Send SelectKnownPacks S2C, then wait for client response.
            // 26.1 clients only confirm "26.1" pack (not older ones), so send
            // only that pack to avoid unconfirmed-pack entry resolution failures.
            ctx.writeAndFlush(clientVersion.isAtLeast(ProtocolVersion.RELEASE_26_1)
                    ? new SelectKnownPacksS2CPacketV775()
                    : new SelectKnownPacksS2CPacket());
        } else {
            // V764/V765: Send registry data directly — single CompoundTag in network NBT.
            ctx.writeAndFlush(RegistryDataPacketV764.create(ctx.alloc()));

            // Send feature flags
            ctx.writeAndFlush(new UpdateEnabledFeaturesPacketV761());

            // Send UpdateTags
            if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_20_3)) {
                ctx.writeAndFlush(UpdateTagsPacketV765.INSTANCE);
            } else {
                ctx.writeAndFlush(UpdateTagsPacketV764.INSTANCE);
            }

            // Signal end of Configuration phase
            ctx.writeAndFlush(new ConfigFinishS2CPacket());
        }

        // Wait for ConfigFinishC2SPacket (v764/v765) or SelectKnownPacksC2SPacket (v766+)
    }

    private void handleConfigFinish(ChannelHandlerContext ctx) {
        // Transition from CONFIGURATION to PLAY
        state = ConnectionState.PLAY;
        setCodecState(ctx, ConnectionState.PLAY);

        // Proceed with join game
        handleJoinGame(ctx);
    }

    private void handleSelectKnownPacks(ChannelHandlerContext ctx) {
        boolean isV775 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_26_1);
        boolean isV774 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_21_11);
        boolean isV773 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_21_9);
        boolean isV772 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_21_7);
        boolean isV771 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_21_6);
        boolean isV770 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_21_5);
        boolean isV769 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_21_4);
        boolean isV768 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_21_2);
        boolean isV767 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_21);

        // 26.1 added world_clock registry; dimension_type built-in data references it,
        // so it must be sent BEFORE dimension_type to satisfy cross-references.
        if (isV775) {
            ctx.writeAndFlush(RegistryDataPacketV766.createBuiltIn(ctx.alloc(),
                    "minecraft:world_clock",
                    "minecraft:overworld", "minecraft:the_end"));
        }
        // Use createBuiltIn() for dimension_type — client uses its built-in overworld
        // (minY=-64, height=384 = 24 sections). Chunk serialization is adjusted to match.
        ctx.writeAndFlush(RegistryDataPacketV766.createBuiltIn(ctx.alloc(),
                "minecraft:dimension_type",
                "minecraft:overworld", "minecraft:overworld_caves",
                "minecraft:the_nether", "minecraft:the_end"));
        // Biome: need the_void at index 0, plains at index 1 to match chunk biome palette value 1.
        // 1.21 wolf variants (built-in) reference biomes by registry ID. Without them,
        // the client errors: "Unbound values in registry minecraft:worldgen/biome".
        // 26.1 clients only confirm "26.1" pack — must send full biome list (65 entries).
        if (isV775) {
            ctx.writeAndFlush(RegistryDataPacketV766.createBiomeV775(ctx.alloc()));
        } else if (isV767) {
            ctx.writeAndFlush(RegistryDataPacketV766.createBuiltIn(ctx.alloc(),
                    "minecraft:worldgen/biome",
                    "minecraft:the_void", "minecraft:plains",
                    "minecraft:forest", "minecraft:grove",
                    "minecraft:old_growth_pine_taiga", "minecraft:old_growth_spruce_taiga",
                    "minecraft:snowy_taiga", "minecraft:taiga"));
        } else {
            ctx.writeAndFlush(RegistryDataPacketV766.createBuiltIn(ctx.alloc(),
                    "minecraft:worldgen/biome",
                    "minecraft:the_void", "minecraft:plains"));
        }
        ctx.writeAndFlush(RegistryDataPacketV766.createChatType(ctx.alloc()));
        // Damage types: 26.1 added spear + wind_charge (50 entries)
        // 1.21.2 added ender_pearl + mace_smash damage types (48 entries)
        // 1.21 added minecraft:campfire damage type (45 entries vs 44)
        ctx.writeAndFlush(isV775
                ? RegistryDataPacketV766.createDamageTypeV775(ctx.alloc())
                : isV768
                ? RegistryDataPacketV766.createDamageTypeV768(ctx.alloc())
                : isV767
                ? RegistryDataPacketV766.createDamageTypeV767(ctx.alloc())
                : RegistryDataPacketV766.createDamageType(ctx.alloc()));
        // 1.21 added bolt + flow trim patterns (18 entries vs 16)
        ctx.writeAndFlush(isV767
                ? RegistryDataPacketV766.createTrimPatternV767(ctx.alloc())
                : RegistryDataPacketV766.createTrimPattern(ctx.alloc()));
        // 26.1 added resin trim material (11 entries)
        ctx.writeAndFlush(isV775
                ? RegistryDataPacketV766.createTrimMaterialV775(ctx.alloc())
                : RegistryDataPacketV766.createTrimMaterial(ctx.alloc()));
        // 26.1 added flow + guster banner patterns (43 entries)
        ctx.writeAndFlush(isV775
                ? RegistryDataPacketV766.createBannerPatternV775(ctx.alloc())
                : RegistryDataPacketV766.createBannerPattern(ctx.alloc()));
        // 1.21 has 9 wolf variants (all built-in); 1.20.5 had 1 (pale with data)
        ctx.writeAndFlush(isV767
                ? RegistryDataPacketV766.createWolfVariantV767(ctx.alloc())
                : RegistryDataPacketV766.createWolfVariant(ctx.alloc()));
        // 1.21 added painting_variant, enchantment, and jukebox_song as synchronized registries.
        // Without these RegistryData packets, 1.21 clients hang during CONFIG phase.
        if (isV767) {
            // 26.1 added 20 new paintings (51 total), new jukebox song (tears), lunge enchantment
            ctx.writeAndFlush(isV775
                    ? RegistryDataPacketV766.createPaintingVariantV775(ctx.alloc())
                    : isV772
                    ? RegistryDataPacketV766.createPaintingVariantV772(ctx.alloc())
                    : RegistryDataPacketV766.createPaintingVariant(ctx.alloc()));
            ctx.writeAndFlush(isV775
                    ? RegistryDataPacketV766.createEnchantmentV775(ctx.alloc())
                    : RegistryDataPacketV766.createEnchantment(ctx.alloc()));
            ctx.writeAndFlush(isV775
                    ? RegistryDataPacketV766.createJukeboxSongV775(ctx.alloc())
                    : isV772
                    ? RegistryDataPacketV766.createJukeboxSongV772(ctx.alloc())
                    : RegistryDataPacketV766.createJukeboxSong(ctx.alloc()));
        }
        // 1.21.2 added instrument registry (8 goat horns, all built-in)
        if (isV768) {
            ctx.writeAndFlush(RegistryDataPacketV766.createInstrument(ctx.alloc()));
        }
        // 1.21.6 added dialog registry (3 built-in entries)
        if (isV771) {
            ctx.writeAndFlush(RegistryDataPacketV766.createDialog(ctx.alloc()));
        }
        // 1.21.11 added zombie_nautilus_variant and timeline registries
        if (isV774) {
            ctx.writeAndFlush(RegistryDataPacketV766.createBuiltIn(ctx.alloc(),
                    "minecraft:zombie_nautilus_variant",
                    "minecraft:temperate", "minecraft:warm"));
            ctx.writeAndFlush(RegistryDataPacketV766.createBuiltIn(ctx.alloc(),
                    "minecraft:timeline",
                    "minecraft:day", "minecraft:early_game",
                    "minecraft:moon", "minecraft:villager_schedule"));
        }
        // 26.1 added 4 new sound variant registries
        if (isV775) {
            ctx.writeAndFlush(RegistryDataPacketV766.createBuiltIn(ctx.alloc(),
                    "minecraft:cat_sound_variant",
                    "minecraft:classic", "minecraft:royal"));
            ctx.writeAndFlush(RegistryDataPacketV766.createBuiltIn(ctx.alloc(),
                    "minecraft:chicken_sound_variant",
                    "minecraft:classic", "minecraft:picky"));
            ctx.writeAndFlush(RegistryDataPacketV766.createBuiltIn(ctx.alloc(),
                    "minecraft:cow_sound_variant",
                    "minecraft:classic", "minecraft:moody"));
            ctx.writeAndFlush(RegistryDataPacketV766.createBuiltIn(ctx.alloc(),
                    "minecraft:pig_sound_variant",
                    "minecraft:classic", "minecraft:mini", "minecraft:big"));
        }
        // 1.21.5 added 6 new variant registries (all built-in)
        if (isV770) {
            ctx.writeAndFlush(RegistryDataPacketV766.createPigVariant(ctx.alloc()));
            ctx.writeAndFlush(RegistryDataPacketV766.createCowVariant(ctx.alloc()));
            ctx.writeAndFlush(RegistryDataPacketV766.createChickenVariant(ctx.alloc()));
            ctx.writeAndFlush(RegistryDataPacketV766.createFrogVariant(ctx.alloc()));
            ctx.writeAndFlush(RegistryDataPacketV766.createCatVariant(ctx.alloc()));
            ctx.writeAndFlush(RegistryDataPacketV766.createWolfSoundVariant(ctx.alloc()));
        }

        // Send feature flags and tags (required for block rendering)
        ctx.writeAndFlush(new UpdateEnabledFeaturesPacketV761());
        // 1.21 added minecraft:enchantment tag registry (7 registries vs 6)
        // 1.21.2 added minecraft:worldgen/biome tag registry (8 registries vs 7) —
        // required for enchantment built-in data parsing (references biome tags)
        ctx.writeAndFlush(isV775 ? UpdateTagsPacketV775.INSTANCE
                        : isV774 ? UpdateTagsPacketV774.INSTANCE
                        : isV773 ? UpdateTagsPacketV773.INSTANCE
                        : isV771 ? UpdateTagsPacketV771.INSTANCE
                        : isV770 ? UpdateTagsPacketV770.INSTANCE
                        : isV769 ? UpdateTagsPacketV769.INSTANCE
                        : isV768 ? UpdateTagsPacketV768.INSTANCE
                        : isV767 ? UpdateTagsPacketV767.INSTANCE
                        : UpdateTagsPacketV766.INSTANCE);

        ctx.writeAndFlush(new ConfigFinishS2CPacket());
    }

    // ========================================================================
    // Play state: join game
    // ========================================================================

    private void handleJoinGame(ChannelHandlerContext ctx) {
        if (pendingUsername == null) {
            sendPlayDisconnect(ctx, "Login failed");
            return;
        }

        // Ban check — reject banned players/IPs before registration
        {
            String ip = PlayerManager.extractIp(ctx.channel().remoteAddress());
            if (com.github.martinambrus.rdforward.server.api.BanManager.isPlayerBanned(pendingUsername)
                    || (ip != null && com.github.martinambrus.rdforward.server.api.BanManager.isIpBanned(ip))) {
                sendPlayDisconnect(ctx, "You are banned from this server");
                return;
            }
            if (com.github.martinambrus.rdforward.server.api.BanManager.isTempBanned(pendingUsername)) {
                String remaining = com.github.martinambrus.rdforward.server.api.BanManager.formatDuration(
                        com.github.martinambrus.rdforward.server.api.BanManager.getTempBanRemaining(pendingUsername));
                sendPlayDisconnect(ctx, "You are temporarily banned (" + remaining + " remaining)");
                return;
            }
        }

        // Whitelist check — reject non-whitelisted players when whitelist is enabled
        if (!com.github.martinambrus.rdforward.server.api.WhitelistManager.isAllowed(pendingUsername)) {
            System.out.println("[INFO] " + pendingUsername + " was rejected (not white-listed)");
            sendPlayDisconnect(ctx, "You are not white-listed on this server!");
            return;
        }

        // Kick duplicate player
        if (!pendingUsername.trim().isEmpty()) {
            playerManager.kickDuplicatePlayer(pendingUsername.trim(), world);
        }

        // Register player
        player = playerManager.addPlayer(pendingUsername, authenticatedUuid, ctx.channel(), clientVersion);
        if (player == null) {
            sendPlayDisconnect(ctx, "Server is full!");
            return;
        }

        boolean isV775 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_26_1);
        boolean isV774 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_21_11);
        boolean isV773 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_21_9);
        boolean isV771 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_21_6);
        boolean isV770 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_21_5);
        boolean isV769 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_21_4);
        boolean isV768 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_21_2);
        boolean isV766 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_20_5);
        boolean isV765 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_20_3);
        boolean isV764 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_20_2);
        boolean isV763 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_20);
        boolean isV762 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_19_4);
        boolean isV761 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_19_3);
        boolean isV760 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_19_1);
        boolean isV759 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_19);
        boolean isV758 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_18_2);
        boolean isV757 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_18);
        boolean isV756 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_17_1);
        boolean isV755 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_17);
        boolean isV751 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_16_2);
        boolean isV735 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_16);
        boolean isV573 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_15);
        boolean isV477 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_14);
        boolean isV393 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_13);
        boolean isV109 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_9);
        boolean isV47 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_8);
        int entityId = player.getPlayerId() + 1;

        // Send JoinGame
        // maxPlayers=20 limits the tab list to a single compact column.
        // Using the actual MAX_PLAYERS (128) creates a huge multi-column grid.
        // v762 (1.19.4) added minecraft:damage_type registry and has_precipitation biome field.
        // v751 (1.16.2) rewrote JoinGame: isHardcore separated, NBT dimension type,
        //   registry-format codec with biome registry, VarInt maxPlayers.
        // v735 (1.16) rewrote JoinGame with NBT dimension codec.
        // v573 (1.15) added hashedSeed + enableRespawnScreen.
        // v477 (1.14) removed difficulty from JoinGame and added viewDistance.
        // v108 (1.9.1) changed dimension from byte to int.
        int gm = ServerProperties.getGameMode();
        int diff = ServerProperties.getDifficulty();
        if (isV768) {
            ctx.writeAndFlush(new JoinGamePacketV768(entityId, gm,
                    20, ChunkManager.CLIENT_VIEW_DISTANCE, ChunkManager.CLIENT_VIEW_DISTANCE));
        } else if (isV766) {
            ctx.writeAndFlush(new JoinGamePacketV766(entityId, gm,
                    20, ChunkManager.CLIENT_VIEW_DISTANCE, ChunkManager.CLIENT_VIEW_DISTANCE));
        } else if (isV764) {
            ctx.writeAndFlush(new JoinGamePacketV764(entityId, gm,
                    20, ChunkManager.CLIENT_VIEW_DISTANCE, ChunkManager.CLIENT_VIEW_DISTANCE));
        } else if (isV763) {
            ctx.writeAndFlush(new JoinGamePacketV763(entityId, gm,
                    20, ChunkManager.CLIENT_VIEW_DISTANCE, ChunkManager.CLIENT_VIEW_DISTANCE));
        } else if (isV762) {
            ctx.writeAndFlush(new JoinGamePacketV762(entityId, gm,
                    20, ChunkManager.CLIENT_VIEW_DISTANCE, ChunkManager.CLIENT_VIEW_DISTANCE));
        } else if (isV761) {
            // V761 reuses V760 JoinGame format; registry handles the ID shift (0x25 -> 0x24)
            ctx.writeAndFlush(new JoinGamePacketV760(entityId, gm,
                    20, ChunkManager.CLIENT_VIEW_DISTANCE, ChunkManager.CLIENT_VIEW_DISTANCE));
        } else if (isV760) {
            ctx.writeAndFlush(new JoinGamePacketV760(entityId, gm,
                    20, ChunkManager.CLIENT_VIEW_DISTANCE, ChunkManager.CLIENT_VIEW_DISTANCE));
        } else if (isV759) {
            ctx.writeAndFlush(new JoinGamePacketV759(entityId, gm,
                    20, ChunkManager.CLIENT_VIEW_DISTANCE, ChunkManager.CLIENT_VIEW_DISTANCE));
        } else if (isV758) {
            ctx.writeAndFlush(new JoinGamePacketV758(entityId, gm,
                    20, ChunkManager.CLIENT_VIEW_DISTANCE, ChunkManager.CLIENT_VIEW_DISTANCE));
        } else if (isV757) {
            ctx.writeAndFlush(new JoinGamePacketV757(entityId, gm,
                    20, ChunkManager.CLIENT_VIEW_DISTANCE, ChunkManager.CLIENT_VIEW_DISTANCE));
        } else if (isV755) {
            ctx.writeAndFlush(new JoinGamePacketV755(entityId, gm,
                    20, ChunkManager.CLIENT_VIEW_DISTANCE));
        } else if (isV751) {
            ctx.writeAndFlush(new JoinGamePacketV751(entityId, gm,
                    20, ChunkManager.CLIENT_VIEW_DISTANCE));
        } else if (isV735) {
            ctx.writeAndFlush(new JoinGamePacketV735(entityId, gm,
                    20, ChunkManager.CLIENT_VIEW_DISTANCE));
        } else if (isV573) {
            ctx.writeAndFlush(new JoinGamePacketV573(entityId, gm, 0,
                    20, "default", ChunkManager.CLIENT_VIEW_DISTANCE));
        } else if (isV477) {
            ctx.writeAndFlush(new JoinGamePacketV477(entityId, gm, 0,
                    20, "default", ChunkManager.CLIENT_VIEW_DISTANCE));
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_9_1)) {
            ctx.writeAndFlush(new JoinGamePacketV108(entityId, gm, 0, diff,
                    20, "default"));
        } else if (isV47) {
            ctx.writeAndFlush(new JoinGamePacketV47(entityId, gm, 0, diff,
                    20, "default"));
        } else {
            ctx.writeAndFlush(new JoinGamePacket(entityId, gm, 0, diff,
                    20, "default"));
        }

        // 1.19.3+: Send UpdateEnabledFeatures right after JoinGame
        // (v764+ sends this during Configuration phase instead)
        if (!isV764 && (isV763 || isV762 || isV761)) {
            ctx.writeAndFlush(new UpdateEnabledFeaturesPacketV761());
        }

        // 1.20.3+: Send GameEvent(13) "Start waiting for level chunks" so client
        // exits the loading screen promptly once chunks arrive.
        if (isV765) {
            ctx.writeAndFlush(new NettyChangeGameStatePacket(
                    NettyChangeGameStatePacket.START_WAITING_CHUNKS, 0.0f));
        }

        // 1.14+: Send chunk cache radius (view distance) right after JoinGame
        if (isV477) {
            ctx.writeAndFlush(new SetChunkCacheRadiusPacketV477(ChunkManager.CLIENT_VIEW_DISTANCE));
        }

        // 1.13+: Send mandatory DeclareCommands, UpdateRecipes, UpdateTags, Brand
        if (isV393) {
            int opLevel = com.github.martinambrus.rdforward.server.api.PermissionManager.getOpLevel(player.getUsername());
            java.util.List<String> cmds = com.github.martinambrus.rdforward.server.api.CommandRegistry.getCommandNamesForOpLevel(opLevel);
            ctx.writeAndFlush(DeclareCommandsPacketV393.withCommands(cmds));
            ctx.writeAndFlush(isV768 ? new UpdateRecipesPacketV768() : new UpdateRecipesPacketV393());
            // v764+ sends UpdateTags during Configuration phase
            if (!isV764) {
                // 1.14 added entity_types as a 4th tag category
                // 1.16 requires essential fluid tags (water/lava) or client crashes during rendering
                // 1.16.2 removed minecraft:furnace_materials from item tags
                ctx.writeAndFlush(isV763 ? UpdateTagsPacketV763.INSTANCE
                        : isV762 ? UpdateTagsPacketV762.INSTANCE
                        : isV761 ? UpdateTagsPacketV761.INSTANCE
                        : isV760 ? UpdateTagsPacketV759.INSTANCE
                        : isV759 ? UpdateTagsPacketV759.INSTANCE
                        : isV758 ? UpdateTagsPacketV758.INSTANCE
                        : isV757 ? UpdateTagsPacketV757.INSTANCE
                        : isV755 ? UpdateTagsPacketV755.INSTANCE
                        : isV751 ? UpdateTagsPacketV751.INSTANCE
                        : isV735 ? UpdateTagsPacketV735.INSTANCE
                        : isV477 ? UpdateTagsPacketV477.INSTANCE : UpdateTagsPacketV393.INSTANCE);
            }
            // Brand plugin message — 1.13 client NPEs without it
            byte[] brand = "RDForward".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] brandData = new byte[brand.length + 1];
            brandData[0] = (byte) brand.length;
            System.arraycopy(brand, 0, brandData, 1, brand.length);
            ctx.writeAndFlush(new NettyPluginMessageS2CPacketV393(
                    "minecraft:brand", brandData));
        }

        // Send OP permission level via EntityEvent so the client knows which commands to show.
        // Status 24 = non-op, 25 = level 1, ..., 28 = level 4.
        if (isV47) {
            int opLvl = com.github.martinambrus.rdforward.server.api.PermissionManager.getOpLevel(player.getUsername());
            ctx.writeAndFlush(new NettyEntityEventPacket(entityId, NettyEntityEventPacket.OP_PERMISSION_BASE + opLvl));
        }

        // Send PlayerAbilities with gamemode-aware flags
        ctx.writeAndFlush(new PlayerAbilitiesPacketV73(ServerProperties.getAbilitiesFlags(), 0.05f, 0.1f));

        // Send Entity Properties for movement speed
        // 1.20.5+ uses VarInt registry ID (17 = movement_speed)
        // 1.16+ uses namespaced snake_case attribute names
        if (isV766) {
            ctx.writeAndFlush(new NettyEntityPropertiesPacketV766(entityId,
                    NettyEntityPropertiesPacketV766.MOVEMENT_SPEED, 0.10000000149011612));
        } else {
            String movementSpeedKey = isV735
                    ? "minecraft:generic.movement_speed"
                    : "generic.movementSpeed";
            if (isV755) {
                ctx.writeAndFlush(new NettyEntityPropertiesPacketV755(entityId,
                        movementSpeedKey, 0.10000000149011612));
            } else if (isV47) {
                ctx.writeAndFlush(new NettyEntityPropertiesPacketV47(entityId,
                        movementSpeedKey, 0.10000000149011612));
            } else {
                ctx.writeAndFlush(new NettyEntityPropertiesPacket(entityId,
                        movementSpeedKey, 0.10000000149011612));
            }
        }

        // Determine spawn position (lookup by UUID in online mode, username in offline mode)
        SpawnPositionResolver.SpawnPosition spawn = SpawnPositionResolver.resolve(
                world, player.getUsername(), player.getUuid());
        player.updatePositionDouble(spawn.x, spawn.y, spawn.z, spawn.yaw, spawn.pitch);

        // Send spawn position
        int spawnBlockX = (int) Math.floor(spawn.x);
        int spawnBlockY = (int) Math.floor(spawn.y);
        int spawnBlockZ = (int) Math.floor(spawn.z);
        if (isV773) {
            ctx.writeAndFlush(new SpawnPositionPacketV773(spawnBlockX, spawnBlockY, spawnBlockZ));
        } else if (isV755) {
            ctx.writeAndFlush(new SpawnPositionPacketV755(spawnBlockX, spawnBlockY, spawnBlockZ));
        } else if (isV477) {
            ctx.writeAndFlush(new SpawnPositionPacketV477(spawnBlockX, spawnBlockY, spawnBlockZ));
        } else if (isV47) {
            ctx.writeAndFlush(new SpawnPositionPacketV47(spawnBlockX, spawnBlockY, spawnBlockZ));
        } else {
            ctx.writeAndFlush(new SpawnPositionPacket(spawnBlockX, spawnBlockY, spawnBlockZ));
        }

        // Send initial time update
        // v775: worldAge removed (handled by world_clock/timeline system)
        // v768: doDaylightCycle boolean replaces negative-timeOfDay trick
        long timeOfDay = world.isTimeFrozen() ? -world.getWorldTime() : world.getWorldTime();
        if (isV775) {
            ctx.writeAndFlush(new NettyTimeUpdatePacketV775(0, timeOfDay));
        } else if (isV768) {
            ctx.writeAndFlush(new NettyTimeUpdatePacketV768(0, timeOfDay));
        } else {
            ctx.writeAndFlush(new NettyTimeUpdatePacket(0, timeOfDay));
        }

        // Send initial weather state
        if (world.getWeather() != ServerWorld.WeatherState.CLEAR) {
            ctx.writeAndFlush(new NettyChangeGameStatePacket(
                    NettyChangeGameStatePacket.BEGIN_RAIN, 0));
            ctx.writeAndFlush(new NettyChangeGameStatePacket(
                    NettyChangeGameStatePacket.RAIN_LEVEL, 1.0f));
            if (world.getWeather() == ServerWorld.WeatherState.THUNDER) {
                ctx.writeAndFlush(new NettyChangeGameStatePacket(
                        NettyChangeGameStatePacket.THUNDER_LEVEL, 1.0f));
            }
        }

        // S2C Y: 1.7.2 = eye-level (client subtracts yOffset=1.62); 1.8+ = feet-level.
        float alphaSpawnYaw = (spawn.yaw + 180.0f) % 360.0f;
        double clientY = isV47 ? spawn.y - PLAYER_EYE_HEIGHT : spawn.y;

        // 1.14+: Send chunk cache center before chunks.
        if (isV477) {
            int spawnChunkX = spawnBlockX >> 4;
            int spawnChunkZ = spawnBlockZ >> 4;
            ctx.writeAndFlush(new SetChunkCacheCenterPacketV477(spawnChunkX, spawnChunkZ));
            player.setLastChunkCenter(spawnChunkX, spawnChunkZ);
        }

        // Send initial chunks. addPlayer is deferred until after the login
        // sequence completes to prevent the tick loop from racing with us.
        // v764+ batch wrapping is handled inside sendOrQueueChunks.
        chunkManager.sendInitialChunks(player, spawnBlockX, spawnBlockZ);

        // Send player position AFTER chunks. The client uses PlayerPosition as
        // the signal to exit the loading screen and start physics. Sending it
        // after chunks ensures terrain collision data is available, preventing
        // the player from briefly falling into the ground before chunks load.
        // SetChunkCacheCenter (above) tells the client which chunk to prioritize.
        if (isV768) {
            awaitingTeleportConfirm = true;
            ctx.writeAndFlush(new NettyPlayerPositionS2CPacketV768(
                    spawn.x, clientY, spawn.z, alphaSpawnYaw, spawn.pitch, ++nextTeleportId));
        } else if (isV762) { // V762-V767 use NettyPlayerPositionS2CPacketV762
            awaitingTeleportConfirm = true;
            ctx.writeAndFlush(new NettyPlayerPositionS2CPacketV762(
                    spawn.x, clientY, spawn.z, alphaSpawnYaw, spawn.pitch, ++nextTeleportId));
        } else if (isV755) {
            awaitingTeleportConfirm = true;
            ctx.writeAndFlush(new NettyPlayerPositionS2CPacketV755(
                    spawn.x, clientY, spawn.z, alphaSpawnYaw, spawn.pitch, ++nextTeleportId));
        } else if (isV109) {
            awaitingTeleportConfirm = true;
            ctx.writeAndFlush(new NettyPlayerPositionS2CPacketV109(
                    spawn.x, clientY, spawn.z, alphaSpawnYaw, spawn.pitch, ++nextTeleportId));
        } else if (isV47) {
            ctx.writeAndFlush(new NettyPlayerPositionS2CPacketV47(
                    spawn.x, clientY, spawn.z, alphaSpawnYaw, spawn.pitch));
        } else {
            ctx.writeAndFlush(new NettyPlayerPositionS2CPacket(
                    spawn.x, spawn.y, spawn.z, alphaSpawnYaw, spawn.pitch, false));
        }

        // Send existing players — deferred by 1 second so the tick loop can deliver
        // async chunks first. Clients (especially 1.8) silently discard SpawnPlayer
        // packets for entities in chunks that haven't been loaded yet. The tick loop
        // delivers ~4-8 chunks per tick (20 TPS), so 1 second covers ~80-160 chunks,
        // more than enough for the initial 121.
        // Capture version flags for the scheduled lambda.
        final boolean fIsV109 = isV109, fIsV47 = isV47;
        final boolean fIsV477 = isV477, fIsV573 = isV573, fIsV764 = isV764;
        final boolean fIsV766 = isV766, fIsV768 = isV768, fIsV769 = isV769;
        final boolean fIsV770 = isV770, fIsV771 = isV771, fIsV773 = isV773, fIsV774 = isV774;
        final boolean fIsV759 = isV759, fIsV761 = isV761;
        final ConnectedPlayer fPlayer = player;
        final ProtocolVersion fClientVersion = clientVersion;
        ctx.channel().eventLoop().schedule(() -> {
            if (!ctx.channel().isActive()) return;
            spawnExistingPlayersForNetty(ctx, fPlayer, fClientVersion,
                    fIsV774, fIsV773, fIsV771, fIsV770, fIsV769, fIsV768,
                    fIsV766, fIsV764, fIsV573, fIsV477, fIsV109, fIsV47,
                    fIsV761, fIsV759);
        }, 1, TimeUnit.SECONDS);

        // Configure translator with client version BEFORE broadcasts —
        // broadcastPlayerListAdd sends to ALL players including this one,
        // so the translator must know the version to emit the correct format.
        loginComplete = true;
        ClassicToNettyTranslator translator = ctx.pipeline().get(ClassicToNettyTranslator.class);
        if (translator != null) {
            translator.setClientVersion(clientVersion);
        }
        // Broadcast new player spawn to everyone else (immediate — other clients have chunks)
        playerManager.broadcastPlayerListAdd(player);
        playerManager.broadcastPlayerSpawn(player);

        // Initialize inventory adapter tracking
        InventoryAdapter adapter = playerManager.getInventoryAdapter();
        adapter.initPlayer(player.getUsername());

        // Give 1 cobblestone for right-click.
        // v404 (1.13.2)+ uses boolean+VarInt slot format (also used by v477/1.14).
        // For 1.14+ clients: delay the SetSlot by 10 seconds so the client's async
        // resource reload (model baking) completes before the hotbar tries to render
        // the item. Without this delay, ItemRenderer NPEs on null BakedModel.
        boolean isV404 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_13_2);
        Runnable sendSetSlot = () -> {
            if (isV766) {
                ctx.writeAndFlush(new NettySetSlotPacketV766(0, 0, 36,
                        BlockStateMapper.toV765ItemId(BlockRegistry.COBBLESTONE), 1));
            } else if (isV765) {
                ctx.writeAndFlush(new NettySetSlotPacketV756(0, 0, 36,
                        BlockStateMapper.toV765ItemId(BlockRegistry.COBBLESTONE), 1));
            } else if (isV764) {
                ctx.writeAndFlush(new NettySetSlotPacketV756(0, 0, 36,
                        BlockStateMapper.toV759ItemId(BlockRegistry.COBBLESTONE), 1));
            } else if (isV763) {
                ctx.writeAndFlush(new NettySetSlotPacketV756(0, 0, 36,
                        BlockStateMapper.toV759ItemId(BlockRegistry.COBBLESTONE), 1));
            } else if (isV762) {
                ctx.writeAndFlush(new NettySetSlotPacketV756(0, 0, 36,
                        BlockStateMapper.toV759ItemId(BlockRegistry.COBBLESTONE), 1));
            } else if (isV761) {
                ctx.writeAndFlush(new NettySetSlotPacketV756(0, 0, 36,
                        BlockStateMapper.toV759ItemId(BlockRegistry.COBBLESTONE), 1));
            } else if (isV760) {
                ctx.writeAndFlush(new NettySetSlotPacketV756(0, 0, 36,
                        BlockStateMapper.toV759ItemId(BlockRegistry.COBBLESTONE), 1));
            } else if (isV759) {
                ctx.writeAndFlush(new NettySetSlotPacketV756(0, 0, 36,
                        BlockStateMapper.toV759ItemId(BlockRegistry.COBBLESTONE), 1));
            } else if (isV756) {
                ctx.writeAndFlush(new NettySetSlotPacketV756(0, 0, 36,
                        BlockStateMapper.toV755ItemId(BlockRegistry.COBBLESTONE), 1));
            } else if (isV755) {
                ctx.writeAndFlush(new NettySetSlotPacketV404(0, 36,
                        BlockStateMapper.toV755ItemId(BlockRegistry.COBBLESTONE), 1));
            } else if (isV735) {
                ctx.writeAndFlush(new NettySetSlotPacketV404(0, 36,
                        BlockStateMapper.toV735ItemId(BlockRegistry.COBBLESTONE), 1));
            } else if (isV404) {
                ctx.writeAndFlush(new NettySetSlotPacketV404(0, 36,
                        BlockStateMapper.toV393ItemId(BlockRegistry.COBBLESTONE), 1));
            } else if (isV393) {
                ctx.writeAndFlush(new NettySetSlotPacketV393(0, 36,
                        BlockStateMapper.toV393ItemId(BlockRegistry.COBBLESTONE), 1));
            } else if (isV47) {
                ctx.writeAndFlush(new NettySetSlotPacketV47(0, 36, BlockRegistry.COBBLESTONE, 1, 0));
            } else {
                ctx.writeAndFlush(new NettySetSlotPacket(0, 36, BlockRegistry.COBBLESTONE, 1, 0));
            }
        };
        if (isV477) {
            ctx.executor().schedule(sendSetSlot, 10, java.util.concurrent.TimeUnit.SECONDS);
        } else {
            sendSetSlot.run();
        }
        adapter.setSlot(player.getUsername(), 36, BlockRegistry.COBBLESTONE, 1, 0);

        // Start KeepAlive heartbeat with configurable interval and timeout
        boolean isV340 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_12_2);
        int keepAliveInterval = ServerProperties.getKeepAliveIntervalSeconds();
        long keepAliveTimeoutMs = ServerProperties.getKeepAliveTimeoutSeconds() * 1000L;
        keepAliveTask = ctx.executor().scheduleAtFixedRate(() -> {
            if (ctx.channel().isActive()) {
                // Check for timeout before sending next keep-alive
                if (System.currentTimeMillis() - player.getLastKeepAliveResponseTime() > keepAliveTimeoutMs) {
                    sendPlayDisconnect(ctx, "Timed out");
                    return;
                }
                player.setKeepAliveSentNanos(System.nanoTime());
                if (isV340) {
                    ctx.writeAndFlush(new KeepAlivePacketV340(++keepAliveCounter));
                } else if (isV47) {
                    ctx.writeAndFlush(new KeepAlivePacketV47(++keepAliveCounter));
                } else {
                    ctx.writeAndFlush(new KeepAlivePacketV17(++keepAliveCounter));
                }
            }
        }, keepAliveInterval, keepAliveInterval, TimeUnit.SECONDS);

        // Send tab list entries (for v4/v5 using old format; v47 already sent ADD_PLAYER above)
        if (!isV47) {
            for (ConnectedPlayer existing : playerManager.getAllPlayers()) {
                if (existing != player) {
                    ctx.writeAndFlush(new NettyPlayerListItemPacket(existing.getUsername(), true, 0));
                }
            }
        }

        playerManager.broadcastChat((byte) 0, player.getUsername() + " joined the game");
        ServerEvents.PLAYER_JOIN.invoker().onPlayerJoin(player.getUsername(), clientVersion);

        String ip = PlayerManager.extractIp(ctx.channel().remoteAddress());
        System.out.println("Netty login complete: " + player.getUsername()
                + " (" + clientVersion.getDisplayName()
                + ", ID " + player.getPlayerId()
                + ", " + playerManager.getPlayerCount() + " online"
                + (ip != null ? ", ip " + ip : "") + ")");
    }

    // ========================================================================
    // Play state: gameplay packets
    // ========================================================================

    private void handlePlayPacket(ChannelHandlerContext ctx, Packet packet) {
        if (!loginComplete) return;

        // V47 position packets (must be checked before base Alpha versions)
        if (packet instanceof PlayerPositionAndLookC2SPacketV47) {
            PlayerPositionAndLookC2SPacketV47 p = (PlayerPositionAndLookC2SPacketV47) packet;
            float classicYaw = (p.getYaw() + 180.0f) % 360.0f;
            updatePosition(ctx, p.getX(), p.getY(), p.getZ(), classicYaw, p.getPitch());
        } else if (packet instanceof PlayerPositionPacketV47) {
            PlayerPositionPacketV47 p = (PlayerPositionPacketV47) packet;
            updatePosition(ctx, p.getX(), p.getY(), p.getZ(),
                    player.getFloatYaw(), player.getFloatPitch());
        } else if (packet instanceof PlayerPositionAndLookC2SPacket) {
            handlePositionAndLook(ctx, (PlayerPositionAndLookC2SPacket) packet);
        } else if (packet instanceof PlayerPositionPacket) {
            handlePosition(ctx, (PlayerPositionPacket) packet);
        } else if (packet instanceof PlayerLookPacket) {
            handleLook(ctx, (PlayerLookPacket) packet);
        } else if (packet instanceof PlayerOnGroundPacket) {
            // No-op
        } else if (packet instanceof PlayerDiggingPacketV477) {
            handleDiggingV477(ctx, (PlayerDiggingPacketV477) packet);
        } else if (packet instanceof PlayerDiggingPacketV47) {
            handleDiggingV47(ctx, (PlayerDiggingPacketV47) packet);
        } else if (packet instanceof PlayerDiggingPacket) {
            handleDigging(ctx, (PlayerDiggingPacket) packet);
        } else if (packet instanceof BlockPlacementData) {
            handleBlockPlacement(ctx, (BlockPlacementData) packet);
        } else if (packet instanceof NettyWindowClickPacketV47 wc47) {
            handleNettyWindowClick(ctx, wc47.getWindowId(), wc47.getSlotIndex(),
                    wc47.getButton(), wc47.getActionNumber(), wc47.getMode());
        } else if (packet instanceof NettyWindowClickPacket wc) {
            handleNettyWindowClick(ctx, wc.getWindowId(), wc.getSlotIndex(),
                    wc.getButton(), wc.getActionNumber(), wc.getMode());
        } else if (packet instanceof CloseWindowPacket) {
            handleNettyCloseWindow();
        } else if (packet instanceof ChatCommandC2SPacketV759) {
            handleChatCommand(ctx, (ChatCommandC2SPacketV759) packet);
        } else if (packet instanceof NettyChatC2SPacket) {
            handleChat(ctx, (NettyChatC2SPacket) packet);
        } else if (packet instanceof ClientCommandPacket) {
            ClientCommandPacket cmd = (ClientCommandPacket) packet;
            if (cmd.getActionId() == ClientCommandPacket.RESPAWN) {
                handleRespawn(ctx);
            }
        } else if (packet instanceof TeleportConfirmPacketV109) {
            awaitingTeleportConfirm = false;
        } else if (packet instanceof KeepAlivePacketV47
                || packet instanceof KeepAlivePacketV17
                || packet instanceof KeepAlivePacketV340) {
            // Keep-alive response — measure RTT
            if (player != null) {
                player.updateRtt(player.getKeepAliveSentNanos());
                player.setLastKeepAliveResponseTime(System.currentTimeMillis());
            }
        } else if (packet instanceof HoldingChangePacketBeta
                || packet instanceof AnimationPacket
                || packet instanceof AnimationPacketV47
                || packet instanceof NettyEntityActionPacket
                || packet instanceof NettyEntityActionPacketV47
                || packet instanceof NettySteerVehiclePacket
                || packet instanceof NettySteerVehiclePacketV47
                || packet instanceof PlayerInputPacketV768
                || packet instanceof ConfirmTransactionPacket
                || packet instanceof NettyCreativeSlotPacket
                || packet instanceof NettyCreativeSlotPacketV47
                || packet instanceof EnchantItemPacket
                || packet instanceof NettyUpdateSignPacket
                || packet instanceof NettyUpdateSignPacketV47
                || packet instanceof PlayerAbilitiesPacketV73
                || packet instanceof PlayerAbilitiesPacketV735
                || packet instanceof NettyTabCompletePacket
                || packet instanceof NettyTabCompletePacketV47
                || packet instanceof NettyClientSettingsPacket
                || packet instanceof NettyClientSettingsPacketV47
                || packet instanceof NettyPluginMessagePacket
                || packet instanceof NettyPluginMessagePacketV47
                || packet instanceof NettyUseEntityPacket
                || packet instanceof NettyUseEntityPacketV47
                || packet instanceof UseItemPacketV109
                || packet instanceof AnimationPacketV109
                || packet instanceof NettyClientSettingsPacketV109
                || packet instanceof NettyTabCompletePacketV109
                || packet instanceof ChunkBatchReceivedPacket
                || packet instanceof NoOpPacket) {
            // Silently accept
        }
    }

    /**
     * Spawn existing players for a newly connected Netty client.
     * Deferred from login to give the tick loop time to deliver async chunks,
     * preventing the client from silently discarding entities in unloaded chunks.
     */
    private void spawnExistingPlayersForNetty(ChannelHandlerContext ctx, ConnectedPlayer self,
            ProtocolVersion clientVer,
            boolean isV774, boolean isV773, boolean isV771, boolean isV770,
            boolean isV769, boolean isV768, boolean isV766, boolean isV764,
            boolean isV573, boolean isV477, boolean isV109, boolean isV47,
            boolean isV761, boolean isV759) {
        for (ConnectedPlayer existing : playerManager.getAllPlayers()) {
            if (existing == self) continue;
            int existingEntityId = existing.getPlayerId() + 1;
            int alphaYaw = (existing.getYaw() + 128) & 0xFF;
            int pitch = existing.getPitch() & 0xFF;
            String existingUuid = existing.getUuid();

            if (isV109) {
                double ex = existing.getX() / 32.0;
                double ey = (existing.getY() - PLAYER_EYE_HEIGHT_FIXED) / 32.0;
                double ez = existing.getZ() / 32.0;
                if (isV761) {
                    ctx.writeAndFlush(NettyPlayerInfoUpdatePacketV761.addPlayer(
                            existingUuid, existing.getUsername(), 1, 0));
                } else {
                    ctx.writeAndFlush(isV759
                            ? NettyPlayerListItemPacketV759.addPlayer(
                                    existingUuid, existing.getUsername(), 1, 0)
                            : NettyPlayerListItemPacketV47.addPlayer(
                                    existingUuid, existing.getUsername(), 1, 0));
                }
                if (isV774) {
                    ctx.writeAndFlush(new NettySpawnEntityPacketV774(
                            existingEntityId, existingUuid, ex, ey, ez, alphaYaw, pitch));
                } else if (isV773) {
                    ctx.writeAndFlush(new NettySpawnEntityPacketV773(
                            existingEntityId, existingUuid, ex, ey, ez, alphaYaw, pitch));
                } else if (isV771) {
                    ctx.writeAndFlush(new NettySpawnEntityPacketV771(
                            existingEntityId, existingUuid, ex, ey, ez, alphaYaw, pitch));
                } else if (isV770) {
                    ctx.writeAndFlush(new NettySpawnEntityPacketV770(
                            existingEntityId, existingUuid, ex, ey, ez, alphaYaw, pitch));
                } else if (isV769) {
                    ctx.writeAndFlush(new NettySpawnEntityPacketV769(
                            existingEntityId, existingUuid, ex, ey, ez, alphaYaw, pitch));
                } else if (isV768) {
                    ctx.writeAndFlush(new NettySpawnEntityPacketV768(
                            existingEntityId, existingUuid, ex, ey, ez, alphaYaw, pitch));
                } else if (isV766) {
                    ctx.writeAndFlush(new NettySpawnEntityPacketV766(
                            existingEntityId, existingUuid, ex, ey, ez, alphaYaw, pitch));
                } else if (isV764) {
                    ctx.writeAndFlush(new NettySpawnEntityPacketV764(
                            existingEntityId, existingUuid, ex, ey, ez, alphaYaw, pitch));
                } else if (isV573) {
                    ctx.writeAndFlush(new NettySpawnPlayerPacketV573(
                            existingEntityId, existingUuid, ex, ey, ez, alphaYaw, pitch));
                } else if (isV477) {
                    ctx.writeAndFlush(new NettySpawnPlayerPacketV477(
                            existingEntityId, existingUuid, ex, ey, ez, alphaYaw, pitch));
                } else {
                    ctx.writeAndFlush(new NettySpawnPlayerPacketV109(
                            existingEntityId, existingUuid, ex, ey, ez, alphaYaw, pitch));
                }
                if (!isV764) {
                    ctx.writeAndFlush(new com.github.martinambrus.rdforward.protocol.packet.netty.EntityHeadRotationPacket(existingEntityId, alphaYaw));
                }
            } else if (isV47) {
                int existingFeetY = (int) existing.getY() - PLAYER_EYE_HEIGHT_FIXED;
                ctx.writeAndFlush(NettyPlayerListItemPacketV47.addPlayer(
                        existingUuid, existing.getUsername(), 1, 0));
                ctx.writeAndFlush(new NettySpawnPlayerPacketV47(
                        existingEntityId, existingUuid,
                        (int) existing.getX(), existingFeetY, (int) existing.getZ(),
                        alphaYaw, pitch, (short) 0));
                ctx.writeAndFlush(new com.github.martinambrus.rdforward.protocol.packet.netty.EntityHeadRotationPacket(existingEntityId, alphaYaw));
            } else {
                int existingFeetY = (int) existing.getY() - PLAYER_EYE_HEIGHT_FIXED;
                Packet spawnPacket = clientVer.isAtLeast(ProtocolVersion.RELEASE_1_7_6)
                        ? new NettySpawnPlayerPacketV5(
                                existingEntityId, existingUuid, existing.getUsername(),
                                (int) existing.getX(), existingFeetY, (int) existing.getZ(),
                                alphaYaw, pitch, (short) 0)
                        : new NettySpawnPlayerPacket(
                                existingEntityId, existingUuid, existing.getUsername(),
                                (int) existing.getX(), existingFeetY, (int) existing.getZ(),
                                alphaYaw, pitch, (short) 0);
                ctx.writeAndFlush(spawnPacket);
                // 1.7.x uses int entityId; 1.8+ uses VarInt
                ctx.writeAndFlush(new com.github.martinambrus.rdforward.protocol.packet.alpha.EntityHeadRotationPacket(existingEntityId, alphaYaw));
            }
        }
    }

    private void handlePositionAndLook(ChannelHandlerContext ctx,
                                       PlayerPositionAndLookC2SPacket packet) {
        if (player == null) return;
        float classicYaw = (packet.getYaw() + 180.0f) % 360.0f;
        updatePosition(ctx, packet.getX(), packet.getY(), packet.getZ(),
                classicYaw, packet.getPitch());
    }

    private void handlePosition(ChannelHandlerContext ctx, PlayerPositionPacket packet) {
        if (player == null) return;
        updatePosition(ctx, packet.getX(), packet.getY(), packet.getZ(),
                player.getFloatYaw(), player.getFloatPitch());
    }

    private void handleLook(ChannelHandlerContext ctx, PlayerLookPacket packet) {
        if (player == null) return;
        float classicYaw = (packet.getYaw() + 180.0f) % 360.0f;
        updatePosition(ctx, player.getDoubleX(),
                player.getDoubleY() - PLAYER_EYE_HEIGHT, player.getDoubleZ(),
                classicYaw, packet.getPitch());
    }

    private void updatePosition(ChannelHandlerContext ctx,
                                double x, double y, double z, float yaw, float pitch) {
        // Reject C2S position updates until the client confirms our teleport.
        // Without this, the client's pre-teleport position (at world spawn) would
        // update the server-side player position, causing updatePlayerChunks to
        // unload the correct chunks around the teleport destination.
        if (awaitingTeleportConfirm) {
            return;
        }

        double eyeY = y + PLAYER_EYE_HEIGHT;

        // Fall below world — respawn
        if (y < -10) {
            respawnToSafePosition(ctx, yaw);
            return;
        }

        // During teleport grace, skip chunk-boundary checks to give old clients
        // time to process chunk data. Accept the movement unconditionally.
        boolean inGrace = player.isInTeleportGrace();

        // Reject movement into chunks not yet sent to this client.
        int destChunkX = (int) Math.floor(x) >> 4;
        int destChunkZ = (int) Math.floor(z) >> 4;
        if (!inGrace && !chunkManager.isChunkSentToPlayer(player, destChunkX, destChunkZ)) {
            if (stuckAtUnloadedChunk) {
                return; // Already sent teleport-back, don't spam
            }
            stuckAtUnloadedChunk = true;

            // Teleport back to last known good position
            double lastX = player.getDoubleX();
            double lastEyeY = player.getDoubleY();
            double lastZ = player.getDoubleZ();
            double lastFeetY = lastEyeY - PLAYER_EYE_HEIGHT;
            float alphaYaw = (yaw + 180.0f) % 360.0f;

            if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_21_2)) {
                awaitingTeleportConfirm = true;
                ctx.writeAndFlush(new NettyPlayerPositionS2CPacketV768(
                        lastX, lastFeetY, lastZ, alphaYaw, pitch, ++nextTeleportId));
            } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_19_4)) {
                awaitingTeleportConfirm = true;
                ctx.writeAndFlush(new NettyPlayerPositionS2CPacketV762(
                        lastX, lastFeetY, lastZ, alphaYaw, pitch, ++nextTeleportId));
            } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_17)) {
                awaitingTeleportConfirm = true;
                ctx.writeAndFlush(new NettyPlayerPositionS2CPacketV755(
                        lastX, lastFeetY, lastZ, alphaYaw, pitch, ++nextTeleportId));
            } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_9)) {
                awaitingTeleportConfirm = true;
                ctx.writeAndFlush(new NettyPlayerPositionS2CPacketV109(
                        lastX, lastFeetY, lastZ, alphaYaw, pitch, ++nextTeleportId));
            } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_8)) {
                ctx.writeAndFlush(new NettyPlayerPositionS2CPacketV47(
                        lastX, lastFeetY, lastZ, alphaYaw, pitch));
            } else {
                // 1.7.2: S2C Y is eye-level
                ctx.writeAndFlush(new NettyPlayerPositionS2CPacket(
                        lastX, lastEyeY, lastZ, alphaYaw, pitch, false));
            }
            return;
        }
        stuckAtUnloadedChunk = false;

        short fixedX = toFixedPoint(x);
        short fixedY = toFixedPoint(eyeY);
        short fixedZ = toFixedPoint(z);
        byte byteYaw = toByteRotation(yaw);
        byte bytePitch = toByteRotation(pitch);

        player.updatePositionDouble(x, eyeY, z, yaw, pitch);

        ServerEvents.PLAYER_MOVE.invoker().onPlayerMove(
                player.getUsername(), fixedX, fixedY, fixedZ, byteYaw, bytePitch);

        playerManager.broadcastPositionUpdate(player, fixedX, fixedY, fixedZ, byteYaw, bytePitch);
    }

    private void handleDiggingV477(ChannelHandlerContext ctx, PlayerDiggingPacketV477 packet) {
        if (player == null) return;

        if (packet.getStatus() == PlayerDiggingPacketV477.STATUS_STARTED
                || packet.getStatus() == PlayerDiggingPacketV477.STATUS_FINISHED) {
            int x = packet.getX();
            int y = packet.getY();
            int z = packet.getZ();
            if (!world.inBounds(x, y, z)) return;
            byte existingBlock = world.getBlock(x, y, z);
            if (existingBlock == 0) return;

            EventResult result = ServerEvents.BLOCK_BREAK.invoker()
                    .onBlockBreak(player.getUsername(), x, y, z, existingBlock & 0xFF);
            if (result == EventResult.CANCEL) {
                // Resend existing block so client doesn't show ghost removal
                sendBlockChange(ctx, x, y, z, existingBlock & 0xFF, 0);
                if (packet instanceof PlayerDiggingPacketV759) {
                    ctx.writeAndFlush(new BlockChangedAckPacketV759(
                            ((PlayerDiggingPacketV759) packet).getSequence()));
                } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_15)) {
                    ctx.writeAndFlush(new AcknowledgePlayerDiggingPacketV573(
                            x, y, z, existingBlock & 0xFF, packet.getStatus(), false));
                }
                return;
            }

            world.queueBlockChange(x, y, z, (byte) 0);

            // 1.19+: BlockChangedAck replaces AcknowledgePlayerDigging
            // 1.15+: Send AcknowledgePlayerDigging to prevent client revert
            if (packet instanceof PlayerDiggingPacketV759) {
                ctx.writeAndFlush(new BlockChangedAckPacketV759(
                        ((PlayerDiggingPacketV759) packet).getSequence()));
            } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_15)) {
                ctx.writeAndFlush(new AcknowledgePlayerDiggingPacketV573(
                        x, y, z, 0, packet.getStatus(), true));
            }
        }
    }

    private void handleDiggingV47(ChannelHandlerContext ctx, PlayerDiggingPacketV47 packet) {
        if (player == null) return;

        if (packet.getStatus() == PlayerDiggingPacketV47.STATUS_STARTED
                || packet.getStatus() == PlayerDiggingPacketV47.STATUS_FINISHED) {
            int x = packet.getX();
            int y = packet.getY();
            int z = packet.getZ();
            if (!world.inBounds(x, y, z)) return;
            byte existingBlock = world.getBlock(x, y, z);
            if (existingBlock == 0) return;

            EventResult result = ServerEvents.BLOCK_BREAK.invoker()
                    .onBlockBreak(player.getUsername(), x, y, z, existingBlock & 0xFF);
            if (result == EventResult.CANCEL) {
                sendBlockChange(ctx, x, y, z, existingBlock & 0xFF, 0);
                return;
            }

            world.queueBlockChange(x, y, z, (byte) 0);
        }
    }

    private void handleDigging(ChannelHandlerContext ctx, PlayerDiggingPacket packet) {
        if (player == null) return;

        if (packet.getStatus() == PlayerDiggingPacket.STATUS_STARTED
                || packet.getStatus() == PlayerDiggingPacket.STATUS_FINISHED) {
            int x = packet.getX();
            int y = packet.getY();
            int z = packet.getZ();
            if (!world.inBounds(x, y, z)) return;
            byte existingBlock = world.getBlock(x, y, z);
            if (existingBlock == 0) return;

            EventResult result = ServerEvents.BLOCK_BREAK.invoker()
                    .onBlockBreak(player.getUsername(), x, y, z, existingBlock & 0xFF);
            if (result == EventResult.CANCEL) {
                sendBlockChange(ctx, x, y, z, existingBlock & 0xFF, 0);
                return;
            }

            world.queueBlockChange(x, y, z, (byte) 0);
        }
    }

    private void handleBlockPlacement(ChannelHandlerContext ctx, BlockPlacementData packet) {
        if (player == null) return;

        int x = packet.getX();
        int y = packet.getY();
        int z = packet.getZ();
        int direction = packet.getDirection();

        if (direction == -1) return;

        // Compute target position
        int targetX = x, targetY = y, targetZ = z;
        switch (direction) {
            case 0: targetY--; break;
            case 1: targetY++; break;
            case 2: targetZ--; break;
            case 3: targetZ++; break;
            case 4: targetX--; break;
            case 5: targetX++; break;
        }

        short itemId = packet.getItemId();
        if (itemId < 0) return;

        // Creative mode: accept any block ID (1-MAX_BLOCK_ID)
        if (itemId < 1 || itemId > BlockRegistry.MAX_BLOCK_ID) {
            sendBlockChange(ctx, targetX, targetY, targetZ, 0, 0);
            return;
        }

        if (!world.inBounds(targetX, targetY, targetZ)) {
            sendBlockChange(ctx, targetX, targetY, targetZ, 0, 0);
            return;
        }

        // Body overlap check
        double px = player.getDoubleX();
        double feetY = player.getDoubleY() - PLAYER_EYE_HEIGHT;
        double pz = player.getDoubleZ();
        boolean overlapsX = targetX < px + 0.3 && px - 0.3 < targetX + 1;
        boolean overlapsY = targetY < feetY + 1.8 && feetY < targetY + 1;
        boolean overlapsZ = targetZ < pz + 0.3 && pz - 0.3 < targetZ + 1;
        if (overlapsX && overlapsY && overlapsZ) {
            sendBlockChange(ctx, targetX, targetY, targetZ, 0, 0);
            return;
        }

        // Determine world block type (creative mode: grass at surface, cobblestone else)
        int surfaceY = world.getHeight() * 2 / 3;
        byte worldBlockType = (targetY == surfaceY)
                ? (byte) BlockRegistry.GRASS
                : (byte) BlockRegistry.COBBLESTONE;

        EventResult result = ServerEvents.BLOCK_PLACE.invoker()
                .onBlockPlace(player.getUsername(), targetX, targetY, targetZ, worldBlockType & 0xFF);
        if (result == EventResult.CANCEL) {
            // Resend air so client doesn't show ghost placement
            sendBlockChange(ctx, targetX, targetY, targetZ, 0, 0);
            if (packet instanceof NettyBlockPlacementPacketV768) {
                ctx.writeAndFlush(new BlockChangedAckPacketV759(
                        ((NettyBlockPlacementPacketV768) packet).getSequence()));
            } else if (packet instanceof NettyBlockPlacementPacketV759) {
                ctx.writeAndFlush(new BlockChangedAckPacketV759(
                        ((NettyBlockPlacementPacketV759) packet).getSequence()));
            }
            return;
        }

        if (!world.setBlock(targetX, targetY, targetZ, worldBlockType)) return;
        chunkManager.setBlock(targetX, targetY, targetZ, worldBlockType);

        playerManager.broadcastPacketExcept(
                new SetBlockServerPacket(targetX, targetY, targetZ, worldBlockType & 0xFF), player);

        // Confirm block to this client
        sendBlockChange(ctx, targetX, targetY, targetZ, itemId & 0xFF, 0);

        // 1.19+: Send BlockChangedAck after placement
        if (packet instanceof NettyBlockPlacementPacketV768) {
            ctx.writeAndFlush(new BlockChangedAckPacketV759(
                    ((NettyBlockPlacementPacketV768) packet).getSequence()));
        } else if (packet instanceof NettyBlockPlacementPacketV759) {
            ctx.writeAndFlush(new BlockChangedAckPacketV759(
                    ((NettyBlockPlacementPacketV759) packet).getSequence()));
        }

        // Grass conversion delay
        if (worldBlockType != (byte) (itemId & 0xFF)) {
            final int fx = targetX, fy = targetY, fz = targetZ;
            final int fBlockType = worldBlockType & 0xFF;
            ctx.executor().schedule(() -> {
                if (ctx.channel().isActive()) {
                    sendBlockChange(ctx, fx, fy, fz, fBlockType, 0);
                }
            }, 200, TimeUnit.MILLISECONDS);
        }
    }

    private void handleNettyWindowClick(ChannelHandlerContext ctx,
                                          int windowId, int slotIndex, int button,
                                          int actionNumber, int mode) {
        if (player == null) return;
        InventoryAdapter adapter = playerManager.getInventoryAdapter();
        adapter.processWindowClick(player.getUsername(), slotIndex, button, mode);
        ctx.writeAndFlush(new ConfirmTransactionPacket(windowId, actionNumber, true));
    }

    private void handleNettyCloseWindow() {
        if (player == null) return;
        InventoryAdapter adapter = playerManager.getInventoryAdapter();
        adapter.processCloseWindow(player.getUsername());
    }

    private void handleChat(ChannelHandlerContext ctx, NettyChatC2SPacket packet) {
        if (player == null) return;
        String message = packet.getMessage();

        if (message.startsWith("/")) {
            dispatchCommand(message.substring(1));
            return;
        }

        EventResult result = ServerEvents.CHAT.invoker().onChat(player.getUsername(), message);
        if (result == EventResult.CANCEL) return;

        System.out.println("[Chat] " + player.getUsername() + ": " + message);
        playerManager.broadcastChat(player.getPlayerId(), player.getUsername() + ": " + message);
    }

    private void handleChatCommand(ChannelHandlerContext ctx, ChatCommandC2SPacketV759 packet) {
        if (player == null) return;
        // getMessage() returns "/" + command
        String message = packet.getMessage();
        if (message.startsWith("/")) {
            dispatchCommand(message.substring(1));
        }
    }

    private void dispatchCommand(String command) {
        boolean isV766 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_20_5);
        boolean isV765 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_20_3);
        boolean isV764 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_20_2);
        boolean isV763 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_20);
        boolean isV762 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_19_4);
        boolean isV761 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_19_3);
        boolean isV760 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_19_1);
        boolean isV759 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_19);
        boolean isV735 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_16);
        boolean isV47 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_8);
        boolean handled = CommandRegistry.dispatch(command, player.getUsername(), false,
                reply -> {
                    if (isV765) {
                        player.sendPacket(new SystemChatPacketV765(reply, false));
                    } else {
                        String json = "{\"text\":\"" + reply.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}";
                        if (isV764) {
                            player.sendPacket(new SystemChatPacketV760(json, false));
                        } else if (isV763) {
                            player.sendPacket(new SystemChatPacketV760(json, false));
                        } else if (isV762) {
                            player.sendPacket(new SystemChatPacketV760(json, false));
                        } else if (isV761) {
                            player.sendPacket(new SystemChatPacketV760(json, false));
                        } else if (isV760) {
                            player.sendPacket(new SystemChatPacketV760(json, false));
                        } else if (isV759) {
                            player.sendPacket(new SystemChatPacketV759(json, 0));
                        } else if (isV735) {
                            player.sendPacket(new NettyChatS2CPacketV735(json, (byte) 0, 0L, 0L));
                        } else if (isV47) {
                            player.sendPacket(new NettyChatS2CPacketV47(json, (byte) 0));
                        } else {
                            player.sendPacket(new NettyChatS2CPacket(json));
                        }
                    }
                });
        if (!handled) {
            String unknownMsg = "Unknown command: " + command.split("\\s+")[0];
            if (isV765) {
                player.sendPacket(new SystemChatPacketV765(unknownMsg, false));
            } else {
                String json = "{\"text\":\"" + unknownMsg + "\"}";
                if (isV764) {
                    player.sendPacket(new SystemChatPacketV760(json, false));
                } else if (isV763) {
                    player.sendPacket(new SystemChatPacketV760(json, false));
                } else if (isV762) {
                    player.sendPacket(new SystemChatPacketV760(json, false));
                } else if (isV761) {
                    player.sendPacket(new SystemChatPacketV760(json, false));
                } else if (isV760) {
                    player.sendPacket(new SystemChatPacketV760(json, false));
                } else if (isV759) {
                    player.sendPacket(new SystemChatPacketV759(json, 0));
                } else if (isV735) {
                    player.sendPacket(new NettyChatS2CPacketV735(json, (byte) 0, 0L, 0L));
                } else if (isV47) {
                    player.sendPacket(new NettyChatS2CPacketV47(json, (byte) 0));
                } else {
                    player.sendPacket(new NettyChatS2CPacket(json));
                }
            }
        }
    }

    private void handleRespawn(ChannelHandlerContext ctx) {
        if (player == null) return;
        respawnToSafePosition(ctx, player.getFloatYaw());
    }

    private void respawnToSafePosition(ChannelHandlerContext ctx, float yaw) {
        int cx = ((world.getWidth() / 2) >> 4) * 16 + 8;
        int cz = ((world.getDepth() / 2) >> 4) * 16 + 8;
        int heuristicY = world.getHeight() * 2 / 3 + 1;
        int[] safe = world.findSafePosition(cx, heuristicY, cz, 50);
        double spawnX = safe[0] + 0.5;
        double spawnFeetY = safe[1];
        double spawnEyeY = spawnFeetY + PLAYER_EYE_HEIGHT;
        double spawnZ = safe[2] + 0.5;
        player.updatePositionDouble(spawnX, spawnEyeY, spawnZ, yaw, 0);

        // S2C Y: 1.7.2 = eye-level; 1.8+ = feet-level.
        float alphaYaw = (yaw + 180.0f) % 360.0f;
        if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_21_2)) {
            awaitingTeleportConfirm = true;
            ctx.writeAndFlush(new NettyPlayerPositionS2CPacketV768(
                    spawnX, spawnFeetY, spawnZ, alphaYaw, 0, ++nextTeleportId));
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_19_4)) {
            awaitingTeleportConfirm = true;
            ctx.writeAndFlush(new NettyPlayerPositionS2CPacketV762(
                    spawnX, spawnFeetY, spawnZ, alphaYaw, 0, ++nextTeleportId));
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_17)) {
            awaitingTeleportConfirm = true;
            ctx.writeAndFlush(new NettyPlayerPositionS2CPacketV755(
                    spawnX, spawnFeetY, spawnZ, alphaYaw, 0, ++nextTeleportId));
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_9)) {
            awaitingTeleportConfirm = true;
            ctx.writeAndFlush(new NettyPlayerPositionS2CPacketV109(
                    spawnX, spawnFeetY, spawnZ, alphaYaw, 0, ++nextTeleportId));
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_8)) {
            ctx.writeAndFlush(new NettyPlayerPositionS2CPacketV47(
                    spawnX, spawnFeetY, spawnZ, alphaYaw, 0));
        } else {
            ctx.writeAndFlush(new NettyPlayerPositionS2CPacket(
                    spawnX, spawnEyeY, spawnZ, alphaYaw, 0, false));
        }

        chunkManager.updatePlayerChunks(player);

        // 1.13+ clients may discard their chunk render cache during void fall.
        // updatePlayerChunks won't resend chunks that are still "tracked",
        // so force-resend them. Also resend SetChunkCacheCenter for 1.14+.
        if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_13)) {
            int spawnBlockX = (int) Math.floor(spawnX);
            int spawnBlockZ = (int) Math.floor(spawnZ);
            if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_14)) {
                int respawnChunkX = spawnBlockX >> 4;
                int respawnChunkZ = spawnBlockZ >> 4;
                ctx.writeAndFlush(new SetChunkCacheCenterPacketV477(
                        respawnChunkX, respawnChunkZ));
                player.setLastChunkCenter(respawnChunkX, respawnChunkZ);
            }
            chunkManager.resendPlayerChunks(player);
        }

        short fixedX = toFixedPoint(spawnX);
        short fixedY = toFixedPoint(spawnEyeY);
        short fixedZ = toFixedPoint(spawnZ);
        byte byteYaw = toByteRotation(yaw);
        playerManager.broadcastPacketExcept(
                new PlayerTeleportPacket(player.getPlayerId(),
                        fixedX, fixedY, fixedZ, byteYaw & 0xFF, 0),
                player);
    }

    private void sendBlockChange(ChannelHandlerContext ctx, int x, int y, int z,
                                  int blockType, int metadata) {
        if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_26_1)) {
            ctx.writeAndFlush(new NettyBlockChangePacketV477(x, y, z,
                    BlockStateMapper.toV775BlockState(blockType)));
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_19)) {
            // V759-V774 share the same block state IDs
            ctx.writeAndFlush(new NettyBlockChangePacketV477(x, y, z,
                    BlockStateMapper.toV759BlockState(blockType)));
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_17)) {
            ctx.writeAndFlush(new NettyBlockChangePacketV477(x, y, z,
                    BlockStateMapper.toV755BlockState(blockType)));
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_16)) {
            ctx.writeAndFlush(new NettyBlockChangePacketV477(x, y, z,
                    BlockStateMapper.toV735BlockState(blockType)));
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_14)) {
            ctx.writeAndFlush(new NettyBlockChangePacketV477(x, y, z,
                    BlockStateMapper.toV393BlockState(blockType)));
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_13)) {
            ctx.writeAndFlush(new NettyBlockChangePacketV393(x, y, z,
                    BlockStateMapper.toV393BlockState(blockType)));
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_8)) {
            ctx.writeAndFlush(new NettyBlockChangePacketV47(x, y, z, blockType, metadata));
        } else {
            ctx.writeAndFlush(new NettyBlockChangePacket(x, y, z, blockType, metadata));
        }
    }

    private void sendPlayDisconnect(ChannelHandlerContext ctx, String reason) {
        if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_20_3)) {
            ctx.writeAndFlush(new NettyDisconnectPacketV765(reason))
                    .addListener(io.netty.channel.ChannelFutureListener.CLOSE);
        } else {
            String json = "{\"text\":\"" + reason.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}";
            ctx.writeAndFlush(new NettyDisconnectPacket(json))
                    .addListener(io.netty.channel.ChannelFutureListener.CLOSE);
        }
    }

    // ========================================================================
    // Lifecycle
    // ========================================================================

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (keepAliveTask != null) {
            keepAliveTask.cancel(false);
        }
        if (player != null) {
            // Remove player first so getPlayerCount() is accurate for event listeners
            playerManager.removePlayer(ctx.channel());
            System.out.println(player.getUsername() + " disconnected"
                    + " (" + playerManager.getPlayerCount() + " online)");
            ServerEvents.PLAYER_LEAVE.invoker().onPlayerLeave(player.getUsername());
            playerManager.getInventoryAdapter().removePlayer(player.getUsername());
            world.rememberPlayerPosition(player);
            chunkManager.removePlayer(player);
            playerManager.broadcastPlayerListRemove(player);
            playerManager.broadcastChat((byte) 0, player.getUsername() + " left the game");
            playerManager.broadcastPlayerDespawn(player);
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (isNormalDisconnect(cause)) {
            System.out.println("Netty client disconnected"
                    + (player != null ? " (" + player.getUsername() + ")" : ""));
        } else {
            System.err.println("Netty connection error"
                    + (player != null ? " (" + player.getUsername() + ")" : "")
                    + ": " + cause.getMessage());
            cause.printStackTrace(System.err);
        }
        ctx.close();
    }

    private static boolean isNormalDisconnect(Throwable cause) {
        if (cause instanceof java.net.SocketException || cause instanceof java.io.IOException) {
            String msg = cause.getMessage();
            return msg != null && (msg.contains("Connection reset")
                    || msg.contains("forcibly closed")
                    || msg.contains("Broken pipe"));
        }
        return false;
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private void setCodecState(ChannelHandlerContext ctx, ConnectionState newState) {
        NettyPacketDecoder decoder = ctx.pipeline().get(NettyPacketDecoder.class);
        if (decoder != null) {
            decoder.setConnectionState(newState);
        }
        NettyPacketEncoder encoder = ctx.pipeline().get(NettyPacketEncoder.class);
        if (encoder != null) {
            encoder.setConnectionState(newState);
        }
    }

    private static short toFixedPoint(double d) {
        return (short) (d * 32);
    }

    private static byte toByteRotation(float degrees) {
        return (byte) ((degrees / 360.0f) * 256);
    }

    /**
     * Escape a string for safe embedding inside a JSON string literal.
     * Handles backslash, double-quote, common control characters, and
     * any other char below U+0020 via \\uXXXX.
     */
    private static String escapeJsonString(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"':  sb.append("\\\""); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        return sb.toString();
    }
}
