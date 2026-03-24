package com.github.martinambrus.rdforward.server;

/**
 * Lifecycle states for a chunk in the server's chunk pipeline.
 *
 * Each status represents a stage in the chunk's progression from disk/generation
 * to being fully available for serialization and sending to clients.
 *
 * Status progression: EMPTY -> LOADING -> GENERATING -> LIT -> READY -> UNLOADING
 *
 * Transitions are driven by {@link ChunkHolder} and {@link ChunkManager} which
 * compose CompletableFutures to chain the required operations.
 */
public enum ChunkStatus {

    /** Holder created, no data yet. */
    EMPTY,

    /** Disk load submitted to I/O thread. */
    LOADING,

    /** Chunk generation in progress on the generation pool. */
    GENERATING,

    /** Skylight computation complete. */
    LIT,

    /** Chunk is fully loaded/generated and ready for use. */
    READY,

    /** Chunk is being unloaded (save if dirty, then remove). */
    UNLOADING;

    /** Check if this status is at least as advanced as the given status. */
    public boolean isAtLeast(ChunkStatus other) {
        return this.ordinal() >= other.ordinal();
    }
}
