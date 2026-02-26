package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.codec.RawPacketDecoder;
import com.github.martinambrus.rdforward.protocol.codec.RawPacketEncoder;
import com.github.martinambrus.rdforward.protocol.crypto.CipherDecoder;
import com.github.martinambrus.rdforward.protocol.crypto.CipherEncoder;
import com.github.martinambrus.rdforward.protocol.crypto.MinecraftCipher;
import com.github.martinambrus.rdforward.protocol.event.EventResult;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.alpha.*;
import com.github.martinambrus.rdforward.protocol.packet.classic.PlayerTeleportPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockServerPacket;
import com.github.martinambrus.rdforward.server.api.CommandRegistry;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Arrays;
import java.util.List;
import com.github.martinambrus.rdforward.server.event.ServerEvents;
import com.github.martinambrus.rdforward.world.BlockRegistry;
import io.netty.channel.ChannelHandlerContext;
import java.util.concurrent.TimeUnit;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import javax.crypto.Cipher;

import java.util.Map;

/**
 * Handles individual Alpha client connections on the server side.
 *
 * Manages the full Alpha client lifecycle:
 * 1. Handshake (0x02) -> respond with HandshakeS2C("-")
 * 2. Login (0x01) -> register player, send LoginS2C, chunks, position
 * 3. Gameplay packet routing (position, digging, placement, chat)
 * 4. Disconnect/cleanup
 *
 * Incoming Alpha packets are handled directly. Outgoing Classic packets
 * (from PlayerManager broadcasts) are translated by ClassicToAlphaTranslator
 * in the outbound pipeline.
 */
public class AlphaConnectionHandler extends SimpleChannelInboundHandler<Packet> {

    private static final int LOGIN_TIMEOUT_SECONDS = 5;

    /**
     * Player eye height in blocks. Must use (double) 1.62f to match the MC Alpha
     * client's float yOffset precision. The client computes feet = posY - (double)(1.62f).
     * Using the double literal 1.62 causes a ~5e-9 precision mismatch that places
     * feet fractionally below block boundaries, defeating collision detection.
     */
    private static final double PLAYER_EYE_HEIGHT = (double) 1.62f;
    /** Same in fixed-point units, rounded up to avoid sub-block clipping. */
    static final int PLAYER_EYE_HEIGHT_FIXED = (int) Math.ceil(PLAYER_EYE_HEIGHT * 32);

    private final ProtocolVersion serverVersion;
    private final ServerWorld world;
    private final PlayerManager playerManager;
    private final ChunkManager chunkManager;

    /**
     * Delay (ms) after the last block placement before replenishing cobblestone.
     * Batching replenishment avoids sending AddToInventory packets during rapid
     * placement, which can confuse the Alpha client's input handling.
     */
    private static final long REPLENISH_DELAY_MS = 1000;

    private String pendingUsername;
    private boolean detectedString16 = false;
    private ProtocolVersion clientVersion;
    private ConnectedPlayer player;
    private boolean loginComplete = false;

    // v39+ encryption state
    private KeyPair rsaKeyPair;
    private byte[] verifyToken;
    private boolean awaitingEncryptionResponse = false;
    private boolean awaitingClientStatus = false;

    /**
     * Server-side estimate of the player's cobblestone count. Corrected by
     * inventory sync packets (0x05) from the client. Used to calculate how
     * many to give back when replenishing (top up to 64).
     */
    private int trackedCobblestone = 0;
    private java.util.concurrent.ScheduledFuture<?> replenishTask;
    private java.util.concurrent.ScheduledFuture<?> keepAliveTask;
    private int keepAliveCounter = 0;

    /**
     * Tracks the feet Y where the current fall started. Used for pre-rewrite
     * clients (v13/v14) that lack UpdateHealthPacket — when the fall exceeds
     * 10 blocks, the server teleports the player to the ground to prevent
     * lethal fall damage.
     */
    private double fallStartFeetY = Double.NaN;

    public AlphaConnectionHandler(ProtocolVersion serverVersion, ServerWorld world,
                                  PlayerManager playerManager, ChunkManager chunkManager) {
        this.serverVersion = serverVersion;
        this.world = world;
        this.playerManager = playerManager;
        this.chunkManager = chunkManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        if (!loginComplete) {
            if (packet instanceof HandshakeC2SPacket) {
                handleHandshake(ctx, (HandshakeC2SPacket) packet);
            } else if (packet instanceof EncryptionKeyResponsePacket) {
                handleEncryptionResponse(ctx, (EncryptionKeyResponsePacket) packet);
            } else if (packet instanceof ClientStatusPacket) {
                handleClientStatus(ctx, (ClientStatusPacket) packet);
            } else if (packet instanceof LoginC2SPacket) {
                // Pre-rewrite clients (v13) skip Handshake and send Login directly.
                // Extract username from the Login packet if no Handshake was received.
                LoginC2SPacket loginPacket = (LoginC2SPacket) packet;
                if (pendingUsername == null) {
                    pendingUsername = loginPacket.getUsername();
                }
                handleLogin(ctx, loginPacket.getProtocolVersion());
            }
            // Ignore other packets before login completes
            return;
        }

        // Route gameplay packets
        if (packet instanceof PlayerPositionAndLookC2SPacket) {
            handlePositionAndLook(ctx, (PlayerPositionAndLookC2SPacket) packet);
        } else if (packet instanceof PlayerPositionPacket) {
            handlePosition(ctx, (PlayerPositionPacket) packet);
        } else if (packet instanceof PlayerLookPacket) {
            handleLook(ctx, (PlayerLookPacket) packet);
        } else if (packet instanceof PlayerOnGroundPacket) {
            // No-op: no position data, just onGround flag
        } else if (packet instanceof PlayerDiggingPacket) {
            handleDigging(ctx, (PlayerDiggingPacket) packet);
        } else if (packet instanceof BlockPlacementData) {
            handleBlockPlacement(ctx, (BlockPlacementData) packet);
        } else if (packet instanceof ChatPacket) {
            handleChat(ctx, (ChatPacket) packet);
        } else if (packet instanceof HoldingChangePacket || packet instanceof HoldingChangePacketBeta) {
            // Silently accept (hotbar slot change)
        } else if (packet instanceof AnimationPacket) {
            // Silently accept (arm swing animation)
        } else if (packet instanceof PlayerInventoryPacket) {
            handleInventorySync((PlayerInventoryPacket) packet);
        } else if (packet instanceof PickupSpawnPacket pickupSpawn) {
            // Only replenish when the dropped item is cobblestone. Throwing
            // other items (e.g. porkchops on v13/v14) should not give cobble.
            if (pickupSpawn.getItemId() == BlockRegistry.COBBLESTONE) {
                int droppedCount = pickupSpawn.getCount() & 0xFF;
                if (droppedCount < 1) droppedCount = 1;
                giveItem(ctx, BlockRegistry.COBBLESTONE, droppedCount);
            }
        } else if (packet instanceof RespawnPacket) {
            handleRespawn(ctx);
        } else if (packet instanceof ClientStatusPacket) {
            // v39+ respawn uses ClientStatusPacket with payload=1
            ClientStatusPacket cs = (ClientStatusPacket) packet;
            if (cs.getPayload() == ClientStatusPacket.RESPAWN) {
                handleRespawn(ctx);
            }
        } else if (packet instanceof WindowClickPacket) {
            handleWindowClick(ctx, (WindowClickPacket) packet);
        } else if (packet instanceof CloseWindowPacket) {
            handleCloseWindow(ctx);
            // v17+ creative mode manages inventory natively, no reset needed.
            if (clientVersion.isAtLeast(ProtocolVersion.BETA_1_0)
                    && !clientVersion.isAtLeast(ProtocolVersion.BETA_1_8)) {
                resetInventory(ctx);
            }
        } else if (packet instanceof CreativeSlotPacket
                || packet instanceof CreativeSlotPacketV22
                || packet instanceof CreativeSlotPacketV39
                || packet instanceof PlayerAbilitiesPacket
                || packet instanceof PlayerAbilitiesPacketV39
                || packet instanceof PlayerAbilitiesPacketV73
                || packet instanceof EnchantItemPacket) {
            // Creative mode actions — silently accept
        } else if (packet instanceof UseEntityPacket
                || packet instanceof ConfirmTransactionPacket
                || packet instanceof UpdateSignPacket
                || packet instanceof EntityEquipmentPacket
                || packet instanceof EntityActionPacket
                || packet instanceof EntityActionPacketV73
                || packet instanceof InputPacket
                || packet instanceof CustomPayloadPacket
                || packet instanceof ClientSettingsPacket
                || packet instanceof ClientSettingsPacketV47
                || packet instanceof TabCompletePacket) {
            // Silently accept (not yet implemented server-side)
        } else if (packet instanceof KeepAlivePacket) {
            // Silently accept
        } else if (packet instanceof DisconnectPacket) {
            ctx.close();
        }
    }

