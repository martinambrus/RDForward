package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.codec.PacketDecoder;
import com.github.martinambrus.rdforward.protocol.event.EventResult;
import com.github.martinambrus.rdforward.world.BlockRegistry;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.classic.*;
import com.github.martinambrus.rdforward.protocol.translation.VersionTranslator;
import com.github.martinambrus.rdforward.server.api.CommandRegistry;
import com.github.martinambrus.rdforward.server.event.ServerEvents;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;

import java.io.IOException;
import java.util.Arrays;

/**
 * Handles individual client connections on the server side.
 *
 * Responsibilities:
 * 1. Process Player Identification (Classic 0x00) to determine client's protocol version
 * 2. Send Server Identification (Classic 0x00) in response
 * 3. Insert VersionTranslator into the pipeline if client version differs
 * 4. Send the full world via Classic level transfer sequence
 * 5. Spawn self and existing players for the new client
 * 6. Route game packets (block changes, position updates, chat) to the server
 *
 * Login sequence (MC Classic protocol):
 *   Client: PlayerIdentification (0x00) — protocol version, username, key
 *   Server: ServerIdentification (0x00) — version, name, MOTD, user type
 *   Server: LevelInitialize (0x02)
 *   Server: LevelDataChunk (0x03) x N — compressed world data in 1KB chunks
 *   Server: LevelFinalize (0x04) — world dimensions
 *   Server: SpawnPlayer (0x07) with ID -1 (self)
 *   Server: SpawnPlayer (0x07) for each existing player
 *   Normal gameplay begins
 */
public class ServerConnectionHandler extends SimpleChannelInboundHandler<Packet> {

    /** Seconds to wait for PlayerIdentification before disconnecting. */
    static final int LOGIN_TIMEOUT_SECONDS = 30;

    /** Player eye height in blocks. Internal Y convention is eye-level. */
    private static final double PLAYER_EYE_HEIGHT = 1.62;

    private final ProtocolVersion serverVersion;
    private final ServerWorld world;
    private final PlayerManager playerManager;
    private final ChunkManager chunkManager;
    private ProtocolVersion clientVersion;
    private ConnectedPlayer player;
    private boolean loginComplete = false;

    public ServerConnectionHandler(ProtocolVersion serverVersion, ServerWorld world,
                                   PlayerManager playerManager, ChunkManager chunkManager) {
        this.serverVersion = serverVersion;
        this.world = world;
        this.playerManager = playerManager;
        this.chunkManager = chunkManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        // First packet must be PlayerIdentification (Classic 0x00)
        if (!loginComplete && packet instanceof PlayerIdentificationPacket) {
            handlePlayerIdentification(ctx, (PlayerIdentificationPacket) packet);
            return;
        }

        if (!loginComplete) {
            // Non-identification packet before login — close immediately to prevent
            // clients from holding connections open by sending continuous junk
            System.err.println("Received unexpected pre-login packet 0x"
                    + String.format("%02X", packet.getPacketId()) + ", disconnecting");
            ctx.writeAndFlush(new DisconnectPacket("Invalid login sequence"));
            ctx.close();
            return;
        }

        // Route game packets by type
        if (packet instanceof SetBlockClientPacket) {
            handleSetBlock(ctx, (SetBlockClientPacket) packet);
        } else if (packet instanceof PlayerTeleportPacket) {
            handlePlayerPosition(ctx, (PlayerTeleportPacket) packet);
        } else if (packet instanceof MessagePacket) {
            handleMessage(ctx, (MessagePacket) packet);
        }
    }

