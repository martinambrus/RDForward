package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.alpha.ChangeGameStatePacket;
import com.github.martinambrus.rdforward.protocol.packet.alpha.PlayerPositionAndLookS2CPacket;
import com.github.martinambrus.rdforward.protocol.packet.alpha.TimeUpdatePacket;
import com.github.martinambrus.rdforward.protocol.packet.alpha.TimeUpdatePacketV47;
import com.github.martinambrus.rdforward.protocol.packet.classic.DespawnPlayerPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.MessagePacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.OrientationUpdatePacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.PlayerTeleportPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.PositionOrientationUpdatePacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.PositionUpdatePacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.SpawnPlayerPacket;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyChangeGameStatePacket;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyPlayerPositionS2CPacket;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyPlayerPositionS2CPacketV47;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyPlayerPositionS2CPacketV109;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyPlayerPositionS2CPacketV755;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyTimeUpdatePacket;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyTimeUpdatePacketV768;
import io.netty.channel.Channel;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.LevelEvent;
import org.cloudburstmc.protocol.bedrock.packet.LevelEventPacket;
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetTimePacket;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages connected players on the server.
 *
 * Handles player ID assignment (MC Classic uses signed byte IDs, 0-127 usable,
 * -1 reserved for "self" in spawn packets), join/leave lifecycle, and
 * broadcasting packets to all or specific players.
 *
 * Thread-safe: uses ConcurrentHashMap since Netty I/O threads and the
 * tick loop access this concurrently.
 */
public class PlayerManager {

    /** Hard ceiling for player IDs: MC Classic uses signed byte IDs (0-127). */
    public static final int MAX_PLAYER_IDS = 128;

    /** Configurable max players (set via ServerProperties). Capped at MAX_PLAYER_IDS. */
    private static volatile int maxPlayers = MAX_PLAYER_IDS;

    public static int getMaxPlayers() { return maxPlayers; }

    public static void setMaxPlayers(int max) {
        int clamped = Math.max(1, Math.min(MAX_PLAYER_IDS, max));
        if (clamped != max) {
            System.err.println("[WARN] max-players=" + max + " clamped to " + clamped
                    + " (valid range: 1-" + MAX_PLAYER_IDS + ")");
        }
        maxPlayers = clamped;
    }

    /** Player eye height matching Alpha client precision. */
    private static final double PLAYER_EYE_HEIGHT = (double) 1.62f;

    /** Counter for Netty 1.9+ teleport IDs. */
    private static final AtomicInteger teleportIdCounter = new AtomicInteger();

    /** Map from channel to player (for looking up player by network connection). */
    private final Map<Channel, ConnectedPlayer> playersByChannel = new ConcurrentHashMap<>();

    /** Map from player ID to player (for looking up by ID). */
    private final Map<Byte, ConnectedPlayer> playersById = new ConcurrentHashMap<>();

    /** Tracks which IDs are in use. */
    private final boolean[] usedIds = new boolean[MAX_PLAYER_IDS];

    /** Shared inventory adapter for cross-version inventory tracking. */
    private final InventoryAdapter inventoryAdapter = new InventoryAdapter();

    /**
     * Register a new player. Assigns the next available player ID.
     * If the username is empty/blank, generates "Player&lt;ID&gt;".
     * If the username is already taken, appends a number to make it unique.
     * Channel may be null for Bedrock players (they use a different transport).
     * Returns null if the server is full.
     */
    public synchronized ConnectedPlayer addPlayer(String username, Channel channel, ProtocolVersion version) {
        byte id = allocateId();
        if (id == -1) {
            return null; // Server full
        }

        // Generate a default name if none provided
        if (username == null || username.trim().isEmpty()) {
            username = "Player" + (id + 1);
        } else {
            username = username.trim();
        }

        // Ensure uniqueness: if the name is taken, append a number
        String baseName = username;
        int suffix = 2;
        while (isNameTaken(username)) {
            username = baseName + suffix;
            suffix++;
        }

        ConnectedPlayer player = new ConnectedPlayer(id, username, channel, version);
        if (channel != null) {
            playersByChannel.put(channel, player);
        }
        playersById.put(id, player);
        return player;
    }

