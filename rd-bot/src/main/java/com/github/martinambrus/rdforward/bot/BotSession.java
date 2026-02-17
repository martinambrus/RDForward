package com.github.martinambrus.rdforward.bot;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.alpha.*;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyBlockPlacementPacket;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyBlockPlacementPacketV47;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyChatC2SPacket;
import com.github.martinambrus.rdforward.protocol.packet.netty.PlayerDiggingPacketV47;
import io.netty.channel.Channel;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * Tracks state received from S2C packets during a bot session.
 * Thread-safe: all collections use concurrent variants, wait methods
 * use CountDownLatch or polling with timeout.
 */
public class BotSession {

    private final Channel channel;
    private final ProtocolVersion version;

    // Login state
    private volatile int entityId;
    private volatile double x, y, z;
    private volatile float yaw, pitch;
    private volatile boolean loginComplete;
    private final CountDownLatch loginLatch = new CountDownLatch(1);

    // Received packets
    private final CopyOnWriteArrayList<Packet> receivedPackets = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<String> chatMessages = new CopyOnWriteArrayList<>();
    /** Packed (x, y, z) -> blockType from BlockChangePacket. */
    private final ConcurrentHashMap<Long, Integer> blockChanges = new ConcurrentHashMap<>();
    /** entityId -> playerName from SpawnPlayerPacket. */
    private final ConcurrentHashMap<Integer, String> spawnedPlayers = new ConcurrentHashMap<>();
    /** Entity IDs seen in DestroyEntity packets. */
    private final Set<Integer> despawnedEntities = ConcurrentHashMap.newKeySet();
    /** Incremented each time the server sends a position update. */
    private final AtomicInteger positionUpdateCount = new AtomicInteger();
    /** Packed (chunkX, chunkZ) -> byte[32768] block IDs in AlphaChunk YZX order. */
    private final ConcurrentHashMap<Long, byte[]> chunkBlocks = new ConcurrentHashMap<>();
    /** Spawn Y (set once during markLoginComplete from current y). */
    private volatile double spawnY = Double.NaN;

    // Listeners for wait-for-packet
    private final CopyOnWriteArrayList<PacketListener<?>> packetListeners = new CopyOnWriteArrayList<>();

    public BotSession(Channel channel, ProtocolVersion version) {
        this.channel = channel;
        this.version = version;
    }

    // ---- Recording methods (called by BotPacketHandler) ----

    void recordPacket(Packet packet) {
        receivedPackets.add(packet);
        notifyListeners(packet);
    }

    void recordLogin(int entityId) {
        this.entityId = entityId;
    }

    void recordPosition(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        positionUpdateCount.incrementAndGet();
    }

    void markLoginComplete() {
        if (Double.isNaN(spawnY)) {
            spawnY = y;
        }
        this.loginComplete = true;
        loginLatch.countDown();
    }

    void recordChat(String message) {
        chatMessages.add(message);
    }

    void recordBlockChange(int x, int y, int z, int blockType) {
        blockChanges.put(packCoords(x, y, z), blockType);
    }

    void recordSpawnPlayer(int entityId, String playerName) {
        spawnedPlayers.put(entityId, playerName);
    }

    void recordDespawn(int entityId) {
        despawnedEntities.add(entityId);
    }

    void recordChunkBlocks(int chunkX, int chunkZ, byte[] blockIds) {
        chunkBlocks.put(packChunkCoord(chunkX, chunkZ), blockIds);
    }

    // ---- Wait methods ----

