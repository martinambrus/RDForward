package com.github.martinambrus.rdforward.client;

import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Shared multiplayer state accessible from both Netty I/O threads
 * and the game's render thread.
 *
 * This is the bridge between the network layer (ClientConnectionHandler)
 * and the game's Mixin-injected code. All fields are thread-safe.
 *
 * World data flow:
 *   Network thread writes world blocks → game thread reads them for rendering
 *   Game thread writes block changes → network thread sends SetBlockClient packets
 */
public class MultiplayerState {

    private static final MultiplayerState INSTANCE = new MultiplayerState();

    public static MultiplayerState getInstance() { return INSTANCE; }

    /** Whether we're currently connected to a server. */
    private volatile boolean connected = false;

    /** The world block data received from the server (Classic format). */
    private volatile byte[] worldBlocks;
    private volatile int worldWidth, worldHeight, worldDepth;
    private volatile boolean worldReady = false;

    /** Remote players currently in the game. */
    private final Map<Byte, RemotePlayer> remotePlayers = new ConcurrentHashMap<>();

    /** Queued block changes from the server to apply on the game thread. */
    private final Queue<BlockChange> pendingBlockChanges = new ConcurrentLinkedQueue<>();

    /** Queued chat messages from the server. */
    private final Queue<String> pendingChatMessages = new ConcurrentLinkedQueue<>();

    /** Our assigned player ID from the server. */
    private volatile byte localPlayerId = -1;

    /** Server info received during login. */
    private volatile String serverName = "";
    private volatile String serverMotd = "";

    private MultiplayerState() {}

    // -- World data --

    public void setWorldData(byte[] blocks, int width, int height, int depth) {
        this.worldBlocks = blocks;
        this.worldWidth = width;
        this.worldHeight = height;
        this.worldDepth = depth;
        this.worldReady = true;
    }

    public byte getBlock(int x, int y, int z) {
        if (!worldReady || worldBlocks == null) return 0;
        if (x < 0 || x >= worldWidth || y < 0 || y >= worldHeight || z < 0 || z >= worldDepth) return 0;
        return worldBlocks[(y * worldDepth + z) * worldWidth + x];
    }

    public byte[] getWorldBlocks() { return worldBlocks; }
    public boolean isWorldReady() { return worldReady; }
    public int getWorldWidth() { return worldWidth; }
    public int getWorldHeight() { return worldHeight; }
    public int getWorldDepth() { return worldDepth; }

    // -- Block changes --

    public void queueBlockChange(int x, int y, int z, byte blockType) {
        pendingBlockChanges.add(new BlockChange(x, y, z, blockType));
    }

    public BlockChange pollBlockChange() {
        return pendingBlockChanges.poll();
    }

    // -- Remote players --

    public void addRemotePlayer(byte id, String name, short x, short y, short z, byte yaw, byte pitch) {
        remotePlayers.put(id, new RemotePlayer(id, name, x, y, z, yaw, pitch));
    }

    public void removeRemotePlayer(byte id) {
        remotePlayers.remove(id);
    }

    public RemotePlayer getRemotePlayer(byte id) {
        return remotePlayers.get(id);
    }

    public Collection<RemotePlayer> getRemotePlayers() {
        return remotePlayers.values();
    }

    // -- Chat --

    public void queueChatMessage(String message) {
        pendingChatMessages.add(message);
        // Keep only the last 100 messages
        while (pendingChatMessages.size() > 100) {
            pendingChatMessages.poll();
        }
    }

    public String pollChatMessage() {
        return pendingChatMessages.poll();
    }

    // -- Connection state --

    public void setConnected(boolean connected) {
        this.connected = connected;
        if (!connected) {
            // Only clear transient state, NOT world data.
            // World data is managed explicitly via resetWorldData().
            // This prevents a race where the old connection's channelInactive
            // fires after a new connection has already loaded world data.
            remotePlayers.clear();
            pendingBlockChanges.clear();
            pendingChatMessages.clear();
            localPlayerId = -1;
        }
    }

    public boolean isConnected() { return connected; }
    public byte getLocalPlayerId() { return localPlayerId; }
    public void setLocalPlayerId(byte id) { this.localPlayerId = id; }
    public String getServerName() { return serverName; }
    public void setServerName(String name) { this.serverName = name; }
    public String getServerMotd() { return serverMotd; }
    public void setServerMotd(String motd) { this.serverMotd = motd; }

    /**
     * Clear world data so stale data isn't applied on reconnect.
     * Called at the start of a new connection before world transfer begins.
     */
    public void resetWorldData() {
        worldReady = false;
        worldBlocks = null;
        serverName = "";
        serverMotd = "";
    }

    /**
     * A block change to be applied on the game thread.
     */
    public static class BlockChange {
        public final int x, y, z;
        public final byte blockType;

        public BlockChange(int x, int y, int z, byte blockType) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.blockType = blockType;
        }
    }
}
