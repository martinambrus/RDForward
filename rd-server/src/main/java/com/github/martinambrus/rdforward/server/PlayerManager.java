package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.classic.DespawnPlayerPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.MessagePacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.SpawnPlayerPacket;
import io.netty.channel.Channel;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    /** Maximum number of simultaneous players (MC Classic limit: 128). */
    public static final int MAX_PLAYERS = 128;

    /** Map from channel to player (for looking up player by network connection). */
    private final Map<Channel, ConnectedPlayer> playersByChannel = new ConcurrentHashMap<>();

    /** Map from player ID to player (for looking up by ID). */
    private final Map<Byte, ConnectedPlayer> playersById = new ConcurrentHashMap<>();

    /** Tracks which IDs are in use. */
    private final boolean[] usedIds = new boolean[MAX_PLAYERS];

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
     * Get the current player count.
     */
    public int getPlayerCount() {
        return playersById.size();
    }

    /**
     * Send a packet to all connected players.
     */
    public void broadcastPacket(Packet packet) {
        for (ConnectedPlayer player : playersById.values()) {
            player.sendPacket(packet);
        }
    }

    /**
     * Send a packet to all players except the specified one.
     */
    public void broadcastPacketExcept(Packet packet, ConnectedPlayer exclude) {
        for (ConnectedPlayer player : playersById.values()) {
            if (player != exclude) {
                player.sendPacket(packet);
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
        for (int i = 0; i < MAX_PLAYERS; i++) {
            if (!usedIds[i]) {
                usedIds[i] = true;
                return (byte) i;
            }
        }
        return -1;
    }
}