    public boolean waitForLogin(long timeoutMs) throws InterruptedException {
        return loginLatch.await(timeoutMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Wait for a specific packet type to be received.
     * Returns the first matching packet, or null on timeout.
     */
    @SuppressWarnings("unchecked")
    public <T extends Packet> T waitForPacket(Class<T> packetClass, long timeoutMs) throws InterruptedException {
        // Check already-received packets first
        for (Packet p : receivedPackets) {
            if (packetClass.isInstance(p)) {
                return (T) p;
            }
        }

        // Set up listener for future packets
        PacketListener<T> listener = new PacketListener<>(packetClass, p -> true);
        packetListeners.add(listener);
        try {
            return listener.await(timeoutMs);
        } finally {
            packetListeners.remove(listener);
        }
    }

    /**
     * Wait for a chat message matching the given regex pattern.
     * Returns the matching message, or null on timeout.
     */
    public String waitForChat(String regex, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;

        // Check already-received messages
        for (String msg : chatMessages) {
            if (msg.matches(".*" + regex + ".*")) {
                return msg;
            }
        }

        // Poll for new messages
        while (System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
            for (String msg : chatMessages) {
                if (msg.matches(".*" + regex + ".*")) {
                    return msg;
                }
            }
        }
        return null;
    }

    /**
     * Wait for a SpawnPlayerPacket (or SpawnPlayerPacketV39) to be received.
     * Returns the first one, or null on timeout.
     */
    public Packet waitForPlayerSpawn(long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;

        // Check already-received packets
        for (Packet p : receivedPackets) {
            if (p instanceof SpawnPlayerPacket || p instanceof SpawnPlayerPacketV39) {
                return p;
            }
        }

        // Poll for new packets
        while (System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
            for (Packet p : receivedPackets) {
                if (p instanceof SpawnPlayerPacket || p instanceof SpawnPlayerPacketV39) {
                    return p;
                }
            }
        }
        return null;
    }

    /**
     * Wait for a block change at specific coordinates.
     * Returns the block type, or -1 on timeout.
     */
    public int waitForBlockChange(int x, int y, int z, long timeoutMs) throws InterruptedException {
        long key = packCoords(x, y, z);
        long deadline = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < deadline) {
            Integer blockType = blockChanges.get(key);
            if (blockType != null) {
                return blockType;
            }
            Thread.sleep(50);
        }
        return -1;
    }

    /**
     * Wait for a block change at specific coordinates to reach the expected value.
     * Useful for waiting for a block to become air (0) after breaking.
     * Returns the block type, or -1 on timeout.
     */
    public int waitForBlockChangeValue(int x, int y, int z, int expectedValue, long timeoutMs)
            throws InterruptedException {
        long key = packCoords(x, y, z);
        long deadline = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < deadline) {
            Integer val = blockChanges.get(key);
            if (val != null && val == expectedValue) {
                return val;
            }
            Thread.sleep(50);
        }
        Integer val = blockChanges.get(key);
        return val != null ? val : -1;
    }

