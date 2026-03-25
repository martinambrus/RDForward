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
import com.github.martinambrus.rdforward.world.ChunkSerializationPool;
import com.github.martinambrus.rdforward.world.WorldGenerator;
import com.github.martinambrus.rdforward.world.alpha.AlphaChunk;
import com.github.martinambrus.rdforward.world.alpha.AlphaLevelFormat;
import com.github.martinambrus.rdforward.world.alpha.CanonicalChunkData;
import com.github.martinambrus.rdforward.world.alpha.CanonicalSection;
import com.github.martinambrus.rdforward.world.alpha.CanonicalSectionWriter;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.server.api.Scheduler;

import java.io.ByteArrayOutputStream;
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

import com.github.martinambrus.rdforward.server.bedrock.BedrockChunkConverter;

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

    /**
     * Unified chunk lifecycle holders (Phase 6). Provides a single point of
     * truth for chunk status, player tracking, dirty state, and packet cache.
     * Coexists with the legacy maps during incremental migration.
     */
    private final ConcurrentHashMap<ChunkCoord, ChunkHolder> chunkHolders = new ConcurrentHashMap<>();

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

    /** Reference to the authoritative Classic/RD world for block data overlay. */
    private ServerWorld serverWorld;

    /** Max chunks to deliver per player per tick (pacing). */
    private static final int MAX_CHUNKS_PER_PLAYER_PER_TICK = 4;
    /** Max total chunk deliveries across all players per tick (bounds worst-case tick time). */
    private static final int MAX_TOTAL_DELIVERIES_PER_TICK = 32;
    /** Max dirty chunks to save per incremental save call. */
    private static final int INCREMENTAL_SAVE_BATCH = 4;

    /** Worker pool for CPU-bound chunk generation and skylight computation. */
    private static final AtomicInteger WORKER_ID = new AtomicInteger();
    private final ExecutorService generationPool = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "chunk-gen-" + WORKER_ID.incrementAndGet());
        t.setDaemon(true);
        return t;
    });

    /** Dedicated I/O thread for all disk reads/writes (MPSC lock-free queue). */
    private final ChunkIOThread ioThread = new ChunkIOThread();

    /** Expose the I/O thread so ServerWorld can route its saves through it. */
    public ChunkIOThread getIOThread() { return ioThread; }

    /** Fine-grained per-chunk locking for concurrent operations. */
    private final ChunkLockManager chunkLocks = new ChunkLockManager();

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
    private final ConcurrentHashMap<Long, FutureChunkPackets> chunkPacketCache = new ConcurrentHashMap<>();

    /** Max entries in the chunk packet cache before eviction. */
    private static final int MAX_CACHE_SIZE = 2000;

    /** Cache hit/miss counters for observability (reset on stats log). */
    private final AtomicInteger cacheHits = new AtomicInteger();
    private final AtomicInteger cacheMisses = new AtomicInteger();

    /** Tick counter for periodic stats logging. */
    private int statsTicks = 0;
    private static final int STATS_LOG_INTERVAL_TICKS = 6000; // ~5 minutes at 20 TPS

    /** Bedrock chunk converter for cache invalidation on block changes. */
    private volatile BedrockChunkConverter bedrockChunkConverter;

    /**
     * Per-chunk block change counter for adaptive batching.
     * When a chunk accumulates more than {@link #BATCH_RESEND_THRESHOLD}
     * changes, it is flagged for full resend instead of individual updates.
     */
    private final ConcurrentHashMap<ChunkCoord, AtomicInteger> chunkChangeCounts = new ConcurrentHashMap<>();

    /** Chunks that need a full resend due to excessive individual block changes. */
    private final Set<ChunkCoord> batchResendChunks = ConcurrentHashMap.newKeySet();

    /** Number of individual block changes in a chunk before triggering a full resend. */
    private static final int BATCH_RESEND_THRESHOLD = 64;

    /** Pre-filled sky light array for empty air sections above terrain (V755 light). */
    private static final byte[] FULL_SKY_LIGHT = new byte[2048];
    /** Pre-filled biomes array (plains=1) for V755/V735/V751 chunk packets. */
    private static final int[] PLAINS_BIOMES_1024 = new int[1024];
    /** Pre-filled byte biomes (plains=1) for v109/v110 (1.9-1.12) chunk packets. */
    private static final byte[] PLAINS_BIOMES_256 = new byte[256];
    static {
        java.util.Arrays.fill(FULL_SKY_LIGHT, (byte) 0xFF);
        java.util.Arrays.fill(PLAINS_BIOMES_1024, 1);
        java.util.Arrays.fill(PLAINS_BIOMES_256, (byte) 1);
    }

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
        ioThread.start();
        deliveryTask = Scheduler.runRepeating(0, 1, this::deliverReadyChunks);
    }

    // --- Protocol bucket constants ---
    // Each constant maps to exactly one serialization path in buildChunkPackets().
    // Must be kept in sync with protocolBucket().
    static final int BUCKET_ALPHA       = 2;  // Pre-1.2.1 (Alpha/Beta flat format)
    static final int BUCKET_V28         = 3;  // 1.2.1+ (section-based, PreChunk required)
    static final int BUCKET_V39         = 4;  // 1.3.1+ (no PreChunk)
    static final int BUCKET_V47         = 5;  // 1.8+
    static final int BUCKET_V109        = 6;  // 1.9+ (paletted, no block entity count)
    static final int BUCKET_V110        = 7;  // 1.9.4+ (with block entity count)
    static final int BUCKET_V393        = 8;  // 1.13+
    static final int BUCKET_V477        = 9;  // 1.14+
    static final int BUCKET_V573        = 10; // 1.15+
    static final int BUCKET_V735        = 11; // 1.16+
    static final int BUCKET_V751        = 12; // 1.16.2+
    static final int BUCKET_V755        = 13; // 1.17+
    static final int BUCKET_V757        = 14; // 1.18+
    static final int BUCKET_V759        = 15; // 1.19+
    static final int BUCKET_V763        = 16; // 1.20+
    static final int BUCKET_V764        = 17; // 1.20.2+
    static final int BUCKET_V766        = 18; // 1.20.5+
    static final int BUCKET_V770        = 19; // 1.21.5+
    static final int BUCKET_V775        = 20; // 26.1+

    /**
     * Map a protocol bucket to a {@link CanonicalSectionWriter} target constant.
     * Returns -1 for buckets that don't use the canonical path (Alpha, V28, V39, V47).
     */
    static int bucketToTarget(int bucket) {
        if (bucket >= BUCKET_V775) return CanonicalSectionWriter.TARGET_V775;
        if (bucket >= BUCKET_V770) return CanonicalSectionWriter.TARGET_V770;
        if (bucket >= BUCKET_V759) return CanonicalSectionWriter.TARGET_V759;
        if (bucket >= BUCKET_V757) return CanonicalSectionWriter.TARGET_V757;
        if (bucket >= BUCKET_V755) return CanonicalSectionWriter.TARGET_V755;
        if (bucket >= BUCKET_V735) return CanonicalSectionWriter.TARGET_V735;
        if (bucket >= BUCKET_V477) return CanonicalSectionWriter.TARGET_V477;
        if (bucket >= BUCKET_V393) return CanonicalSectionWriter.TARGET_V393;
        if (bucket >= BUCKET_V109) return CanonicalSectionWriter.TARGET_V109;
        return -1; // not a paletted bucket
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
        if (v.isAtLeast(ProtocolVersion.RELEASE_26_1))   return BUCKET_V775;
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_21_5)) return BUCKET_V770;
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_20_5)) return BUCKET_V766;
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_20_2)) return BUCKET_V764;
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_20))   return BUCKET_V763;
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_19))   return BUCKET_V759;
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_18))   return BUCKET_V757;
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_17))   return BUCKET_V755;
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_16_2)) return BUCKET_V751;
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_16))   return BUCKET_V735;
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_15))   return BUCKET_V573;
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_14))   return BUCKET_V477;
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_13))   return BUCKET_V393;
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_9_4))  return BUCKET_V110;
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_9))    return BUCKET_V109;
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_8))    return BUCKET_V47;
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_3_1))  return BUCKET_V39;
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_2_1))  return BUCKET_V28;
        return BUCKET_ALPHA;
    }

    /**
     * Set the Bedrock chunk converter for cache invalidation.
     * Called from RDServer when the converter is lazily created.
     */
    public void setBedrockChunkConverter(BedrockChunkConverter converter) {
        this.bedrockChunkConverter = converter;
    }

    /**
     * Invalidate all cached chunk packets for a given chunk coordinate.
     * Called when a block changes in that chunk.
     */
    private void invalidateChunkCache(int chunkX, int chunkZ) {
        for (int bucket = BUCKET_ALPHA; bucket <= BUCKET_V775; bucket++) {
            FutureChunkPackets entry = chunkPacketCache.remove(cacheKey(chunkX, chunkZ, bucket));
            if (entry != null) entry.invalidate(); // signal in-flight serialization to discard
        }
        // Also invalidate Bedrock chunk cache
        BedrockChunkConverter converter = bedrockChunkConverter;
        if (converter != null) {
            converter.invalidateCache(chunkX, chunkZ);
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

    /** Get or create a ChunkHolder for the given coordinate. */
    private ChunkHolder getOrCreateHolder(ChunkCoord coord) {
        return chunkHolders.computeIfAbsent(coord, ChunkHolder::new);
    }

    /** Get the ChunkHolder for a coordinate, or null if none exists. */
    public ChunkHolder getHolder(ChunkCoord coord) {
        return chunkHolders.get(coord);
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
            // Remove player from ChunkHolder tracking
            ChunkHolder holder = chunkHolders.get(coord);
            if (holder != null) holder.removeTracker(player);
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

        // Create/update ChunkHolder lifecycle
        ChunkHolder holder = getOrCreateHolder(coord);
        holder.setStatus(ChunkStatus.LOADING);
        holder.setCurrentTransition(future);

        // Phase 1: Load from disk on the I/O thread
        ioThread.submitLoad(worldDir, coord).thenAcceptAsync(diskChunk -> {
            try (LockToken lock = chunkLocks.acquire(coord, Usage.WORLDGEN)) {
                holder.setStatus(ChunkStatus.GENERATING);

                // Double-check cache (may have been loaded between our check and task start)
                AlphaChunk alreadyLoaded = loadedChunks.get(coord);
                if (alreadyLoaded != null) {
                    pendingChunks.remove(coord);
                    holder.setChunk(alreadyLoaded);
                    holder.setStatus(ChunkStatus.READY);
                    future.complete(alreadyLoaded);
                    return;
                }

                AlphaChunk chunk = diskChunk;

                // Generate if not on disk (CPU-bound, runs on generation pool)
                if (chunk == null && worldGenerator.supportsChunkGeneration()) {
                    chunk = worldGenerator.generateChunk(coord.getX(), coord.getZ(), seed);
                } else if (chunk == null && serverWorld != null) {
                    // Finite-world mode (RubyDung/Classic): create empty chunk
                    // so overlayServerWorldBlocks can populate it.
                    chunk = new AlphaChunk(coord.getX(), coord.getZ());
                }

                if (chunk != null) {
                    overlayServerWorldBlocks(chunk);
                    holder.setStatus(ChunkStatus.LIT);
                    chunk.generateSkylightMap();
                    loadedChunks.put(coord, chunk);
                    holder.setChunk(chunk);
                    holder.setStatus(ChunkStatus.READY);
                }

                pendingChunks.remove(coord);
                future.complete(chunk);
            } catch (Exception e) {
                pendingChunks.remove(coord);
                future.completeExceptionally(e);
            }
        }, generationPool).exceptionally(ex -> {
            // Handle RejectedExecutionException from pool shutdown
            if (!future.isDone()) {
                pendingChunks.remove(coord);
                future.completeExceptionally(ex);
            }
            return null;
        });

        return future;
    }

    /**
     * Deliver ready chunks to players. Called every tick by the Scheduler.
     * Drains the pending-sends queue, sending up to {@link #MAX_CHUNKS_PER_PLAYER_PER_TICK}
     * per player per tick.
     */
    private void deliverReadyChunks() {
        // Periodic stats logging
        if (++statsTicks >= STATS_LOG_INTERVAL_TICKS) {
            statsTicks = 0;
            int hits = cacheHits.getAndSet(0);
            int misses = cacheMisses.getAndSet(0);
            int total = hits + misses;
            int hitRate = total > 0 ? (hits * 100 / total) : 0;
            System.out.println("[ChunkManager] Stats: loaded=" + loadedChunks.size()
                    + " cached=" + chunkPacketCache.size()
                    + " cacheHitRate=" + hitRate + "% (" + hits + "/" + total + ")"
                    + " ioTasks=" + ioThread.getTasksProcessed());
        }

        // Evict stale cache entries on the tick thread (avoids contention with async serialization)
        if (chunkPacketCache.size() >= MAX_CACHE_SIZE) {
            int toEvict = MAX_CACHE_SIZE / 2;
            Iterator<Long> cacheIt = chunkPacketCache.keySet().iterator();
            while (cacheIt.hasNext() && toEvict-- > 0) {
                cacheIt.next();
                cacheIt.remove();
            }
        }

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

            int playerBudget = player.getChunkSendBudget();
            int perPlayerLimit = Math.min(playerBudget,
                    MAX_TOTAL_DELIVERIES_PER_TICK - totalSent);
            int sent = 0;
            Iterator<ChunkCoord> it = pending.iterator();
            while (it.hasNext() && sent < perPlayerLimit) {
                ChunkCoord coord = it.next();
                AlphaChunk chunk = loadedChunks.get(coord);
                if (chunk != null) {
                    if (isInViewDistance(player, coord)) {
                        if (sendChunkToPlayer(player, chunk)) {
                            current.add(coord);
                            it.remove();
                            sent++;
                        }
                        // else: async serialization in flight, leave in queue
                    } else {
                        it.remove(); // out of view distance, discard
                    }
                } else if (!pendingChunks.containsKey(coord)) {
                    it.remove();
                }
            }
            player.updateChunkSendRate(sent);
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
     * Shutdown the chunk worker pool. Waits for in-flight generation tasks
     * to complete before saving dirty chunks and stopping the I/O thread.
     *
     * Order matters: generationPool must fully drain first because in-flight
     * generation tasks may submit saves to ioThread. Shutting ioThread before
     * the pool drains would reject those saves.
     */
    public void shutdown() {
        // 1. Stop accepting new generation/serialization tasks
        generationPool.shutdown();
        if (deliveryTask != null) deliveryTask.cancel();

        // 2. Wait for all in-flight generation + serialization to finish
        try {
            if (!generationPool.awaitTermination(10, TimeUnit.SECONDS)) {
                System.err.println("[ChunkManager] Forcing shutdown of generation pool");
                generationPool.shutdownNow();
                generationPool.awaitTermination(5, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            generationPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // 3. Now safe to save + stop I/O thread (no more gen tasks will submit saves)
        pendingSendsPerPlayer.clear();
        pendingChunks.clear();
        saveAllDirty();
        ioThread.shutdown();
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

        // Build desired set and load list using pre-computed spiral order
        // (closest chunks first, no per-player sort needed)
        ChunkCoord[] spiral = SpiralIterator.computeOffsets(viewDistance);
        Set<ChunkCoord> desired = new HashSet<>(spiral.length * 2);
        List<ChunkCoord> toLoad = new ArrayList<>();
        for (ChunkCoord offset : spiral) {
            ChunkCoord coord = new ChunkCoord(centerChunkX + offset.getX(),
                    centerChunkZ + offset.getZ());
            desired.add(coord);
            if (!current.contains(coord)) {
                toLoad.add(coord); // already in spiral (closest-first) order
            }
        }

        // Find chunks to unload (in current but not in desired)
        List<ChunkCoord> toUnload = new ArrayList<>();
        for (ChunkCoord coord : current) {
            if (!desired.contains(coord)) {
                toUnload.add(coord);
            }
        }

        // Unload chunks that left view distance
        for (ChunkCoord coord : toUnload) {
            sendChunkUnload(player, coord);
            current.remove(coord);
            // Update ChunkHolder tracker
            ChunkHolder holder = chunkHolders.get(coord);
            if (holder != null) holder.removeTracker(player);
            if (!isChunkNeededByAnyPlayer(coord)) {
                unloadChunk(coord);
            }
        }

        // Update ChunkHolder trackers for desired chunks
        for (ChunkCoord coord : desired) {
            getOrCreateHolder(coord).addTracker(player);
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

        // Build chunk list in spiral order (closest first, no sort needed)
        ChunkCoord[] spiral = SpiralIterator.computeOffsets(viewDistance);
        List<ChunkCoord> toSend = new ArrayList<>(spiral.length);
        for (ChunkCoord offset : spiral) {
            toSend.add(new ChunkCoord(centerChunkX + offset.getX(),
                    centerChunkZ + offset.getZ()));
        }

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
                if (sendChunkToPlayer(player, cached)) {
                    current.add(coord);
                    sentCount++;
                } else {
                    // Async serialization started — queue for delivery next tick
                    if (pendingQueue == null) {
                        pendingQueue = pendingSendsPerPlayer.computeIfAbsent(
                                player, k -> new ConcurrentLinkedQueue<>());
                    }
                    pendingQueue.add(coord);
                }
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
        // Update ChunkHolder state
        ChunkHolder holder = chunkHolders.get(coord);
        if (holder != null) {
            holder.markDirty();
            holder.invalidatePacketCache();
        }
        invalidateChunkCache(coord.getX(), coord.getZ());

        AtomicInteger counter = chunkChangeCounts.computeIfAbsent(coord, k -> new AtomicInteger());
        if (counter.incrementAndGet() >= BATCH_RESEND_THRESHOLD) {
            batchResendChunks.add(coord);
            counter.set(0);
        }

        return true;
    }

    /**
     * Check for chunks that need a full resend due to excessive individual block changes.
     * Called from the tick loop. When a chunk accumulates more than
     * {@link #BATCH_RESEND_THRESHOLD} individual block changes, resending the full
     * chunk is more efficient than many individual UpdateBlockPacket messages.
     */
    public void checkBatchResend() {
        if (batchResendChunks.isEmpty()) return;

        Iterator<ChunkCoord> it = batchResendChunks.iterator();
        while (it.hasNext()) {
            ChunkCoord coord = it.next();
            it.remove();
            AlphaChunk chunk = loadedChunks.get(coord);
            if (chunk == null) continue;

            // Resend to all players who have this chunk loaded
            for (Map.Entry<ConnectedPlayer, Set<ChunkCoord>> entry : playerChunks.entrySet()) {
                if (entry.getValue().contains(coord)) {
                    sendChunkToPlayer(entry.getKey(), chunk);
                    entry.getKey().flushPackets();
                }
            }
        }
    }

    /**
     * Reset per-chunk block change counters. Called once per tick to
     * allow a fresh window of changes before triggering the next batch resend.
     * Removes entries with zero counts (no changes this tick) to prevent
     * unbounded growth, while keeping active entries in-place to avoid
     * ConcurrentHashMap segment rebuild overhead.
     */
    public void resetChangeCounters() {
        if (!chunkChangeCounts.isEmpty()) {
            Iterator<Map.Entry<ChunkCoord, AtomicInteger>> it =
                    chunkChangeCounts.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<ChunkCoord, AtomicInteger> entry = it.next();
                if (entry.getValue().get() == 0) {
                    it.remove();
                } else {
                    entry.getValue().set(0);
                }
            }
        }
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
            if (ioThread.isSaveInFlight(coord)) continue; // already in-flight
            AlphaChunk chunk = loadedChunks.get(coord);
            if (chunk != null) {
                // Snapshot NBT on tick thread under SAVE lock
                AlphaLevelFormat.SaveTask saveTask;
                try (LockToken lock = chunkLocks.acquire(coord, Usage.SAVE)) {
                    saveTask = AlphaLevelFormat.prepareSave(worldDir, chunk);
                }
                ioThread.submitSave(coord, saveTask).whenComplete((v, ex) -> {
                    if (ex != null) {
                        System.err.println("Failed to save chunk " + coord + ": " + ex.getMessage());
                    } else {
                        dirtyChunks.remove(coord);
                    }
                    ioThread.completeSave(coord);
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
        if (dirtyChunks.isEmpty()) return;
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (ChunkCoord coord : dirtyChunks) {
            if (ioThread.isSaveInFlight(coord)) continue; // in-flight incremental save
            AlphaChunk chunk = loadedChunks.get(coord);
            if (chunk != null) {
                AlphaLevelFormat.SaveTask saveTask;
                try (LockToken lock = chunkLocks.acquire(coord, Usage.SAVE)) {
                    saveTask = AlphaLevelFormat.prepareSave(worldDir, chunk);
                }
                CompletableFuture<Void> f = ioThread.submitSave(coord, saveTask);
                f.whenComplete((v, ex) -> {
                    if (ex != null) {
                        System.err.println("Failed to save chunk " + coord + ": " + ex.getMessage());
                    } else {
                        dirtyChunks.remove(coord);
                    }
                    ioThread.completeSave(coord);
                });
                futures.add(f);
            }
        }
        if (!futures.isEmpty()) {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            System.out.println("Saved " + futures.size() + " dirty chunk(s) to " + worldDir);
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
     *
     * Returns true if packets were sent, false if serialization is pending
     * (the chunk stays in the player's pending queue for the next tick).
     *
     * WARNING: version branches in buildChunkPackets() must match protocolBucket()
     * — each bucket must correspond to exactly one serialization path.
     */
    private boolean sendChunkToPlayer(ConnectedPlayer player, AlphaChunk chunk) {
        // Bedrock (CloudburstMC) — uses its own chunk format via BedrockChunkConverter
        if (player.getBedrockSession() != null) {
            player.getBedrockSession().sendChunkData(chunk);
            return true;
        }
        // Legacy MCPE — uses version-specific codec chunk format
        if (player.getMcpeSession() != null) {
            player.getMcpeSession().sendChunkData(serverWorld, chunk.getXPos(), chunk.getZPos());
            return true;
        }

        // Check chunk packet cache for TCP clients
        int bucket = protocolBucket(player.getProtocolVersion());
        long key = cacheKey(chunk.getXPos(), chunk.getZPos(), bucket);
        FutureChunkPackets cached = chunkPacketCache.get(key);

        if (cached != null && cached != FutureChunkPackets.EMPTY) {
            if (cached.isReady()) {
                cacheHits.incrementAndGet();
                for (Packet p : cached.getPackets()) {
                    player.writePacket(p);
                }
                return true;
            }
            // Serialization in flight — not ready yet
            return false;
        }

        cacheMisses.incrementAndGet();
        FutureChunkPackets future = new FutureChunkPackets();
        FutureChunkPackets existing = chunkPacketCache.putIfAbsent(key, future);
        if (existing != null && existing != FutureChunkPackets.EMPTY) {
            // Another thread beat us — check if it's already ready
            if (existing.isReady()) {
                for (Packet p : existing.getPackets()) {
                    player.writePacket(p);
                }
                return true;
            }
            return false; // in-flight from other thread
        }

        // Submit serialization to the generation pool
        FutureChunkPackets toComplete = (existing == null) ? future : existing;
        try {
            generationPool.submit(() -> {
                try {
                    if (toComplete.isInvalidated()) return; // chunk was modified, discard
                    Packet[] packets = buildChunkPackets(chunk, bucket);
                    if (!toComplete.isInvalidated()) {
                        toComplete.complete(packets);
                    }
                } catch (Exception e) {
                    System.err.println("[ChunkManager] Async serialization failed for chunk ("
                            + chunk.getXPos() + ", " + chunk.getZPos() + ") bucket " + bucket
                            + ": " + e.getMessage());
                    chunkPacketCache.remove(key); // allow retry
                }
            });
        } catch (java.util.concurrent.RejectedExecutionException e) {
            // Pool is shutting down — fall back to synchronous serialization
            chunkPacketCache.remove(key);
            Packet[] packets = buildChunkPackets(chunk, bucket);
            toComplete.complete(packets);
            for (Packet p : packets) {
                player.writePacket(p);
            }
            return true;
        }
        return false; // will be delivered next tick
    }

    /**
     * Build serialized chunk packets for the given protocol bucket.
     * This method is designed to run on the generation pool thread.
     * It reads from immutable chunk data only.
     *
     * For paletted versions (V109+), uses the canonical intermediate form:
     * blocks are iterated once to build version-independent palettes and
     * pre-packed index arrays, then each version only remaps ~10-15 palette
     * entries and copies the packed data. This eliminates redundant 32K-block
     * iterations when multiple protocol versions view the same chunk.
     */
    private Packet[] buildChunkPackets(AlphaChunk chunk, int bucket) {
        List<Packet> collectedPackets = new ArrayList<>();

        int target = bucketToTarget(bucket);

        if (target >= 0) {
            // === Canonical path for all paletted versions (V109 through V770) ===
            CanonicalChunkData canonical = chunk.getOrBuildCanonical();
            buildCanonicalChunkPackets(chunk, bucket, target, canonical, collectedPackets);
        } else if (bucket >= BUCKET_V47) { // 1.8+
            // v47: ushort blockStates, raw (uncompressed), VarInt data size
            AlphaChunk.V47ChunkData v47Data = chunk.serializeForV47Protocol();
            collectedPackets.add(new MapChunkPacketV47(
                chunk.getXPos(), chunk.getZPos(), true,
                v47Data.getPrimaryBitMask() & 0xFFFF,
                v47Data.getRawData()
            ));
        } else if (bucket >= BUCKET_V39) { // 1.3.1+
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
                    + ") bucket " + bucket + ": " + e.getMessage());
            }
        } else if (bucket >= BUCKET_V28) { // 1.2.1+
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
                    + ") bucket " + bucket + ": " + e.getMessage());
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
                    + ") bucket " + bucket + ": " + e.getMessage());
            }
        }

        return collectedPackets.toArray(new Packet[0]);
    }

    /**
     * Build chunk packets using canonical data for paletted versions (V109 through V770).
     * Sections are serialized via CanonicalSectionWriter which only remaps the small
     * palette (~10-15 entries) and copies pre-packed long arrays.
     */
    private void buildCanonicalChunkPackets(AlphaChunk chunk, int bucket, int target,
                                             CanonicalChunkData canonical,
                                             List<Packet> collectedPackets) {
        int chunkX = chunk.getXPos();
        int chunkZ = chunk.getZPos();

        // --- Build light masks from canonical sections (for V477+ which need separate light) ---
        int skyLightMask = 0;
        int blockLightMask = 0;
        List<byte[]> skyArrays = new ArrayList<>();
        List<byte[]> blockArrays = new ArrayList<>();

        // Light data comes from populated sections (0-7)
        for (int section = 0; section < 8; section++) {
            CanonicalSection cs = canonical.getSection(section);
            if (cs.getSkyLight() != null) {
                skyLightMask |= (1 << (section + 1));
                skyArrays.add(cs.getSkyLight());
            }
            if (cs.getBlockLight() != null) {
                blockLightMask |= (1 << (section + 1));
                blockArrays.add(cs.getBlockLight());
            }
        }

        int emptySkyLightMask = ~skyLightMask & 0x3FFFF;
        int emptyBlockLightMask = ~blockLightMask & 0x3FFFF;
        byte[][] skyArr = skyArrays.toArray(new byte[0][]);
        byte[][] blockArr = blockArrays.toArray(new byte[0][]);

        // --- Build section data bytes using CanonicalSectionWriter ---
        ByteArrayOutputStream baos = ChunkSerializationPool.borrowBAOS();
        try {
            boolean needs16Sections = (target >= CanonicalSectionWriter.TARGET_V755);

            if (needs16Sections) {
                // 1.17+: all 16 sections must be present
                for (int section = 0; section < 16; section++) {
                    if (section < 8 && !canonical.getSection(section).isEmpty()) {
                        CanonicalSectionWriter.writePopulatedSection(baos, canonical.getSection(section), target);
                    } else {
                        CanonicalSectionWriter.writeEmptySection(baos, target);
                    }
                }
            } else {
                // Pre-1.17: only sections with non-air blocks
                for (int section = 0; section < 8; section++) {
                    if ((canonical.getPrimaryBitMask() & (1 << section)) != 0) {
                        CanonicalSectionWriter.writePopulatedSection(baos, canonical.getSection(section), target);
                    }
                }
            }

            // Append biomes to section data for versions that include them inline.
            // Must check bucket (not target) because v573 (1.15) shares TARGET_V477's
            // section format but sends biomes as a separate packet field.
            if (bucket <= BUCKET_V110) {
                // v109/v110 (1.9-1.12): byte[256] biomes appended after sections
                baos.write(PLAINS_BIOMES_256, 0, 256);
            } else if (bucket == BUCKET_V393 || bucket == BUCKET_V477) {
                // v393/v477 (1.13-1.14): int[256] biomes appended after sections
                for (int i = 0; i < 256; i++) {
                    baos.write(0); baos.write(0); baos.write(0); baos.write(1); // big-endian int = 1
                }
            }

            byte[] sectionData = baos.toByteArray();

            // --- Construct version-specific packets ---
            if (bucket >= BUCKET_V759) {
                // 1.19+ (combined chunk+light, heightmap non-spanning)
                long[] heightmap = buildHeightmapLongArrayNonSpanning(chunk);

                if (bucket >= BUCKET_V770) {
                    // v770: binary heightmaps, 24-section world
                    byte[] adjusted = build24SectionData(sectionData, target);
                    long[] adjustedHeightmap = buildHeightmapForMinY(chunk, 64);
                    int adjSkyMask = skyLightMask << 4;
                    int adjBlockMask = blockLightMask << 4;
                    collectedPackets.add(new MapChunkPacketV770(
                        chunkX, chunkZ, adjustedHeightmap, adjustedHeightmap,
                        adjusted,
                        adjSkyMask, adjBlockMask,
                        ~adjSkyMask & 0x3FFFFFF, ~adjBlockMask & 0x3FFFFFF,
                        skyArr, blockArr));
                } else if (bucket >= BUCKET_V766) {
                    // v766+: 24-section world
                    byte[] adjusted = build24SectionData(sectionData, target);
                    long[] adjustedHeightmap = buildHeightmapForMinY(chunk, 64);
                    int adjSkyMask = skyLightMask << 4;
                    int adjBlockMask = blockLightMask << 4;
                    collectedPackets.add(new MapChunkPacketV764(
                        chunkX, chunkZ, adjustedHeightmap, adjustedHeightmap,
                        adjusted,
                        adjSkyMask, adjBlockMask,
                        ~adjSkyMask & 0x3FFFFFF, ~adjBlockMask & 0x3FFFFFF,
                        skyArr, blockArr));
                } else if (bucket >= BUCKET_V764) {
                    collectedPackets.add(new MapChunkPacketV764(
                        chunkX, chunkZ, heightmap, heightmap,
                        sectionData,
                        skyLightMask, blockLightMask,
                        emptySkyLightMask, emptyBlockLightMask,
                        skyArr, blockArr));
                } else if (bucket >= BUCKET_V763) {
                    collectedPackets.add(new MapChunkPacketV763(
                        chunkX, chunkZ, heightmap, heightmap,
                        sectionData,
                        skyLightMask, blockLightMask,
                        emptySkyLightMask, emptyBlockLightMask,
                        skyArr, blockArr));
                } else {
                    collectedPackets.add(new MapChunkPacketV757(
                        chunkX, chunkZ, heightmap, heightmap,
                        sectionData,
                        skyLightMask, blockLightMask,
                        emptySkyLightMask, emptyBlockLightMask,
                        skyArr, blockArr));
                }

            } else if (bucket >= BUCKET_V757) {
                // 1.18: combined chunk+light
                long[] heightmap = buildHeightmapLongArrayNonSpanning(chunk);
                collectedPackets.add(new MapChunkPacketV757(
                    chunkX, chunkZ, heightmap, heightmap,
                    sectionData,
                    skyLightMask, blockLightMask,
                    emptySkyLightMask, emptyBlockLightMask,
                    skyArr, blockArr));

            } else if (bucket >= BUCKET_V755) {
                // 1.17: separate UpdateLight + MapChunk
                long[] heightmap = buildHeightmapLongArrayNonSpanning(chunk);

                // For v755, light sections include empty air sections above terrain
                int v755SkyMask = skyLightMask;
                int v755BlockMask = blockLightMask;
                List<byte[]> v755SkyArrays = new ArrayList<>(skyArrays);
                List<byte[]> v755BlockArrays = new ArrayList<>(blockArrays);
                // Add full-sky-light for empty air sections above terrain (8-15)
                for (int section = 8; section < 16; section++) {
                    v755SkyMask |= (1 << (section + 1));
                    v755SkyArrays.add(FULL_SKY_LIGHT);
                }
                int v755EmptySkyMask = ~v755SkyMask & 0x3FFFF;
                int v755EmptyBlockMask = ~v755BlockMask & 0x3FFFF;

                collectedPackets.add(new UpdateLightPacketV755(
                    chunkX, chunkZ,
                    v755SkyMask, v755BlockMask,
                    v755EmptySkyMask, v755EmptyBlockMask,
                    v755SkyArrays.toArray(new byte[0][]),
                    v755BlockArrays.toArray(new byte[0][])));

                collectedPackets.add(new MapChunkPacketV755(
                    chunkX, chunkZ,
                    0xFFFF, // all 16 sections present in the data stream
                    heightmap, heightmap,
                    PLAINS_BIOMES_1024, sectionData));

            } else if (bucket >= BUCKET_V735) {
                // 1.16+: separate UpdateLight + MapChunk
                long[] heightmap = buildHeightmapLongArrayNonSpanning(chunk);

                if (bucket >= BUCKET_V751) {
                    collectedPackets.add(new UpdateLightPacketV735(
                        chunkX, chunkZ,
                        skyLightMask, blockLightMask,
                        emptySkyLightMask, emptyBlockLightMask,
                        skyArr, blockArr));
                    collectedPackets.add(new MapChunkPacketV751(
                        chunkX, chunkZ, true,
                        canonical.getPrimaryBitMask(),
                        heightmap, heightmap,
                        PLAINS_BIOMES_1024, sectionData));
                } else {
                    collectedPackets.add(new UpdateLightPacketV735(
                        chunkX, chunkZ,
                        skyLightMask, blockLightMask,
                        emptySkyLightMask, emptyBlockLightMask,
                        skyArr, blockArr));
                    collectedPackets.add(new MapChunkPacketV735(
                        chunkX, chunkZ, true,
                        canonical.getPrimaryBitMask(),
                        heightmap, heightmap,
                        PLAINS_BIOMES_1024, sectionData));
                }

            } else if (bucket >= BUCKET_V573) {
                // 1.15: separate UpdateLight + MapChunk, biomes separate int[1024]
                long[] heightmap = buildHeightmapLongArray(chunk);

                collectedPackets.add(new UpdateLightPacketV477(
                    chunkX, chunkZ,
                    skyLightMask, blockLightMask,
                    emptySkyLightMask, emptyBlockLightMask,
                    skyArr, blockArr));

                collectedPackets.add(new MapChunkPacketV573(
                    chunkX, chunkZ, true,
                    canonical.getPrimaryBitMask(),
                    heightmap, heightmap,
                    PLAINS_BIOMES_1024, sectionData));

            } else if (bucket >= BUCKET_V477) {
                // 1.14: MapChunk + separate UpdateLight, biomes in data
                long[] heightmap = buildHeightmapLongArray(chunk);

                collectedPackets.add(new MapChunkPacketV477(
                    chunkX, chunkZ, true,
                    canonical.getPrimaryBitMask(),
                    heightmap, heightmap,
                    sectionData));

                collectedPackets.add(new UpdateLightPacketV477(
                    chunkX, chunkZ,
                    skyLightMask, blockLightMask,
                    emptySkyLightMask, emptyBlockLightMask,
                    skyArr, blockArr));

            } else if (bucket >= BUCKET_V393) {
                // 1.13: MapChunk only (light in sections, biomes in data)
                collectedPackets.add(new MapChunkPacketV109(
                    chunkX, chunkZ, true,
                    canonical.getPrimaryBitMask(),
                    sectionData,
                    true)); // writeBlockEntityCount for 1.9.4+

            } else {
                // 1.9-1.9.4: MapChunk only (light in sections, biomes in data)
                boolean writeBlockEntityCount = (bucket >= BUCKET_V110);
                collectedPackets.add(new MapChunkPacketV109(
                    chunkX, chunkZ, true,
                    canonical.getPrimaryBitMask(),
                    sectionData,
                    writeBlockEntityCount));
            }
        } finally {
            ChunkSerializationPool.returnBAOS(baos);
        }
    }

    /**
     * Build 24-section data for 1.20.5+ worlds (minY=-64, height=384).
     * Prepends 4 empty sections (Y -64 to -1) and appends 4 (Y 256-319)
     * to the 16-section data.
     */
    private byte[] build24SectionData(byte[] sixteenSectionData, int target) {
        // Write empty sections directly into the output stream to avoid
        // intermediate byte[] allocation + System.arraycopy overhead.
        ByteArrayOutputStream out = new ByteArrayOutputStream(
                sixteenSectionData.length + 512); // rough estimate for 8 empty sections
        for (int i = 0; i < 4; i++) {
            CanonicalSectionWriter.writeEmptySection(out, target);
        }
        out.write(sixteenSectionData, 0, sixteenSectionData.length);
        for (int i = 0; i < 4; i++) {
            CanonicalSectionWriter.writeEmptySection(out, target);
        }
        return out.toByteArray();
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
        // Update ChunkHolder lifecycle
        ChunkHolder holder = chunkHolders.remove(coord);
        if (holder != null) holder.setStatus(ChunkStatus.UNLOADING);
        if (chunk != null && dirtyChunks.remove(coord)) {
            AlphaLevelFormat.SaveTask saveTask;
            try (LockToken lock = chunkLocks.acquire(coord, Usage.SAVE)) {
                saveTask = AlphaLevelFormat.prepareSave(worldDir, chunk);
            }
            ioThread.submitSave(coord, saveTask).whenComplete((v, ex) -> {
                if (ex != null) {
                    System.err.println("Failed to save chunk " + coord + " during unload: " + ex.getMessage());
                }
                ioThread.completeSave(coord);
            });
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
     * Check whether a specific chunk has been sent to a player.
     * O(1) lookup into the player's ConcurrentHashMap-backed set.
     *
     * @param player the player to check
     * @param chunkX chunk X coordinate (block >> 4)
     * @param chunkZ chunk Z coordinate (block >> 4)
     * @return true if the chunk is in the player's sent set
     */
    public boolean isChunkSentToPlayer(ConnectedPlayer player, int chunkX, int chunkZ) {
        Set<ChunkCoord> chunks = playerChunks.get(player);
        return chunks != null && chunks.contains(new ChunkCoord(chunkX, chunkZ));
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
