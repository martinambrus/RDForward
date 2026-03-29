package com.github.martinambrus.rdforward.server.lce;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.alpha.*;
import com.github.martinambrus.rdforward.protocol.packet.classic.PlayerTeleportPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockServerPacket;
import com.github.martinambrus.rdforward.protocol.packet.lce.*;
import com.github.martinambrus.rdforward.server.*;
import com.github.martinambrus.rdforward.protocol.event.EventResult;
import com.github.martinambrus.rdforward.server.event.ServerEvents;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Handles LCE (Legacy Console Edition) client connections.
 *
 * State machine: AWAITING_PRELOGIN -> AWAITING_LOGIN -> CONNECTED
 *
 * LCE uses a two-phase login:
 * 1. PreLogin (ID 2): netcode version check + UGC privilege exchange
 * 2. Login (ID 1): game version check + player registration + world data
 *
 * After login, gameplay packets are routed the same way as Alpha/Beta/Release.
 */
public class LCEConnectionHandler extends SimpleChannelInboundHandler<Packet> {

    private static final int LOGIN_TIMEOUT_SECONDS = 15;
    private static final int KEEPALIVE_TIMEOUT_MS = 30_000;
    private static final short LCE_NETCODE_VERSION = 560;
    private static final int LCE_PROTOCOL_VERSION = 78;
    private static final double PLAYER_EYE_HEIGHT = (double) 1.62f;
    private static final int PLAYER_EYE_HEIGHT_FIXED = (int) Math.ceil(PLAYER_EYE_HEIGHT * 32);

    private enum State { AWAITING_PRELOGIN, AWAITING_LOGIN, CONNECTED }

    private final ServerWorld world;
    private final PlayerManager playerManager;
    private final ChunkManager chunkManager;

    private State state = State.AWAITING_PRELOGIN;
    private String pendingUsername;
    private ConnectedPlayer player;

    private int keepAliveCounter;
    private long lastKeepAliveResponseTime;
    private ScheduledFuture<?> loginTimeoutTask;
    private ScheduledFuture<?> keepAliveTask;

    public LCEConnectionHandler(ProtocolVersion serverVersion, ServerWorld world,
                                 PlayerManager playerManager, ChunkManager chunkManager) {
        this.world = world;
        this.playerManager = playerManager;
        this.chunkManager = chunkManager;
    }