    /**
     * Remove a player by their network channel and free their ID.
     */
    public synchronized void removePlayer(Channel channel) {
        ConnectedPlayer player = playersByChannel.remove(channel);
        if (player != null) {
            playersById.remove(player.getPlayerId());
            usedIds[player.getPlayerId()] = false;
        }
    }

    /**
     * Remove a player by their ID and free the ID.
     * Used for Bedrock players which don't have a Netty channel.
     */
    public synchronized void removePlayerById(byte playerId) {
        ConnectedPlayer player = playersById.remove(playerId);
        if (player != null) {
            if (player.getChannel() != null) {
                playersByChannel.remove(player.getChannel());
            }
            usedIds[playerId] = false;
        }
    }

    /**
     * Look up a player by their network channel.
     */
    public ConnectedPlayer getPlayer(Channel channel) {
        return playersByChannel.get(channel);
    }

    /**
     * Look up a player by their ID.
     */
    public ConnectedPlayer getPlayer(byte playerId) {
        return playersById.get(playerId);
    }

    /**
     * Get all connected players (both TCP and Bedrock).
     */
    public Collection<ConnectedPlayer> getAllPlayers() {
        return playersById.values();
    }

    /**
     * Get the shared inventory adapter.
     */
    public InventoryAdapter getInventoryAdapter() {
        return inventoryAdapter;
    }

    /**
     * Get the current player count.
     */
    public int getPlayerCount() {
        return playersById.size();
    }

    /**
     * Send a packet to all connected players.
     * Uses write+flush batching so the PrioritizingOutboundHandler
     * can reorder queued packets by priority before flushing.
     */
    public void broadcastPacket(Packet packet) {
        for (ConnectedPlayer player : playersById.values()) {
            player.writePacket(packet);
        }
        for (ConnectedPlayer player : playersById.values()) {
            player.flushPackets();
        }
    }

    /**
     * Send a packet to all players except the specified one.
     * Uses write+flush batching for priority reordering.
     * Entity position packets are throttled for high-RTT players to
     * prevent saturating their connection with updates they can't keep up with.
     */
    public void broadcastPacketExcept(Packet packet, ConnectedPlayer exclude) {
        boolean isEntityPositionPacket = (packet instanceof PlayerTeleportPacket)
                || (packet instanceof PositionOrientationUpdatePacket)
                || (packet instanceof PositionUpdatePacket)
                || (packet instanceof OrientationUpdatePacket);

        for (ConnectedPlayer player : playersById.values()) {
            if (player == exclude) continue;
            if (isEntityPositionPacket) {
                int tier = player.getRttTier();
                if (tier == 1 && player.incrementAndGetThrottleCounter() % 2 != 0) continue;
                else if (tier == 2 && player.incrementAndGetThrottleCounter() % 5 != 0) continue;
            }
            player.writePacket(packet);
        }
        for (ConnectedPlayer player : playersById.values()) {
            if (player != exclude) {
                player.flushPackets();
            }
        }
    }

    /**
     * Broadcast a chat message to all players.
     */
    public void broadcastChat(byte senderId, String message) {
        broadcastPacket(new MessagePacket(senderId, message));
    }

    /**
     * Send a chat message to a specific player (e.g., command response).
     */
    public void sendChat(ConnectedPlayer player, String message) {
        player.sendPacket(new MessagePacket((byte) 0, message));
    }