    private void handleHandshake(ChannelHandlerContext ctx, HandshakeC2SPacket packet) {
        pendingUsername = packet.getUsername();

        if (packet.isV39Format()) {
            // v39+ Handshake: protocol version + username + hostname + port.
            // Username is already extracted, no ";host:port" suffix stripping needed.
            detectedString16 = true;

            // Configure codec for String16
            RawPacketDecoder decoder = ctx.pipeline().get(RawPacketDecoder.class);
            if (decoder != null) {
                decoder.setUseString16(true);
            }
            RawPacketEncoder encoder = ctx.pipeline().get(RawPacketEncoder.class);
            if (encoder != null) {
                encoder.setUseString16(true);
            }

            // Determine client version immediately from handshake protocol version
            clientVersion = ProtocolVersion.fromNumber(packet.getProtocolVersion(),
                    ProtocolVersion.Family.RELEASE);
            if (clientVersion == null) {
                String[] messages = buildUnsupportedVersionMessages(packet.getProtocolVersion());
                System.out.println("Rejected client"
                        + (pendingUsername != null ? " (" + pendingUsername + ")" : "")
                        + ": " + messages[1]);
                ctx.writeAndFlush(new DisconnectPacket(messages[0]));
                ctx.close();
                return;
            }

            // Update decoder protocol version for v39 packet registry
            if (decoder != null) {
                decoder.setProtocolVersion(clientVersion);
            }

            // Generate RSA keypair and verify token for encryption handshake
            try {
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                keyGen.initialize(1024);
                rsaKeyPair = keyGen.generateKeyPair();
                verifyToken = new byte[4];
                new java.security.SecureRandom().nextBytes(verifyToken);

                // Send Encryption Key Request (no S2C Handshake for v39+).
                // serverId="-" signals offline mode: the client checks
                // !"-".equals(serverId) to decide whether to authenticate
                // with Mojang's session server. Sending "" would trigger auth.
                ctx.writeAndFlush(new EncryptionKeyRequestPacket(
                        "-", rsaKeyPair.getPublic().getEncoded(), verifyToken));
                awaitingEncryptionResponse = true;
            } catch (Exception e) {
                System.err.println("Failed to generate RSA keypair: " + e.getMessage());
                ctx.writeAndFlush(new DisconnectPacket("Encryption error"));
                ctx.close();
            }
            return;
        }

        // Pre-v39 Handshake: single string field.
        // Beta 1.8+ sends "username;host:port" in the Handshake. Strip the
        // ";host:port" suffix to get just the username.
        int semicolon = pendingUsername.indexOf(';');
        if (semicolon >= 0) {
            pendingUsername = pendingUsername.substring(0, semicolon);
        }

        // Beta 1.5+ uses String16 encoding instead of Java Modified UTF-8.
        // The Handshake auto-detects the format; configure codec for all
        // subsequent packets on this channel. Also track the detection so
        // handleLogin can use Family.BETA for version resolution (v13/v14
        // clash between Alpha and Beta).
        if (packet.isDetectedString16()) {
            detectedString16 = true;
            RawPacketDecoder decoder = ctx.pipeline().get(RawPacketDecoder.class);
            if (decoder != null) {
                decoder.setUseString16(true);
            }
            RawPacketEncoder encoder = ctx.pipeline().get(RawPacketEncoder.class);
            if (encoder != null) {
                encoder.setUseString16(true);
            }
        }

        // Respond with offline-mode hash
        ctx.writeAndFlush(new HandshakeS2CPacket("-"));
    }

    private void handleEncryptionResponse(ChannelHandlerContext ctx, EncryptionKeyResponsePacket packet) {
        if (!awaitingEncryptionResponse || rsaKeyPair == null) {
            ctx.writeAndFlush(new DisconnectPacket("Unexpected encryption response"));
            ctx.close();
            return;
        }
        awaitingEncryptionResponse = false;

        try {
            // Decrypt shared secret and verify token using RSA private key
            Cipher rsaCipher = Cipher.getInstance("RSA");
            rsaCipher.init(Cipher.DECRYPT_MODE, rsaKeyPair.getPrivate());
            byte[] sharedSecret = rsaCipher.doFinal(packet.getData1());

            rsaCipher.init(Cipher.DECRYPT_MODE, rsaKeyPair.getPrivate());
            byte[] decryptedToken = rsaCipher.doFinal(packet.getData2());

            // Verify the token matches
            if (!Arrays.equals(decryptedToken, verifyToken)) {
                System.err.println("Encryption verify token mismatch for " + pendingUsername);
                ctx.writeAndFlush(new DisconnectPacket("Encryption verification failed"));
                ctx.close();
                return;
            }

            // Send empty EncryptionKeyResponse to signal "enable encryption now"
            ctx.writeAndFlush(new EncryptionKeyResponsePacket(new byte[0], new byte[0]))
                    .addListener(future -> {
                if (!future.isSuccess()) {
                    ctx.close();
                    return;
                }
                // Install cipher handlers in the pipeline
                MinecraftCipher decryptCipher = new MinecraftCipher(Cipher.DECRYPT_MODE, sharedSecret);
                MinecraftCipher encryptCipher = new MinecraftCipher(Cipher.ENCRYPT_MODE, sharedSecret);
                ctx.pipeline().addBefore("decoder", "decrypt", new CipherDecoder(decryptCipher));
                ctx.pipeline().addBefore("encoder", "encrypt", new CipherEncoder(encryptCipher));

                awaitingClientStatus = true;
            });
        } catch (Exception e) {
            System.err.println("Encryption handshake failed for " + pendingUsername + ": " + e.getMessage());
            ctx.writeAndFlush(new DisconnectPacket("Encryption error"));
            ctx.close();
        }
    }