    private void handlePlayerIdentification(ChannelHandlerContext ctx, PlayerIdentificationPacket identification) {
        clientVersion = ProtocolVersion.fromNumber(identification.getProtocolVersion());
        String username = identification.getUsername();

        if (clientVersion == null) {
            ctx.writeAndFlush(new DisconnectPacket("Unknown protocol version: " + identification.getProtocolVersion()));
            ctx.close();
            return;
        }

        // If a non-blank username is already online, kick the old connection
        if (username != null && !username.trim().isEmpty()) {
            playerManager.kickDuplicatePlayer(username.trim(), world);
        }

        // Register the player (assigns an ID, may rename blank/duplicate names)
        player = playerManager.addPlayer(username, ctx.channel(), clientVersion);
        if (player == null) {
            ctx.writeAndFlush(new DisconnectPacket("Server is full!"));
            ctx.close();
            return;
        }
        // Use the assigned username from here on (blank names become "Player<ID>")
        username = player.getUsername();

        // If client is on a different version, insert version translator
        // in the outbound pipeline (after encoder, so in outbound direction:
        // handler -> translator -> encoder -> network)
        if (clientVersion != serverVersion) {
            ctx.pipeline().addAfter("encoder", "translator",
                    new VersionTranslator(serverVersion, clientVersion));

            PacketDecoder decoder = ctx.pipeline().get(PacketDecoder.class);
            if (decoder != null) {
                decoder.setProtocolVersion(clientVersion);
            }

            System.out.println("Client '" + username + "' connected with "
                    + clientVersion.getDisplayName() + " protocol — version translator active");
        }

        // Send Server Identification
        ctx.writeAndFlush(new ServerIdentificationPacket(
                serverVersion.getVersionNumber(),
                "RDForward Server",
                "Welcome to RDForward!",
                ServerIdentificationPacket.USER_TYPE_NORMAL
        ));

        // Send world data via Classic level transfer
        sendWorldData(ctx);

        // Restore saved position if available, otherwise spawn at world center
        // Use the assigned username (player.getUsername()), not the raw packet name,
        // because empty names get renamed to "Player<ID>" by addPlayer().
        java.util.Map<String, short[]> savedPositions = world.loadPlayerPositions();
        short[] savedPos = savedPositions.get(player.getUsername());

        short spawnX, spawnY, spawnZ;
        byte spawnYaw = 0, spawnPitch = 0;
        if (savedPos != null) {
            spawnX = savedPos[0];
            spawnY = savedPos[1];
            spawnZ = savedPos[2];
            spawnYaw = (byte) savedPos[3];
            spawnPitch = (byte) savedPos[4];
            System.out.println("Restored position for " + player.getUsername());
        } else {
            // Default: center of world, on top of terrain
            // Surface is at y = height*2/3, feet at +1 block above.
            // Y is eye-level (feet + 1.62) to match the client convention.
            // Compute feet as exact fixed-point, then add ceil(1.62*32) to avoid
            // truncation placing feet inside the surface block.
            spawnX = (short) ((world.getWidth() / 2) * 32 + 16);
            int feetFixedPoint = (world.getHeight() * 2 / 3 + 1) * 32;
            int eyeOffset = (int) Math.ceil(1.62 * 32);  // 52 (not 51)
            spawnY = (short) (feetFixedPoint + eyeOffset);
            spawnZ = (short) ((world.getDepth() / 2) * 32 + 16);
        }
        player.updatePosition(spawnX, spawnY, spawnZ, spawnYaw, spawnPitch);

        // Spawn self (player ID -1 = "this is you")
        ctx.writeAndFlush(new SpawnPlayerPacket(
            -1, username, spawnX, spawnY, spawnZ, spawnYaw & 0xFF, spawnPitch & 0xFF
        ));

        // Send existing players to the new client
        for (ConnectedPlayer existing : playerManager.getAllPlayers()) {
            if (existing != player) {
                ctx.writeAndFlush(new SpawnPlayerPacket(
                    existing.getPlayerId(), existing.getUsername(),
                    existing.getX(), existing.getY(), existing.getZ(),
                    existing.getYaw(), existing.getPitch()
                ));
            }
        }

        // Tab list ADD must precede SpawnPlayer for 1.8+ clients (they resolve player
        // name from tab list by UUID and silently drop SpawnPlayer otherwise).
        playerManager.broadcastPlayerListAdd(player);

        // Broadcast new player's spawn to everyone else
        playerManager.broadcastPlayerSpawn(player);

        loginComplete = true;

        // Remove the login timeout — normal gameplay uses keep-alive pings instead
        if (ctx.pipeline().get("loginTimeout") != null) {
            ctx.pipeline().remove("loginTimeout");
        }

        System.out.println("Login complete: " + username
                + " (protocol: " + clientVersion.getDisplayName()
                + ", version " + clientVersion.getVersionNumber()
                + ", ID " + player.getPlayerId()
                + ", " + playerManager.getPlayerCount() + " online)");

        playerManager.broadcastChat((byte) 0, username + " joined the game");

        // Fire player join event
        ServerEvents.PLAYER_JOIN.invoker().onPlayerJoin(player.getUsername(), clientVersion);
    }

    /**
     * Send the world using Classic level transfer protocol.
     */
    private void sendWorldData(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new LevelInitializePacket());

        try {
            byte[] compressed = world.serializeForClassicProtocol();
            int totalLength = compressed.length;
            int offset = 0;

            while (offset < totalLength) {
                int chunkSize = Math.min(1024, totalLength - offset);
                byte[] chunkData = Arrays.copyOfRange(compressed, offset, offset + chunkSize);

                if (chunkData.length < 1024) {
                    chunkData = Arrays.copyOf(chunkData, 1024);
                }

                int percent = (int) ((offset + chunkSize) * 100L / totalLength);
                ctx.writeAndFlush(new LevelDataChunkPacket(chunkSize, chunkData, percent));
                offset += chunkSize;
            }
        } catch (IOException e) {
            System.err.println("Failed to serialize world: " + e.getMessage());
            ctx.writeAndFlush(new DisconnectPacket("Server error: failed to send world"));
            ctx.close();
            return;
        }