    /**
     * Wait for an entity to be despawned (DestroyEntity received).
     * Returns true if seen before timeout, false otherwise.
     */
    public boolean waitForDespawn(int entityId, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (despawnedEntities.contains(entityId)) {
                return true;
            }
            Thread.sleep(50);
        }
        return despawnedEntities.contains(entityId);
    }

    /**
     * Returns the current position update count (incremented on each S2C position packet).
     */
    public int getPositionUpdateCount() {
        return positionUpdateCount.get();
    }

    /**
     * Wait for a position update beyond the given previous count.
     * Returns true if a new update arrived before timeout.
     */
    public boolean waitForPositionUpdate(int previousCount, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (positionUpdateCount.get() > previousCount) {
                return true;
            }
            Thread.sleep(50);
        }
        return positionUpdateCount.get() > previousCount;
    }

    // ---- Send methods ----

    public void sendPacket(Packet packet) {
        channel.writeAndFlush(packet);
    }

    public void sendChat(String message) {
        if (version.isAtLeast(ProtocolVersion.RELEASE_1_7_2)) {
            sendPacket(new NettyChatC2SPacket(message));
        } else {
            sendPacket(new ChatPacket(message));
        }
    }

    public void sendPosition(double x, double y, double z, float yaw, float pitch) {
        // C2S: y = feet, stance = eyes
        double feetY = y;
        double eyesY = y + (double) 1.62f;
        sendPacket(new PlayerPositionAndLookC2SPacket(x, feetY, eyesY, z, yaw, pitch, true));
    }

    /**
     * Send a block placement packet using the correct format for this session's
     * protocol version.
     */
    public void sendBlockPlace(int x, int y, int z, int direction, int itemId) {
        if (version.isAtLeast(ProtocolVersion.RELEASE_1_8)) {
            // Netty 1.8+: packed Position coordinates, V47 slot format
            sendPacket(new NettyBlockPlacementPacketV47(x, y, z, direction, (short) itemId));
            return;
        }
        if (version.isAtLeast(ProtocolVersion.RELEASE_1_7_2)) {
            // Netty 1.7.2-1.7.10: int coordinates, short(-1) slot format
            sendPacket(new NettyBlockPlacementPacket(x, y, z, direction, (short) itemId));
            return;
        }
        if (version.isAtLeast(ProtocolVersion.RELEASE_1_3_1)) {
            // v39+: unconditional NBT + cursor bytes
            sendPacket(new PlayerBlockPlacementPacketV39(
                    x, y, z, direction, (short) itemId, (byte) 64, (short) 0));
        } else if (version.isAtLeast(ProtocolVersion.BETA_1_9_PRE5)) {
            // v21-v29: conditional NBT for damageable items
            sendPacket(new PlayerBlockPlacementPacketV22(
                    x, y, z, direction, (short) itemId, (byte) 64, (short) 0));
        } else if (version.isAtLeast(ProtocolVersion.BETA_1_8)) {
            // v17-v20: short damage
            sendPacket(new PlayerBlockPlacementPacketV17(
                    x, y, z, direction, (short) itemId, (byte) 64, (short) 0));
        } else if (version.isAtLeast(ProtocolVersion.BETA_1_0)) {
            // v7-v16: Beta format (coords first, byte damage)
            sendPacket(new PlayerBlockPlacementPacketBeta(
                    x, y, z, direction, (short) itemId, (byte) 64, (byte) 0));
        } else {
            // Alpha v1-v6: item-first format
            sendPacket(new PlayerBlockPlacementPacket(x, y, z, direction, (short) itemId));
        }
    }

    public void sendDigging(int status, int x, int y, int z, int face) {
        if (version.isAtLeast(ProtocolVersion.RELEASE_1_8)) {
            sendPacket(new PlayerDiggingPacketV47(status, x, y, z, face));
        } else {
            sendPacket(new PlayerDiggingPacket(status, x, y, z, face));
        }
    }

    // ---- Getters ----

    public int getEntityId() { return entityId; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public boolean isLoginComplete() { return loginComplete; }
    public ProtocolVersion getVersion() { return version; }
    public CopyOnWriteArrayList<Packet> getReceivedPackets() { return receivedPackets; }
    public CopyOnWriteArrayList<String> getChatMessages() { return chatMessages; }
    public ConcurrentHashMap<Integer, String> getSpawnedPlayers() { return spawnedPlayers; }
    public ConcurrentHashMap<Long, Integer> getBlockChanges() { return blockChanges; }
    public Channel getChannel() { return channel; }
    public double getSpawnY() { return spawnY; }

    /**
     * Returns the block ID at the given world coordinates, or -1 if the
     * chunk data for that position has not been received.
     */
    public int getBlockAt(int worldX, int worldY, int worldZ) {
        int chunkX = worldX >> 4;
        int chunkZ = worldZ >> 4;
        byte[] blocks = chunkBlocks.get(packChunkCoord(chunkX, chunkZ));
        if (blocks == null) return -1;
        int localX = worldX & 15;
        int localZ = worldZ & 15;
        // AlphaChunk YZX order: index = y + z*128 + x*2048
        if (worldY < 0 || worldY >= 128) return -1;
        int index = worldY + localZ * 128 + localX * 2048;
        return blocks[index] & 0xFF;
    }

    /**
     * Returns true if the bot is standing on solid ground: the block at
     * feet level is air and the block below is solid (non-zero).
     * Returns false if chunk data is missing.
     */
    public boolean isOnGround() {
        // Alpha S2C Y = eyes; Netty 1.7.x Y = eyes; Netty 1.8+ Y = feet
        double feetY;
        if (version.isAtLeast(ProtocolVersion.RELEASE_1_8)) {
            feetY = y;
        } else {
            feetY = y - (double) 1.62f;
        }
        int blockX = (int) Math.floor(x);
        int feetBlockY = (int) Math.floor(feetY);
        int blockZ = (int) Math.floor(z);

        int feetBlock = getBlockAt(blockX, feetBlockY, blockZ);
        int belowBlock = getBlockAt(blockX, feetBlockY - 1, blockZ);
        if (feetBlock == -1 || belowBlock == -1) return false;
        return feetBlock == 0 && belowBlock != 0;
    }

    // ---- Helpers ----

    private static long packChunkCoord(int chunkX, int chunkZ) {
        return ((long) chunkX & 0xFFFFFFFFL) << 32 | ((long) chunkZ & 0xFFFFFFFFL);
    }

    private static long packCoords(int x, int y, int z) {
        return ((long) x & 0xFFFFFFFFL) << 32 | ((long) y & 0xFFL) << 24 | ((long) z & 0xFFFFFFL);
    }

    @SuppressWarnings("unchecked")
    private void notifyListeners(Packet packet) {
        for (PacketListener<?> listener : packetListeners) {
            listener.tryMatch(packet);
        }
    }

    /**
     * Internal listener that waits for a specific packet type.
     */
    static class PacketListener<T extends Packet> {
        private final Class<T> packetClass;
        private final Predicate<T> predicate;
        private final CountDownLatch latch = new CountDownLatch(1);
        private volatile T result;

        PacketListener(Class<T> packetClass, Predicate<T> predicate) {
            this.packetClass = packetClass;
            this.predicate = predicate;
        }

        @SuppressWarnings("unchecked")
        void tryMatch(Packet packet) {
            if (result != null) return;
            if (packetClass.isInstance(packet)) {
                T typed = (T) packet;
                if (predicate.test(typed)) {
                    result = typed;
                    latch.countDown();
                }
            }
        }

        T await(long timeoutMs) throws InterruptedException {
            latch.await(timeoutMs, TimeUnit.MILLISECONDS);
            return result;
        }
    }
}
