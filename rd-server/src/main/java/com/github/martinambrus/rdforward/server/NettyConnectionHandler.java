package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.codec.NettyPacketDecoder;
import com.github.martinambrus.rdforward.protocol.codec.NettyPacketEncoder;
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

    // Encryption state
    private KeyPair rsaKeyPair;
    private byte[] verifyToken;
    private boolean awaitingEncryptionResponse = false;

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
                if (packet instanceof LoginStartPacket) {
                    handleLoginStart(ctx, (LoginStartPacket) packet);
                } else if (packet instanceof NettyEncryptionResponsePacketV759) {
                    handleEncryptionResponseV759(ctx, (NettyEncryptionResponsePacketV759) packet);
                } else if (packet instanceof NettyEncryptionResponsePacketV47) {
                    handleEncryptionResponseV47(ctx, (NettyEncryptionResponsePacketV47) packet);
                } else if (packet instanceof NettyEncryptionResponsePacket) {
                    handleEncryptionResponse(ctx, (NettyEncryptionResponsePacket) packet);
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
        if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_19_4)) {
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
                + "\"players\":{\"max\":" + PlayerManager.MAX_PLAYERS
                + ",\"online\":" + playerManager.getPlayerCount() + "},"
                + "\"description\":{\"text\":\"RDForward Server\"}"
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

    private void handleLoginStart(ChannelHandlerContext ctx, LoginStartPacket packet) {
        pendingUsername = packet.getUsername();

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            rsaKeyPair = keyGen.generateKeyPair();
            verifyToken = new byte[4];
            new java.security.SecureRandom().nextBytes(verifyToken);

            if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_8)) {
                ctx.writeAndFlush(new NettyEncryptionRequestPacketV47(
                        "", rsaKeyPair.getPublic().getEncoded(), verifyToken));
            } else {
                ctx.writeAndFlush(new NettyEncryptionRequestPacket(
                        "", rsaKeyPair.getPublic().getEncoded(), verifyToken));
            }
            awaitingEncryptionResponse = true;
        } catch (Exception e) {
            System.err.println("Failed to generate RSA keypair: " + e.getMessage());
            sendLoginDisconnect(ctx, "Encryption error");
        }
    }

    private void handleEncryptionResponse(ChannelHandlerContext ctx,
                                           NettyEncryptionResponsePacket packet) {
        if (!awaitingEncryptionResponse || rsaKeyPair == null) {
            sendLoginDisconnect(ctx, "Unexpected encryption response");
            return;
        }
        awaitingEncryptionResponse = false;

        try {
            Cipher rsaCipher = Cipher.getInstance("RSA");
            rsaCipher.init(Cipher.DECRYPT_MODE, rsaKeyPair.getPrivate());
            byte[] sharedSecret = rsaCipher.doFinal(packet.getSharedSecret());

            rsaCipher.init(Cipher.DECRYPT_MODE, rsaKeyPair.getPrivate());
            byte[] decryptedToken = rsaCipher.doFinal(packet.getVerifyToken());

            if (!Arrays.equals(decryptedToken, verifyToken)) {
                System.err.println("Encryption verify token mismatch for " + pendingUsername);
                sendLoginDisconnect(ctx, "Encryption verification failed");
                return;
            }

            // Install cipher handlers in the pipeline
            MinecraftCipher decryptCipher = new MinecraftCipher(Cipher.DECRYPT_MODE, sharedSecret);
            MinecraftCipher encryptCipher = new MinecraftCipher(Cipher.ENCRYPT_MODE, sharedSecret);
            ctx.pipeline().addBefore("decoder", "decrypt", new CipherDecoder(decryptCipher));
            ctx.pipeline().addBefore("encoder", "encrypt", new CipherEncoder(encryptCipher));

            // Send LoginSuccess — this transitions to PLAY state
            // 1.16 changed UUID from VarIntString to binary (2 longs)
            String uuid = ClassicToNettyTranslator.generateOfflineUuid(pendingUsername);
            if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_19)) {
                ctx.writeAndFlush(new LoginSuccessPacketV759(uuid, pendingUsername));
            } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_16)) {
                ctx.writeAndFlush(new LoginSuccessPacketV735(uuid, pendingUsername));
            } else {
                ctx.writeAndFlush(new LoginSuccessPacket(uuid, pendingUsername));
            }

            // Transition to PLAY state
            state = ConnectionState.PLAY;
            setCodecState(ctx, ConnectionState.PLAY);

            // Now proceed with join game
            handleJoinGame(ctx);

        } catch (Exception e) {
            System.err.println("Encryption handshake failed for " + pendingUsername
                    + ": " + e.getMessage());
            sendLoginDisconnect(ctx, "Encryption error");
        }
    }

    private void handleEncryptionResponseV47(ChannelHandlerContext ctx,
                                              NettyEncryptionResponsePacketV47 packet) {
        if (!awaitingEncryptionResponse || rsaKeyPair == null) {
            sendLoginDisconnect(ctx, "Unexpected encryption response");
            return;
        }
        awaitingEncryptionResponse = false;

        try {
            Cipher rsaCipher = Cipher.getInstance("RSA");
            rsaCipher.init(Cipher.DECRYPT_MODE, rsaKeyPair.getPrivate());
            byte[] sharedSecret = rsaCipher.doFinal(packet.getSharedSecret());

            rsaCipher.init(Cipher.DECRYPT_MODE, rsaKeyPair.getPrivate());
            byte[] decryptedToken = rsaCipher.doFinal(packet.getVerifyToken());

            if (!Arrays.equals(decryptedToken, verifyToken)) {
                System.err.println("Encryption verify token mismatch for " + pendingUsername);
                sendLoginDisconnect(ctx, "Encryption verification failed");
                return;
            }

            // Install cipher handlers in the pipeline
            MinecraftCipher decryptCipher = new MinecraftCipher(Cipher.DECRYPT_MODE, sharedSecret);
            MinecraftCipher encryptCipher = new MinecraftCipher(Cipher.ENCRYPT_MODE, sharedSecret);
            ctx.pipeline().addBefore("decoder", "decrypt", new CipherDecoder(decryptCipher));
            ctx.pipeline().addBefore("encoder", "encrypt", new CipherEncoder(encryptCipher));

            // Send LoginSuccess — this transitions to PLAY state
            // 1.16 changed UUID from VarIntString to binary (2 longs)
            String uuid = ClassicToNettyTranslator.generateOfflineUuid(pendingUsername);
            if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_19)) {
                ctx.writeAndFlush(new LoginSuccessPacketV759(uuid, pendingUsername));
            } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_16)) {
                ctx.writeAndFlush(new LoginSuccessPacketV735(uuid, pendingUsername));
            } else {
                ctx.writeAndFlush(new LoginSuccessPacket(uuid, pendingUsername));
            }

            // Transition to PLAY state
            state = ConnectionState.PLAY;
            setCodecState(ctx, ConnectionState.PLAY);

            // Now proceed with join game
            handleJoinGame(ctx);

        } catch (Exception e) {
            System.err.println("Encryption handshake failed for " + pendingUsername
                    + ": " + e.getMessage());
            sendLoginDisconnect(ctx, "Encryption error");
        }
    }

    private void handleEncryptionResponseV759(ChannelHandlerContext ctx,
                                               NettyEncryptionResponsePacketV759 packet) {
        if (!awaitingEncryptionResponse || rsaKeyPair == null) {
            sendLoginDisconnect(ctx, "Unexpected encryption response");
            return;
        }
        awaitingEncryptionResponse = false;

        try {
            Cipher rsaCipher = Cipher.getInstance("RSA");
            rsaCipher.init(Cipher.DECRYPT_MODE, rsaKeyPair.getPrivate());
            byte[] sharedSecret = rsaCipher.doFinal(packet.getSharedSecret());

            if (packet.getVerifyToken() != null) {
                // Traditional RSA-encrypted verify token — decrypt and verify
                rsaCipher.init(Cipher.DECRYPT_MODE, rsaKeyPair.getPrivate());
                byte[] decryptedToken = rsaCipher.doFinal(packet.getVerifyToken());

                if (!Arrays.equals(decryptedToken, verifyToken)) {
                    System.err.println("Encryption verify token mismatch for " + pendingUsername);
                    sendLoginDisconnect(ctx, "Encryption verification failed");
                    return;
                }
            }
            // else: Chat signing mode (salt + signature). In offline mode, skip
            // verify token check — the RSA-encrypted shared secret already proves
            // the client received our encryption request.

            // Install cipher handlers in the pipeline
            MinecraftCipher decryptCipher = new MinecraftCipher(Cipher.DECRYPT_MODE, sharedSecret);
            MinecraftCipher encryptCipher = new MinecraftCipher(Cipher.ENCRYPT_MODE, sharedSecret);
            ctx.pipeline().addBefore("decoder", "decrypt", new CipherDecoder(decryptCipher));
            ctx.pipeline().addBefore("encoder", "encrypt", new CipherEncoder(encryptCipher));

            // Send LoginSuccess — 1.19 adds empty property array
            String uuid = ClassicToNettyTranslator.generateOfflineUuid(pendingUsername);
            ctx.writeAndFlush(new LoginSuccessPacketV759(uuid, pendingUsername));

            // Transition to PLAY state
            state = ConnectionState.PLAY;
            setCodecState(ctx, ConnectionState.PLAY);

            // Now proceed with join game
            handleJoinGame(ctx);

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
            String ip = null;
            if (ctx.channel().remoteAddress() instanceof java.net.InetSocketAddress) {
                ip = ((java.net.InetSocketAddress) ctx.channel().remoteAddress())
                        .getAddress().getHostAddress();
            }
            if (com.github.martinambrus.rdforward.server.api.BanManager.isPlayerBanned(pendingUsername)
                    || (ip != null && com.github.martinambrus.rdforward.server.api.BanManager.isIpBanned(ip))) {
                sendPlayDisconnect(ctx, "You are banned from this server");
                return;
            }
        }

        // Kick duplicate player
        if (!pendingUsername.trim().isEmpty()) {
            playerManager.kickDuplicatePlayer(pendingUsername.trim(), world);
        }

        // Register player
        player = playerManager.addPlayer(pendingUsername, ctx.channel(), clientVersion);
        if (player == null) {
            sendPlayDisconnect(ctx, "Server is full!");
            return;
        }

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
        if (isV762) {
            ctx.writeAndFlush(new JoinGamePacketV762(entityId, 1,
                    20, ChunkManager.DEFAULT_VIEW_DISTANCE, ChunkManager.DEFAULT_VIEW_DISTANCE));
        } else if (isV761) {
            // V761 reuses V760 JoinGame format; registry handles the ID shift (0x25 -> 0x24)
            ctx.writeAndFlush(new JoinGamePacketV760(entityId, 1,
                    20, ChunkManager.DEFAULT_VIEW_DISTANCE, ChunkManager.DEFAULT_VIEW_DISTANCE));
        } else if (isV760) {
            ctx.writeAndFlush(new JoinGamePacketV760(entityId, 1,
                    20, ChunkManager.DEFAULT_VIEW_DISTANCE, ChunkManager.DEFAULT_VIEW_DISTANCE));
        } else if (isV759) {
            ctx.writeAndFlush(new JoinGamePacketV759(entityId, 1,
                    20, ChunkManager.DEFAULT_VIEW_DISTANCE, ChunkManager.DEFAULT_VIEW_DISTANCE));
        } else if (isV758) {
            ctx.writeAndFlush(new JoinGamePacketV758(entityId, 1,
                    20, ChunkManager.DEFAULT_VIEW_DISTANCE, ChunkManager.DEFAULT_VIEW_DISTANCE));
        } else if (isV757) {
            ctx.writeAndFlush(new JoinGamePacketV757(entityId, 1,
                    20, ChunkManager.DEFAULT_VIEW_DISTANCE, ChunkManager.DEFAULT_VIEW_DISTANCE));
        } else if (isV755) {
            ctx.writeAndFlush(new JoinGamePacketV755(entityId, 1,
                    20, ChunkManager.DEFAULT_VIEW_DISTANCE));
        } else if (isV751) {
            ctx.writeAndFlush(new JoinGamePacketV751(entityId, 1,
                    20, ChunkManager.DEFAULT_VIEW_DISTANCE));
        } else if (isV735) {
            ctx.writeAndFlush(new JoinGamePacketV735(entityId, 1,
                    20, ChunkManager.DEFAULT_VIEW_DISTANCE));
        } else if (isV573) {
            ctx.writeAndFlush(new JoinGamePacketV573(entityId, 1, 0,
                    20, "default", ChunkManager.DEFAULT_VIEW_DISTANCE));
        } else if (isV477) {
            ctx.writeAndFlush(new JoinGamePacketV477(entityId, 1, 0,
                    20, "default", ChunkManager.DEFAULT_VIEW_DISTANCE));
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_9_1)) {
            ctx.writeAndFlush(new JoinGamePacketV108(entityId, 1, 0, 0,
                    20, "default"));
        } else if (isV47) {
            ctx.writeAndFlush(new JoinGamePacketV47(entityId, 1, 0, 0,
                    20, "default"));
        } else {
            ctx.writeAndFlush(new JoinGamePacket(entityId, 1, 0, 0,
                    20, "default"));
        }

        // 1.19.3+: Send UpdateEnabledFeatures right after JoinGame
        if (isV762 || isV761) {
            ctx.writeAndFlush(new UpdateEnabledFeaturesPacketV761());
        }

        // 1.14+: Send chunk cache radius (view distance) right after JoinGame
        if (isV477) {
            ctx.writeAndFlush(new SetChunkCacheRadiusPacketV477(ChunkManager.DEFAULT_VIEW_DISTANCE));
        }

        // 1.13+: Send mandatory DeclareCommands, UpdateRecipes, UpdateTags, Brand
        if (isV393) {
            ctx.writeAndFlush(new DeclareCommandsPacketV393());
            ctx.writeAndFlush(new UpdateRecipesPacketV393());
            // 1.14 added entity_types as a 4th tag category
            // 1.16 requires essential fluid tags (water/lava) or client crashes during rendering
            // 1.16.2 removed minecraft:furnace_materials from item tags
            ctx.writeAndFlush(isV762 ? new UpdateTagsPacketV762()
                    : isV761 ? new UpdateTagsPacketV761()
                    : isV760 ? new UpdateTagsPacketV759()
                    : isV759 ? new UpdateTagsPacketV759()
                    : isV758 ? new UpdateTagsPacketV758()
                    : isV757 ? new UpdateTagsPacketV757()
                    : isV755 ? new UpdateTagsPacketV755()
                    : isV751 ? new UpdateTagsPacketV751()
                    : isV735 ? new UpdateTagsPacketV735()
                    : isV477 ? new UpdateTagsPacketV477() : new UpdateTagsPacketV393());
            // Brand plugin message — 1.13 client NPEs without it
            byte[] brand = "RDForward".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] brandData = new byte[brand.length + 1];
            brandData[0] = (byte) brand.length;
            System.arraycopy(brand, 0, brandData, 1, brand.length);
            ctx.writeAndFlush(new NettyPluginMessageS2CPacketV393(
                    "minecraft:brand", brandData));
        }

        // Send PlayerAbilities (creative: invulnerable + allowFlying + creative = 0x0D)
        ctx.writeAndFlush(new PlayerAbilitiesPacketV73(0x0D, 0.05f, 0.1f));

        // Send Entity Properties for movement speed
        // 1.16+ uses namespaced snake_case attribute names
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

        // Determine spawn position
        Map<String, short[]> savedPositions = world.loadPlayerPositions();
        short[] savedPos = savedPositions.get(player.getUsername());

        double spawnX, spawnY, spawnZ;
        float spawnYaw = 0, spawnPitch = 0;
        if (savedPos != null) {
            spawnX = savedPos[0] / 32.0;
            spawnY = savedPos[1] / 32.0;
            spawnZ = savedPos[2] / 32.0;
            spawnYaw = (savedPos[3] & 0xFF) * 360.0f / 256.0f;
            spawnPitch = (savedPos[4] & 0xFF) * 360.0f / 256.0f;

            // Snap feet to nearest block surface
            double feetY = spawnY - PLAYER_EYE_HEIGHT;
            double fracFeet = feetY - Math.floor(feetY);
            if (fracFeet > 1.0 - (1.0 / 16.0)) {
                feetY = Math.ceil(feetY);
                spawnY = feetY + PLAYER_EYE_HEIGHT;
            } else if (fracFeet > 0 && fracFeet < (1.0 / 16.0)) {
                feetY = Math.floor(feetY);
                spawnY = feetY + PLAYER_EYE_HEIGHT;
            }

            int feetBlockX = (int) Math.floor(spawnX);
            int feetBlockY = (int) Math.floor(feetY);
            int feetBlockZ = (int) Math.floor(spawnZ);
            if (world.inBounds(feetBlockX, feetBlockY, feetBlockZ)
                    && (world.getBlock(feetBlockX, feetBlockY, feetBlockZ) != 0
                        || world.getBlock(feetBlockX, feetBlockY + 1, feetBlockZ) != 0)) {
                int[] safe = world.findSafePosition(feetBlockX, feetBlockY, feetBlockZ, 50);
                spawnX = safe[0] + 0.5;
                spawnY = safe[1] + PLAYER_EYE_HEIGHT;
                spawnZ = safe[2] + 0.5;
            }
        } else {
            int cx = world.getWidth() / 2;
            int cz = world.getDepth() / 2;
            int heuristicY = world.getHeight() * 2 / 3 + 1;
            int[] safe = world.findSafePosition(cx, heuristicY, cz, 50);
            spawnX = safe[0] + 0.5;
            spawnY = safe[1] + PLAYER_EYE_HEIGHT;
            spawnZ = safe[2] + 0.5;
        }

        player.updatePositionDouble(spawnX, spawnY, spawnZ, spawnYaw, spawnPitch);

        // Send spawn position
        int spawnBlockX = (int) Math.floor(spawnX);
        int spawnBlockY = (int) Math.floor(spawnY);
        int spawnBlockZ = (int) Math.floor(spawnZ);
        if (isV755) {
            ctx.writeAndFlush(new SpawnPositionPacketV755(spawnBlockX, spawnBlockY, spawnBlockZ));
        } else if (isV477) {
            ctx.writeAndFlush(new SpawnPositionPacketV477(spawnBlockX, spawnBlockY, spawnBlockZ));
        } else if (isV47) {
            ctx.writeAndFlush(new SpawnPositionPacketV47(spawnBlockX, spawnBlockY, spawnBlockZ));
        } else {
            ctx.writeAndFlush(new SpawnPositionPacket(spawnBlockX, spawnBlockY, spawnBlockZ));
        }

        // Send initial time update
        long timeOfDay = world.isTimeFrozen() ? -world.getWorldTime() : world.getWorldTime();
        ctx.writeAndFlush(new NettyTimeUpdatePacket(0, timeOfDay));

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
        float alphaSpawnYaw = (spawnYaw + 180.0f) % 360.0f;
        double clientY = isV47 ? spawnY - PLAYER_EYE_HEIGHT : spawnY;

        // 1.14+: Send chunk cache center before chunks.
        if (isV477) {
            ctx.writeAndFlush(new SetChunkCacheCenterPacketV477(
                    spawnBlockX >> 4, spawnBlockZ >> 4));
        }

        // Send initial chunks. addPlayer is deferred until after the login
        // sequence completes to prevent the tick loop from racing with us.
        chunkManager.sendInitialChunks(player, spawnBlockX, spawnBlockZ);

        // Send player position AFTER chunks. The client uses PlayerPosition as
        // the signal to exit the loading screen and start physics. Sending it
        // after chunks ensures terrain collision data is available, preventing
        // the player from briefly falling into the ground before chunks load.
        // SetChunkCacheCenter (above) tells the client which chunk to prioritize.
        if (isV762) {
            awaitingTeleportConfirm = true;
            ctx.writeAndFlush(new NettyPlayerPositionS2CPacketV762(
                    spawnX, clientY, spawnZ, alphaSpawnYaw, spawnPitch, ++nextTeleportId));
        } else if (isV755) {
            awaitingTeleportConfirm = true;
            ctx.writeAndFlush(new NettyPlayerPositionS2CPacketV755(
                    spawnX, clientY, spawnZ, alphaSpawnYaw, spawnPitch, ++nextTeleportId));
        } else if (isV109) {
            awaitingTeleportConfirm = true;
            ctx.writeAndFlush(new NettyPlayerPositionS2CPacketV109(
                    spawnX, clientY, spawnZ, alphaSpawnYaw, spawnPitch, ++nextTeleportId));
        } else if (isV47) {
            ctx.writeAndFlush(new NettyPlayerPositionS2CPacketV47(
                    spawnX, clientY, spawnZ, alphaSpawnYaw, spawnPitch));
        } else {
            ctx.writeAndFlush(new NettyPlayerPositionS2CPacket(
                    spawnX, spawnY, spawnZ, alphaSpawnYaw, spawnPitch, false));
        }

        // Send existing players — for 1.8+, PlayerListItem ADD must precede SpawnPlayer
        // because the client resolves player name from tab list by UUID.
        for (ConnectedPlayer existing : playerManager.getAllPlayers()) {
            if (existing != player) {
                int existingEntityId = existing.getPlayerId() + 1;
                int alphaYaw = (existing.getYaw() + 128) & 0xFF;
                int pitch = existing.getPitch() & 0xFF;
                String existingUuid = ClassicToNettyTranslator.generateOfflineUuid(existing.getUsername());

                if (isV109) {
                    // 1.9: PlayerListItem ADD before SpawnPlayer (double coords)
                    double ex = existing.getX() / 32.0;
                    double ey = (existing.getY() - PLAYER_EYE_HEIGHT_FIXED) / 32.0;
                    double ez = existing.getZ() / 32.0;
                    // 1.19.3+: PlayerInfoUpdate replaces PlayerListItem
                    // 1.19+: PlayerListItem ADD gains Optional ProfilePublicKey
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
                    // 1.15 removed entity metadata entirely from SpawnPlayer
                    // 1.14 used 0xFF metadata terminator (empty metadata)
                    if (isV573) {
                        ctx.writeAndFlush(new NettySpawnPlayerPacketV573(
                                existingEntityId, existingUuid, ex, ey, ez,
                                alphaYaw, pitch));
                    } else if (isV477) {
                        ctx.writeAndFlush(new NettySpawnPlayerPacketV477(
                                existingEntityId, existingUuid, ex, ey, ez,
                                alphaYaw, pitch));
                    } else {
                        ctx.writeAndFlush(new NettySpawnPlayerPacketV109(
                                existingEntityId, existingUuid, ex, ey, ez,
                                alphaYaw, pitch));
                    }
                } else if (isV47) {
                    // 1.8: PlayerListItem ADD before SpawnPlayer (fixed-point coords)
                    int existingFeetY = (int) existing.getY() - PLAYER_EYE_HEIGHT_FIXED;
                    ctx.writeAndFlush(NettyPlayerListItemPacketV47.addPlayer(
                            existingUuid, existing.getUsername(), 1, 0));
                    ctx.writeAndFlush(new NettySpawnPlayerPacketV47(
                            existingEntityId, existingUuid,
                            (int) existing.getX(), existingFeetY, (int) existing.getZ(),
                            alphaYaw, pitch, (short) 0));
                } else {
                    int existingFeetY = (int) existing.getY() - PLAYER_EYE_HEIGHT_FIXED;
                    Packet spawnPacket = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_7_6)
                            ? new NettySpawnPlayerPacketV5(
                                    existingEntityId, existingUuid, existing.getUsername(),
                                    (int) existing.getX(), existingFeetY, (int) existing.getZ(),
                                    alphaYaw, pitch, (short) 0)
                            : new NettySpawnPlayerPacket(
                                    existingEntityId, existingUuid, existing.getUsername(),
                                    (int) existing.getX(), existingFeetY, (int) existing.getZ(),
                                    alphaYaw, pitch, (short) 0);
                    ctx.writeAndFlush(spawnPacket);
                }
            }
        }

        // Configure translator with client version BEFORE broadcasts —
        // broadcastPlayerListAdd sends to ALL players including this one,
        // so the translator must know the version to emit the correct format.
        loginComplete = true;
        ClassicToNettyTranslator translator = ctx.pipeline().get(ClassicToNettyTranslator.class);
        if (translator != null) {
            translator.setClientVersion(clientVersion);
        }

        // Broadcast new player spawn to everyone else
        // PlayerListItem ADD is broadcast before spawn (handled by translator for v47 clients)
        playerManager.broadcastPlayerListAdd(player);
        playerManager.broadcastPlayerSpawn(player);

        // Initialize inventory adapter tracking
        InventoryAdapter adapter = playerManager.getInventoryAdapter();
        adapter.initPlayer(player.getUsername());

        // Give 1 cobblestone for right-click
        // v404 (1.13.2)+ uses boolean+VarInt slot format (also used by v477/1.14)
        boolean isV404 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_13_2);
        if (isV762) {
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
        adapter.setSlot(player.getUsername(), 36, BlockRegistry.COBBLESTONE, 1, 0);

        // Start KeepAlive heartbeat
        boolean isV340 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_12_2);
        keepAliveTask = ctx.executor().scheduleAtFixedRate(() -> {
            if (ctx.channel().isActive()) {
                if (isV340) {
                    ctx.writeAndFlush(new KeepAlivePacketV340(++keepAliveCounter));
                } else if (isV47) {
                    ctx.writeAndFlush(new KeepAlivePacketV47(++keepAliveCounter));
                } else {
                    ctx.writeAndFlush(new KeepAlivePacketV17(++keepAliveCounter));
                }
            }
        }, 10, 10, TimeUnit.SECONDS);

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

        System.out.println("Netty login complete: " + player.getUsername()
                + " (" + clientVersion.getDisplayName()
                + ", ID " + player.getPlayerId()
                + ", " + playerManager.getPlayerCount() + " online)");
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
                || packet instanceof HoldingChangePacketBeta
                || packet instanceof AnimationPacket
                || packet instanceof AnimationPacketV47
                || packet instanceof NettyEntityActionPacket
                || packet instanceof NettyEntityActionPacketV47
                || packet instanceof NettySteerVehiclePacket
                || packet instanceof NettySteerVehiclePacketV47
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
                || packet instanceof KeepAlivePacketV340
                || packet instanceof NoOpPacket) {
            // Silently accept
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
        if (awaitingTeleportConfirm) return;

        double eyeY = y + PLAYER_EYE_HEIGHT;

        // Fall below world — respawn
        if (y < -10) {
            respawnToSafePosition(ctx, yaw);
            return;
        }

        short fixedX = toFixedPoint(x);
        short fixedY = toFixedPoint(eyeY);
        short fixedZ = toFixedPoint(z);
        byte byteYaw = toByteRotation(yaw);
        byte bytePitch = toByteRotation(pitch);

        player.updatePositionDouble(x, eyeY, z, yaw, pitch);

        ServerEvents.PLAYER_MOVE.invoker().onPlayerMove(
                player.getUsername(), fixedX, fixedY, fixedZ, byteYaw, bytePitch);

        playerManager.broadcastPacketExcept(
                new PlayerTeleportPacket(player.getPlayerId(),
                        fixedX, fixedY, fixedZ, byteYaw & 0xFF, bytePitch & 0xFF),
                player);
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
            if (result == EventResult.CANCEL) return;

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
            if (result == EventResult.CANCEL) return;

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
            if (result == EventResult.CANCEL) return;

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
        if (result == EventResult.CANCEL) return;

        if (!world.setBlock(targetX, targetY, targetZ, worldBlockType)) return;
        chunkManager.setBlock(targetX, targetY, targetZ, worldBlockType);

        playerManager.broadcastPacketExcept(
                new SetBlockServerPacket(targetX, targetY, targetZ, worldBlockType & 0xFF), player);

        // Confirm block to this client
        sendBlockChange(ctx, targetX, targetY, targetZ, itemId & 0xFF, 0);

        // 1.19+: Send BlockChangedAck after placement
        if (packet instanceof NettyBlockPlacementPacketV759) {
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
        boolean isV762 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_19_4);
        boolean isV761 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_19_3);
        boolean isV760 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_19_1);
        boolean isV759 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_19);
        boolean isV735 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_16);
        boolean handled = CommandRegistry.dispatch(command, player.getUsername(), false,
                reply -> {
                    String json = "{\"text\":\"" + reply.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}";
                    if (isV762) {
                        player.sendPacket(new SystemChatPacketV760(json, false));
                    } else if (isV761) {
                        player.sendPacket(new SystemChatPacketV760(json, false));
                    } else if (isV760) {
                        player.sendPacket(new SystemChatPacketV760(json, false));
                    } else if (isV759) {
                        player.sendPacket(new SystemChatPacketV759(json, 1));
                    } else if (isV735) {
                        player.sendPacket(new NettyChatS2CPacketV735(json, (byte) 0, 0L, 0L));
                    } else {
                        player.sendPacket(new NettyChatS2CPacket(json));
                    }
                });
        if (!handled) {
            String json = "{\"text\":\"Unknown command: " + command.split("\\s+")[0] + "\"}";
            if (isV762) {
                player.sendPacket(new SystemChatPacketV760(json, false));
            } else if (isV761) {
                player.sendPacket(new SystemChatPacketV760(json, false));
            } else if (isV760) {
                player.sendPacket(new SystemChatPacketV760(json, false));
            } else if (isV759) {
                player.sendPacket(new SystemChatPacketV759(json, 1));
            } else if (isV735) {
                player.sendPacket(new NettyChatS2CPacketV735(json, (byte) 0, 0L, 0L));
            } else {
                player.sendPacket(new NettyChatS2CPacket(json));
            }
        }
    }

    private void handleRespawn(ChannelHandlerContext ctx) {
        if (player == null) return;
        respawnToSafePosition(ctx, player.getFloatYaw());
    }

    private void respawnToSafePosition(ChannelHandlerContext ctx, float yaw) {
        int cx = world.getWidth() / 2;
        int cz = world.getDepth() / 2;
        int heuristicY = world.getHeight() * 2 / 3 + 1;
        int[] safe = world.findSafePosition(cx, heuristicY, cz, 50);
        double spawnX = safe[0] + 0.5;
        double spawnFeetY = safe[1];
        double spawnEyeY = spawnFeetY + PLAYER_EYE_HEIGHT;
        double spawnZ = safe[2] + 0.5;
        player.updatePositionDouble(spawnX, spawnEyeY, spawnZ, yaw, 0);

        // S2C Y: 1.7.2 = eye-level; 1.8+ = feet-level.
        float alphaYaw = (yaw + 180.0f) % 360.0f;
        if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_19_4)) {
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
        if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_19)) {
            // V759, V760, and V761 share the same block state IDs
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
        String json = "{\"text\":\"" + reason.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}";
        ctx.writeAndFlush(new NettyDisconnectPacket(json))
                .addListener(io.netty.channel.ChannelFutureListener.CLOSE);
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
            System.out.println(player.getUsername() + " disconnected"
                    + " (" + (playerManager.getPlayerCount() - 1) + " online)");
            ServerEvents.PLAYER_LEAVE.invoker().onPlayerLeave(player.getUsername());
            playerManager.getInventoryAdapter().removePlayer(player.getUsername());
            world.rememberPlayerPosition(player);
            chunkManager.removePlayer(player);
            playerManager.broadcastPlayerListRemove(player);
            playerManager.broadcastChat((byte) 0, player.getUsername() + " left the game");
            playerManager.broadcastPlayerDespawn(player);
            playerManager.removePlayer(ctx.channel());
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("Netty connection error"
                + (player != null ? " (" + player.getUsername() + ")" : "")
                + ": " + cause.getMessage());
        ctx.close();
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
}
