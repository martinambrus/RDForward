package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.world.alpha.AlphaChunk;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Unified state container for a single chunk in the server's chunk pipeline.
 *
 * Replaces the scattered per-chunk state previously held across multiple maps
 * in ChunkManager (loadedChunks, dirtyChunks, pendingChunks, chunkPacketCache).
 *
 * Each ChunkHolder tracks:
 * - The chunk's lifecycle status (via {@link ChunkStatus})
 * - The loaded/generated chunk data
 * - Which players are tracking this chunk
 * - Dirty state for save scheduling
 * - Per-bucket serialized packet cache
 * - The current transition future (for async progression)
 *
 * Thread safety: status and chunk are volatile for visibility. Callers must
 * write chunk before status (setChunk then setStatus) so that readers who
 * observe READY are guaranteed to see the chunk reference. The trackedBy
 * set uses ConcurrentHashMap.newKeySet(). Packet cache is a ConcurrentHashMap.
 */
public final class ChunkHolder {

    private final ChunkCoord coord;
    private volatile ChunkStatus status = ChunkStatus.EMPTY;
    private volatile AlphaChunk chunk;
    private volatile boolean dirty;
    private volatile CompletableFuture<AlphaChunk> currentTransition;

    /** Players that need this chunk loaded and sent. */
    private final Set<ConnectedPlayer> trackedBy = ConcurrentHashMap.newKeySet();

    /** Per-protocol-bucket serialized packet cache. */
    private final ConcurrentHashMap<Integer, FutureChunkPackets> cachedPackets = new ConcurrentHashMap<>();

    public ChunkHolder(ChunkCoord coord) {
        this.coord = coord;
    }

    // --- Status ---

    public ChunkCoord getCoord() { return coord; }

    public ChunkStatus getStatus() { return status; }

    public void setStatus(ChunkStatus status) { this.status = status; }

    // --- Chunk data ---

    public AlphaChunk getChunk() { return chunk; }

    public void setChunk(AlphaChunk chunk) {
        this.chunk = chunk;
    }

    // --- Dirty tracking ---

    public boolean isDirty() { return dirty; }

    public void markDirty() { this.dirty = true; }

    public void clearDirty() { this.dirty = false; }

    // --- Player tracking ---

    public void addTracker(ConnectedPlayer player) {
        trackedBy.add(player);
    }

    public void removeTracker(ConnectedPlayer player) {
        trackedBy.remove(player);
    }

    public boolean isNeeded() {
        return !trackedBy.isEmpty();
    }

    public Set<ConnectedPlayer> getTrackers() {
        return trackedBy;
    }

    // --- Packet cache ---

    public FutureChunkPackets getCachedPackets(int bucket) {
        return cachedPackets.get(bucket);
    }

    public FutureChunkPackets putCachedPacketsIfAbsent(int bucket, FutureChunkPackets future) {
        return cachedPackets.putIfAbsent(bucket, future);
    }

    public void putCachedPackets(int bucket, FutureChunkPackets future) {
        cachedPackets.put(bucket, future);
    }

    public void invalidatePacketCache() {
        for (FutureChunkPackets fcp : cachedPackets.values()) {
            fcp.invalidate();
        }
        cachedPackets.clear();
    }

    // --- Transition future ---

    public CompletableFuture<AlphaChunk> getCurrentTransition() {
        return currentTransition;
    }

    public void setCurrentTransition(CompletableFuture<AlphaChunk> future) {
        this.currentTransition = future;
    }
}
