package com.github.martinambrus.rdforward.android.multiplayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Thread-safe singleton managing shared state between Netty I/O threads
 * and the Android game's render thread.
 */
public class MultiplayerState {

    private static final MultiplayerState INSTANCE = new MultiplayerState();
    public static MultiplayerState getInstance() { return INSTANCE; }

    private volatile boolean connected = false;
    private volatile byte[] worldBlocks;
    private volatile int worldWidth, worldHeight, worldDepth;
    private volatile boolean worldReady = false;

    private final Map<Byte, RemotePlayer> remotePlayers = new ConcurrentHashMap<>();
    private final Queue<BlockChange> pendingBlockChanges = new ConcurrentLinkedQueue<>();
    private final Queue<PendingPrediction> pendingPredictions = new ConcurrentLinkedQueue<>();
    private static final long PREDICTION_TIMEOUT_MS = 2000;
    private final Queue<String> pendingChatMessages = new ConcurrentLinkedQueue<>();
    private volatile short[] pendingSelfTeleport = null;
    private volatile byte localPlayerId = -1;
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

    public byte[] getWorldBlocks() { return worldBlocks; }
    public boolean isWorldReady() { return worldReady; }
    public int getWorldWidth() { return worldWidth; }
    public int getWorldHeight() { return worldHeight; }
    public int getWorldDepth() { return worldDepth; }

    // -- Block changes --

    public void queueBlockChange(int x, int y, int z, byte blockType) {
        pendingBlockChanges.add(new BlockChange(x, y, z, blockType));
    }

    public BlockChange pollBlockChange() { return pendingBlockChanges.poll(); }

    // -- Remote players --

    public void addRemotePlayer(byte id, String name, short x, short y, short z, byte yaw, byte pitch) {
        remotePlayers.put(id, new RemotePlayer(id, name, x, y, z, yaw, pitch));
    }

    public void removeRemotePlayer(byte id) { remotePlayers.remove(id); }
    public RemotePlayer getRemotePlayer(byte id) { return remotePlayers.get(id); }
    public Collection<RemotePlayer> getRemotePlayers() { return remotePlayers.values(); }

    // -- Chat --

    public void queueChatMessage(String message) {
        pendingChatMessages.add(message);
        while (pendingChatMessages.size() > 100) pendingChatMessages.poll();
    }

    public String pollChatMessage() { return pendingChatMessages.poll(); }

    // -- Connection state --

    public void setConnected(boolean connected) {
        this.connected = connected;
        if (!connected) {
            remotePlayers.clear();
            pendingBlockChanges.clear();
            pendingPredictions.clear();
            pendingChatMessages.clear();
            pendingSelfTeleport = null;
            localPlayerId = -1;
        }
    }

    public boolean isConnected() { return connected; }

    // -- Self-teleport --

    public void queueSelfTeleport(short x, short y, short z) {
        pendingSelfTeleport = new short[]{x, y, z};
    }

    public short[] pollSelfTeleport() {
        short[] result = pendingSelfTeleport;
        pendingSelfTeleport = null;
        return result;
    }

    public String getServerName() { return serverName; }
    public void setServerName(String name) { this.serverName = name; }
    public String getServerMotd() { return serverMotd; }
    public void setServerMotd(String motd) { this.serverMotd = motd; }

    public void resetWorldData() {
        worldReady = false;
        worldBlocks = null;
        serverName = "";
        serverMotd = "";
    }

    // -- Block predictions --

    public void addPrediction(int x, int y, int z, byte originalBlockType) {
        pendingPredictions.add(new PendingPrediction(x, y, z, originalBlockType, System.currentTimeMillis()));
    }

    public void confirmPrediction(int x, int y, int z) {
        Iterator<PendingPrediction> it = pendingPredictions.iterator();
        while (it.hasNext()) {
            PendingPrediction p = it.next();
            if (p.x == x && p.y == y && p.z == z) { it.remove(); return; }
        }
    }

    public List<PendingPrediction> pollTimedOutPredictions() {
        List<PendingPrediction> timedOut = new ArrayList<>();
        long now = System.currentTimeMillis();
        Iterator<PendingPrediction> it = pendingPredictions.iterator();
        while (it.hasNext()) {
            PendingPrediction p = it.next();
            if (now - p.timestamp > PREDICTION_TIMEOUT_MS) { timedOut.add(p); it.remove(); }
        }
        return timedOut;
    }

    public static class BlockChange {
        public final int x, y, z;
        public final byte blockType;
        public BlockChange(int x, int y, int z, byte blockType) {
            this.x = x; this.y = y; this.z = z; this.blockType = blockType;
        }
    }

    public static class PendingPrediction {
        public final int x, y, z;
        public final byte originalBlockType;
        public final long timestamp;
        public PendingPrediction(int x, int y, int z, byte originalBlockType, long timestamp) {
            this.x = x; this.y = y; this.z = z;
            this.originalBlockType = originalBlockType; this.timestamp = timestamp;
        }
    }
}
