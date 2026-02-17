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
        ProtocolVersion resolved = ProtocolVersion.fromNumber(pv, ProtocolVersion.Family.RELEASE);
        if (resolved != null) {
            clientVersion = resolved;
        }

        if (packet.getNextState() == 1) {
            // Status
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
        String versionName = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_7_6)
                ? "1.7.10" : "1.7.5";
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

            ctx.writeAndFlush(new NettyEncryptionRequestPacket(
                    "", rsaKeyPair.getPublic().getEncoded(), verifyToken));
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
            String uuid = ClassicToNettyTranslator.generateOfflineUuid(pendingUsername);
            ctx.writeAndFlush(new LoginSuccessPacket(uuid, pendingUsername));

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

        int entityId = player.getPlayerId() + 1;

        // Send JoinGame
        // maxPlayers=20 limits the tab list to a single compact column.
        // Using the actual MAX_PLAYERS (128) creates a huge multi-column grid.
        ctx.writeAndFlush(new JoinGamePacket(entityId, 1, 0, 0,
                20, "default"));

        // Send PlayerAbilities (creative: invulnerable + allowFlying + creative = 0x0D)
        ctx.writeAndFlush(new PlayerAbilitiesPacketV73(0x0D, 0.05f, 0.1f));

        // Send Entity Properties for movement speed
        ctx.writeAndFlush(new NettyEntityPropertiesPacket(entityId,
                "generic.movementSpeed", 0.10000000149011612));

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
        ctx.writeAndFlush(new SpawnPositionPacket(spawnBlockX, spawnBlockY, spawnBlockZ));

        // Send chunks BEFORE player position — the 1.7.2 client applies
        // gravity immediately on receiving position, so chunks must be
        // loaded first to prevent falling through unloaded terrain.
        chunkManager.addPlayer(player);
        chunkManager.sendInitialChunks(player, spawnBlockX, spawnBlockZ);

        // Send player position — 1.7.2 S2C Y is eye-level (posY), not feet.
        // The client does posY = Y, then feetY = posY - 1.62.
        float alphaSpawnYaw = (spawnYaw + 180.0f) % 360.0f;
        ctx.writeAndFlush(new NettyPlayerPositionS2CPacket(
                spawnX, spawnY, spawnZ, alphaSpawnYaw, spawnPitch, false));

        // Send existing players
        boolean useV5Spawn = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_7_6);
        for (ConnectedPlayer existing : playerManager.getAllPlayers()) {
            if (existing != player) {
                int existingEntityId = existing.getPlayerId() + 1;
                int existingFeetY = (int) existing.getY() - PLAYER_EYE_HEIGHT_FIXED;
                int alphaYaw = (existing.getYaw() + 128) & 0xFF;
                int pitch = existing.getPitch() & 0xFF;
                String existingUuid = ClassicToNettyTranslator.generateOfflineUuid(existing.getUsername());
                Packet spawnPacket = useV5Spawn
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

        // Broadcast new player spawn to everyone else
        playerManager.broadcastPlayerSpawn(player);

        loginComplete = true;

        // Configure translator with client version for version-aware packet translation
        ClassicToNettyTranslator translator = ctx.pipeline().get(ClassicToNettyTranslator.class);
        if (translator != null) {
            translator.setClientVersion(clientVersion);
        }

        // Give 1 cobblestone for right-click
        ctx.writeAndFlush(new NettySetSlotPacket(0, 36, BlockRegistry.COBBLESTONE, 1, 0));

        // Start KeepAlive heartbeat
        keepAliveTask = ctx.executor().scheduleAtFixedRate(() -> {
            if (ctx.channel().isActive()) {
                ctx.writeAndFlush(new KeepAlivePacketV17(++keepAliveCounter));
            }
        }, 10, 10, TimeUnit.SECONDS);

        // Send tab list entries
        for (ConnectedPlayer existing : playerManager.getAllPlayers()) {
            if (existing != player) {
                ctx.writeAndFlush(new NettyPlayerListItemPacket(existing.getUsername(), true, 0));
            }
        }
        playerManager.broadcastPlayerListAdd(player);

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

        if (packet instanceof PlayerPositionAndLookC2SPacket) {
            handlePositionAndLook(ctx, (PlayerPositionAndLookC2SPacket) packet);
        } else if (packet instanceof PlayerPositionPacket) {
            handlePosition(ctx, (PlayerPositionPacket) packet);
        } else if (packet instanceof PlayerLookPacket) {
            handleLook(ctx, (PlayerLookPacket) packet);
        } else if (packet instanceof PlayerOnGroundPacket) {
            // No-op
        } else if (packet instanceof PlayerDiggingPacket) {
            handleDigging(ctx, (PlayerDiggingPacket) packet);
        } else if (packet instanceof BlockPlacementData) {
            handleBlockPlacement(ctx, (BlockPlacementData) packet);
        } else if (packet instanceof NettyChatC2SPacket) {
            handleChat(ctx, (NettyChatC2SPacket) packet);
        } else if (packet instanceof ClientCommandPacket) {
            ClientCommandPacket cmd = (ClientCommandPacket) packet;
            if (cmd.getActionId() == ClientCommandPacket.RESPAWN) {
                handleRespawn(ctx);
            }
        } else if (packet instanceof KeepAlivePacketV17
                || packet instanceof HoldingChangePacketBeta
                || packet instanceof AnimationPacket
                || packet instanceof NettyEntityActionPacket
                || packet instanceof NettySteerVehiclePacket
                || packet instanceof CloseWindowPacket
                || packet instanceof NettyWindowClickPacket
                || packet instanceof ConfirmTransactionPacket
                || packet instanceof NettyCreativeSlotPacket
                || packet instanceof EnchantItemPacket
                || packet instanceof NettyUpdateSignPacket
                || packet instanceof PlayerAbilitiesPacketV73
                || packet instanceof NettyTabCompletePacket
                || packet instanceof NettyClientSettingsPacket
                || packet instanceof NettyPluginMessagePacket
                || packet instanceof NettyUseEntityPacket) {
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

    private void handleChat(ChannelHandlerContext ctx, NettyChatC2SPacket packet) {
        if (player == null) return;
        String message = packet.getMessage();

        if (message.startsWith("/")) {
            String command = message.substring(1);
            boolean handled = CommandRegistry.dispatch(command, player.getUsername(), false,
                    reply -> {
                        String json = "{\"text\":\"" + reply.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}";
                        player.sendPacket(new NettyChatS2CPacket(json));
                    });
            if (!handled) {
                String json = "{\"text\":\"Unknown command: " + command.split("\\s+")[0] + "\"}";
                player.sendPacket(new NettyChatS2CPacket(json));
            }
            return;
        }

        EventResult result = ServerEvents.CHAT.invoker().onChat(player.getUsername(), message);
        if (result == EventResult.CANCEL) return;

        System.out.println("[Chat] " + player.getUsername() + ": " + message);
        playerManager.broadcastChat(player.getPlayerId(), player.getUsername() + ": " + message);
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

        float alphaYaw = (yaw + 180.0f) % 360.0f;
        // S2C Y is eye-level for 1.7.2
        ctx.writeAndFlush(new NettyPlayerPositionS2CPacket(
                spawnX, spawnEyeY, spawnZ, alphaYaw, 0, false));

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
        ctx.writeAndFlush(new NettyBlockChangePacket(x, y, z, blockType, metadata));
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