    /**
     * Broadcast a time update to all players using version-appropriate packets.
     *
     * @param worldAge  total ticks since world creation (Netty format)
     * @param timeOfDay current time of day (0-24000, negative = frozen)
     */
    public void broadcastTimeUpdate(long worldAge, long timeOfDay) {
        // Pre-1.6.1 clients don't understand negative-means-frozen convention;
        // negative values wrap to nighttime. Use absolute value for them.
        // The negative convention was added in 1.6.1 alongside doDaylightCycle.
        TimeUpdatePacket preNetty = new TimeUpdatePacket(Math.abs(timeOfDay));
        TimeUpdatePacketV47 preNettyV47 = new TimeUpdatePacketV47(worldAge, timeOfDay);
        TimeUpdatePacketV47 preNettyV47Abs = new TimeUpdatePacketV47(worldAge, Math.abs(timeOfDay));
        NettyTimeUpdatePacket netty = new NettyTimeUpdatePacket(worldAge, timeOfDay);
        NettyTimeUpdatePacketV768 nettyV768 = new NettyTimeUpdatePacketV768(worldAge, timeOfDay);

        for (ConnectedPlayer player : playersById.values()) {
            ProtocolVersion v = player.getProtocolVersion();
            if (v == ProtocolVersion.BEDROCK) {
                if (player.getBedrockSession() != null) {
                    SetTimePacket stp = new SetTimePacket();
                    stp.setTime((int) (Math.abs(timeOfDay) % 24000));
                    player.getBedrockSession().getSession().sendPacket(stp);
                } else if (player.getMcpeSession() != null) {
                    player.getMcpeSession().sendTimeUpdate((int) (Math.abs(timeOfDay) % 24000));
                }
            } else if (v.isAtLeast(ProtocolVersion.RELEASE_1_21_2)) {
                player.sendPacket(nettyV768);
            } else if (v.isAtLeast(ProtocolVersion.RELEASE_1_7_2)) {
                player.sendPacket(netty);
            } else if (v.isAtLeast(ProtocolVersion.RELEASE_1_6_1)) {
                player.sendPacket(preNettyV47);
            } else if (v.isAtLeast(ProtocolVersion.RELEASE_1_4_2)) {
                player.sendPacket(preNettyV47Abs);
            } else if (v.isAtLeast(ProtocolVersion.ALPHA_1_0_15)) {
                player.sendPacket(preNetty);
            }
        }
    }

    /**
     * Broadcast a weather state change to all players using version-appropriate packets.
     *
     * @param weather the new weather state
     */
    public void broadcastWeatherChange(ServerWorld.WeatherState weather) {
        boolean isRaining = weather != ServerWorld.WeatherState.CLEAR;
        boolean isThunder = weather == ServerWorld.WeatherState.THUNDER;

        // Pre-Netty: reason 1=begin rain, 2=end rain
        ChangeGameStatePacket preNetty = new ChangeGameStatePacket(
                isRaining ? ChangeGameStatePacket.BEGIN_RAIN : ChangeGameStatePacket.END_RAIN, 0);
        // Netty: reason 2=begin rain, 1=end rain (swapped from pre-Netty)
        NettyChangeGameStatePacket nettyWeather = new NettyChangeGameStatePacket(
                isRaining ? NettyChangeGameStatePacket.BEGIN_RAIN : NettyChangeGameStatePacket.END_RAIN, 0);
        NettyChangeGameStatePacket nettyRainLevel = isRaining
                ? new NettyChangeGameStatePacket(NettyChangeGameStatePacket.RAIN_LEVEL, 1.0f) : null;
        NettyChangeGameStatePacket nettyThunderLevel = new NettyChangeGameStatePacket(
                NettyChangeGameStatePacket.THUNDER_LEVEL, isThunder ? 1.0f : 0.0f);

        for (ConnectedPlayer player : playersById.values()) {
            ProtocolVersion v = player.getProtocolVersion();
            if (v == ProtocolVersion.BEDROCK) {
                if (player.getBedrockSession() != null) {
                    LevelEventPacket lep = new LevelEventPacket();
                    lep.setPosition(Vector3f.ZERO);
                    if (isThunder) {
                        lep.setType(LevelEvent.START_THUNDERSTORM);
                        lep.setData(65535);
                    } else if (isRaining) {
                        lep.setType(LevelEvent.START_RAINING);
                        lep.setData(65535);
                    } else {
                        lep.setType(LevelEvent.STOP_RAINING);
                        lep.setData(0);
                    }
                    player.getBedrockSession().getSession().sendPacket(lep);
                }
            } else if (v.isAtLeast(ProtocolVersion.RELEASE_1_7_2)) {
                player.sendPacket(nettyWeather);
                if (nettyRainLevel != null) {
                    player.sendPacket(nettyRainLevel);
                }
                player.sendPacket(nettyThunderLevel);
            } else if (v.isAtLeast(ProtocolVersion.BETA_1_5)) {
                player.sendPacket(preNetty);
            }
        }
    }