        ctx.writeAndFlush(new LevelFinalizePacket(
            world.getWidth(), world.getHeight(), world.getDepth()
        ));
    }

    private void handleSetBlock(ChannelHandlerContext ctx, SetBlockClientPacket packet) {
        int x = packet.getX();
        int y = packet.getY();
        int z = packet.getZ();
        byte blockType = (packet.getMode() == 0) ? 0 : (byte) packet.getBlockType();

        if (!world.inBounds(x, y, z)) {
            return;
        }

        // RubyDung only has 3 block types. Override placed blocks to match terrain:
        // grass at the surface layer, cobblestone everywhere else.
        if (blockType != 0 && serverVersion == ProtocolVersion.RUBYDUNG) {
            int surfaceY = world.getHeight() * 2 / 3;
            blockType = (y == surfaceY)
                    ? (byte) BlockRegistry.GRASS
                    : (byte) BlockRegistry.COBBLESTONE;
        }

        // Prevent placing blocks inside the player's body.
        // Player AABB: 0.6 wide (±0.3), 1.8 tall from feet.
        if (blockType != 0 && player != null) {
            double px = player.getDoubleX();
            double feetY = player.getDoubleY() - PLAYER_EYE_HEIGHT;
            double pz = player.getDoubleZ();
            boolean overlapsX = x < px + 0.3 && px - 0.3 < x + 1;
            boolean overlapsY = y < feetY + 1.8 && feetY < y + 1;
            boolean overlapsZ = z < pz + 0.3 && pz - 0.3 < z + 1;
            if (overlapsX && overlapsY && overlapsZ) return;
        }

        // Fire cancellable block events
        if (blockType == 0) {
            // Breaking: mode 0 = destroy
            byte existingBlock = world.getBlock(x, y, z);
            EventResult result = ServerEvents.BLOCK_BREAK.invoker()
                    .onBlockBreak(player.getUsername(), x, y, z, existingBlock & 0xFF);
            if (result == EventResult.CANCEL) return;
        } else {
            // Placing
            EventResult result = ServerEvents.BLOCK_PLACE.invoker()
                    .onBlockPlace(player.getUsername(), x, y, z, blockType & 0xFF);
            if (result == EventResult.CANCEL) return;
        }

        // Queue for tick loop processing instead of applying immediately.
        // This ensures block changes are processed at a consistent rate
        // and allows the tick loop to batch/validate them.
        world.queueBlockChange(x, y, z, blockType);
    }

    private void handlePlayerPosition(ChannelHandlerContext ctx, PlayerTeleportPacket packet) {
        if (player == null) return;

        short x = packet.getX();
        short y = packet.getY();
        short z = packet.getZ();
        byte yaw = (byte) packet.getYaw();
        byte pitch = (byte) packet.getPitch();

        // If player falls below the world, teleport to spawn
        if (y / 32 < -10) {
            short spawnX = (short) ((world.getWidth() / 2) * 32 + 16);
            int feetFixed = (world.getHeight() * 2 / 3 + 1) * 32;
            int eyeOffset = (int) Math.ceil(1.62 * 32);
            short spawnY = (short) (feetFixed + eyeOffset);
            short spawnZ = (short) ((world.getDepth() / 2) * 32 + 16);
            player.updatePosition(spawnX, spawnY, spawnZ, yaw, (byte) 0);
            ctx.writeAndFlush(new PlayerTeleportPacket(
                -1, spawnX, spawnY, spawnZ,
                player.getYaw() & 0xFF, 0
            ));
            return;
        }

        player.updatePosition(x, y, z, yaw, pitch);

        // Fire player move event
        ServerEvents.PLAYER_MOVE.invoker().onPlayerMove(player.getUsername(), x, y, z, yaw, pitch);

        // Broadcast absolute position to all other players
        playerManager.broadcastPacketExcept(
            new PlayerTeleportPacket(player.getPlayerId(), x, y, z, yaw & 0xFF, pitch & 0xFF),
            player
        );
    }

    private void handleMessage(ChannelHandlerContext ctx, MessagePacket packet) {
        if (player == null) return;

        String message = packet.getMessage();

        // Route commands (messages starting with "/")
        if (message.startsWith("/")) {
            String command = message.substring(1);
            // Reply sender: sends a private chat message back to the player
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
            // Fire player leave event before cleanup
            ServerEvents.PLAYER_LEAVE.invoker().onPlayerLeave(player.getUsername());
            // Save position before removal so it persists for reconnect
            world.rememberPlayerPosition(player);
            // Unregister from chunk tracking (unloads chunks no longer needed)
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
                System.err.println("Login timeout — client did not send PlayerIdentification within "
                    + LOGIN_TIMEOUT_SECONDS + " seconds");
                ctx.writeAndFlush(new DisconnectPacket("Login timed out"));
            }
            // Post-login timeouts are handled by keep-alive pings, not read timeouts
        } else {
            System.err.println("Connection error"
                + (player != null ? " (" + player.getUsername() + ")" : "")
                + ": " + cause.getMessage());
        }
        ctx.close();
    }
}