    /**
     * Called when this handler is added to an already-active pipeline.
     * channelActive() does not fire for handlers added after the channel
     * is already active, so we schedule the login timeout here instead.
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        if (ctx.channel().isActive()) {
            scheduleLoginTimeout(ctx);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        scheduleLoginTimeout(ctx);
    }

    private void scheduleLoginTimeout(ChannelHandlerContext ctx) {
        if (loginTimeoutTask != null) return;
        loginTimeoutTask = ctx.executor().schedule(() -> {
            if (state != State.CONNECTED) {
                System.out.println("LCE login timeout, disconnecting");
                ctx.writeAndFlush(new DisconnectPacket("Login timed out"));
                ctx.close();
            }
        }, LOGIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        switch (state) {
            case AWAITING_PRELOGIN:
                if (packet instanceof LCEPreLoginC2SPacket) {
                    handlePreLogin(ctx, (LCEPreLoginC2SPacket) packet);
                }
                break;

            case AWAITING_LOGIN:
                if (packet instanceof LCELoginC2SPacket) {
                    handleLogin(ctx, (LCELoginC2SPacket) packet);
                }
                break;

            case CONNECTED:
                handleGamePacket(ctx, packet);
                break;
        }
    }

    private void handlePreLogin(ChannelHandlerContext ctx, LCEPreLoginC2SPacket packet) {
        short netcodeVersion = packet.getNetcodeVersion();
        pendingUsername = packet.getLoginKey();

        System.out.println("LCE PreLogin from " + pendingUsername
                + " (netcode version " + netcodeVersion + ")");

        // Send PreLogin response with empty player list and default settings
        ctx.writeAndFlush(new LCEPreLoginS2CPacket(
                LCE_NETCODE_VERSION,
                "",                   // sessionId (unused in offline mode)
                (byte) 0,             // friendsOnlyBits
                0,                    // ugcPlayersVersion
                0,                    // playerCount
                new long[0],          // playerXuids
                new byte[14],         // uniqueSaveName
                0,                    // serverSettings
                (byte) 0,             // hostIndex
                0                     // texturePackId
        ));

        state = State.AWAITING_LOGIN;
    }

    private void handleLogin(ChannelHandlerContext ctx, LCELoginC2SPacket packet) {
        int clientVersion = packet.getClientVersion();
        String userName = packet.getUserName();

        System.out.println("LCE Login from " + userName
                + " (protocol version " + clientVersion + ")");

        if (clientVersion != LCE_PROTOCOL_VERSION) {
            System.out.println("Rejected LCE client: incompatible protocol version "
                    + clientVersion + " (expected " + LCE_PROTOCOL_VERSION + ")");
            ctx.writeAndFlush(new DisconnectPacket("Incompatible version"));
            ctx.close();
            return;
        }

        // Use the username from the login packet (may differ from PreLogin)
        if (userName != null && !userName.isEmpty()) {
            pendingUsername = userName;
        }

        // Register player
        player = playerManager.addPlayer(pendingUsername, null, ctx.channel(),
                ProtocolVersion.LCE_TU19);
        if (player == null) {
            ctx.writeAndFlush(new DisconnectPacket("Server is full!"));
            ctx.close();
            return;
        }

        // Configure translator
        ClassicToLCETranslator translator = ctx.pipeline().get(ClassicToLCETranslator.class);
        if (translator != null) {
            translator.setClientVersion(ProtocolVersion.LCE_TU19);
        }

        // Send Login response
        ctx.writeAndFlush(LCELoginS2CPacket.builder()
                .clientVersion(LCE_PROTOCOL_VERSION)
                .userName(pendingUsername)
                .levelType("default")
                .gameType(1)                    // creative
                .mapHeight((byte) -128)         // signed byte: -128 = 128
                .maxPlayers((byte) 8)
                .difficulty((byte) 1)           // easy
                .playerIndex((byte) (player.getPlayerId() & 0xFF))
                .gamePrivileges(0x1FB30)        // Op, CanFly, Invulnerable, CreativeMode, etc.
                .xzSize((short) 320)            // LEVEL_MAX_WIDTH = 5*64
                .hellScale((byte) 8)            // HELL_LEVEL_MAX_SCALE
                .build());

        // Determine spawn position
        SpawnPositionResolver.SpawnPosition spawn = SpawnPositionResolver.resolve(
                world, player.getUsername(), player.getUuid());
        player.updatePositionDouble(spawn.x, spawn.y, spawn.z, spawn.yaw, spawn.pitch);

        int spawnBlockX = (int) Math.floor(spawn.x);
        int spawnBlockY = (int) Math.floor(spawn.y);
        int spawnBlockZ = (int) Math.floor(spawn.z);

        ctx.writeAndFlush(new SpawnPositionPacket(spawnBlockX, spawnBlockY, spawnBlockZ));

        double feetY = spawn.y - PLAYER_EYE_HEIGHT;
        float alphaSpawnYaw = (spawn.yaw + 180.0f) % 360.0f;
        ctx.writeAndFlush(new PlayerPositionAndLookS2CPacket(
                spawn.x, spawn.y, feetY, spawn.z, alphaSpawnYaw, spawn.pitch, true));

        // Register for chunk tracking and send initial chunks
        chunkManager.addPlayer(player);
        chunkManager.sendInitialChunks(player, spawnBlockX, spawnBlockZ);

        // Send existing players to this client
        for (ConnectedPlayer existing : playerManager.getAllPlayers()) {
            if (existing != player) {
                int existingEntityId = existing.getPlayerId() + 1;
                int existingFeetY = (int) existing.getY() - PLAYER_EYE_HEIGHT_FIXED;
                int alphaYaw = (existing.getYaw() + 128) & 0xFF;
                int pitch = existing.getPitch() & 0xFF;
                ctx.writeAndFlush(new LCEAddPlayerPacket(
                        existingEntityId, existing.getUsername(),
                        (int) existing.getX(), existingFeetY, (int) existing.getZ(),
                        (byte) alphaYaw, (byte) pitch, (byte) alphaYaw,
                        (short) 0, 0L, 0L, (byte) 0, 0, 0, 0));
            }
        }

        // PlayerListAdd MUST come before PlayerSpawn — 1.8+ clients
        // ignore SpawnPlayer for UUIDs not yet in the tablist.
        playerManager.broadcastPlayerListAdd(player);
        playerManager.broadcastPlayerSpawn(player);

        state = State.CONNECTED;

        // Cancel login timeout — keepalive handles timeout from here
        if (loginTimeoutTask != null) {
            loginTimeoutTask.cancel(false);
            loginTimeoutTask = null;
        }

        // Start keepalive (every 15 seconds, matching vanilla)
        lastKeepAliveResponseTime = System.currentTimeMillis();
        keepAliveTask = ctx.executor().scheduleAtFixedRate(() -> {
            if (state != State.CONNECTED || !ctx.channel().isActive()) return;
            long elapsed = System.currentTimeMillis() - lastKeepAliveResponseTime;
            if (elapsed > KEEPALIVE_TIMEOUT_MS) {
                System.out.println("LCE keepalive timeout"
                        + (player != null ? " (" + player.getUsername() + ")" : ""));
                ctx.close();
                return;
            }
            ctx.writeAndFlush(new KeepAlivePacketV17(++keepAliveCounter));
        }, 15, 15, TimeUnit.SECONDS);

        String ip = ctx.channel().remoteAddress() != null
                ? ctx.channel().remoteAddress().toString() : "unknown";
        System.out.println(pendingUsername + " (LCE) joined the game"
                + " (" + playerManager.getPlayerCount() + " online, ip " + ip + ")");
        ServerEvents.PLAYER_JOIN.invoker().onPlayerJoin(player.getUsername(), ProtocolVersion.LCE_TU19);
    }

    private void handleGamePacket(ChannelHandlerContext ctx, Packet packet) {
        if (packet instanceof PlayerPositionAndLookC2SPacket) {
            handlePositionAndLook(ctx, (PlayerPositionAndLookC2SPacket) packet);
        } else if (packet instanceof PlayerPositionPacket) {
            handlePosition(ctx, (PlayerPositionPacket) packet);
        } else if (packet instanceof PlayerLookPacket) {
            handleLook(ctx, (PlayerLookPacket) packet);
        } else if (packet instanceof PlayerOnGroundPacket) {
            // No-op: onGround only
        } else if (packet instanceof LCEChatPacket) {
            handleChat(ctx, (LCEChatPacket) packet);
        } else if (packet instanceof PlayerDiggingPacket) {
            handleDigging(ctx, (PlayerDiggingPacket) packet);
        } else if (packet instanceof PlayerBlockPlacementPacketV39) {
            handleBlockPlacement(ctx, (PlayerBlockPlacementPacketV39) packet);
        } else if (packet instanceof LCEBatchPacket) {
            handleBatch(ctx, (LCEBatchPacket) packet);
        } else if (packet instanceof KeepAlivePacketV17) {
            lastKeepAliveResponseTime = System.currentTimeMillis();
        } else if (packet instanceof DisconnectPacket) {
            ctx.close();
        }
    }

    /**
     * Unpack an LCE batch packet and handle each sub-packet.
     * The batch contains multiple standard game packets concatenated together.
     * Sub-packet 0x12 inside a batch is Animation (not another batch).
     */
    private void handleBatch(ChannelHandlerContext ctx, LCEBatchPacket batch) {
        io.netty.buffer.ByteBuf buf = io.netty.buffer.Unpooled.wrappedBuffer(batch.getSubPacketData());
        try {
        while (buf.readableBytes() > 0) {
            buf.markReaderIndex();
            int subId = buf.readUnsignedByte();

            // Resolve sub-packet: 0x12 inside a batch is Animation (not another batch),
            // and 0x13 is EntityAction (v73 format with jumpBoost). All other IDs use the registry.
            Packet subPacket = createBatchSubPacket(subId);
            if (subPacket == null) {
                System.out.println("[LCE-BATCH] Unknown sub-packet 0x"
                        + Integer.toHexString(subId) + ", stopping batch parse ("
                        + buf.readableBytes() + " bytes remaining)");
                break;
            }

            try {
                subPacket.read(buf);
            } catch (Exception e) {
                System.err.println("[LCE-BATCH] Error reading sub-packet 0x"
                        + Integer.toHexString(subId) + ": " + e.getMessage());
                break;
            }

            handleGamePacket(ctx, subPacket);
        }
        } finally {
            buf.release();
        }
    }

