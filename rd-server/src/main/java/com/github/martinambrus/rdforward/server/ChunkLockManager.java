package com.github.martinambrus.rdforward.server;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Fine-grained per-chunk lock manager for concurrent chunk operations.
 *
 * Uses a ReentrantReadWriteLock per chunk coordinate:
 *   - READ: multiple concurrent readers (serialization, sending).
 *   - Exclusive (WORLDGEN, SAVE): blocks other exclusive and read operations.
 *
 * Deadlock prevention: {@link #acquireAll} sorts coords by natural order
 * (x then z) before acquiring, ensuring all callers lock in the same order.
 *
 * Memory management: lock state entries are cleaned up when no locks are
 * held via conditional ConcurrentHashMap.remove().
 */
public final class ChunkLockManager {

    private static final Comparator<ChunkCoord> COORD_ORDER =
            Comparator.comparingInt(ChunkCoord::getX).thenComparingInt(ChunkCoord::getZ);

    private final ConcurrentHashMap<ChunkCoord, ReentrantReadWriteLock> locks = new ConcurrentHashMap<>();

    /**
     * Acquire a lock on a single chunk coordinate.
     * For READ: acquires the read lock (concurrent with other readers).
     * For WORLDGEN/SAVE: acquires the write lock (exclusive).
     */
    public LockToken acquire(ChunkCoord coord, Usage usage) {
        ReentrantReadWriteLock rwLock = locks.computeIfAbsent(coord, k -> new ReentrantReadWriteLock());
        if (usage == Usage.READ) {
            rwLock.readLock().lock();
        } else {
            rwLock.writeLock().lock();
        }
        return new LockToken(coord, usage, this);
    }

    /**
     * Acquire locks on multiple chunk coordinates with deadlock prevention.
     * Sorts coords by natural order before acquiring sequentially.
     */
    public List<LockToken> acquireAll(List<ChunkCoord> coords, Usage usage) {
        List<ChunkCoord> sorted = new ArrayList<>(coords);
        sorted.sort(COORD_ORDER);
        List<LockToken> tokens = new ArrayList<>(sorted.size());
        for (ChunkCoord coord : sorted) {
            tokens.add(acquire(coord, usage));
        }
        return tokens;
    }

    void release(LockToken token) {
        // Atomic unlock + cleanup: compute() holds the segment lock, preventing
        // another thread from acquiring between our unlock and the emptiness check.
        locks.compute(token.coord, (coord, rwLock) -> {
            if (rwLock == null) return null;

            if (token.usage == Usage.READ) {
                rwLock.readLock().unlock();
            } else {
                rwLock.writeLock().unlock();
            }

            // Remove entry if no locks held (atomic with the unlock above)
            if (!rwLock.isWriteLocked() && rwLock.getReadLockCount() == 0) {
                return null; // removes the entry
            }
            return rwLock; // keep the entry
        });
    }

    public static void releaseAll(List<LockToken> tokens) {
        for (LockToken token : tokens) {
            token.close();
        }
    }
}
