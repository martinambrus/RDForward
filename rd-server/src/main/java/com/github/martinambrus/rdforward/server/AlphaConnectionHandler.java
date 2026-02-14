package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.codec.RawPacketDecoder;
import com.github.martinambrus.rdforward.protocol.event.EventResult;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.alpha.*;
import com.github.martinambrus.rdforward.protocol.packet.classic.PlayerTeleportPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockServerPacket;
import com.github.martinambrus.rdforward.server.api.CommandRegistry;
import com.github.martinambrus.rdforward.server.event.ServerEvents;
import com.github.martinambrus.rdforward.world.BlockRegistry;
import io.netty.channel.ChannelHandlerContext;
import java.util.concurrent.TimeUnit;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;

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
     * Cooldown period (ms) after placing a block during which dig packets are
     * ignored. The Alpha client erroneously sends dig packets (0x0E) during
     * sustained rapid right-clicking (e.g. building a column while jumping).
     */
    private static final long PLACEMENT_DIG_COOLDOWN_MS = 500;

    private String pendingUsername;
    private ProtocolVersion clientVersion;
    private ConnectedPlayer player;
    private boolean loginComplete = false;
    private long lastPlacementTime = 0;

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
            } else if (packet instanceof LoginC2SPacket) {
                handleLogin(ctx, (LoginC2SPacket) packet);
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
        } else if (packet instanceof PlayerBlockPlacementPacket) {
            handleBlockPlacement(ctx, (PlayerBlockPlacementPacket) packet);
        } else if (packet instanceof ChatPacket) {
            handleChat(ctx, (ChatPacket) packet);
        } else if (packet instanceof HoldingChangePacket) {
            // Silently accept (hotbar slot change)
        } else if (packet instanceof AnimationPacket) {
            // Silently accept (arm swing animation)
        } else if (packet instanceof PlayerInventoryPacket) {
            // Silently accept (inventory sync)
        } else if (packet instanceof PickupSpawnPacket) {
            // Client tried to drop an item — return it to their inventory
            PickupSpawnPacket drop = (PickupSpawnPacket) packet;
            if (drop.getItemId() > 0 && drop.getCount() > 0) {
                giveItem(ctx, drop.getItemId(), drop.getCount());
            }
        } else if (packet instanceof KeepAlivePacket) {
            // Silently accept
        } else if (packet instanceof DisconnectPacket) {
            ctx.close();
        }
    }

    private void handleHandshake(ChannelHandlerContext ctx, HandshakeC2SPacket packet) {
        pendingUsername = packet.getUsername();
        // Respond with offline-mode hash
        ctx.writeAndFlush(new HandshakeS2CPacket("-"));
    }

    private void handleLogin(ChannelHandlerContext ctx, LoginC2SPacket packet) {
        if (pendingUsername == null) {
            ctx.writeAndFlush(new DisconnectPacket("Handshake not received"));
            ctx.close();
            return;
        }

        // Determine protocol version from the client's login packet
        clientVersion = ProtocolVersion.fromNumber(packet.getProtocolVersion());
        if (clientVersion == null) {
            ctx.writeAndFlush(new DisconnectPacket(
                    "Unknown protocol version: " + packet.getProtocolVersion()));
            ctx.close();
            return;
        }

        // Update decoder's protocol version if needed
        RawPacketDecoder decoder = ctx.pipeline().get(RawPacketDecoder.class);
        if (decoder != null) {
            decoder.setProtocolVersion(clientVersion);
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

        // Send LoginS2C
        ctx.writeAndFlush(new LoginS2CPacket(entityId, 0L, (byte) 0));

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

            // Safety check: ensure player isn't inside solid blocks.
            // Fixed-point truncation can place feet slightly inside the ground
            // (e.g. feetY=42.97 when the player was standing at Y=43.0), so
            // check the actual block the feet are in without any epsilon.
            double feetY = spawnY - PLAYER_EYE_HEIGHT;
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

        // Send spawn position (block coords for compass)
        int spawnBlockX = (int) Math.floor(spawnX);
        int spawnBlockY = (int) Math.floor(spawnY);
        int spawnBlockZ = (int) Math.floor(spawnZ);
        ctx.writeAndFlush(new SpawnPositionPacket(spawnBlockX, spawnBlockY, spawnBlockZ));

        // Register for chunk tracking and send initial chunks
        chunkManager.addPlayer(player);
        chunkManager.sendInitialChunks(player, spawnBlockX, spawnBlockZ);

        // Send player position and look.
        // spawnY is eye-level (internal convention). Alpha S2C needs posY and feet.
        double feetY = spawnY - PLAYER_EYE_HEIGHT;
        double posY = feetY + PLAYER_EYE_HEIGHT;

        // S2C y = posY (eyes), stance = feet. The client sets posY from y
        // and computes BB.minY (feet) = posY - (double)(1.62f).
        // spawnYaw is Classic convention; Alpha client expects Alpha (0=South)
        float alphaSpawnYaw = (spawnYaw + 180.0f) % 360.0f;
        ctx.writeAndFlush(new PlayerPositionAndLookS2CPacket(
                spawnX, posY, feetY, spawnZ, alphaSpawnYaw, spawnPitch, true));

        // Send existing players to this client (as Alpha spawn packets).
        // Internal Y is eye-level; Alpha SpawnPlayerPacket expects feet Y.
        for (ConnectedPlayer existing : playerManager.getAllPlayers()) {
            if (existing != player) {
                int existingEntityId = existing.getPlayerId() + 1;
                int existingFeetY = (int) existing.getY() - PLAYER_EYE_HEIGHT_FIXED;
                ctx.writeAndFlush(new com.github.martinambrus.rdforward.protocol.packet.alpha.SpawnPlayerPacket(
                        existingEntityId, existing.getUsername(),
                        (int) existing.getX(), existingFeetY, (int) existing.getZ(),
                        (existing.getYaw() + 128) & 0xFF, existing.getPitch() & 0xFF, (short) 0));
            }
        }

        // Broadcast new player's spawn to everyone else (as Classic packet, translator converts)
        playerManager.broadcastPlayerSpawn(player);

        loginComplete = true;

        // Give the player cobblestone so they can place blocks
        giveItem(ctx, BlockRegistry.COBBLESTONE, 64);

        // Remove login timeout
        if (ctx.pipeline().get("loginTimeout") != null) {
            ctx.pipeline().remove("loginTimeout");
        }

        System.out.println("Alpha login complete: " + player.getUsername()
                + " (protocol: " + clientVersion.getDisplayName()
                + ", version " + clientVersion.getVersionNumber()
                + ", ID " + player.getPlayerId()
                + ", " + playerManager.getPlayerCount() + " online)");

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

        // If player falls below the world, teleport to spawn
        if (y < -10) {
            double spawnX = world.getWidth() / 2.0 + 0.5;
            int feetBlockY = world.getHeight() * 2 / 3 + 1;
            double spawnEyeY = feetBlockY + PLAYER_EYE_HEIGHT;
            double spawnZ = world.getDepth() / 2.0 + 0.5;
            player.updatePositionDouble(spawnX, spawnEyeY, spawnZ, yaw, 0);

            // S2C y = posY (eyes), stance = feet
            // yaw is Classic convention internally; Alpha client expects Alpha (0=South)
            float alphaYaw = (yaw + 180.0f) % 360.0f;
            double spawnFeetY = feetBlockY + 0.5; // small offset to prevent re-falling
            ctx.writeAndFlush(new PlayerPositionAndLookS2CPacket(
                    spawnX, spawnFeetY + PLAYER_EYE_HEIGHT, spawnFeetY, spawnZ,
                    alphaYaw, 0, true));

            // Broadcast updated position to other players
            short fixedX = toFixedPoint(spawnX);
            short fixedY = toFixedPoint(spawnEyeY);
            short fixedZ = toFixedPoint(spawnZ);
            byte byteYaw = toByteRotation(yaw);
            playerManager.broadcastPacketExcept(
                    new PlayerTeleportPacket(player.getPlayerId(),
                            fixedX, fixedY, fixedZ, byteYaw & 0xFF, 0),
                    player);
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

    private void handleDigging(ChannelHandlerContext ctx, PlayerDiggingPacket packet) {
        if (player == null) return;

        long timeSincePlacement = System.currentTimeMillis() - lastPlacementTime;

        // Alpha has instant block breaking — handle both "started" (instant break)
        // and "finished" (survival mode break animation complete)
        if (packet.getStatus() == PlayerDiggingPacket.STATUS_STARTED
                || packet.getStatus() == PlayerDiggingPacket.STATUS_FINISHED) {

            // Suppress erroneous dig packets during rapid right-click placement.
            // The Alpha client sends dig packets (0x0E) during sustained rapid
            // right-clicking (e.g. building a column while jumping).
            if (timeSincePlacement < PLACEMENT_DIG_COOLDOWN_MS) {
                return;
            }
            int x = packet.getX();
            int y = packet.getY();
            int z = packet.getZ();

            if (!world.inBounds(x, y, z)) return;

            byte existingBlock = world.getBlock(x, y, z);
            if (existingBlock == 0) return; // already air, nothing to break

            EventResult result = ServerEvents.BLOCK_BREAK.invoker()
                    .onBlockBreak(player.getUsername(), x, y, z, existingBlock & 0xFF);
            if (result == EventResult.CANCEL) return;

            world.queueBlockChange(x, y, z, (byte) 0);
        }
    }

    private void handleBlockPlacement(ChannelHandlerContext ctx, PlayerBlockPlacementPacket packet) {
        if (player == null) return;

        int x = packet.getX();
        int y = packet.getY();
        int z = packet.getZ();
        int direction = packet.getDirection();

        // Special case: direction -1 means "use item" without placing, ignore
        if (direction == -1) return;

        lastPlacementTime = System.currentTimeMillis();

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

        if (!world.inBounds(targetX, targetY, targetZ)) return;

        // Prevent placing blocks inside the player's body.
        // Player AABB: 0.6 wide (±0.3), 1.8 tall from feet.
        double px = player.getDoubleX();
        double feetY = player.getDoubleY() - PLAYER_EYE_HEIGHT;
        double pz = player.getDoubleZ();
        boolean overlapsX = targetX < px + 0.3 && px - 0.3 < targetX + 1;
        boolean overlapsY = targetY < feetY + 1.8 && feetY < targetY + 1;
        boolean overlapsZ = targetZ < pz + 0.3 && pz - 0.3 < targetZ + 1;
        if (overlapsX && overlapsY && overlapsZ) return;

        short itemId = packet.getItemId();
        if (itemId < 0) return; // empty hand, no block to place

        // Determine world block type: RubyDung palette overrides surface to grass
        byte worldBlockType;
        if (serverVersion == ProtocolVersion.RUBYDUNG) {
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
        if (!world.setBlock(targetX, targetY, targetZ, worldBlockType)) return;
        chunkManager.setBlock(targetX, targetY, targetZ, worldBlockType);

        // Broadcast grass/cobblestone to all other players (Classic translator handles Alpha)
        playerManager.broadcastPacketExcept(
                new SetBlockServerPacket(targetX, targetY, targetZ, worldBlockType & 0xFF), player);

        // Confirm block to this Alpha client. Alpha v6 requires server confirmation;
        // without it, the client removes its predicted blocks after ~2-3 seconds.
        ctx.writeAndFlush(new BlockChangePacket(
                targetX, targetY, targetZ, itemId & 0xFF, 0));

        // If the world block differs from held item (e.g. grass at surface),
        // send a delayed conversion so the client sees: cobble placed → transforms to grass
        if (worldBlockType != (byte) (itemId & 0xFF)) {
            final int fx = targetX, fy = targetY, fz = targetZ;
            final int fBlockType = worldBlockType & 0xFF;
            ctx.executor().schedule(() -> {
                if (ctx.channel().isActive()) {
                    ctx.writeAndFlush(new BlockChangePacket(fx, fy, fz, fBlockType, 0));
                }
            }, 200, TimeUnit.MILLISECONDS);
        }

        // Replenish 1 cobblestone so the player can keep placing (survival mode,
        // simulates infinite cobblestone like creative mode)
        giveItem(ctx, BlockRegistry.COBBLESTONE, 1);
    }

    /**
     * Give items to the player using the Alpha AddToInventory packet (0x11).
     * The client handles slot assignment (first matching stack or first empty slot).
     */
    private void giveItem(ChannelHandlerContext ctx, int itemId, int count) {
        ctx.writeAndFlush(new AddToInventoryPacket(itemId, count, 0));
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
        if (player != null) {
            System.out.println(player.getUsername() + " disconnected"
                    + " (" + (playerManager.getPlayerCount() - 1) + " online)");
            ServerEvents.PLAYER_LEAVE.invoker().onPlayerLeave(player.getUsername());
            world.rememberPlayerPosition(player);
            chunkManager.removePlayer(player);
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

    private static short toFixedPoint(double d) {
        return (short) (d * 32);
    }

    private static byte toByteRotation(float degrees) {
        // Callers already pass Classic convention degrees (0=North).
        // Simple degrees-to-byte conversion with no offset.
        return (byte) ((degrees / 360.0f) * 256);
    }
}