    /**
     * Create a packet for a batch sub-packet ID. Inside a batch, 0x12 is Animation
     * (not LCEBatchPacket) and 0x13 is EntityAction (v73 format with jumpBoost).
     */
    private static Packet createBatchSubPacket(int subId) {
        if (subId == 0x12) return new AnimationPacket();
        if (subId == 0x13) return new EntityActionPacketV73();
        Packet p = com.github.martinambrus.rdforward.protocol.packet.PacketRegistry.createPacket(
                com.github.martinambrus.rdforward.protocol.ProtocolVersion.LCE_TU19,
                com.github.martinambrus.rdforward.protocol.packet.PacketDirection.CLIENT_TO_SERVER, subId);
        // Nested batches are not valid
        return (p instanceof LCEBatchPacket) ? null : p;
    }

    private void handlePositionAndLook(ChannelHandlerContext ctx, PlayerPositionAndLookC2SPacket packet) {
        double x = packet.getX();
        double y = packet.getStance(); // C2S: Y=feet, Stance=eye-level (internal convention)
        double z = packet.getZ();
        float yaw = packet.getYaw();
        float pitch = packet.getPitch();

        // Convert Alpha yaw (0=South) to Classic convention (0=North): +180
        float classicYaw = (yaw + 180.0f) % 360.0f;

        player.updatePositionDouble(x, y, z, classicYaw, pitch);
        chunkManager.updatePlayerChunks(player);

        short fixedX = toFixedPoint(x);
        short fixedY = toFixedPoint(y);
        short fixedZ = toFixedPoint(z);
        byte byteYaw = toByteRotation(classicYaw);
        byte bytePitch = toByteRotation(pitch);
        playerManager.broadcastPositionUpdate(player, fixedX, fixedY, fixedZ, byteYaw, bytePitch);
    }

