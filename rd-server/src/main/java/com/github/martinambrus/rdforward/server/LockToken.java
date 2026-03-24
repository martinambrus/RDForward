package com.github.martinambrus.rdforward.server;

/**
 * RAII-style token returned by {@link ChunkLockManager#acquire}.
 * Implements AutoCloseable so locks can be released via try-with-resources.
 */
public final class LockToken implements AutoCloseable {

    final ChunkCoord coord;
    final Usage usage;
    final ChunkLockManager manager;

    LockToken(ChunkCoord coord, Usage usage, ChunkLockManager manager) {
        this.coord = coord;
        this.usage = usage;
        this.manager = manager;
    }

    @Override
    public void close() {
        manager.release(this);
    }
}