    /**
     * Send a spawn packet for the given player to all other players.
     */
    public void broadcastPlayerSpawn(ConnectedPlayer player) {
        SpawnPlayerPacket spawn = new SpawnPlayerPacket(
            player.getPlayerId(), player.getUsername(),
            player.getX(), player.getY(), player.getZ(),
            player.getYaw(), player.getPitch()
        );
        broadcastPacketExcept(spawn, player);
    }

    /**
     * Send a despawn packet for the given player to all other players.
     */
    public void broadcastPlayerDespawn(ConnectedPlayer player) {
        broadcastPacketExcept(new DespawnPlayerPacket(player.getPlayerId()), player);
    }

    /**
     * Broadcast a Tab list "add" entry for the given player to all v17+ players.
     */
    public void broadcastPlayerListAdd(ConnectedPlayer newPlayer) {
        com.github.martinambrus.rdforward.protocol.packet.alpha.PlayerListItemPacket packet =
                new com.github.martinambrus.rdforward.protocol.packet.alpha.PlayerListItemPacket(
                        newPlayer.getUsername(), true, 0);
        for (ConnectedPlayer p : playersById.values()) {
            if (p.getProtocolVersion().isAtLeast(ProtocolVersion.BETA_1_8)) {
                p.sendPacket(packet);
            }
        }
    }

    /**
     * Broadcast a Tab list "remove" entry for the given player to all v17+ players.
     */
    public void broadcastPlayerListRemove(ConnectedPlayer leavingPlayer) {
        com.github.martinambrus.rdforward.protocol.packet.alpha.PlayerListItemPacket packet =
                new com.github.martinambrus.rdforward.protocol.packet.alpha.PlayerListItemPacket(
                        leavingPlayer.getUsername(), false, 0);
        for (ConnectedPlayer p : playersById.values()) {
            if (p.getProtocolVersion().isAtLeast(ProtocolVersion.BETA_1_8)) {
                p.sendPacket(packet);
            }
        }
    }