    private void handlePosition(ChannelHandlerContext ctx, PlayerPositionPacket packet) {
        double x = packet.getX();
        double y = packet.getStance(); // C2S: Y=feet, Stance=eye-level (internal convention)
        double z = packet.getZ();

        player.updatePositionDouble(x, y, z, player.getFloatYaw(), player.getFloatPitch());
        chunkManager.updatePlayerChunks(player);

        short fixedX = toFixedPoint(x);
        short fixedY = toFixedPoint(y);
        short fixedZ = toFixedPoint(z);
        byte byteYaw = toByteRotation(player.getFloatYaw());
        byte bytePitch = toByteRotation(player.getFloatPitch());
        playerManager.broadcastPositionUpdate(player, fixedX, fixedY, fixedZ, byteYaw, bytePitch);
    }

    private void handleLook(ChannelHandlerContext ctx, PlayerLookPacket packet) {
        float yaw = packet.getYaw();
        float pitch = packet.getPitch();
        float classicYaw = (yaw + 180.0f) % 360.0f;

        player.updatePositionDouble(player.getDoubleX(), player.getDoubleY(),
                player.getDoubleZ(), classicYaw, pitch);

        byte byteYaw = toByteRotation(classicYaw);
        byte bytePitch = toByteRotation(pitch);
        playerManager.broadcastPositionUpdate(player,
                (short) player.getX(), (short) player.getY(), (short) player.getZ(),
                byteYaw, bytePitch);
    }

    private void handleChat(ChannelHandlerContext ctx, LCEChatPacket packet) {
        String message = packet.getMessage();
        if (message == null || message.isEmpty()) return;

        if (message.startsWith("/")) {
            String command = message.substring(1);
            boolean handled = com.github.martinambrus.rdforward.server.api.CommandRegistry.dispatch(
                    command, player.getUsername(), false,
                    reply -> playerManager.sendChat(player, reply));
            if (!handled) {
                playerManager.sendChat(player, "Unknown command: " + command.split("\\s+")[0]);
            }
        } else {
            playerManager.broadcastChat(player.getPlayerId(), player.getUsername() + ": " + message);
        }
    }

    private void handleDigging(ChannelHandlerContext ctx, PlayerDiggingPacket packet) {
        if (packet.getStatus() != 0) return;

        int x = packet.getX();
        int y = packet.getY();
        int z = packet.getZ();

        if (!world.inBounds(x, y, z)) return;

        int oldBlock = world.getBlock(x, y, z);
        if (oldBlock == 0) return;

        EventResult result = ServerEvents.BLOCK_BREAK.invoker()
                .onBlockBreak(player.getUsername(), x, y, z, oldBlock & 0xFF);
        if (result == EventResult.CANCEL) {
            ctx.writeAndFlush(new BlockChangePacketV39(x, (byte) y, z, (short) (oldBlock & 0xFF), (byte) 0));
            return;
        }

        if (!world.setBlock(x, y, z, (byte) 0)) return;
        chunkManager.setBlock(x, y, z, (byte) 0);

        playerManager.broadcastPacketExcept(
                new SetBlockServerPacket(x, y, z, (byte) 0), player);

        // Send block change confirmation to prevent client revert
        ctx.writeAndFlush(new BlockChangePacketV39(x, (byte) y, z, (short) 0, (byte) 0));
    }

