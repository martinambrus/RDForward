package com.github.martinambrus.rdforward.bot;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.alpha.*;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyBlockPlacementPacket;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyBlockPlacementPacketV47;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyBlockPlacementPacketV109;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyBlockPlacementPacketV315;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyBlockPlacementPacketV477;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyBlockPlacementPacketV759;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyBlockPlacementPacketV768;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyChatC2SPacket;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyWindowClickPacket;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyWindowClickPacketV47;
import com.github.martinambrus.rdforward.protocol.packet.netty.PlayerDiggingPacketV47;
import com.github.martinambrus.rdforward.protocol.packet.netty.PlayerDiggingPacketV477;
import com.github.martinambrus.rdforward.protocol.packet.netty.PlayerDiggingPacketV759;
import io.netty.channel.Channel;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.BedrockClientSession;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.ItemUseTransaction;
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket;
import com.github.martinambrus.rdforward.server.bedrock.BedrockProtocolConstants;

import java.util.Map;
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
    private final BedrockClientSession bedrockSession;
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
    /** Packed (chunkX, chunkZ) set for Bedrock chunks (received, not yet parsed). */
    private final Set<Long> bedrockChunksReceived = ConcurrentHashMap.newKeySet();
    /** Spawn Y (set once during markLoginComplete from current y). */
    private volatile double spawnY = Double.NaN;

    // Inventory tracking — AddToInventory accumulator (Alpha v1-v6, slot-less 0x11 packets)
    private final ConcurrentHashMap<Integer, AtomicInteger> receivedItemTotals = new ConcurrentHashMap<>();

    // Inventory tracking — SetSlot tracker (Beta+, 0x67 packets target specific slots)
    private final int[] slotItemIds = new int[45];
    private final int[] slotCounts = new int[45];

    // Time tracking
    private volatile long lastTimeOfDay = -1;
    private final AtomicInteger timeUpdateCount = new AtomicInteger();

    // Weather tracking
    private volatile int lastWeatherReason = -1;
    private final AtomicInteger weatherChangeCount = new AtomicInteger();

    // Listeners for wait-for-packet
    private final CopyOnWriteArrayList<PacketListener<?>> packetListeners = new CopyOnWriteArrayList<>();

    public BotSession(Channel channel, ProtocolVersion version) {
        this.channel = channel;
        this.bedrockSession = null;
        this.version = version;
        java.util.Arrays.fill(slotItemIds, -1);
    }

    BotSession(BedrockClientSession bedrockSession, ProtocolVersion version) {
        this.channel = null;
        this.bedrockSession = bedrockSession;
        this.version = version;
        java.util.Arrays.fill(slotItemIds, -1);
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

    void recordBedrockChunk(int chunkX, int chunkZ) {
        bedrockChunksReceived.add(packChunkCoord(chunkX, chunkZ));
    }

    void recordAddToInventory(int itemId, int count) {
        receivedItemTotals.computeIfAbsent(itemId, k -> new AtomicInteger())
                .addAndGet(count);
    }

    void recordSetSlot(int slot, int itemId, int count) {
        if (slot >= 0 && slot < slotItemIds.length) {
            slotItemIds[slot] = itemId;
            slotCounts[slot] = count;
        }
    }

    void recordTimeUpdate(long timeOfDay) {
        this.lastTimeOfDay = timeOfDay;
        timeUpdateCount.incrementAndGet();
    }

    void recordWeatherChange(int reason) {
        this.lastWeatherReason = reason;
        weatherChangeCount.incrementAndGet();
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
     * Wait for a player with the given name to appear in spawnedPlayers.
     * Works across all protocol versions since both TCP and Bedrock bots
     * populate spawnedPlayers via recordSpawnPlayer().
     *
     * @return the entity ID of the spawned player, or null on timeout
     */
    public Integer waitForSpawnedPlayer(String name, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            for (Map.Entry<Integer, String> entry : spawnedPlayers.entrySet()) {
                if (entry.getValue().equals(name)) {
                    return entry.getKey();
                }
            }
            Thread.sleep(50);
        }
        for (Map.Entry<Integer, String> entry : spawnedPlayers.entrySet()) {
            if (entry.getValue().equals(name)) {
                return entry.getKey();
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
     * Wait for chunk data at the given world coordinates to be available.
     * Returns true if the chunk was received before timeout.
     */
    public boolean waitForChunkAt(int worldX, int worldZ, long timeoutMs) throws InterruptedException {
        int chunkX = worldX >> 4;
        int chunkZ = worldZ >> 4;
        long key = packChunkCoord(chunkX, chunkZ);
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (chunkBlocks.containsKey(key) || bedrockChunksReceived.contains(key)) return true;
            Thread.sleep(50);
        }
        return chunkBlocks.containsKey(key) || bedrockChunksReceived.contains(key);
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

    // ---- Time/weather query/wait methods ----

    public long getLastTimeOfDay() { return lastTimeOfDay; }
    public int getTimeUpdateCount() { return timeUpdateCount.get(); }
    public int getLastWeatherReason() { return lastWeatherReason; }
    public int getWeatherChangeCount() { return weatherChangeCount.get(); }

    public boolean waitForTimeUpdate(int previousCount, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (timeUpdateCount.get() > previousCount) return true;
            Thread.sleep(50);
        }
        return timeUpdateCount.get() > previousCount;
    }

    public boolean waitForWeatherChange(int previousCount, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (weatherChangeCount.get() > previousCount) return true;
            Thread.sleep(50);
        }
        return weatherChangeCount.get() > previousCount;
    }

    // ---- Inventory query/wait methods ----

    public int getReceivedItemTotal(int itemId) {
        AtomicInteger total = receivedItemTotals.get(itemId);
        return total != null ? total.get() : 0;
    }

    public boolean waitForReceivedItemTotal(int itemId, int minTotal, long timeoutMs)
            throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (getReceivedItemTotal(itemId) >= minTotal) return true;
            Thread.sleep(50);
        }
        return getReceivedItemTotal(itemId) >= minTotal;
    }

    public int getSlotItemId(int slot) {
        if (slot < 0 || slot >= slotItemIds.length) return -1;
        return slotItemIds[slot];
    }

    public int getSlotCount(int slot) {
        if (slot < 0 || slot >= slotCounts.length) return 0;
        return slotCounts[slot];
    }

    public boolean waitForNonEmptySlot(int slot, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (getSlotItemId(slot) > 0) return true;
            Thread.sleep(50);
        }
        return getSlotItemId(slot) > 0;
    }

    public boolean waitForSlotItem(int slot, int expectedItemId, long timeoutMs)
            throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (getSlotItemId(slot) == expectedItemId) return true;
            Thread.sleep(50);
        }
        return getSlotItemId(slot) == expectedItemId;
    }

    // ---- Send methods ----

    public void sendPacket(Packet packet) {
        channel.writeAndFlush(packet);
    }

    public void sendChat(String message) {
        if (version == ProtocolVersion.BEDROCK) {
            org.cloudburstmc.protocol.bedrock.packet.TextPacket text =
                    new org.cloudburstmc.protocol.bedrock.packet.TextPacket();
            text.setType(org.cloudburstmc.protocol.bedrock.packet.TextPacket.Type.CHAT);
            text.setSourceName("");
            text.setMessage(message);
            text.setNeedsTranslation(false);
            text.setXuid("");
            text.setPlatformChatId("");
            text.setFilteredMessage("");
            bedrockSession.sendPacketImmediately(text);
            return;
        }
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
        if (version == ProtocolVersion.BEDROCK) {
            InventoryTransactionPacket pkt = new InventoryTransactionPacket();
            pkt.setTransactionType(InventoryTransactionType.ITEM_USE);
            pkt.setActionType(0); // click block (place)
            pkt.setBlockPosition(Vector3i.from(x, y, z));
            pkt.setBlockFace(direction);
            pkt.setHotbarSlot(0);
            pkt.setItemInHand(ItemData.AIR);
            pkt.setPlayerPosition(Vector3f.ZERO);
            pkt.setClickPosition(Vector3f.ZERO);
            pkt.setHeadPosition(Vector3f.ZERO);
            pkt.setBlockDefinition(BedrockProtocolConstants.getBlockDefinitions().getDefinition(0));
            pkt.setTriggerType(ItemUseTransaction.TriggerType.PLAYER_INPUT);
            pkt.setClientInteractPrediction(ItemUseTransaction.PredictedResult.SUCCESS);
            bedrockSession.sendPacketImmediately(pkt);
            return;
        }
        if (version.isAtLeast(ProtocolVersion.RELEASE_1_21_2)) {
            // Netty 1.21.2+: V768 format (V759 + worldBorderHit boolean)
            sendPacket(new NettyBlockPlacementPacketV768(x, y, z, direction));
            return;
        }
        if (version.isAtLeast(ProtocolVersion.RELEASE_1_19_1)) {
            // Netty 1.19+: V759 format (V477 + VarInt sequence)
            sendPacket(new NettyBlockPlacementPacketV759(x, y, z, direction));
            return;
        }
        if (version.isAtLeast(ProtocolVersion.RELEASE_1_14)) {
            // Netty 1.14-1.18.2: V477 format
            sendPacket(new NettyBlockPlacementPacketV477(x, y, z, direction));
            return;
        }
        if (version.isAtLeast(ProtocolVersion.RELEASE_1_11)) {
            // Netty 1.11+: packed Position, VarInt face, VarInt hand, float cursors
            sendPacket(new NettyBlockPlacementPacketV315(x, y, z, direction));
            return;
        }
        if (version.isAtLeast(ProtocolVersion.RELEASE_1_9)) {
            // Netty 1.9-1.10: packed Position, VarInt face, VarInt hand, byte cursors
            sendPacket(new NettyBlockPlacementPacketV109(x, y, z, direction));
            return;
        }
        if (version.isAtLeast(ProtocolVersion.RELEASE_1_8)) {
            // Netty 1.8: packed Position coordinates, V47 slot format
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
        if (version == ProtocolVersion.BEDROCK) {
            // Use InventoryTransaction with actionType=2 (break) for creative mode
            InventoryTransactionPacket pkt = new InventoryTransactionPacket();
            pkt.setTransactionType(InventoryTransactionType.ITEM_USE);
            pkt.setActionType(2); // break
            pkt.setBlockPosition(Vector3i.from(x, y, z));
            pkt.setBlockFace(face);
            pkt.setHotbarSlot(0);
            pkt.setItemInHand(ItemData.AIR);
            pkt.setPlayerPosition(Vector3f.ZERO);
            pkt.setClickPosition(Vector3f.ZERO);
            pkt.setHeadPosition(Vector3f.ZERO);
            pkt.setBlockDefinition(BedrockProtocolConstants.getBlockDefinitions().getDefinition(0));
            pkt.setTriggerType(ItemUseTransaction.TriggerType.PLAYER_INPUT);
            pkt.setClientInteractPrediction(ItemUseTransaction.PredictedResult.SUCCESS);
            bedrockSession.sendPacketImmediately(pkt);
            return;
        }
        if (version.isAtLeast(ProtocolVersion.RELEASE_1_19_1)) {
            sendPacket(new PlayerDiggingPacketV759(status, x, y, z, face));
        } else if (version.isAtLeast(ProtocolVersion.RELEASE_1_14)) {
            sendPacket(new PlayerDiggingPacketV477(status, x, y, z, face));
        } else if (version.isAtLeast(ProtocolVersion.RELEASE_1_8)) {
            sendPacket(new PlayerDiggingPacketV47(status, x, y, z, face));
        } else {
            sendPacket(new PlayerDiggingPacket(status, x, y, z, face));
        }
    }

    // ---- Inventory action counter ----
    private final AtomicInteger actionNumberCounter = new AtomicInteger(0);

    /**
     * Send a WindowClick packet for the player's inventory (window 0).
     * Auto-increments the action number.
     *
     * @param slot   slot index (-999 for outside window)
     * @param button mouse button (0=left, 1=right)
     * @param mode   click mode (0=normal, 4=drop)
     * @return the action number used
     */
    public int sendWindowClick(int slot, int button, int mode) {
        int actionNum = actionNumberCounter.incrementAndGet();
        if (version == ProtocolVersion.BEDROCK) {
            return actionNum; // No-op for Bedrock
        }
        if (version.isAtLeast(ProtocolVersion.RELEASE_1_8)) {
            sendPacket(new NettyWindowClickPacketV47(0, slot, button, actionNum, mode));
        } else if (version.isAtLeast(ProtocolVersion.RELEASE_1_7_2)) {
            sendPacket(new NettyWindowClickPacket(0, slot, button, actionNum, mode));
        } else if (version.isAtLeast(ProtocolVersion.BETA_1_5)) {
            // Beta 1.5+ with shift byte
            WindowClickPacketBeta15 pkt = new WindowClickPacketBeta15();
            // Use write-only constructor pattern via the packet's write method
            sendPacket(createBeta15WindowClick(0, slot, button, actionNum, mode == 1 ? 1 : 0));
        } else if (version.isAtLeast(ProtocolVersion.BETA_1_0)) {
            sendPacket(createBasicWindowClick(0, slot, button, actionNum));
        }
        // Alpha v1-v6: no WindowClick packet
        return actionNum;
    }

    /**
     * Send a CloseWindow packet (window 0 = player inventory).
     */
    public void sendCloseWindow(int windowId) {
        if (version == ProtocolVersion.BEDROCK) return;
        sendPacket(new CloseWindowPacket());
    }

    /**
     * Wait for a ConfirmTransaction S2C packet matching the given action number.
     * Returns the accepted boolean, or null on timeout.
     */
    public Boolean waitForConfirmTransaction(int actionNum, long timeoutMs)
            throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            for (Packet p : receivedPackets) {
                if (p instanceof ConfirmTransactionPacket ct) {
                    if (ct.getActionNum() == actionNum) {
                        return ct.isAccepted();
                    }
                }
            }
            Thread.sleep(50);
        }
        return null;
    }

    /**
     * Bulk-update slot tracking from a WindowItems packet.
     */
    void recordWindowItems(short[] itemIds, byte[] counts) {
        if (itemIds == null) return;
        for (int i = 0; i < itemIds.length && i < slotItemIds.length; i++) {
            slotItemIds[i] = itemIds[i];
            slotCounts[i] = counts != null && i < counts.length ? counts[i] & 0xFF : 0;
        }
    }

    private WindowClickPacket createBasicWindowClick(int windowId, int slot, int button, int actionNum) {
        // Create a WindowClickPacket by making use of its write/read format
        io.netty.buffer.ByteBuf buf = io.netty.buffer.Unpooled.buffer();
        buf.writeByte(windowId);
        buf.writeShort(slot);
        buf.writeByte(button);
        buf.writeShort(actionNum);
        buf.writeShort(-1); // empty item
        WindowClickPacket pkt = new WindowClickPacket();
        pkt.read(buf);
        buf.release();
        return pkt;
    }

    private WindowClickPacketBeta15 createBeta15WindowClick(int windowId, int slot, int button,
                                                             int actionNum, int shift) {
        io.netty.buffer.ByteBuf buf = io.netty.buffer.Unpooled.buffer();
        buf.writeByte(windowId);
        buf.writeShort(slot);
        buf.writeByte(button);
        buf.writeShort(actionNum);
        buf.writeByte(shift);
        buf.writeShort(-1); // empty item
        WindowClickPacketBeta15 pkt = new WindowClickPacketBeta15();
        pkt.read(buf);
        buf.release();
        return pkt;
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
