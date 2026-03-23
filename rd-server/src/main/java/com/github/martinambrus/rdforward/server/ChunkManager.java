package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.alpha.MapChunkPacket;
import com.github.martinambrus.rdforward.protocol.packet.alpha.MapChunkPacketV28;
import com.github.martinambrus.rdforward.protocol.packet.alpha.MapChunkPacketV39;
import com.github.martinambrus.rdforward.protocol.packet.alpha.PreChunkPacket;
import com.github.martinambrus.rdforward.protocol.packet.netty.MapChunkPacketV109;
import com.github.martinambrus.rdforward.protocol.packet.netty.MapChunkPacketV47;
import com.github.martinambrus.rdforward.protocol.packet.netty.MapChunkPacketV477;
import com.github.martinambrus.rdforward.protocol.packet.netty.MapChunkPacketV573;
import com.github.martinambrus.rdforward.protocol.packet.netty.MapChunkPacketV735;
import com.github.martinambrus.rdforward.protocol.packet.netty.MapChunkPacketV751;
import com.github.martinambrus.rdforward.protocol.packet.netty.MapChunkPacketV755;
import com.github.martinambrus.rdforward.protocol.packet.netty.MapChunkPacketV757;
import com.github.martinambrus.rdforward.protocol.packet.netty.MapChunkPacketV763;
import com.github.martinambrus.rdforward.protocol.packet.netty.MapChunkPacketV764;
import com.github.martinambrus.rdforward.protocol.packet.netty.MapChunkPacketV770;
import com.github.martinambrus.rdforward.protocol.packet.netty.UnloadChunkPacketV109;
import com.github.martinambrus.rdforward.protocol.packet.netty.UpdateLightPacketV477;
import com.github.martinambrus.rdforward.protocol.packet.netty.UpdateLightPacketV735;
import com.github.martinambrus.rdforward.protocol.packet.netty.UpdateLightPacketV755;
import com.github.martinambrus.rdforward.server.api.ServerProperties;
import com.github.martinambrus.rdforward.world.WorldGenerator;
import com.github.martinambrus.rdforward.world.alpha.AlphaChunk;
import com.github.martinambrus.rdforward.world.alpha.AlphaLevelFormat;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.server.api.Scheduler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages chunk loading, unloading, and per-player chunk tracking
 * for Alpha-style infinite worlds.
 *
 * Responsibilities:
 * 1. Track which chunks are loaded in memory
 * 2. Track which chunks each player has received
 * 3. Load/generate chunks on demand when players move
 * 4. Send PreChunkPacket + MapChunkPacket for newly visible chunks
 * 5. Send PreChunkPacket(unload) for chunks that leave view distance
 * 6. Unload chunks from memory when no player needs them
 * 7. Save dirty chunks to disk via AlphaLevelFormat
 *
 * Thread safety: all maps are ConcurrentHashMap. The chunk update cycle
 * is called from the tick loop thread, while player add/remove may come
 * from Netty I/O threads, so concurrent access is expected.
 */
public class ChunkManager {

    /** Default view distance in chunks (radius around the player's chunk).
     *  Configured via "view-distance" in server.properties. E2E tests can
     *  override via -De2e.viewDistance=N system property. */
    public static final int DEFAULT_VIEW_DISTANCE =
            ServerProperties.getViewDistance();

    /** View distance advertised to the client in JoinGame / SetChunkCacheRadius.
     *  Always >= 2 so the client doesn't cap its effective render distance below
     *  its options.txt renderDistance:2 (min(clientRender, serverView)). */
    public static final int CLIENT_VIEW_DISTANCE = Math.max(DEFAULT_VIEW_DISTANCE, 2);

    /** Chunks loaded in memory, keyed by coordinate. */
    private final Map<ChunkCoord, AlphaChunk> loadedChunks = new ConcurrentHashMap<>();

    /** Which chunks each player currently has loaded (sent to their client). */
    private final Map<ConnectedPlayer, Set<ChunkCoord>> playerChunks = new ConcurrentHashMap<>();

    /** World generator for creating new chunks. */
    private final WorldGenerator worldGenerator;

    /** World seed for reproducible generation. */
    private final long seed;

    /** Directory for Alpha-format chunk files. */
    private final File worldDir;

    /** View distance in chunks (radius). */
    private final int viewDistance;

    /** Tracks which chunks have been modified since last save. */
    private final Set<ChunkCoord> dirtyChunks = ConcurrentHashMap.newKeySet();

    /** Tracks chunks currently being written to disk by a worker thread,
     *  so saveIncrementally() doesn't pick the same chunk twice. */
    private final Set<ChunkCoord> savingChunks = ConcurrentHashMap.newKeySet();

    /** Reference to the authoritative Classic/RD world for block data overlay. */
    private ServerWorld serverWorld;

    /** Max chunks to deliver per player per tick (pacing). */
    private static final int MAX_CHUNKS_PER_PLAYER_PER_TICK = 4;
    /** Max total chunk deliveries across all players per tick (bounds worst-case tick time). */
    private static final int MAX_TOTAL_DELIVERIES_PER_TICK = 32;
    /** Max dirty chunks to save per incremental save call. */
    private static final int INCREMENTAL_SAVE_BATCH = 4;