    private void handleBlockPlacement(ChannelHandlerContext ctx, PlayerBlockPlacementPacketV39 packet) {
        int x = packet.getX();
        int y = packet.getY();
        int z = packet.getZ();
        int direction = packet.getDirection();

        if (direction == -1) return; // Item use, not block placement

        // Offset by direction to get the target block position
        int targetX = x, targetY = y, targetZ = z;
        switch (direction) {
            case 0: targetY--; break;
            case 1: targetY++; break;
            case 2: targetZ--; break;
            case 3: targetZ++; break;
            case 4: targetX--; break;
            case 5: targetX++; break;
        }

        if (!world.inBounds(targetX, targetY, targetZ)) return;
        int existingBlock = world.getBlock(targetX, targetY, targetZ);
        if (existingBlock != 0) return;

        // Body overlap check: prevent placing blocks inside the player's body
        double px = player.getDoubleX();
        double feetY = player.getDoubleY() - PLAYER_EYE_HEIGHT;
        double pz = player.getDoubleZ();
        boolean overlapsX = targetX < px + 0.3 && px - 0.3 < targetX + 1;
        boolean overlapsY = targetY < feetY + 1.8 && feetY < targetY + 1;
        boolean overlapsZ = targetZ < pz + 0.3 && pz - 0.3 < targetZ + 1;
        if (overlapsX && overlapsY && overlapsZ) {
            ctx.writeAndFlush(new BlockChangePacketV39(targetX, (byte) targetY, targetZ, (short) 0, (byte) 0));
            return;
        }

        // Convert all blocks to cobblestone (grass at surface level), matching RubyDung/Creative
        int surfaceY = world.getHeight() * 2 / 3;
        byte blockType = (targetY == surfaceY)
                ? (byte) 2  // grass
                : (byte) 4; // cobblestone

        EventResult result = ServerEvents.BLOCK_PLACE.invoker()
                .onBlockPlace(player.getUsername(), targetX, targetY, targetZ, blockType & 0xFF);
        if (result == EventResult.CANCEL) {
            ctx.writeAndFlush(new BlockChangePacketV39(targetX, (byte) targetY, targetZ, (short) 0, (byte) 0));
            return;
        }

        if (!world.setBlock(targetX, targetY, targetZ, blockType)) return;
        chunkManager.setBlock(targetX, targetY, targetZ, blockType);

        playerManager.broadcastPacketExcept(
                new SetBlockServerPacket(targetX, targetY, targetZ, blockType), player);
        ctx.writeAndFlush(new BlockChangePacketV39(targetX, (byte) targetY, targetZ, (short) blockType, (byte) 0));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        state = State.AWAITING_PRELOGIN; // terminal — prevents keepalive from firing
        if (keepAliveTask != null) {
            keepAliveTask.cancel(false);
            keepAliveTask = null;
        }
        if (loginTimeoutTask != null) {
            loginTimeoutTask.cancel(false);
            loginTimeoutTask = null;
        }
        if (player != null) {
            playerManager.removePlayer(ctx.channel());
            System.out.println(player.getUsername() + " (LCE) disconnected"
                    + " (" + playerManager.getPlayerCount() + " online)");
            ServerEvents.PLAYER_LEAVE.invoker().onPlayerLeave(player.getUsername());
            world.rememberPlayerPosition(player);
            chunkManager.removePlayer(player);
            playerManager.broadcastPlayerListRemove(player);
            playerManager.broadcastPlayerDespawn(player);
        }
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof ReadTimeoutException) {
            System.out.println("LCE client timed out"
                    + (player != null ? " (" + player.getUsername() + ")" : ""));
        } else if (isNormalDisconnect(cause)) {
            System.out.println("LCE client disconnected"
                    + (player != null ? " (" + player.getUsername() + ")" : ""));
        } else {
            System.err.println("LCE connection error"
                    + (player != null ? " (" + player.getUsername() + ")" : "")
                    + ": " + cause.getMessage());
            cause.printStackTrace();
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

    private static short toFixedPoint(double value) {
        return (short) Math.round(value * 32.0);
    }

    private static byte toByteRotation(float degrees) {
        return (byte) (degrees * 256.0f / 360.0f);
    }
}
