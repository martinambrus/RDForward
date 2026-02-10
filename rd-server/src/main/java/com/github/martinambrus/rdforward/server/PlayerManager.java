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
     * Returns null if the server is full.
     */
    public synchronized ConnectedPlayer addPlayer(String username, Channel channel, ProtocolVersion version) {
        byte id = allocateId();
        if (id == -1) {
            return null; // Server full
        }

        ConnectedPlayer player = new ConnectedPlayer(id, username, channel, version);
        playersByChannel.put(channel, player);
        playersById.put(id, player);
        return player;
    }

    /**
     * Remove a player and free their ID.
     */
    public synchronized void removePlayer(Channel channel) {
        ConnectedPlayer player = playersByChannel.remove(channel);
        if (player != null) {
            playersById.remove(player.getPlayerId());
            usedIds[player.getPlayerId()] = false;
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
     * Get all connected players.
     */
    public Collection<ConnectedPlayer> getAllPlayers() {
        return playersByChannel.values();
    }

    /**
     * Get the current player count.
     */
    public int getPlayerCount() {
        return playersByChannel.size();
    }

    /**
     * Send a packet to all connected players.
     */
    public void broadcastPacket(Packet packet) {
        for (ConnectedPlayer player : playersByChannel.values()) {
            player.sendPacket(packet);
        }
    }

    /**
     * Send a packet to all players except the specified one.
     */
    public void broadcastPacketExcept(Packet packet, ConnectedPlayer exclude) {
        for (ConnectedPlayer player : playersByChannel.values()) {
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