    private void handleClientStatus(ChannelHandlerContext ctx, ClientStatusPacket packet) {
        if (packet.getPayload() == ClientStatusPacket.INITIAL_SPAWN) {
            if (!awaitingClientStatus) {
                ctx.writeAndFlush(new DisconnectPacket("Unexpected client status"));
                ctx.close();
                return;
            }
            awaitingClientStatus = false;
            // clientVersion was already set in handleHandshake for v39+
            handleLogin(ctx, clientVersion.getVersionNumber());
        } else if (packet.getPayload() == ClientStatusPacket.RESPAWN) {
            handleRespawn(ctx);
        }
    }

    private void handleLogin(ChannelHandlerContext ctx, int loginProtocolVersion) {
        if (pendingUsername == null) {
            ctx.writeAndFlush(new DisconnectPacket("Handshake not received"));
            ctx.close();
            return;
        }

        // For v39+ clients, clientVersion was already set in handleHandshake.
        // For pre-v39 clients, determine protocol version from the login packet.
        if (clientVersion == null) {
            // String16 detection from Handshake disambiguates version clashes:
            // v13 is shared by Alpha 1.0.15 and Beta 1.6.1-1.7, v14 by Alpha
            // 1.0.16 and Beta 1.7.2-1.7.3. String16 means Beta 1.5+ for certain.
            if (detectedString16) {
                clientVersion = ProtocolVersion.fromNumber(loginProtocolVersion,
                        ProtocolVersion.Family.BETA, ProtocolVersion.Family.RELEASE);
            } else {
                clientVersion = ProtocolVersion.fromNumber(loginProtocolVersion,
                        ProtocolVersion.Family.ALPHA, ProtocolVersion.Family.BETA);
            }
            if (clientVersion == null) {
                String[] messages = buildUnsupportedVersionMessages(loginProtocolVersion);
                System.out.println("Rejected client"
                        + (pendingUsername != null ? " (" + pendingUsername + ")" : "")
                        + ": " + messages[1]);
                ctx.writeAndFlush(new DisconnectPacket(messages[0]));
                ctx.close();
                return;
            }

            // Update decoder's protocol version if needed
            RawPacketDecoder decoder = ctx.pipeline().get(RawPacketDecoder.class);
            if (decoder != null) {
                decoder.setProtocolVersion(clientVersion);
            }
        }

        // Beta 1.8+ uses KeepAlivePacketV17 (with int ID). Tell the translator
        // to drop tick-loop PingPackets — zero-payload KeepAlive would misalign
        // the stream (client reads 4 extra bytes as keepAliveId).
        if (clientVersion.isAtLeast(ProtocolVersion.BETA_1_8)) {
            ClassicToAlphaTranslator translator = ctx.pipeline().get(ClassicToAlphaTranslator.class);
            if (translator != null) {
                translator.setDropPing(true);
                translator.setClientVersion(clientVersion);
            }
        }

        // Pre-1.2.0 Alpha clients have a broken chunk-distance comparator in
        // RenderGlobal that violates Java 7+'s TimSort transitivity contract.
        // On first connect (no saved position), kick with the required JVM flag
        // and save a default spawn position so they're recognized next time.
        if (!clientVersion.isAtLeast(ProtocolVersion.ALPHA_1_2_0)) {
            Map<String, short[]> savedPositions = world.loadPlayerPositions();
            if (!savedPositions.containsKey(pendingUsername.trim())) {
                System.out.println("Rejected " + pendingUsername
                        + ": " + clientVersion.getDisplayName() + " requires JVM flags (first connect)");
                // Save default spawn so they're allowed through next time
                world.savePlayerPosition(pendingUsername.trim());
                ctx.writeAndFlush(new DisconnectPacket(
                        "Add this Java flag to play: -Djava.util.Arrays.useLegacyMergeSort=true"));
                ctx.close();
                return;
            }
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
                ctx.writeAndFlush(new DisconnectPacket("You are banned from this server"));
                ctx.close();
                return;
            }
        }

        // If a non-blank username is already online, kick the old connection
        if (pendingUsername != null && !pendingUsername.trim().isEmpty()) {
            playerManager.kickDuplicatePlayer(pendingUsername.trim(), world);
        }

        // Register the player
        player = playerManager.addPlayer(pendingUsername, ctx.channel(), clientVersion);
        if (player == null) {
            ctx.writeAndFlush(new DisconnectPacket("Server is full!"));
            ctx.close();
            return;
        }

        // Entity ID: playerId + 1 (entity 0 is sometimes special in Alpha)
        int entityId = player.getPlayerId() + 1;