    /** Worker pool for off-thread chunk generation and disk I/O. */
    private static final AtomicInteger WORKER_ID = new AtomicInteger();
    private final ExecutorService chunkWorkerPool = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "chunk-worker-" + WORKER_ID.incrementAndGet());
        t.setDaemon(true);
        return t;
    });

    /** Tracks in-flight generation tasks to prevent duplicate work. */
    private final ConcurrentHashMap<ChunkCoord, CompletableFuture<AlphaChunk>> pendingChunks =
            new ConcurrentHashMap<>();

    /** Chunks ready to send per player, delivered by tick-driven drain task. */
    private final ConcurrentHashMap<ConnectedPlayer, Queue<ChunkCoord>> pendingSendsPerPlayer =
            new ConcurrentHashMap<>();

    /** Scheduler task handle for the delivery loop. */
    private Scheduler.ScheduledTask deliveryTask;

    /**
     * Cache of serialized chunk packets keyed by (chunkX, chunkZ, protocolBucket).
     * Eliminates redundant serialization+compression when multiple players of the
     * same protocol version need the same chunk. Invalidated when blocks change.
     */
    private final ConcurrentHashMap<Long, Packet[]> chunkPacketCache = new ConcurrentHashMap<>();

    /** Max entries in the chunk packet cache before eviction. */
    private static final int MAX_CACHE_SIZE = 2000;

    public ChunkManager(WorldGenerator worldGenerator, long seed, File worldDir) {
        this(worldGenerator, seed, worldDir, DEFAULT_VIEW_DISTANCE);
    }

    public ChunkManager(WorldGenerator worldGenerator, long seed, File worldDir, int viewDistance) {
        this.worldGenerator = worldGenerator;
        this.seed = seed;
        this.worldDir = worldDir;
        this.viewDistance = viewDistance;
    }

    /**
     * Initialize the async chunk delivery task. Must be called after
     * {@link Scheduler#init()} during server startup.
     */
    public void initAsyncDelivery() {
        deliveryTask = Scheduler.runRepeating(0, 1, this::deliverReadyChunks);
    }

    /**
     * Encode a cache key from chunk coordinates and protocol bucket.
     * Layout: bits 47..28 = chunkX (20 bits), bits 27..8 = chunkZ (20 bits),
     * bits 7..0 = bucket (8 bits). Supports coords up to +/-524287.
     */
    private static long cacheKey(int chunkX, int chunkZ, int bucket) {
        return ((long) (chunkX & 0xFFFFF) << 28) | ((long) (chunkZ & 0xFFFFF) << 8) | (bucket & 0xFF);
    }

    /**
     * Determine the protocol bucket for a given protocol version.
     * Versions that share the same chunk wire format map to the same bucket.
     * WARNING: must be kept in sync with sendChunkToPlayer() — each bucket
     * must map to exactly one packet type/serialization path.
     */
    static int protocolBucket(ProtocolVersion v) {
        if (v == ProtocolVersion.BEDROCK) return 0; // Bedrock uses separate path
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_21_5)) return 19;
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_20_5)) return 18;
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_20_2)) return 17; // MapChunkPacketV764
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_20)) return 16;   // MapChunkPacketV763
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_19)) return 15;   // MapChunkPacketV757
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_18)) return 14;
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_17)) return 13;
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_16_2)) return 12;
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_16)) return 11;
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_15)) return 10;
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_14)) return 9;
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_13)) return 8;
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_9_4)) return 7;   // writeBlockEntityCount=true
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_9)) return 6;     // writeBlockEntityCount=false
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_8)) return 5;
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_3_1)) return 4;
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_2_1)) return 3;
        return 2; // Alpha/Beta
    }

    /**
     * Invalidate all cached chunk packets for a given chunk coordinate.
     * Called when a block changes in that chunk.
     */
    private void invalidateChunkCache(int chunkX, int chunkZ) {
        for (int bucket = 2; bucket <= 19; bucket++) {
            chunkPacketCache.remove(cacheKey(chunkX, chunkZ, bucket));
        }
    }

    /**
     * Set the authoritative ServerWorld reference. When set, freshly generated
     * chunks that overlap with the Classic world bounds will be populated from
     * the ServerWorld block data instead of pure generator output.
     */
    public void setServerWorld(ServerWorld serverWorld) {
        this.serverWorld = serverWorld;
    }

    /**
     * Register a player for chunk tracking.
     * Call this when a player finishes login.
     */
    public void addPlayer(ConnectedPlayer player) {
        playerChunks.put(player, ConcurrentHashMap.newKeySet());
    }

    /**
     * Unregister a player, unloading any chunks only they were using.
     * Call this when a player disconnects.
     */
    public void removePlayer(ConnectedPlayer player) {
        Set<ChunkCoord> chunks = playerChunks.remove(player);
        pendingSendsPerPlayer.remove(player);
        if (chunks == null) return;

        for (ChunkCoord coord : chunks) {
            if (!isChunkNeededByAnyPlayer(coord)) {
                unloadChunk(coord);
            }
        }
    }

    /**
     * Load or generate a chunk asynchronously on the worker pool.
     * Returns a future that completes when the chunk is ready in {@code loadedChunks}.
     * Deduplicates: if a generation is already in-flight for this coord,
     * returns the existing future.
     */
    public CompletableFuture<AlphaChunk> getOrLoadChunkAsync(ChunkCoord coord) {
        // Fast path: already cached
        AlphaChunk cached = loadedChunks.get(coord);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        // Deduplicate: only one generation task per coord
        CompletableFuture<AlphaChunk> future = new CompletableFuture<>();
        CompletableFuture<AlphaChunk> existing = pendingChunks.putIfAbsent(coord, future);
        if (existing != null) {
            return existing; // Another request already started this chunk
        }

        chunkWorkerPool.submit(() -> {
            try {
                // Double-check cache (may have been loaded between our check and task start)
                AlphaChunk chunk = loadedChunks.get(coord);
                if (chunk != null) {
                    pendingChunks.remove(coord);
                    future.complete(chunk);
                    return;
                }

                // Try loading from disk
                try {
                    chunk = AlphaLevelFormat.loadChunk(worldDir, coord.getX(), coord.getZ());
                } catch (IOException e) {
                    System.err.println("Failed to load chunk " + coord + ": " + e.getMessage());
                }

                // Generate if not on disk
                if (chunk == null && worldGenerator.supportsChunkGeneration()) {
                    chunk = worldGenerator.generateChunk(coord.getX(), coord.getZ(), seed);
                } else if (chunk == null && serverWorld != null) {
                    // Finite-world mode (RubyDung/Classic): create empty chunk
                    // so overlayServerWorldBlocks can populate it.
                    chunk = new AlphaChunk(coord.getX(), coord.getZ());
                }

                if (chunk != null) {
                    overlayServerWorldBlocks(chunk);
                    chunk.generateSkylightMap();
                    loadedChunks.put(coord, chunk);
                }

                pendingChunks.remove(coord);
                future.complete(chunk);
            } catch (Exception e) {
                pendingChunks.remove(coord);
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * Deliver ready chunks to players. Called every tick by the Scheduler.
     * Drains the pending-sends queue, sending up to {@link #MAX_CHUNKS_PER_PLAYER_PER_TICK}
     * per player per tick.
     */
    private void deliverReadyChunks() {
        int totalSent = 0;
        Iterator<Map.Entry<ConnectedPlayer, Queue<ChunkCoord>>> outerIt =
                pendingSendsPerPlayer.entrySet().iterator();
        while (outerIt.hasNext() && totalSent < MAX_TOTAL_DELIVERIES_PER_TICK) {
            Map.Entry<ConnectedPlayer, Queue<ChunkCoord>> entry = outerIt.next();
            ConnectedPlayer player = entry.getKey();
            Queue<ChunkCoord> pending = entry.getValue();
            Set<ChunkCoord> current = playerChunks.get(player);
            if (current == null) {
                pending.clear();
                outerIt.remove();
                continue;
            }

            int sent = 0;
            int perPlayerLimit = Math.min(MAX_CHUNKS_PER_PLAYER_PER_TICK,
                    MAX_TOTAL_DELIVERIES_PER_TICK - totalSent);
            Iterator<ChunkCoord> it = pending.iterator();
            while (it.hasNext() && sent < perPlayerLimit) {
                ChunkCoord coord = it.next();
                AlphaChunk chunk = loadedChunks.get(coord);
                if (chunk != null) {
                    if (isInViewDistance(player, coord)) {
                        sendChunkToPlayer(player, chunk);
                        current.add(coord);
                    }
                    it.remove();
                    sent++;
                } else if (!pendingChunks.containsKey(coord)) {
                    it.remove();
                }
            }
            totalSent += sent;

            if (pending.isEmpty()) {
                outerIt.remove();
            }
        }
    }

    /**
     * Check if a chunk coord is within a player's current view distance.
     */
    private boolean isInViewDistance(ConnectedPlayer player, ChunkCoord coord) {
        int blockX = player.getX() / 32;
        int blockZ = player.getZ() / 32;
        int centerChunkX = blockX >> 4;
        int centerChunkZ = blockZ >> 4;
        return Math.abs(coord.getX() - centerChunkX) <= viewDistance
            && Math.abs(coord.getZ() - centerChunkZ) <= viewDistance;
    }

    /**
     * Shutdown the chunk worker pool. Waits for in-flight generation tasks,
     * cancels pending deliveries, and saves dirty chunks.
     */
    public void shutdown() {
        chunkWorkerPool.shutdown();
        if (deliveryTask != null) deliveryTask.cancel();
        try {
            if (!chunkWorkerPool.awaitTermination(5, TimeUnit.SECONDS)) {
                System.err.println("[ChunkManager] Forcing shutdown of chunk worker pool");
                chunkWorkerPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            chunkWorkerPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        pendingSendsPerPlayer.clear();
        pendingChunks.clear();
        saveAllDirty();
    }

    /**
     * Update chunk loading for a player based on their current position.
     *
     * Calculates which chunks should be visible given the player's position
     * and the view distance, then:
     * - Sends new chunks that entered the view distance
     * - Unloads chunks that left the view distance
     *
     * Player positions are in fixed-point units (divide by 32 for blocks).
     *
     * @param player the player to update chunks for
     */
    public void updatePlayerChunks(ConnectedPlayer player) {
        Set<ChunkCoord> current = playerChunks.get(player);
        if (current == null) return;

        // Skip new chunk loading for players with critical RTT (>= 500ms).
        // They keep existing chunks but don't load new ones until RTT improves,
        // preventing megabytes of chunk data from queueing into a saturated connection.
        if (player.getRttTier() >= 2) return;

        // Convert fixed-point position to chunk coordinates
        int blockX = player.getX() / 32;
        int blockZ = player.getZ() / 32;
        int centerChunkX = blockX >> 4;
        int centerChunkZ = blockZ >> 4;

        // Calculate desired chunk set within view distance
        Set<ChunkCoord> desired = new HashSet<>();
        for (int dx = -viewDistance; dx <= viewDistance; dx++) {
            for (int dz = -viewDistance; dz <= viewDistance; dz++) {
                desired.add(new ChunkCoord(centerChunkX + dx, centerChunkZ + dz));
            }
        }

        // Find chunks to load (in desired but not in current)
        List<ChunkCoord> toLoad = new ArrayList<>();
        for (ChunkCoord coord : desired) {
            if (!current.contains(coord)) {
                toLoad.add(coord);
            }
        }

        // Find chunks to unload (in current but not in desired)
        List<ChunkCoord> toUnload = new ArrayList<>();
        for (ChunkCoord coord : current) {
            if (!desired.contains(coord)) {
                toUnload.add(coord);
            }
        }



        // Sort chunks to load by distance from player (closest first)
        toLoad.sort((a, b) -> {
            int distA = (a.getX() - centerChunkX) * (a.getX() - centerChunkX)
                      + (a.getZ() - centerChunkZ) * (a.getZ() - centerChunkZ);
            int distB = (b.getX() - centerChunkX) * (b.getX() - centerChunkX)
                      + (b.getZ() - centerChunkZ) * (b.getZ() - centerChunkZ);
            return Integer.compare(distA, distB);
        });

        // Unload chunks that left view distance
        for (ChunkCoord coord : toUnload) {
            sendChunkUnload(player, coord);
            current.remove(coord);
            if (!isChunkNeededByAnyPlayer(coord)) {
                unloadChunk(coord);
            }
        }

        // For Bedrock clients, update the chunk publisher area before sending new chunks
        // so the client accepts chunks outside the original spawn area.
        if (!toLoad.isEmpty() && player.getBedrockSession() != null) {
            player.getBedrockSession().sendChunkPublisherUpdate(
                    blockX, player.getY() / 32, blockZ, viewDistance * 16);
        }

        // Send cached chunks immediately; queue uncached for async generation
        sendOrQueueChunks(player, toLoad, current);
    }

    /**
     * Force-resend all chunks the player currently has tracked.
     * Used after void-fall teleport when the client may have discarded
     * its chunk render cache even though the server still considers
     * those chunks "sent".
     */
    public void resendPlayerChunks(ConnectedPlayer player) {
        Set<ChunkCoord> current = playerChunks.get(player);
        if (current == null || current.isEmpty()) return;
        int sent = 0;
        for (ChunkCoord coord : current) {
            AlphaChunk chunk = getOrLoadChunk(coord);
            if (chunk != null) {
                sendChunkToPlayer(player, chunk);
                sent++;
            }
        }
        player.flushPackets();
        System.out.println("[ChunkManager] Resent " + sent + " chunks to " + player.getUsername());
    }

    /**
     * Send all chunks within view distance to a player.
     * Used during initial login for Alpha-mode clients.
     *
     * @param player the player to send chunks to
     * @param blockX player block X position
     * @param blockZ player block Z position
     */
    public void sendInitialChunks(ConnectedPlayer player, int blockX, int blockZ) {
        Set<ChunkCoord> current = playerChunks.get(player);
        if (current == null) {
            current = ConcurrentHashMap.newKeySet();
            playerChunks.put(player, current);
        }

        int centerChunkX = blockX >> 4;
        int centerChunkZ = blockZ >> 4;

        // Build sorted list of chunks to send (closest first)
        List<ChunkCoord> toSend = new ArrayList<>();
        for (int dx = -viewDistance; dx <= viewDistance; dx++) {
            for (int dz = -viewDistance; dz <= viewDistance; dz++) {
                toSend.add(new ChunkCoord(centerChunkX + dx, centerChunkZ + dz));
            }
        }
        toSend.sort((a, b) -> {
            int distA = (a.getX() - centerChunkX) * (a.getX() - centerChunkX)
                      + (a.getZ() - centerChunkZ) * (a.getZ() - centerChunkZ);
            int distB = (b.getX() - centerChunkX) * (b.getX() - centerChunkX)
                      + (b.getZ() - centerChunkZ) * (b.getZ() - centerChunkZ);
            return Integer.compare(distA, distB);
        });

        int[] counts = sendOrQueueChunks(player, toSend, current);
        player.flushPackets();
        System.out.println("[ChunkManager] Sent " + counts[0] + " initial chunks to " + player.getUsername()
                + " (+" + counts[1] + " async), centered at chunk ("
                + centerChunkX + ", " + centerChunkZ + ")");
    }

    /**
     * Send already-cached chunks immediately and queue uncached ones for
     * async generation. Returns [sentCount, asyncCount].
     */
    private int[] sendOrQueueChunks(ConnectedPlayer player, List<ChunkCoord> coords,
                                     Set<ChunkCoord> current) {
        Queue<ChunkCoord> pendingQueue = null;
        int sentCount = 0;
        for (ChunkCoord coord : coords) {
            AlphaChunk cached = loadedChunks.get(coord);
            if (cached != null) {
                sendChunkToPlayer(player, cached);
                current.add(coord);
                sentCount++;
            } else {
                getOrLoadChunkAsync(coord);
                if (pendingQueue == null) {
                    pendingQueue = pendingSendsPerPlayer.computeIfAbsent(
                            player, k -> new ConcurrentLinkedQueue<>());
                }
                pendingQueue.add(coord);
            }
        }
        int asyncCount = pendingQueue != null ? pendingQueue.size() : 0;
        return new int[]{sentCount, asyncCount};
    }

    /**
     * Get a chunk from memory, or load/generate it if not loaded.
     */
    public AlphaChunk getOrLoadChunk(ChunkCoord coord) {
        AlphaChunk chunk = loadedChunks.get(coord);
        if (chunk != null) {
            return chunk;
        }

        // Try loading from disk
        try {
            chunk = AlphaLevelFormat.loadChunk(worldDir, coord.getX(), coord.getZ());
        } catch (IOException e) {
            System.err.println("Failed to load chunk " + coord + ": " + e.getMessage());
        }

        // If not on disk, generate a fresh chunk
        if (chunk == null && worldGenerator.supportsChunkGeneration()) {
            chunk = worldGenerator.generateChunk(coord.getX(), coord.getZ(), seed);
        } else if (chunk == null && serverWorld != null) {
            // Finite-world mode (RubyDung/Classic): generator doesn't support
            // per-chunk generation, but the full world is in ServerWorld.
            // Create an empty chunk so overlayServerWorldBlocks can populate it.
            chunk = new AlphaChunk(coord.getX(), coord.getZ());
        }

        // Always overlay ServerWorld data so the authoritative world state
        // takes priority over stale disk data or freshly generated terrain.
        if (chunk != null) {
            overlayServerWorldBlocks(chunk);
            // Recompute skylight from the height map after block overlay.
            // Without this, underground blocks retain skylight=15, causing
            // the Alpha client's light engine to cascade-correct on any
            // block change (StackOverflowError).
            chunk.generateSkylightMap();
        }

        if (chunk != null) {
            loadedChunks.put(coord, chunk);
        }

        return chunk;
    }

    /**
     * Sync a freshly generated chunk with the authoritative ServerWorld.
     *
     * For columns within the Classic world bounds: copies block data from ServerWorld.
     * For columns outside the bounds: clears to air (prevents infinite grass plane).
     *
     * This ensures Alpha clients see the same finite world as Classic/RD clients,
     * with a void/edge beyond the world boundaries.
     */
    private void overlayServerWorldBlocks(AlphaChunk chunk) {
        if (serverWorld == null) return;

        int baseX = chunk.getXPos() * AlphaChunk.WIDTH;
        int baseZ = chunk.getZPos() * AlphaChunk.DEPTH;
        int maxY = Math.min(serverWorld.getHeight(), AlphaChunk.HEIGHT);

        // Bulk-read the entire chunk column from ServerWorld in a single lock
        // acquisition instead of 32,768 individual getBlock() calls.
        byte[] regionData = new byte[AlphaChunk.WIDTH * AlphaChunk.DEPTH * maxY];
        serverWorld.getBlockRegion(baseX, baseZ, AlphaChunk.WIDTH, AlphaChunk.DEPTH,
                maxY, regionData);

        // Copy from snapshot to chunk (no lock needed)
        int idx = 0;
        for (int localX = 0; localX < AlphaChunk.WIDTH; localX++) {
            for (int localZ = 0; localZ < AlphaChunk.DEPTH; localZ++) {
                for (int y = 0; y < maxY; y++) {
                    chunk.setBlock(localX, y, localZ, regionData[idx++] & 0xFF);
                }
            }
        }
    }

    /**
     * Get a loaded chunk without loading/generating. Returns null if not in memory.
     */
    public AlphaChunk getChunkIfLoaded(ChunkCoord coord) {
        return loadedChunks.get(coord);
    }

    /**
     * Get a block from the chunk world.
     * Loads the chunk if necessary. Returns 0 (AIR) if chunk can't be loaded.
     */
    public byte getBlock(int blockX, int blockY, int blockZ) {
        ChunkCoord coord = ChunkCoord.fromBlock(blockX, blockZ);
        AlphaChunk chunk = getOrLoadChunk(coord);
        if (chunk == null || blockY < 0 || blockY >= AlphaChunk.HEIGHT) {
            return 0;
        }
        int localX = blockX & 15;
        int localZ = blockZ & 15;
        return (byte) chunk.getBlock(localX, blockY, localZ);
    }

    /**
     * Set a block in the chunk world.
     * Returns true if the block was changed.
     */
    public boolean setBlock(int blockX, int blockY, int blockZ, byte blockType) {
        ChunkCoord coord = ChunkCoord.fromBlock(blockX, blockZ);
        AlphaChunk chunk = getOrLoadChunk(coord);
        if (chunk == null || blockY < 0 || blockY >= AlphaChunk.HEIGHT) {
            return false;
        }
        int localX = blockX & 15;
        int localZ = blockZ & 15;
        int oldBlock = chunk.getBlock(localX, blockY, localZ);
        if (oldBlock == (blockType & 0xFF)) {
            return false;
        }
        chunk.setBlock(localX, blockY, localZ, blockType & 0xFF);
        dirtyChunks.add(coord);
        invalidateChunkCache(coord.getX(), coord.getZ());
        return true;
    }

    /**
     * Save up to {@link #INCREMENTAL_SAVE_BATCH} dirty chunks asynchronously
     * on the chunk worker pool. Called frequently from the tick loop to spread
     * disk I/O over time instead of saving everything in one burst.
     *
     * NBT serialization (toNbt) happens on the tick thread to avoid
     * concurrent-modification issues with entity/tileEntity lists.
     * Only the disk I/O runs on the worker thread.
     */
    public void saveIncrementally() {
        if (dirtyChunks.isEmpty()) return;

        int count = 0;
        Iterator<ChunkCoord> it = dirtyChunks.iterator();
        while (it.hasNext() && count < INCREMENTAL_SAVE_BATCH) {
            ChunkCoord coord = it.next();
            if (savingChunks.contains(coord)) continue; // already in-flight
            AlphaChunk chunk = loadedChunks.get(coord);
            if (chunk != null) {
                // Snapshot NBT on tick thread (safe: entities/tileEntities not modified concurrently)
                AlphaLevelFormat.SaveTask saveTask = AlphaLevelFormat.prepareSave(worldDir, chunk);
                savingChunks.add(coord);
                chunkWorkerPool.submit(() -> {
                    try {
                        saveTask.writeToDisk();
                        dirtyChunks.remove(coord);
                    } catch (IOException e) {
                        System.err.println("Failed to save chunk " + coord + ": " + e.getMessage());
                    } finally {
                        savingChunks.remove(coord);
                    }
                });
                count++;
            } else {
                it.remove(); // chunk was unloaded, no need to save
            }
        }
    }

    /**
     * Save all dirty chunks to disk synchronously on the calling thread.
     * Skips chunks currently being written by an incremental save worker
     * to avoid concurrent file writes.
     */
    public void saveAllDirty() {
        Set<ChunkCoord> saved = new HashSet<>();
        for (ChunkCoord coord : dirtyChunks) {
            if (savingChunks.contains(coord)) continue; // in-flight incremental save
            AlphaChunk chunk = loadedChunks.get(coord);
            if (chunk != null) {
                try {
                    AlphaLevelFormat.saveChunk(worldDir, chunk);
                    saved.add(coord);
                } catch (IOException e) {
                    System.err.println("Failed to save chunk " + coord + ": " + e.getMessage());
                }
            }
        }
        dirtyChunks.removeAll(saved);
        if (!saved.isEmpty()) {
            System.out.println("Saved " + saved.size() + " dirty chunk(s) to " + worldDir);
        }
    }

    /**
     * Save all loaded chunks to disk (for server shutdown).
     */
    public void saveAll() {
        int count = 0;
        for (Map.Entry<ChunkCoord, AlphaChunk> entry : loadedChunks.entrySet()) {
            try {
                AlphaLevelFormat.saveChunk(worldDir, entry.getValue());
                count++;
            } catch (IOException e) {
                System.err.println("Failed to save chunk " + entry.getKey() + ": " + e.getMessage());
            }
        }
        dirtyChunks.clear();
        System.out.println("Saved " + count + " chunk(s) to " + worldDir);
    }

    /**
     * Send a chunk to a player via PreChunkPacket + MapChunkPacket.
     * Uses a per-protocol-bucket packet cache to avoid redundant serialization
     * when multiple players of the same version need the same chunk.
     * WARNING: version branches here must match protocolBucket() — each bucket
     * must correspond to exactly one serialization path below.
     */
    private void sendChunkToPlayer(ConnectedPlayer player, AlphaChunk chunk) {
        // Bedrock (CloudburstMC) — uses its own chunk format via BedrockChunkConverter
        if (player.getBedrockSession() != null) {
            player.getBedrockSession().sendChunkData(chunk);
            return;
        }
        // Legacy MCPE — uses version-specific codec chunk format
        if (player.getMcpeSession() != null) {
            player.getMcpeSession().sendChunkData(serverWorld, chunk.getXPos(), chunk.getZPos());
            return;
        }

        // Check chunk packet cache for TCP clients
        int bucket = protocolBucket(player.getProtocolVersion());
        long key = cacheKey(chunk.getXPos(), chunk.getZPos(), bucket);
        Packet[] cachedPackets = chunkPacketCache.get(key);
        if (cachedPackets != null) {
            for (Packet p : cachedPackets) {
                player.writePacket(p);
            }
            return;
        }

        // Cache miss — serialize normally but collect packets for caching
        List<Packet> collectedPackets = new ArrayList<>();

        if (player.getProtocolVersion().isAtLeast(ProtocolVersion.RELEASE_1_19)) {
            // v759/v760/v761/v762/v763: Same chunk format as v757 but with 1.19 block state IDs.
            AlphaChunk.V757ChunkData v759Data = chunk.serializeForV759Protocol();
            long[] heightmap = buildHeightmapLongArrayNonSpanning(chunk);

            int skyLightMask = 0;
            int blockLightMask = 0;
            java.util.List<byte[]> skyArrays = new java.util.ArrayList<>();
            java.util.List<byte[]> blockArrays = new java.util.ArrayList<>();

            for (int section = 0; section < 8; section++) {
                if (v759Data.getSkyLightSections()[section] != null) {
                    skyLightMask |= (1 << (section + 1));
                    skyArrays.add(v759Data.getSkyLightSections()[section]);
                }
                if (v759Data.getBlockLightSections()[section] != null) {
                    blockLightMask |= (1 << (section + 1));
                    blockArrays.add(v759Data.getBlockLightSections()[section]);
                }
            }

            int emptySkyLightMask = ~skyLightMask & 0x3FFFF;
            int emptyBlockLightMask = ~blockLightMask & 0x3FFFF;

            byte[][] skyArr = skyArrays.toArray(new byte[0][]);
            byte[][] blockArr = blockArrays.toArray(new byte[0][]);

            if (player.getProtocolVersion().isAtLeast(ProtocolVersion.RELEASE_1_21_5)) {
                // v770: same 24-section padding as v766+, but with v770 serialization
                // (no VarInt data array length prefixes) and binary heightmaps.
                AlphaChunk.V757ChunkData v770Data = chunk.serializeForV770Protocol();
                byte[] rawData = v770Data.getRawData();
                byte[] emptySection = buildEmptySection770();
                byte[] adjusted = new byte[4 * emptySection.length + rawData.length + 4 * emptySection.length];
                int pos = 0;
                for (int i = 0; i < 4; i++) {
                    System.arraycopy(emptySection, 0, adjusted, pos, emptySection.length);
                    pos += emptySection.length;
                }
                System.arraycopy(rawData, 0, adjusted, pos, rawData.length);
                pos += rawData.length;
                for (int i = 0; i < 4; i++) {
                    System.arraycopy(emptySection, 0, adjusted, pos, emptySection.length);
                    pos += emptySection.length;
                }

                long[] adjustedHeightmap = buildHeightmapForMinY(chunk, 64);
                int adjustedSkyLightMask = skyLightMask << 4;
                int adjustedBlockLightMask = blockLightMask << 4;
                int adjustedEmptySkyLightMask = ~adjustedSkyLightMask & 0x3FFFFFF;
                int adjustedEmptyBlockLightMask = ~adjustedBlockLightMask & 0x3FFFFFF;

                collectedPackets.add(new MapChunkPacketV770(
                    chunk.getXPos(), chunk.getZPos(),
                    adjustedHeightmap, adjustedHeightmap,
                    adjusted,
                    adjustedSkyLightMask, adjustedBlockLightMask,
                    adjustedEmptySkyLightMask, adjustedEmptyBlockLightMask,
                    skyArr, blockArr));
            } else if (player.getProtocolVersion().isAtLeast(ProtocolVersion.RELEASE_1_20_5)) {
                // v766+: built-in overworld has minY=-64, height=384 (24 sections).
                // Our chunk data has 16 sections for Y 0-255.
                // Prepend 4 empty sections (Y -64 to -1) and append 4 (Y 256-319).
                byte[] rawData = v759Data.getRawData();
                byte[] emptySection = buildEmptySection766();
                byte[] adjusted = new byte[4 * emptySection.length + rawData.length + 4 * emptySection.length];
                int pos = 0;
                for (int i = 0; i < 4; i++) {
                    System.arraycopy(emptySection, 0, adjusted, pos, emptySection.length);
                    pos += emptySection.length;
                }
                System.arraycopy(rawData, 0, adjusted, pos, rawData.length);
                pos += rawData.length;
                for (int i = 0; i < 4; i++) {
                    System.arraycopy(emptySection, 0, adjusted, pos, emptySection.length);
                    pos += emptySection.length;
                }

                // Heightmap: add 64 to each value (minY=-64 offset)
                long[] adjustedHeightmap = buildHeightmapForMinY(chunk, 64);

                // Light masks: shift left by 4 (our sections 0-7 -> positions 5-12 in 26-bit range)
                int adjustedSkyLightMask = skyLightMask << 4;
                int adjustedBlockLightMask = blockLightMask << 4;
                int adjustedEmptySkyLightMask = ~adjustedSkyLightMask & 0x3FFFFFF;
                int adjustedEmptyBlockLightMask = ~adjustedBlockLightMask & 0x3FFFFFF;

                collectedPackets.add(new MapChunkPacketV764(
                    chunk.getXPos(), chunk.getZPos(),
                    adjustedHeightmap, adjustedHeightmap,
                    adjusted,
                    adjustedSkyLightMask, adjustedBlockLightMask,
                    adjustedEmptySkyLightMask, adjustedEmptyBlockLightMask,
                    skyArr, blockArr));
            } else if (player.getProtocolVersion().isAtLeast(ProtocolVersion.RELEASE_1_20_2)) {
                // v764/v765: network NBT for heightmaps (no root name).
                collectedPackets.add(new MapChunkPacketV764(
                    chunk.getXPos(), chunk.getZPos(),
                    heightmap, heightmap,
                    v759Data.getRawData(),
                    skyLightMask, blockLightMask,
                    emptySkyLightMask, emptyBlockLightMask,
                    skyArr, blockArr));
            } else if (player.getProtocolVersion().isAtLeast(ProtocolVersion.RELEASE_1_20)) {
                // v763: trustEdges boolean removed from combined chunk+light packet.
                collectedPackets.add(new MapChunkPacketV763(
                    chunk.getXPos(), chunk.getZPos(),
                    heightmap, heightmap,
                    v759Data.getRawData(),
                    skyLightMask, blockLightMask,
                    emptySkyLightMask, emptyBlockLightMask,
                    skyArr, blockArr));
            } else {
                collectedPackets.add(new MapChunkPacketV757(
                    chunk.getXPos(), chunk.getZPos(),
                    heightmap, heightmap,
                    v759Data.getRawData(),
                    skyLightMask, blockLightMask,
                    emptySkyLightMask, emptyBlockLightMask,
                    skyArr, blockArr));
            }

        } else if (player.getProtocolVersion().isAtLeast(ProtocolVersion.RELEASE_1_18)) {
            // v757: Combined chunk data + update light. All 16 sections present,
            // biomes as per-section paletted containers, no primaryBitMask.
            AlphaChunk.V757ChunkData v757Data = chunk.serializeForV757Protocol();
            long[] heightmap = buildHeightmapLongArrayNonSpanning(chunk);

            // Build light masks from per-section light arrays (same logic as V755)
            int skyLightMask = 0;
            int blockLightMask = 0;
            java.util.List<byte[]> skyArrays = new java.util.ArrayList<>();
            java.util.List<byte[]> blockArrays = new java.util.ArrayList<>();

            for (int section = 0; section < 8; section++) {
                if (v757Data.getSkyLightSections()[section] != null) {
                    skyLightMask |= (1 << (section + 1));
                    skyArrays.add(v757Data.getSkyLightSections()[section]);
                }
                if (v757Data.getBlockLightSections()[section] != null) {
                    blockLightMask |= (1 << (section + 1));
                    blockArrays.add(v757Data.getBlockLightSections()[section]);
                }
            }

            int emptySkyLightMask = ~skyLightMask & 0x3FFFF;
            int emptyBlockLightMask = ~blockLightMask & 0x3FFFF;

            // Single combined packet — no separate UpdateLight
            collectedPackets.add(new MapChunkPacketV757(
                chunk.getXPos(), chunk.getZPos(),
                heightmap, heightmap,
                v757Data.getRawData(),
                skyLightMask, blockLightMask,
                emptySkyLightMask, emptyBlockLightMask,
                skyArrays.toArray(new byte[0][]),
                blockArrays.toArray(new byte[0][])));

        } else if (player.getProtocolVersion().isAtLeast(ProtocolVersion.RELEASE_1_17)) {
            // v755: 15-bit global palette with non-spanning packing, 1.17 block state IDs.
            // Chunk bitmask is BitSet, no fullChunk boolean, UpdateLight masks are BitSet.
            AlphaChunk.V573ChunkData v755Data = chunk.serializeForV755Protocol();
            long[] heightmap = buildHeightmapLongArrayNonSpanning(chunk);

            // Build UpdateLight from per-section light arrays
            int skyLightMask = 0;
            int blockLightMask = 0;
            java.util.List<byte[]> skyArrays = new java.util.ArrayList<>();
            java.util.List<byte[]> blockArrays = new java.util.ArrayList<>();

            for (int section = 0; section < 16; section++) {
                if (v755Data.getSkyLightSections()[section] != null) {
                    skyLightMask |= (1 << (section + 1));
                    skyArrays.add(v755Data.getSkyLightSections()[section]);
                }
                if (v755Data.getBlockLightSections()[section] != null) {
                    blockLightMask |= (1 << (section + 1));
                    blockArrays.add(v755Data.getBlockLightSections()[section]);
                }
            }

            int emptySkyLightMask = ~skyLightMask & 0x3FFFF;
            int emptyBlockLightMask = ~blockLightMask & 0x3FFFF;

            // 1.15+: send UpdateLight BEFORE chunk data
            collectedPackets.add(new UpdateLightPacketV755(
                chunk.getXPos(), chunk.getZPos(),
                skyLightMask, blockLightMask,
                emptySkyLightMask, emptyBlockLightMask,
                skyArrays.toArray(new byte[0][]),
                blockArrays.toArray(new byte[0][])));

            collectedPackets.add(new MapChunkPacketV755(
                chunk.getXPos(), chunk.getZPos(),
                v755Data.getPrimaryBitMask(),
                heightmap, heightmap,
                v755Data.getBiomes(),
                v755Data.getRawData()));

        } else if (player.getProtocolVersion().isAtLeast(ProtocolVersion.RELEASE_1_16)) {
            // v735+: 15-bit global palette with non-spanning packing, 1.16 block state IDs
            AlphaChunk.V573ChunkData v735Data = chunk.serializeForV735Protocol();
            long[] heightmap = buildHeightmapLongArrayNonSpanning(chunk);

            // Build UpdateLight from per-section light arrays
            int skyLightMask = 0;
            int blockLightMask = 0;
            java.util.List<byte[]> skyArrays = new java.util.ArrayList<>();
            java.util.List<byte[]> blockArrays = new java.util.ArrayList<>();

            for (int section = 0; section < 8; section++) {
                if (v735Data.getSkyLightSections()[section] != null) {
                    skyLightMask |= (1 << (section + 1));
                    skyArrays.add(v735Data.getSkyLightSections()[section]);
                }
                if (v735Data.getBlockLightSections()[section] != null) {
                    blockLightMask |= (1 << (section + 1));
                    blockArrays.add(v735Data.getBlockLightSections()[section]);
                }
            }

            int emptySkyLightMask = ~skyLightMask & 0x3FFFF;
            int emptyBlockLightMask = ~blockLightMask & 0x3FFFF;

            // 1.15+: send UpdateLight BEFORE chunk data
            collectedPackets.add(new UpdateLightPacketV735(
                chunk.getXPos(), chunk.getZPos(),
                skyLightMask, blockLightMask,
                emptySkyLightMask, emptyBlockLightMask,
                skyArrays.toArray(new byte[0][]),
                blockArrays.toArray(new byte[0][])));

            // v751 (1.16.2): removed ignoreOldLightData, biomes use VarInt array
            if (player.getProtocolVersion().isAtLeast(ProtocolVersion.RELEASE_1_16_2)) {
                collectedPackets.add(new MapChunkPacketV751(
                    chunk.getXPos(), chunk.getZPos(), true,
                    v735Data.getPrimaryBitMask(),
                    heightmap, heightmap,
                    v735Data.getBiomes(),
                    v735Data.getRawData()));
            } else {
                collectedPackets.add(new MapChunkPacketV735(
                    chunk.getXPos(), chunk.getZPos(), true,
                    v735Data.getPrimaryBitMask(),
                    heightmap, heightmap,
                    v735Data.getBiomes(),
                    v735Data.getRawData()));
            }

        } else if (player.getProtocolVersion().isAtLeast(ProtocolVersion.RELEASE_1_15)) {
            // v573: biomes are a separate field (not inside data array), int[1024] 3D biomes
            AlphaChunk.V573ChunkData v573Data = chunk.serializeForV573Protocol();
            long[] heightmap = buildHeightmapLongArray(chunk);

            // Build UpdateLight from per-section light arrays
            int skyLightMask = 0;
            int blockLightMask = 0;
            java.util.List<byte[]> skyArrays = new java.util.ArrayList<>();
            java.util.List<byte[]> blockArrays = new java.util.ArrayList<>();

            for (int section = 0; section < 8; section++) {
                if (v573Data.getSkyLightSections()[section] != null) {
                    skyLightMask |= (1 << (section + 1));
                    skyArrays.add(v573Data.getSkyLightSections()[section]);
                }
                if (v573Data.getBlockLightSections()[section] != null) {
                    blockLightMask |= (1 << (section + 1));
                    blockArrays.add(v573Data.getBlockLightSections()[section]);
                }
            }

            int emptySkyLightMask = ~skyLightMask & 0x3FFFF;
            int emptyBlockLightMask = ~blockLightMask & 0x3FFFF;

            // 1.15+: send UpdateLight BEFORE chunk data. The 1.15 client's
            // ChunkRenderDispatcher requires light to be present before it
            // schedules chunk meshing; without it, the chunk is skipped.
            collectedPackets.add(new UpdateLightPacketV477(
                chunk.getXPos(), chunk.getZPos(),
                skyLightMask, blockLightMask,
                emptySkyLightMask, emptyBlockLightMask,
                skyArrays.toArray(new byte[0][]),
                blockArrays.toArray(new byte[0][])));

            collectedPackets.add(new MapChunkPacketV573(
                chunk.getXPos(), chunk.getZPos(), true,
                v573Data.getPrimaryBitMask(),
                heightmap, heightmap,
                v573Data.getBiomes(),
                v573Data.getRawData()));

        } else if (player.getProtocolVersion().isAtLeast(ProtocolVersion.RELEASE_1_14)) {
            // v477: heightmaps NBT, no light in sections, blockCount per section
            AlphaChunk.V477ChunkData v477Data = chunk.serializeForV477Protocol();
            long[] heightmap = buildHeightmapLongArray(chunk);

            collectedPackets.add(new MapChunkPacketV477(
                chunk.getXPos(), chunk.getZPos(), true,
                v477Data.getPrimaryBitMask(),
                heightmap, heightmap,
                v477Data.getRawData()));

            // Build UpdateLight from per-section light arrays
            int skyLightMask = 0;
            int blockLightMask = 0;
            java.util.List<byte[]> skyArrays = new java.util.ArrayList<>();
            java.util.List<byte[]> blockArrays = new java.util.ArrayList<>();

            for (int section = 0; section < 8; section++) {
                if (v477Data.getSkyLightSections()[section] != null) {
                    // bit 0 = section -1, bit 1 = section 0, etc.
                    skyLightMask |= (1 << (section + 1));
                    skyArrays.add(v477Data.getSkyLightSections()[section]);
                }
                if (v477Data.getBlockLightSections()[section] != null) {
                    blockLightMask |= (1 << (section + 1));
                    blockArrays.add(v477Data.getBlockLightSections()[section]);
                }
            }

            // Empty masks: sections not in the data masks (18-bit range)
            int emptySkyLightMask = ~skyLightMask & 0x3FFFF;
            int emptyBlockLightMask = ~blockLightMask & 0x3FFFF;

            collectedPackets.add(new UpdateLightPacketV477(
                chunk.getXPos(), chunk.getZPos(),
                skyLightMask, blockLightMask,
                emptySkyLightMask, emptyBlockLightMask,
                skyArrays.toArray(new byte[0][]),
                blockArrays.toArray(new byte[0][])));

        } else if (player.getProtocolVersion().isAtLeast(ProtocolVersion.RELEASE_1_13)) {
            // v393: same wire structure as v109 but with 1.13 global block state IDs,
            // 14-bit global palette, and int[256] biomes
            AlphaChunk.V109ChunkData v393Data = chunk.serializeForV393Protocol();
            collectedPackets.add(new MapChunkPacketV109(
                chunk.getXPos(), chunk.getZPos(), true,
                v393Data.getPrimaryBitMask(),
                v393Data.getRawData(),
                true // writeBlockEntityCount (always for 1.9.4+)
            ));
        } else if (player.getProtocolVersion().isAtLeast(ProtocolVersion.RELEASE_1_9)) {
            // v109: paletted sections, VarInt primaryBitMask
            // v110 (1.9.4) adds block entity count at end; v107-v109 do not
            AlphaChunk.V109ChunkData v109Data = chunk.serializeForV109Protocol();
            boolean writeBlockEntityCount = player.getProtocolVersion()
                    .isAtLeast(ProtocolVersion.RELEASE_1_9_4);
            collectedPackets.add(new MapChunkPacketV109(
                chunk.getXPos(), chunk.getZPos(), true,
                v109Data.getPrimaryBitMask(),
                v109Data.getRawData(),
                writeBlockEntityCount
            ));
        } else if (player.getProtocolVersion().isAtLeast(ProtocolVersion.RELEASE_1_8)) {
            // v47: ushort blockStates, raw (uncompressed), VarInt data size
            AlphaChunk.V47ChunkData v47Data = chunk.serializeForV47Protocol();
            collectedPackets.add(new MapChunkPacketV47(
                chunk.getXPos(), chunk.getZPos(), true,
                v47Data.getPrimaryBitMask() & 0xFFFF,
                v47Data.getRawData()
            ));
        } else if (player.getProtocolVersion().isAtLeast(ProtocolVersion.RELEASE_1_3_1)) {
            // v39+: no PreChunk, use MapChunkPacketV39 (no unused int)
            try {
                AlphaChunk.V28ChunkData v28Data = chunk.serializeForV28Protocol();
                collectedPackets.add(new MapChunkPacketV39(
                    chunk.getXPos(), chunk.getZPos(), true,
                    v28Data.getPrimaryBitMask(), (short) 0,
                    v28Data.getCompressedData()
                ));
            } catch (IOException e) {
                System.err.println("Failed to serialize v39 chunk (" + chunk.getXPos() + ", " + chunk.getZPos()
                    + ") for player " + player.getUsername() + ": " + e.getMessage());
            }
        } else if (player.getProtocolVersion().isAtLeast(ProtocolVersion.RELEASE_1_2_1)) {
            // v28/v29: PreChunk required + section-based chunk format with unused int
            collectedPackets.add(new PreChunkPacket(chunk.getXPos(), chunk.getZPos(), true));
            try {
                AlphaChunk.V28ChunkData v28Data = chunk.serializeForV28Protocol();
                collectedPackets.add(new MapChunkPacketV28(
                    chunk.getXPos(), chunk.getZPos(), true,
                    v28Data.getPrimaryBitMask(), (short) 0,
                    v28Data.getCompressedData()
                ));
            } catch (IOException e) {
                System.err.println("Failed to serialize v28 chunk (" + chunk.getXPos() + ", " + chunk.getZPos()
                    + ") for player " + player.getUsername() + ": " + e.getMessage());
            }
        } else {
            // Pre-v28: PreChunk required + flat chunk format
            collectedPackets.add(new PreChunkPacket(chunk.getXPos(), chunk.getZPos(), true));
            try {
                byte[] compressed = chunk.serializeForAlphaProtocol();
                int blockX = chunk.getXPos() * AlphaChunk.WIDTH;
                int blockZ = chunk.getZPos() * AlphaChunk.DEPTH;
                collectedPackets.add(new MapChunkPacket(
                    blockX, (short) 0, blockZ,
                    AlphaChunk.WIDTH - 1,   // sizeX = 15 (width - 1)
                    AlphaChunk.HEIGHT - 1,  // sizeY = 127 (height - 1)
                    AlphaChunk.DEPTH - 1,   // sizeZ = 15 (depth - 1)
                    compressed
                ));
            } catch (IOException e) {
                System.err.println("Failed to serialize chunk (" + chunk.getXPos() + ", " + chunk.getZPos()
                    + ") for player " + player.getUsername() + ": " + e.getMessage());
            }
        }

        // Cache the built packets and send them.
        // Simple eviction: clear oldest half when the cache is full.
        if (!collectedPackets.isEmpty()) {
            if (chunkPacketCache.size() >= MAX_CACHE_SIZE) {
                // Evict ~half of entries to make room for new chunks.
                // ConcurrentHashMap iteration order is arbitrary, which
                // provides roughly random eviction — good enough for a cache.
                int toEvict = MAX_CACHE_SIZE / 2;
                Iterator<Long> cacheIt = chunkPacketCache.keySet().iterator();
                while (cacheIt.hasNext() && toEvict-- > 0) {
                    cacheIt.next();
                    cacheIt.remove();
                }
            }
            chunkPacketCache.put(key, collectedPackets.toArray(new Packet[0]));
        }
        for (Packet p : collectedPackets) {
            player.writePacket(p);
        }
    }

    /**
     * Tell a player's client to unload a chunk.
     * Pre-v39 uses PreChunkPacket with mode=false.
     * v39+ uses MapChunkPacketV39 with groundUpContinuous=true, primaryBitMask=0.
     * v47 uses MapChunkPacketV47 with groundUpContinuous=true, primaryBitMask=0.
     */
    private void sendChunkUnload(ConnectedPlayer player, ChunkCoord coord) {
        // Bedrock/MCPE clients manage their own chunk cache — no explicit unload needed.
        // Server-side tracking in playerChunks still removes the coord so re-sends work.
        if (player.getBedrockSession() != null || player.getMcpeSession() != null) return;

        if (player.getProtocolVersion().isAtLeast(ProtocolVersion.RELEASE_1_9)) {
            // v109: dedicated UnloadChunk packet
            player.writePacket(new UnloadChunkPacketV109(coord.getX(), coord.getZ()));
        } else if (player.getProtocolVersion().isAtLeast(ProtocolVersion.RELEASE_1_8)) {
            // v47 chunk unload: send empty chunk (primaryBitMask=0, biome-only data, raw)
            player.writePacket(new MapChunkPacketV47(
                coord.getX(), coord.getZ(), true, 0, new byte[256]));
        } else if (player.getProtocolVersion().isAtLeast(ProtocolVersion.RELEASE_1_3_1)) {
            // v39+ chunk unload: send an empty chunk (primaryBitMask=0, biome-only data)
            try {
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                java.util.zip.DeflaterOutputStream dos = new java.util.zip.DeflaterOutputStream(baos);
                dos.write(new byte[256]); // 256 bytes of biome data (all zeros = ocean)
                dos.finish();
                dos.close();
                player.writePacket(new MapChunkPacketV39(
                    coord.getX(), coord.getZ(), true, (short) 0, (short) 0,
                    baos.toByteArray()));
            } catch (IOException e) {
                System.err.println("Failed to send chunk unload (" + coord.getX() + ", " + coord.getZ()
                    + ") for player " + player.getUsername() + ": " + e.getMessage());
            }
        } else {
            player.writePacket(new PreChunkPacket(coord.getX(), coord.getZ(), false));
        }
    }

    /**
     * Unload a chunk from memory, saving it to disk first if dirty.
     */
    private void unloadChunk(ChunkCoord coord) {
        AlphaChunk chunk = loadedChunks.remove(coord);
        invalidateChunkCache(coord.getX(), coord.getZ());
        if (chunk != null && dirtyChunks.remove(coord)) {
            try {
                AlphaLevelFormat.saveChunk(worldDir, chunk);
            } catch (IOException e) {
                System.err.println("Failed to save chunk " + coord + " during unload: " + e.getMessage());
            }
        }
    }

    /**
     * Check if any player currently needs a chunk loaded.
     */
    private boolean isChunkNeededByAnyPlayer(ChunkCoord coord) {
        for (Set<ChunkCoord> chunks : playerChunks.values()) {
            if (chunks.contains(coord)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the number of chunks currently loaded in memory.
     */
    public int getLoadedChunkCount() {
        return loadedChunks.size();
    }

    /**
     * Get the view distance (in chunks).
     */
    public int getViewDistance() {
        return viewDistance;
    }

    /**
     * Get the set of chunks a player currently has loaded.
     * Returns an empty set if the player is not tracked.
     */
    public Set<ChunkCoord> getPlayerLoadedChunks(ConnectedPlayer player) {
        Set<ChunkCoord> chunks = playerChunks.get(player);
        return chunks != null ? chunks : new HashSet<>();
    }

    /**
     * Build a heightmap long array for 1.14+ chunk packets.
     *
     * Each heightmap stores 256 values (16x16 columns) at 9 bits per entry,
     * spanning-packed into longs (entries can span across long boundaries).
     * 1.14's SimpleBitStorage still uses spanning packing.
     *
     * The value for each column is the Y of the highest non-air block + 1
     * (0 for all-air columns).
     *
     * @return long array of 36 longs containing the packed heightmap
     */
    private static long[] buildHeightmapLongArray(AlphaChunk chunk) {
        byte[] heightMap = chunk.getHeightMap();
        int bitsPerEntry = 9;
        int totalBits = 256 * bitsPerEntry; // 2304
        int longsNeeded = (totalBits + 63) / 64; // 36
        long[] result = new long[longsNeeded];
        long mask = (1L << bitsPerEntry) - 1;

        for (int i = 0; i < 256; i++) {
            long value = heightMap[i] & 0xFF;
            int bitIndex = i * bitsPerEntry;
            int longIndex = bitIndex / 64;
            int bitOffset = bitIndex % 64;
            result[longIndex] |= (value & mask) << bitOffset;
            if (bitOffset + bitsPerEntry > 64) {
                int bitsInFirst = 64 - bitOffset;
                result[longIndex + 1] |= (value & mask) >> bitsInFirst;
            }
        }

        return result;
    }

    /**
     * Build heightmap long array using non-spanning bit packing (1.16+).
     * 9 bits per entry, 7 entries per long (63 bits used, 1 bit padding), 37 longs.
     */
    static long[] buildHeightmapLongArrayNonSpanning(AlphaChunk chunk) {
        byte[] heightMap = chunk.getHeightMap();
        int bitsPerEntry = 9;
        int entriesPerLong = 64 / bitsPerEntry; // 7
        int longsNeeded = (256 + entriesPerLong - 1) / entriesPerLong; // 37
        long[] result = new long[longsNeeded];
        long mask = (1L << bitsPerEntry) - 1;

        for (int i = 0; i < 256; i++) {
            long value = heightMap[i] & 0xFF;
            int longIndex = i / entriesPerLong;
            int bitOffset = (i % entriesPerLong) * bitsPerEntry;
            result[longIndex] |= (value & mask) << bitOffset;
        }

        return result;
    }

    /**
     * Build heightmap with an offset for minY != 0.
     * For built-in overworld (minY=-64), offset=64 is added to each value.
     */
    private static long[] buildHeightmapForMinY(AlphaChunk chunk, int offset) {
        byte[] heightMap = chunk.getHeightMap();
        int bitsPerEntry = 9;
        int entriesPerLong = 64 / bitsPerEntry; // 7
        int longsNeeded = (256 + entriesPerLong - 1) / entriesPerLong; // 37
        long[] result = new long[longsNeeded];
        long mask = (1L << bitsPerEntry) - 1;

        for (int i = 0; i < 256; i++) {
            long value = (heightMap[i] & 0xFF) + offset;
            int longIndex = i / entriesPerLong;
            int bitOffset = (i % entriesPerLong) * bitsPerEntry;
            result[longIndex] |= (value & mask) << bitOffset;
        }

        return result;
    }

    /**
     * Build an empty chunk section for 1.20.5+ (24-section worlds).
     * Contains air blocks and plains biome (registry ID 1 in vanilla).
     *
     * Format: short blockCount=0, byte bitsPerBlock=0 (single-valued),
     *   VarInt paletteValue=0 (air), VarInt dataLength=0,
     *   byte biomeBits=0, VarInt biomePaletteValue=1 (plains), VarInt biomeDataLength=0
     */
    private static byte[] buildEmptySection766() {
        // short(0) + byte(0) + VarInt(0) + VarInt(0) + byte(0) + VarInt(1) + VarInt(0)
        return new byte[] {
            0x00, 0x00,  // blockCount = 0 (short)
            0x00,        // bitsPerBlock = 0 (single-valued)
            0x00,        // palette value = 0 (air, VarInt)
            0x00,        // data array length = 0 (VarInt)
            0x00,        // biome bitsPerEntry = 0 (single-valued)
            0x01,        // biome palette value = 1 (plains, VarInt)
            0x00         // biome data array length = 0 (VarInt)
        };
    }

    /**
     * Build an empty chunk section for v770 (1.21.5).
     * Same as v766 but without the two VarInt(0) data array length prefixes.
     */
    private static byte[] buildEmptySection770() {
        // short(0) + byte(0) + VarInt(0) + byte(0) + VarInt(1)
        return new byte[] {
            0x00, 0x00,  // blockCount = 0 (short)
            0x00,        // bitsPerBlock = 0 (single-valued)
            0x00,        // palette value = 0 (air, VarInt)
            // no data array length VarInt in v770
            0x00,        // biome bitsPerEntry = 0 (single-valued)
            0x01         // biome palette value = 1 (plains, VarInt)
            // no biome data array length VarInt in v770
        };
    }
}