    /**
     * Find an online player by username (case-insensitive).
     * Returns null if no player with that name is connected.
     */
    public ConnectedPlayer getPlayerByName(String name) {
        for (ConnectedPlayer p : playersById.values()) {
            if (p.getUsername().equalsIgnoreCase(name)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Kick an existing player with the given username to make room for a
     * new connection. Used when a non-blank username is already online.
     * Handles cleanup: despawn broadcast, position save, removal.
     *
     * @param username the username to kick (case-insensitive match)
     * @param world    the server world (to save the player's position)
     */
    public void kickDuplicatePlayer(String username, ServerWorld world) {
        ConnectedPlayer existing = getPlayerByName(username);
        if (existing == null) return;

        System.out.println("Kicking duplicate login for " + existing.getUsername());
        broadcastPlayerListRemove(existing);
        world.rememberPlayerPosition(existing);
        broadcastChat((byte) 0, existing.getUsername() + " left the game");
        broadcastPlayerDespawn(existing);

        // Send disconnect reason then close. For TCP clients (Classic/Alpha),
        // sendPacket goes through the pipeline translator. For Bedrock,
        // disconnect(reason) sends the reason natively.
        if (existing.getBedrockSession() != null) {
            existing.getBedrockSession().disconnect("Logged in from another location");
        } else {
            existing.sendPacket(new com.github.martinambrus.rdforward.protocol.packet.classic.DisconnectPacket(
                    "Logged in from another location"));
            existing.disconnect();
        }

        // Clean up maps
        if (existing.getChannel() != null) {
            playersByChannel.remove(existing.getChannel());
        }
        playersById.remove(existing.getPlayerId());
        usedIds[existing.getPlayerId()] = false;
    }

    /**
     * Kick a player by username. Broadcasts leave message and cleans up.
     *
     * @param username the username to kick (case-insensitive)
     * @param reason   the kick reason shown to the player and in chat
     * @param world    the server world (to save position)
     * @return true if the player was found and kicked
     */
    public boolean kickPlayer(String username, String reason, ServerWorld world) {
        ConnectedPlayer existing = getPlayerByName(username);
        if (existing == null) return false;

        broadcastPlayerListRemove(existing);
        world.rememberPlayerPosition(existing);
        broadcastChat((byte) 0, existing.getUsername() + " was kicked: " + reason);
        broadcastPlayerDespawn(existing);

        if (existing.getBedrockSession() != null) {
            existing.getBedrockSession().disconnect(reason);
        } else {
            existing.sendPacket(new com.github.martinambrus.rdforward.protocol.packet.classic.DisconnectPacket(reason));
            existing.disconnect();
        }

        if (existing.getChannel() != null) {
            playersByChannel.remove(existing.getChannel());
        }
        playersById.remove(existing.getPlayerId());
        usedIds[existing.getPlayerId()] = false;
        return true;
    }

    /**
     * Teleport a player to the given coordinates and broadcast the move to others.
     *
     * Sends the correct S2C position packet based on the target's protocol version.
     *
     * @param target      the player to teleport
     * @param x           world X
     * @param eyeY        eye-level Y (internal convention)
     * @param z           world Z
     * @param classicYaw  yaw in Classic convention (0=North)
     * @param pitch       pitch in degrees
     * @param chunkManager chunk manager for updating tracked chunks
     */
    public void teleportPlayer(ConnectedPlayer target, double x, double eyeY, double z,
                                float classicYaw, float pitch, ChunkManager chunkManager) {
        target.updatePositionDouble(x, eyeY, z, classicYaw, pitch);

        double feetY = eyeY - PLAYER_EYE_HEIGHT;
        float alphaYaw = (classicYaw + 180.0f) % 360.0f;

        ProtocolVersion version = target.getProtocolVersion();

        if (version == ProtocolVersion.BEDROCK) {
            // Bedrock MovePlayerPacket uses eye-level Y
            float bedrockYaw = (classicYaw - 180.0f + 360.0f) % 360.0f;
            MovePlayerPacket move = new MovePlayerPacket();
            move.setRuntimeEntityId(target.getPlayerId() + 1);
            move.setPosition(Vector3f.from((float) x, (float) eyeY, (float) z));
            move.setRotation(Vector3f.from(pitch, bedrockYaw, bedrockYaw));
            move.setMode(MovePlayerPacket.Mode.TELEPORT);
            move.setTeleportationCause(MovePlayerPacket.TeleportationCause.UNKNOWN);
            move.setOnGround(true);
            target.getBedrockSession().getSession().sendPacket(move);
        } else if (version.isAtLeast(ProtocolVersion.RELEASE_1_17)) {
            target.sendPacket(new NettyPlayerPositionS2CPacketV755(
                    x, feetY, z, alphaYaw, pitch, teleportIdCounter.incrementAndGet()));
        } else if (version.isAtLeast(ProtocolVersion.RELEASE_1_9)) {
            target.sendPacket(new NettyPlayerPositionS2CPacketV109(
                    x, feetY, z, alphaYaw, pitch, teleportIdCounter.incrementAndGet()));
        } else if (version.isAtLeast(ProtocolVersion.RELEASE_1_8)) {
            target.sendPacket(new NettyPlayerPositionS2CPacketV47(
                    x, feetY, z, alphaYaw, pitch));
        } else if (version.isAtLeast(ProtocolVersion.RELEASE_1_7_2)) {
            target.sendPacket(new NettyPlayerPositionS2CPacket(
                    x, eyeY, z, alphaYaw, pitch, false));
        } else {
            // Pre-Netty (Alpha/Beta/pre-1.7): S2C y=eyes, stance=feet
            target.sendPacket(new PlayerPositionAndLookS2CPacket(
                    x, eyeY, feetY, z, alphaYaw, pitch, true));
        }

        chunkManager.updatePlayerChunks(target);

        // Broadcast position to other players
        short fixedX = (short) (x * 32);
        short fixedY = (short) (eyeY * 32);
        short fixedZ = (short) (z * 32);
        byte byteYaw = (byte) ((classicYaw / 360.0f) * 256);
        byte bytePitch = (byte) ((pitch / 360.0f) * 256);
        broadcastPositionUpdate(target, fixedX, fixedY, fixedZ, byteYaw, bytePitch);
    }

    /**
     * Broadcast a position update for a player to all other players.
     * Uses delta compression when the movement fits in signed byte range (±4 blocks),
     * falling back to absolute teleport packets for larger movements.
     */
    public void broadcastPositionUpdate(ConnectedPlayer player, short fixedX, short fixedY, short fixedZ,
                                        byte byteYaw, byte bytePitch) {
        if (player.hasBroadcastPosition()) {
            int dx = fixedX - player.getLastBroadcastX();
            int dy = fixedY - player.getLastBroadcastY();
            int dz = fixedZ - player.getLastBroadcastZ();
            if (dx >= -128 && dx <= 127 && dy >= -128 && dy <= 127 && dz >= -128 && dz <= 127) {
                broadcastPacketExcept(
                        new PositionOrientationUpdatePacket(player.getPlayerId(),
                                dx, dy, dz, byteYaw & 0xFF, bytePitch & 0xFF),
                        player);
            } else {
                broadcastPacketExcept(
                        new PlayerTeleportPacket(player.getPlayerId(),
                                fixedX, fixedY, fixedZ, byteYaw & 0xFF, bytePitch & 0xFF),
                        player);
            }
        } else {
            broadcastPacketExcept(
                    new PlayerTeleportPacket(player.getPlayerId(),
                            fixedX, fixedY, fixedZ, byteYaw & 0xFF, bytePitch & 0xFF),
                    player);
        }
        player.updateLastBroadcastPosition(fixedX, fixedY, fixedZ, byteYaw, bytePitch);
    }

    /**
     * Extract the IP address string from a socket address.
     * Returns null if the address is not an InetSocketAddress.
     */
    public static String extractIp(java.net.SocketAddress addr) {
        if (addr instanceof InetSocketAddress) {
            return ((InetSocketAddress) addr).getAddress().getHostAddress();
        }
        return null;
    }

    /**
     * Extract the IP address string from a player's connection.
     * Returns null if unable to determine.
     */
    public static String extractIp(ConnectedPlayer player) {
        if (player.getBedrockSession() != null) {
            String ip = extractIp(player.getBedrockSession().getSession().getPeer().getSocketAddress());
            if (ip != null) return ip;
        }
        if (player.getChannel() != null) {
            return extractIp(player.getChannel().remoteAddress());
        }
        return null;
    }

    /**
     * Check if a username is already taken by a connected player.
     */
    private boolean isNameTaken(String name) {
        for (ConnectedPlayer p : playersById.values()) {
            if (p.getUsername().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Allocate the next available player ID (0-127).
     * Returns -1 if all slots are taken.
     */
    private byte allocateId() {
        for (int i = 0; i < maxPlayers; i++) {
            if (!usedIds[i]) {
                usedIds[i] = true;
                return (byte) i;
            }
        }
        return -1;
    }
}