        // Send LoginS2C (format varies by version)
        if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_3_1)) {
            // Release 1.3.1+: no empty username, gameMode/dimension as byte.
            ctx.writeAndFlush(new LoginS2CPacketV39(entityId, "default", 1,
                    0, (byte) 0, (byte) 0, (byte) 20));
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_2_1)) {
            // Release 1.2.1+: no seed, int dimension, levelType remains.
            ctx.writeAndFlush(new LoginS2CPacketV28(entityId, "default", 1,
                    0, (byte) 0, (byte) 0, (byte) 20));
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_1)) {
            // Release 1.1+ added levelType String16 between seed and gameMode.
            ctx.writeAndFlush(new LoginS2CPacketV23(entityId, 0L, "default", 1,
                    (byte) 0, (byte) 0, (byte) 128, (byte) 20));
        } else if (clientVersion.isAtLeast(ProtocolVersion.BETA_1_8)) {
            // Beta 1.8+ has native creative mode: gameMode=1 enables instant break,
            // creative inventory, flying, and no fall damage on the client.
            ctx.writeAndFlush(new LoginS2CPacketV17(entityId, 0L, 1,
                    (byte) 0, (byte) 0, (byte) 128, (byte) 20));
        } else if (clientVersion.isAtLeast(ProtocolVersion.ALPHA_1_2_0)) {
            ctx.writeAndFlush(new LoginS2CPacket(entityId, 0L, (byte) 0));
        } else {
            ctx.writeAndFlush(new LoginS2CPacketV2(entityId));
        }

        // v39+: send PlayerAbilities after login (flags: invulnerable+allowFlying+creative).
        // Bit 1 (flying) is deliberately NOT set so the player spawns on the ground
        // instead of mid-air. Players can still fly by double-tapping jump.
        if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_6_1)) {
            ctx.writeAndFlush(new PlayerAbilitiesPacketV73(0x0D, 0.05f, 0.1f));
            // v73+: client relies on Entity Properties for movement speed.
            // Without this, it defaults to EntityLivingBase's 0.7 instead of 0.1.
            // v74+ added modifier list (short count + modifiers) per property.
            if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_6_2)) {
                ctx.writeAndFlush(new EntityPropertiesPacketV74(entityId,
                        "generic.movementSpeed", 0.10000000149011612));
            } else {
                ctx.writeAndFlush(new EntityPropertiesPacket(entityId,
                        "generic.movementSpeed", 0.10000000149011612));
            }
        } else if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_3_1)) {
            ctx.writeAndFlush(new PlayerAbilitiesPacketV39(0x0D, 12, 25));
        }

        // Determine spawn position
        Map<String, short[]> savedPositions = world.loadPlayerPositions();
        short[] savedPos = savedPositions.get(player.getUsername());

        double spawnX, spawnY, spawnZ;
        float spawnYaw = 0, spawnPitch = 0;
        if (savedPos != null) {
            // Convert fixed-point back to double
            spawnX = savedPos[0] / 32.0;
            spawnY = savedPos[1] / 32.0;
            spawnZ = savedPos[2] / 32.0;
            spawnYaw = (savedPos[3] & 0xFF) * 360.0f / 256.0f;
            spawnPitch = (savedPos[4] & 0xFF) * 360.0f / 256.0f;

            // Snap feet to nearest block surface if within fixed-point tolerance.
            // Eye-level Y is saved as fixed-point (round(eyeY*32)/32), so the
            // round-trip through fixed-point + eye height subtraction can shift
            // feet up to ~0.02 blocks from their original integer position.
            // Snap UP if close to the next integer (prevents sinking into block).
            // Snap DOWN if slightly above an integer (prevents creative-mode
            // clients from detecting "not on ground" and enabling flying).
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
                System.out.println("Saved position was inside blocks, relocated to "
                        + safe[0] + "," + safe[1] + "," + safe[2]);
            } else {
                System.out.println("Restored position for " + player.getUsername());
            }
        } else {
            // Default: center of world, find safe position on terrain.
            // Internal Y convention is eye-level (feet + 1.62) to match Classic.
            int cx = world.getWidth() / 2;
            int cz = world.getDepth() / 2;
            int heuristicY = world.getHeight() * 2 / 3 + 1;
            int[] safe = world.findSafePosition(cx, heuristicY, cz, 50);
            spawnX = safe[0] + 0.5;
            spawnY = safe[1] + PLAYER_EYE_HEIGHT; // eye-level
            spawnZ = safe[2] + 0.5;
        }

        // Update player position — internal convention is eye-level Y
        player.updatePositionDouble(spawnX, spawnY, spawnZ, spawnYaw, spawnPitch);

        // Send spawn position (block coords for compass). SpawnPosition (0x06) was
        // added in Alpha 1.1.0 (v2) alongside the compass item. v1 clients don't
        // have this packet and throw "Bad packet id 6" if they receive it.
        int spawnBlockX = (int) Math.floor(spawnX);
        int spawnBlockY = (int) Math.floor(spawnY);
        int spawnBlockZ = (int) Math.floor(spawnZ);
        if (clientVersion.isAtLeast(ProtocolVersion.ALPHA_1_1_0)) {
            ctx.writeAndFlush(new SpawnPositionPacket(spawnBlockX, spawnBlockY, spawnBlockZ));
        }

        // Send player position and look BEFORE chunks.
        // The Alpha 1.1.x client's RenderGlobal chunk comparator violates
        // TimSort's transitivity contract (Java 7+). Sending position first
        // ensures the client knows its location when the first render frame
        // sorts chunks by distance, avoiding the pathological case.
        //
        // spawnY is eye-level (internal convention). Alpha S2C needs posY and feet.
        double feetY = spawnY - PLAYER_EYE_HEIGHT;
        double posY = feetY + PLAYER_EYE_HEIGHT;

        // S2C y = posY (eyes), stance = feet. The client sets posY from y
        // and computes BB.minY (feet) = posY - (double)(1.62f).
        // spawnYaw is Classic convention; Alpha client expects Alpha (0=South)
        float alphaSpawnYaw = (spawnYaw + 180.0f) % 360.0f;
        ctx.writeAndFlush(new PlayerPositionAndLookS2CPacket(
                spawnX, posY, feetY, spawnZ, alphaSpawnYaw, spawnPitch, true));

        // Initialize fall tracking for pre-rewrite clients
        fallStartFeetY = feetY;

        // Register for chunk tracking and send initial chunks.
        chunkManager.addPlayer(player);
        chunkManager.sendInitialChunks(player, spawnBlockX, spawnBlockZ);

        // Send existing players to this client (as Alpha spawn packets).
        // Internal Y is eye-level; Alpha SpawnPlayerPacket expects feet Y.
        for (ConnectedPlayer existing : playerManager.getAllPlayers()) {
            if (existing != player) {
                int existingEntityId = existing.getPlayerId() + 1;
                int existingFeetY = (int) existing.getY() - PLAYER_EYE_HEIGHT_FIXED;
                int alphaYaw = (existing.getYaw() + 128) & 0xFF;
                int pitch = existing.getPitch() & 0xFF;
                if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_3_1)) {
                    ctx.writeAndFlush(new SpawnPlayerPacketV39(
                            existingEntityId, existing.getUsername(),
                            (int) existing.getX(), existingFeetY, (int) existing.getZ(),
                            alphaYaw, pitch, (short) 0));
                } else {
                    ctx.writeAndFlush(new com.github.martinambrus.rdforward.protocol.packet.alpha.SpawnPlayerPacket(
                            existingEntityId, existing.getUsername(),
                            (int) existing.getX(), existingFeetY, (int) existing.getZ(),
                            alphaYaw, pitch, (short) 0));
                }
            }
        }

        // Tab list ADD must precede SpawnPlayer for 1.8+ clients (they resolve player
        // name from tab list by UUID and silently drop SpawnPlayer otherwise).
        playerManager.broadcastPlayerListAdd(player);

        // Broadcast new player's spawn to everyone else (as Classic packet, translator converts)
        playerManager.broadcastPlayerSpawn(player);

        loginComplete = true;

        // Initialize inventory adapter tracking
        InventoryAdapter adapter = playerManager.getInventoryAdapter();
        adapter.initPlayer(player.getUsername());

        // Beta 1.8+ has native creative mode — no cobblestone replenishment needed.
        // Give 1 cobblestone so right-click works immediately without opening
        // the creative inventory first.
        // Earlier versions need survival-mode hacks for block placement.
        if (clientVersion.isAtLeast(ProtocolVersion.BETA_1_8)) {
            giveItem(ctx, BlockRegistry.COBBLESTONE, 1);
            adapter.setSlot(player.getUsername(), 36, BlockRegistry.COBBLESTONE, 1, 0);
        } else {
            // Give the player cobblestone so they can place blocks.
            giveItem(ctx, BlockRegistry.COBBLESTONE, 64);
            trackedCobblestone = 64;
            adapter.setSlot(player.getUsername(), 36, BlockRegistry.COBBLESTONE, 64, 0);

            // Pre-rewrite clients (v13/v14) can't receive UpdateHealthPacket, so
            // fall damage is handled client-side with no server override. Give them
            // cooked pork chops (item 320, heals 4 hearts each, max stack 1) so
            // they can eat to recover from any residual fall damage.
            if (!clientVersion.isAtLeast(ProtocolVersion.ALPHA_1_0_17)) {
                for (int i = 0; i < 35; i++) {
                    giveItem(ctx, 320, 1);
                }
            }
        }

        // Send initial time update (Alpha 1.2.0+ supports day/night cycle)
        if (clientVersion.isAtLeast(ProtocolVersion.ALPHA_1_2_0)) {
            long timeOfDay = world.isTimeFrozen() ? -world.getWorldTime() : world.getWorldTime();
            ctx.writeAndFlush(new TimeUpdatePacket(timeOfDay));
        }

        // Send initial weather state (Beta 1.5+ supports weather)
        if (clientVersion.isAtLeast(ProtocolVersion.BETA_1_5)
                && world.getWeather() != ServerWorld.WeatherState.CLEAR) {
            ctx.writeAndFlush(new ChangeGameStatePacket(ChangeGameStatePacket.BEGIN_RAIN, 0));
        }

        // Beta 1.8+ requires periodic KeepAlive with int ID (client times out otherwise)
        if (clientVersion.isAtLeast(ProtocolVersion.BETA_1_8)) {
            keepAliveTask = ctx.executor().scheduleAtFixedRate(() -> {
                if (ctx.channel().isActive()) {
                    ctx.writeAndFlush(new KeepAlivePacketV17(++keepAliveCounter));
                }
            }, 10, 10, TimeUnit.SECONDS);
        }

        // Remove login timeout
        if (ctx.pipeline().get("loginTimeout") != null) {
            ctx.pipeline().remove("loginTimeout");
        }

        String familyLabel = clientVersion.getFamily() == ProtocolVersion.Family.RELEASE ? "Release"
                : clientVersion.getFamily() == ProtocolVersion.Family.BETA ? "Beta" : "Alpha";
        System.out.println(familyLabel + " login complete: " + player.getUsername()
                + " (protocol: " + clientVersion.getDisplayName()
                + ", version " + clientVersion.getVersionNumber()
                + ", ID " + player.getPlayerId()
                + ", " + playerManager.getPlayerCount() + " online)");

        // Send existing tab list entries to the new player (broadcast already sent above)
        if (clientVersion.isAtLeast(ProtocolVersion.BETA_1_8)) {
            for (ConnectedPlayer existing : playerManager.getAllPlayers()) {
                if (existing != player) {
                    ctx.writeAndFlush(new PlayerListItemPacket(existing.getUsername(), true, 0));
                }
            }
        }

        playerManager.broadcastChat((byte) 0, player.getUsername() + " joined the game");
        ServerEvents.PLAYER_JOIN.invoker().onPlayerJoin(player.getUsername(), clientVersion);
    }

    private void handlePositionAndLook(ChannelHandlerContext ctx,
                                       PlayerPositionAndLookC2SPacket packet) {
        if (player == null) return;
        // Alpha yaw 0=South; internal convention is Classic 0=North. Convert by adding 180°.
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
        // Alpha yaw 0=South; internal convention is Classic 0=North. Convert by adding 180°.
        float classicYaw = (packet.getYaw() + 180.0f) % 360.0f;
        // doubleY is eye-level (internal convention), but updatePosition expects feet Y
        updatePosition(ctx, player.getDoubleX(),
                player.getDoubleY() - PLAYER_EYE_HEIGHT, player.getDoubleZ(),
                classicYaw, packet.getPitch());
    }

    private void updatePosition(ChannelHandlerContext ctx,
                                double x, double y, double z, float yaw, float pitch) {
        // y parameter is FEET position (Alpha convention).
        // Internal convention is eye-level (Classic compatible).
        double eyeY = y + PLAYER_EYE_HEIGHT;

        // Pre-rewrite clients (v13/v14) don't support SpawnPosition (0x06),
        // so their world spawn stays at (0, 0, 0). When the player dies, the
        // client creates a new entity at (0.5, ?, 0.5) with a fresh inventory.
        // Detect this position jump and redirect to a proper safe spawn.
        if (!clientVersion.isAtLeast(ProtocolVersion.ALPHA_1_0_17)
                && x < 2 && z < 2 && x >= 0 && z >= 0) {
            double prevX = player.getDoubleX();
            double prevZ = player.getDoubleZ();
            double dxSq = (prevX - x) * (prevX - x);
            double dzSq = (prevZ - z) * (prevZ - z);
            if (dxSq + dzSq > 100) { // > 10 blocks away = respawn, not walking
                respawnToSafePosition(ctx, yaw);
                return;
            }
        }

        // Fall protection for pre-rewrite clients (v13/v14) that lack
        // UpdateHealthPacket. Track fall distance and teleport to ground
        // before lethal fall damage can occur.
        if (!clientVersion.isAtLeast(ProtocolVersion.ALPHA_1_0_17)) {
            double prevFeetY = player.getDoubleY() - PLAYER_EYE_HEIGHT;
            if (y >= prevFeetY) {
                // Moving up or level — reset fall start
                fallStartFeetY = y;
            } else if (fallStartFeetY - y > 5) {
                // Falling more than 5 blocks — teleport to ground
                teleportToGround(ctx, x, z, yaw, pitch);
                return;
            }
        }

        // If player falls below the world, teleport to spawn.
        // Pre-1.2.2 clients clip Y near 0 at the void floor (their physics
        // code prevents falling below ~Y=0), so the normal -10 threshold
        // never triggers. Use y < 0 for those versions.
        double voidThreshold = clientVersion.isAtLeast(ProtocolVersion.ALPHA_1_2_2) ? -10 : 0;
        if (y < voidThreshold) {
            respawnToSafePosition(ctx, yaw);
            return;
        }

        // Convert to fixed-point for Classic broadcast (eye-level)
        short fixedX = toFixedPoint(x);
        short fixedY = toFixedPoint(eyeY);
        short fixedZ = toFixedPoint(z);
        byte byteYaw = toByteRotation(yaw);
        byte bytePitch = toByteRotation(pitch);

        // Store eye-level Y internally for Classic compatibility
        player.updatePositionDouble(x, eyeY, z, yaw, pitch);

        // Fire move event (eye-level fixed-point)
        ServerEvents.PLAYER_MOVE.invoker().onPlayerMove(
                player.getUsername(), fixedX, fixedY, fixedZ, byteYaw, bytePitch);

        // Broadcast as Classic PlayerTeleportPacket (eye-level, translator adjusts for Alpha)
        playerManager.broadcastPacketExcept(
                new PlayerTeleportPacket(player.getPlayerId(),
                        fixedX, fixedY, fixedZ, byteYaw & 0xFF, bytePitch & 0xFF),
                player);
    }

    /**
     * Teleport the player to a safe ground-level position at world center
     * and re-give cobblestone. Used for void-fall recovery and pre-rewrite
     * client respawn (v13/v14 don't support SpawnPosition so the client
     * respawns at (0, 0, 0) with a fresh empty inventory).
     */
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

        // S2C y = posY (eyes), stance = feet
        // yaw is Classic convention internally; Alpha client expects Alpha (0=South)
        float alphaYaw = (yaw + 180.0f) % 360.0f;
        ctx.writeAndFlush(new PlayerPositionAndLookS2CPacket(
                spawnX, spawnFeetY + PLAYER_EYE_HEIGHT, spawnFeetY, spawnZ,
                alphaYaw, 0, true));

        // Update chunk tracking for the new position
        chunkManager.updatePlayerChunks(player);

        // Broadcast updated position to other players
        short fixedX = toFixedPoint(spawnX);
        short fixedY = toFixedPoint(spawnEyeY);
        short fixedZ = toFixedPoint(spawnZ);
        byte byteYaw = toByteRotation(yaw);
        playerManager.broadcastPacketExcept(
                new PlayerTeleportPacket(player.getPlayerId(),
                        fixedX, fixedY, fixedZ, byteYaw & 0xFF, 0),
                player);

        // Reset fall tracking
        fallStartFeetY = spawnFeetY;
    }

    /**
     * Handle Beta respawn request. Teleport to spawn and re-give cobblestone.
     * v17+ creative mode manages inventory natively, no re-give needed.
     */
    private void handleRespawn(ChannelHandlerContext ctx) {
        if (player == null) return;
        float yaw = player.getFloatYaw();
        respawnToSafePosition(ctx, yaw);
        if (!clientVersion.isAtLeast(ProtocolVersion.BETA_1_8)) {
            giveItem(ctx, BlockRegistry.COBBLESTONE, 64);
            trackedCobblestone = 64;
        }
    }

    /**
     * Teleport the player to the ground directly below their current XZ
     * position. Used by fall protection to catch the player before lethal
     * fall damage. Scans downward for a safe landing spot (solid ground
     * with 2 air blocks above).
     */
    private void teleportToGround(ChannelHandlerContext ctx,
                                   double x, double z, float yaw, float pitch) {
        int blockX = (int) Math.floor(x);
        int blockZ = (int) Math.floor(z);
        int startY = Math.min((int) Math.floor(fallStartFeetY), world.getHeight() - 3);

        // Scan downward from where the fall started to find ground
        int groundFeetY = -1;
        for (int testY = startY; testY >= 0; testY--) {
            if (world.inBounds(blockX, testY, blockZ)
                    && world.getBlock(blockX, testY, blockZ) != 0
                    && world.getBlock(blockX, testY + 1, blockZ) == 0
                    && world.getBlock(blockX, testY + 2, blockZ) == 0) {
                groundFeetY = testY + 1;
                break;
            }
        }
        if (groundFeetY < 0) {
            // No ground below — fall back to world center spawn
            respawnToSafePosition(ctx, yaw);
            return;
        }

        double landX = blockX + 0.5;
        double landFeetY = groundFeetY;
        double landEyeY = landFeetY + PLAYER_EYE_HEIGHT;
        double landZ = blockZ + 0.5;
        player.updatePositionDouble(landX, landEyeY, landZ, yaw, pitch);
        fallStartFeetY = landFeetY;

        float alphaYaw = (yaw + 180.0f) % 360.0f;
        ctx.writeAndFlush(new PlayerPositionAndLookS2CPacket(
                landX, landFeetY + PLAYER_EYE_HEIGHT, landFeetY, landZ,
                alphaYaw, pitch, true));

        // Update chunk tracking and broadcast to other players
        chunkManager.updatePlayerChunks(player);
        short fixedX = toFixedPoint(landX);
        short fixedY = toFixedPoint(landEyeY);
        short fixedZ = toFixedPoint(landZ);
        byte byteYaw = toByteRotation(yaw);
        byte bytePitch = toByteRotation(pitch);
        playerManager.broadcastPacketExcept(
                new PlayerTeleportPacket(player.getPlayerId(),
                        fixedX, fixedY, fixedZ, byteYaw & 0xFF, bytePitch & 0xFF),
                player);
    }

    private void handleDigging(ChannelHandlerContext ctx, PlayerDiggingPacket packet) {
        if (player == null) return;

        // Q-drop (status 4): player pressed Q to drop an item.
        // Beta clients use this instead of PickupSpawnPacket for drops.
        // Replenish cobblestone so the player can keep building.
        if (packet.getStatus() == PlayerDiggingPacket.STATUS_DROP_ITEM) {
            if (clientVersion.isAtLeast(ProtocolVersion.BETA_1_8)) {
                giveItem(ctx, BlockRegistry.COBBLESTONE, 1);
            } else if (clientVersion.isAtLeast(ProtocolVersion.BETA_1_0)) {
                giveItem(ctx, BlockRegistry.COBBLESTONE, 64);
            }
            return;
        }

        // Alpha creative mode: instant block breaking on STARTED or FINISHED
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

        // Special case: direction -1 means "use item" without placing, ignore
        if (direction == -1) return;

        // Compute target position based on face
        int targetX = x;
        int targetY = y;
        int targetZ = z;
        switch (direction) {
            case 0: targetY--; break; // bottom (-Y)
            case 1: targetY++; break; // top (+Y)
            case 2: targetZ--; break; // -Z
            case 3: targetZ++; break; // +Z
            case 4: targetX--; break; // -X
            case 5: targetX++; break; // +X
        }

        short itemId = packet.getItemId();
        if (itemId < 0) return;

        // Validate the item being placed.
        // v17+ creative mode: accept any block ID (1-121), reject items (256+).
        // All placed blocks get converted to grass/cobblestone anyway.
        // Other versions: use server version's block range.
        if (clientVersion.isAtLeast(ProtocolVersion.BETA_1_8)) {
            if (itemId < 1 || itemId > BlockRegistry.MAX_BLOCK_ID) {
                sendBlockChange(ctx, targetX, targetY, targetZ, 0, 0);
                return;
            }
        } else if (!BlockRegistry.isValidBlock(itemId, serverVersion)) {
            sendBlockChange(ctx, targetX, targetY, targetZ, 0, 0);
            return;
        }

        if (!world.inBounds(targetX, targetY, targetZ)) {
            // Cancel the client's predicted block immediately (otherwise the
            // phantom block persists for ~4 seconds, allowing further building
            // above the world height limit).
            sendBlockChange(ctx, targetX, targetY, targetZ, 0, 0);
            // Don't decrement trackedCobblestone — the client's own OOB check
            // usually prevents consumption. Don't reset the replenishment
            // timer either: failed placements don't consume cobblestone, and
            // resetting the timer here prevents it from firing between jump
            // cycles (the ~20-tick gap only barely exceeds the 1-second timer).
            // v1 has no inventory sync and no way to know if the client
            // consumed, so skip the give-back to avoid inflation.
            // Beta uses SetSlot per-placement so just re-set the slot to 64.
            // v17+ creative mode has infinite inventory, no replenishment needed.
            if (clientVersion.isAtLeast(ProtocolVersion.BETA_1_8)) {
                // Creative mode — no replenishment needed
            } else if (clientVersion.isAtLeast(ProtocolVersion.BETA_1_0)) {
                giveItem(ctx, BlockRegistry.COBBLESTONE, 64);
            }
            return;
        }

        // Prevent placing blocks inside the player's body.
        // Player AABB: 0.6 wide (±0.3), 1.8 tall from feet.
        double px = player.getDoubleX();
        double feetY = player.getDoubleY() - PLAYER_EYE_HEIGHT;
        double pz = player.getDoubleZ();
        boolean overlapsX = targetX < px + 0.3 && px - 0.3 < targetX + 1;
        boolean overlapsY = targetY < feetY + 1.8 && feetY < targetY + 1;
        boolean overlapsZ = targetZ < pz + 0.3 && pz - 0.3 < targetZ + 1;
        if (overlapsX && overlapsY && overlapsZ) {
            sendBlockChange(ctx, targetX, targetY, targetZ, 0, 0);
            // Don't decrement or reset the replenishment timer for v2-v6:
            // most overlap rejections happen when the client also rejects the
            // placement (same overlap check), so no cobblestone is consumed.
            // Resetting the timer here prevented it from firing between jump
            // cycles. The inventory sync (0x05) corrects the tracker for the
            // rare case where the client's overlap check does pass.
            // v1 has no inventory sync — skip give-back to avoid inflation.
            // Beta uses SetSlot per-placement so just re-set the slot to 64.
            // v17+ creative mode has infinite inventory, no replenishment needed.
            if (clientVersion.isAtLeast(ProtocolVersion.BETA_1_8)) {
                // Creative mode — no replenishment needed
            } else if (clientVersion.isAtLeast(ProtocolVersion.BETA_1_0)) {
                giveItem(ctx, BlockRegistry.COBBLESTONE, 64);
            }
            return;
        }

        // Determine world block type.
        // RubyDung and v17+ creative mode: convert all blocks to
        // grass at the surface layer, cobblestone everywhere else.
        byte worldBlockType;
        if (serverVersion == ProtocolVersion.RUBYDUNG
                || clientVersion.isAtLeast(ProtocolVersion.BETA_1_8)) {
            int surfaceY = world.getHeight() * 2 / 3;
            worldBlockType = (targetY == surfaceY)
                    ? (byte) BlockRegistry.GRASS
                    : (byte) BlockRegistry.COBBLESTONE;
        } else {
            worldBlockType = (byte) (itemId & 0xFF);
        }

        EventResult result = ServerEvents.BLOCK_PLACE.invoker()
                .onBlockPlace(player.getUsername(), targetX, targetY, targetZ, worldBlockType & 0xFF);
        if (result == EventResult.CANCEL) return;

        // Set block directly in world (bypasses tick queue to avoid double-broadcast)
        if (!world.setBlock(targetX, targetY, targetZ, worldBlockType)) {
            return;
        }
        chunkManager.setBlock(targetX, targetY, targetZ, worldBlockType);

        // Broadcast grass/cobblestone to all other players (Classic translator handles Alpha)
        playerManager.broadcastPacketExcept(
                new SetBlockServerPacket(targetX, targetY, targetZ, worldBlockType & 0xFF), player);

        // Confirm block to this Alpha client. Without server confirmation,
        // the client reverts its predicted blocks after ~4 seconds (80 ticks).
        sendBlockChange(ctx, targetX, targetY, targetZ, itemId & 0xFF, 0);

        // If the world block differs from held item (e.g. grass at surface),
        // send a delayed conversion so the client sees: cobble placed → transforms to grass
        if (worldBlockType != (byte) (itemId & 0xFF)) {
            final int fx = targetX, fy = targetY, fz = targetZ;
            final int fBlockType = worldBlockType & 0xFF;
            ctx.executor().schedule(() -> {
                if (ctx.channel().isActive()) {
                    sendBlockChange(ctx, fx, fy, fz, fBlockType, 0);
                }
            }, 200, TimeUnit.MILLISECONDS);
        }

        // Replenish cobblestone (not needed for v17+ creative mode).
        // - v1 (Alpha 1.0.17): no PlayerInventory (0x05), immediate give-back is safe
        // - v2-v6 (Alpha 1.1.0-1.2.6): batched replenishment to avoid 0x04/0x05 response interference
        // - v7-v14 (Beta 1.0-1.7.3): SetSlot targets a specific slot, immediate per-placement is safe
        // - v17+ (Beta 1.8+): creative mode has infinite inventory, no replenishment needed
        if (clientVersion.isAtLeast(ProtocolVersion.BETA_1_8)) {
            // Creative mode — no replenishment needed
        } else if (clientVersion.isAtLeast(ProtocolVersion.BETA_1_0)) {
            giveItem(ctx, BlockRegistry.COBBLESTONE, 64);
        } else if (!clientVersion.isAtLeast(ProtocolVersion.ALPHA_1_1_0)) {
            giveItem(ctx, BlockRegistry.COBBLESTONE, 1);
        } else {
            trackedCobblestone--;
            scheduleReplenishment(ctx);
        }
    }

    /**
     * Give items to the player. Alpha uses AddToInventory (0x11) which lets
     * the client pick the slot. Beta removed that packet and uses SetSlot (0x67)
     * which targets a specific slot — we always use hotbar slot 0 (window slot 36).
     */
    private void giveItem(ChannelHandlerContext ctx, int itemId, int count) {
        if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_3_1)) {
            ctx.writeAndFlush(new SetSlotPacketV39(0, 36, itemId, count, 0));
        } else if (clientVersion.isAtLeast(ProtocolVersion.BETA_1_9_PRE5)) {
            ctx.writeAndFlush(new SetSlotPacketV22(0, 36, itemId, count, 0));
        } else if (clientVersion.isAtLeast(ProtocolVersion.BETA_1_0)) {
            ctx.writeAndFlush(new SetSlotPacket(0, 36, itemId, count, 0));
        } else {
            ctx.writeAndFlush(new AddToInventoryPacket(itemId, count, 0));
        }
    }

    /** Number of slots in the Beta player inventory window (ID 0). */
    private static final int INVENTORY_SLOTS = 45;
    /** Window slot index for hotbar slot 0. */
    private static final int HOTBAR_SLOT_0 = 36;

    /**
     * Reset the entire player inventory via WindowItems (0x68). Clears all
     * slots and places 64 cobblestone in hotbar slot 0. This prevents
     * cobblestone duplication when players redistribute items across slots
     * before closing the inventory — SetSlot alone only updates one slot,
     * leaving copies in other slots intact.
     */
    private void resetInventory(ChannelHandlerContext ctx) {
        short[] itemIds = new short[INVENTORY_SLOTS];
        byte[] counts = new byte[INVENTORY_SLOTS];
        short[] damages = new short[INVENTORY_SLOTS];
        for (int i = 0; i < INVENTORY_SLOTS; i++) {
            itemIds[i] = -1; // empty
        }
        itemIds[HOTBAR_SLOT_0] = (short) BlockRegistry.COBBLESTONE;
        counts[HOTBAR_SLOT_0] = 64;
        ctx.writeAndFlush(new WindowItemsPacket(0, itemIds, counts, damages));
    }

    /**
     * Handle inventory sync from the client (0x05). The client sends this after
     * any inventory change. We parse the cobblestone count from the main
     * inventory section (type -1) to keep our tracked count accurate.
     */
    private void handleInventorySync(PlayerInventoryPacket packet) {
        if (packet.getType() == -1) {
            // Main inventory — update tracked count from actual client data
            trackedCobblestone = packet.getItemCount(BlockRegistry.COBBLESTONE);
        }
    }

    /**
     * Schedule a batched cobblestone replenishment. Resets the 1-second timer
     * on each call. When the timer fires (no placement for 1 second), tops
     * up to 64 using trackedCobblestone (corrected by inventory sync packets).
     * This avoids sending AddToInventory packets during rapid placement, which
     * can confuse the Alpha client's input handling.
     *
     * A follow-up round fires 1 second after the first to catch cases where the
     * tracker was stale when the first round fired (e.g. the inventory sync
     * corrects it downward, or the client doesn't send a sync at all). The
     * follow-up gives a full 64 cobblestone if the first round gave anything,
     * guaranteeing the client's first slot reaches 64 even without a sync.
     */
    private void scheduleReplenishment(ChannelHandlerContext ctx) {
        if (replenishTask != null) {
            replenishTask.cancel(false);
        }
        replenishTask = ctx.executor().schedule(() -> {
            int deficit = 64 - trackedCobblestone;
            boolean gave = false;
            if (ctx.channel().isActive() && deficit > 0) {
                giveItem(ctx, BlockRegistry.COBBLESTONE, deficit);
                trackedCobblestone = 64;
                gave = true;
            }
            replenishTask = null;

            // Follow-up: re-check deficit after 1 more second in case the
            // tracker was stale when the first round fired. Only sends the
            // remaining deficit (not a full 64) to avoid creating extra stacks.
            if (gave) {
                ctx.executor().schedule(() -> {
                    if (replenishTask == null && ctx.channel().isActive()) {
                        int followUpDeficit = 64 - trackedCobblestone;
                        if (followUpDeficit > 0) {
                            giveItem(ctx, BlockRegistry.COBBLESTONE, followUpDeficit);
                            trackedCobblestone = 64;
                        }
                    }
                }, REPLENISH_DELAY_MS, TimeUnit.MILLISECONDS);
            }
        }, REPLENISH_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    private void handleWindowClick(ChannelHandlerContext ctx, WindowClickPacket packet) {
        if (player == null) return;
        InventoryAdapter adapter = playerManager.getInventoryAdapter();
        int button = packet.getRightClick() & 0xFF;
        int mode = 0;
        if (packet instanceof WindowClickPacketBeta15) {
            byte shift = ((WindowClickPacketBeta15) packet).getShift();
            if (shift == 1) mode = 1;
        }
        adapter.processWindowClick(player.getUsername(), packet.getSlot(), button, mode);
        ctx.writeAndFlush(new ConfirmTransactionPacket(
                packet.getWindowId(), packet.getActionNum(), true));
    }

    private void handleCloseWindow(ChannelHandlerContext ctx) {
        if (player == null) return;
        InventoryAdapter adapter = playerManager.getInventoryAdapter();
        adapter.processCloseWindow(player.getUsername());
    }

    private void handleChat(ChannelHandlerContext ctx, ChatPacket packet) {
        if (player == null) return;

        String message = packet.getMessage();

        // Route commands
        if (message.startsWith("/")) {
            String command = message.substring(1);
            boolean handled = CommandRegistry.dispatch(command, player.getUsername(), false,
                    reply -> playerManager.sendChat(player, reply));
            if (!handled) {
                playerManager.sendChat(player, "Unknown command: " + command.split("\\s+")[0]);
            }
            return;
        }

        // Fire cancellable chat event
        EventResult result = ServerEvents.CHAT.invoker().onChat(player.getUsername(), message);
        if (result == EventResult.CANCEL) return;

        System.out.println("[Chat] " + player.getUsername() + ": " + message);
        playerManager.broadcastChat(player.getPlayerId(), player.getUsername() + ": " + message);
    }

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
        if (cause instanceof ReadTimeoutException) {
            if (!loginComplete) {
                System.err.println("Alpha login timeout — client did not complete login within "
                        + LOGIN_TIMEOUT_SECONDS + " seconds");
                ctx.writeAndFlush(new DisconnectPacket("Login timed out"));
            }
        } else {
            System.err.println("Alpha connection error"
                    + (player != null ? " (" + player.getUsername() + ")" : "")
                    + ": " + cause.getMessage());
        }
        ctx.close();
    }

    /**
     * Build disconnect messages for an unsupported protocol version.
     * Returns [clientMessage, consoleMessage]. The client message is kept short
     * because the Alpha disconnect screen renders text on a single line.
     * The console message includes full version details.
     */
    private static String[] buildUnsupportedVersionMessages(int pv) {
        // Determine which family the unknown version likely belongs to.
        // AlphaConnectionHandler handles all pre-Netty TCP clients (Alpha, Beta, Release).
        // Alpha SMP versions: 1-14, Beta: 7-17, Release: 22+. The ranges overlap,
        // so for truly unsupported versions we show the most likely family.
        ProtocolVersion.Family family;
        if (pv >= 1 && pv <= 14) {
            family = ProtocolVersion.Family.ALPHA;
        } else if (pv >= 15 && pv <= 21) {
            family = ProtocolVersion.Family.BETA;
        } else if (pv >= 22 && pv <= 78) {
            family = ProtocolVersion.Family.RELEASE;
        } else {
            String msg = "Unknown protocol version: " + pv;
            return new String[]{msg, msg};
        }

        List<ProtocolVersion> supported = ProtocolVersion.getByFamily(family);

        // Short message for the client's single-line disconnect screen
        StringBuilder clientMsg = new StringBuilder();
        clientMsg.append("Unsupported v").append(pv).append(". Supported: ");
        for (int i = 0; i < supported.size(); i++) {
            if (i > 0) clientMsg.append(", ");
            clientMsg.append('v').append(supported.get(i).getVersionNumber());
        }

        // Detailed message for the server console
        StringBuilder consoleMsg = new StringBuilder();
        consoleMsg.append("protocol v").append(pv);
        String gameVersions = ProtocolVersion.describeAlphaProtocol(pv);
        if (gameVersions != null) {
            consoleMsg.append(" (").append(gameVersions).append(')');
        }
        consoleMsg.append(" not supported. Supported ");
        consoleMsg.append(family.name().toLowerCase()).append(" versions: ");
        for (int i = 0; i < supported.size(); i++) {
            if (i > 0) consoleMsg.append(", ");
            consoleMsg.append(supported.get(i).getDisplayName());
        }

        return new String[]{clientMsg.toString(), consoleMsg.toString()};
    }

    /**
     * Send a BlockChange packet to this client using the correct format for its version.
     * v39+ uses short block ID (BlockChangePacketV39), pre-v39 uses byte (BlockChangePacket).
     */
    private void sendBlockChange(ChannelHandlerContext ctx, int x, int y, int z, int blockType, int metadata) {
        if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_3_1)) {
            ctx.writeAndFlush(new BlockChangePacketV39(x, y, z, blockType, metadata));
        } else {
            ctx.writeAndFlush(new BlockChangePacket(x, y, z, blockType, metadata));
        }
    }

    private static short toFixedPoint(double d) {
        return (short) (d * 32);
    }

    private static byte toByteRotation(float degrees) {
        // Callers already pass Classic convention degrees (0=North).
        // Simple degrees-to-byte conversion with no offset.
        return (byte) ((degrees / 360.0f) * 256);
    }
}
