package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.world.alpha.AlphaChunk;
import com.github.martinambrus.rdforward.world.alpha.AlphaLevelFormat;

import io.netty.util.internal.PlatformDependent;

import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * Dedicated I/O thread for all chunk and world disk operations.
 *
 * Uses a lock-free MPSC (multi-producer, single-consumer) queue from Netty's
 * JCTools integration. Producers (tick thread, generation threads) enqueue
 * tasks and unpark the I/O thread. The I/O thread drains all available tasks
 * each wake cycle, then parks when idle.
 *
 * This replaces the shared ExecutorService that previously handled both
 * CPU-bound chunk generation and disk I/O on the same thread pool.
 *
 * Crash safety is preserved: each chunk save still uses temp file + atomic
 * rename via {@link AlphaLevelFormat.SaveTask#writeToDisk()}.
 */
public class ChunkIOThread implements Runnable {

    /** MPSC queue — multiple producers (tick/gen threads), single consumer (this thread). */
    private final Queue<IOTask> queue = PlatformDependent.newMpscQueue();

    /** Queue depth high-water mark warning threshold. */
    private static final int QUEUE_DEPTH_WARNING = 64;

    /** Tracks number of tasks processed for periodic stats logging. */
    private final AtomicInteger tasksProcessed = new AtomicInteger();

    /**
     * Tracks in-flight save operations for read-after-write consistency.
     * When a load request arrives for a coord that's being saved, the load
     * can wait on this future to avoid reading stale data from disk.
     */
    private final ConcurrentHashMap<ChunkCoord, CompletableFuture<Void>> inFlightSaves =
            new ConcurrentHashMap<>();

    /** Check if a save is in-flight for the given coord. */
    public boolean isSaveInFlight(ChunkCoord coord) {
        return inFlightSaves.containsKey(coord);
    }

    /** Remove a completed in-flight save entry. */
    public void completeSave(ChunkCoord coord) {
        inFlightSaves.remove(coord);
    }

    private volatile boolean running;
    private Thread thread;

    /** Start the I/O thread. */
    public void start() {
        if (running) return;
        running = true;
        thread = new Thread(this, "RDForward-ChunkIO");
        thread.setDaemon(true);
        thread.start();
    }

    /** Submit a task to the I/O queue. */
    public void submit(IOTask task) {
        queue.add(task);
        LockSupport.unpark(thread);
    }

    /**
     * Submit a chunk save task with in-flight tracking for read-after-write
     * consistency. Returns a future that completes when the save is done.
     */
    public CompletableFuture<Void> submitSave(ChunkCoord coord, AlphaLevelFormat.SaveTask saveTask) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        inFlightSaves.put(coord, future);
        submit(new SaveChunkTask(coord, saveTask, future));
        return future;
    }

    /**
     * Submit a chunk load task. Returns a future that completes with the
     * loaded chunk (or null if not on disk).
     */
    public CompletableFuture<AlphaChunk> submitLoad(File worldDir, ChunkCoord coord) {
        // If this coord is currently being saved, chain the load after the save
        CompletableFuture<Void> saveFuture = inFlightSaves.get(coord);
        CompletableFuture<AlphaChunk> loadFuture = new CompletableFuture<>();
        if (saveFuture != null) {
            // Wait for save to finish, then load
            saveFuture.whenComplete((v, ex) -> submit(new LoadChunkTask(worldDir, coord, loadFuture)));
        } else {
            submit(new LoadChunkTask(worldDir, coord, loadFuture));
        }
        return loadFuture;
    }

    /** Submit a generic write task (e.g., world save, player save). */
    public void submitWrite(Runnable writeAction) {
        submit(new GenericWriteTask(writeAction));
    }

    /** Get the number of tasks processed since startup. */
    public int getTasksProcessed() {
        return tasksProcessed.get();
    }

    @Override
    public void run() {
        while (running) {
            IOTask task = queue.poll();
            if (task != null) {
                // Drain all available tasks before parking
                int drained = 0;
                do {
                    processTask(task);
                    drained++;
                    task = queue.poll();
                } while (task != null);
                tasksProcessed.addAndGet(drained);

                // Warn if queue is backing up (tasks arrived faster than we drained)
                int remaining = queue.size();
                if (remaining > QUEUE_DEPTH_WARNING) {
                    System.err.println("[ChunkIO] Queue backlog: " + remaining
                            + " tasks pending after draining " + drained);
                }
            } else {
                // No tasks — park until unparked by a producer
                LockSupport.parkNanos(1_000_000L); // 1ms max idle
            }
        }

        // Drain remaining tasks on shutdown
        IOTask task;
        while ((task = queue.poll()) != null) {
            processTask(task);
        }
    }

    private void processTask(IOTask task) {
        try {
            task.execute();
        } catch (Exception e) {
            System.err.println("[ChunkIO] Task failed: " + e.getMessage());
            e.printStackTrace();
            task.completeExceptionally(e);
        }
    }

    /** Shutdown the I/O thread. Drains remaining tasks and waits for completion. */
    public void shutdown() {
        running = false;
        if (thread != null) {
            LockSupport.unpark(thread);
            try {
                thread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // === Task types ===

    /** Base interface for all I/O tasks. */
    sealed interface IOTask permits SaveChunkTask, LoadChunkTask, GenericWriteTask {
        void execute() throws Exception;
        default void completeExceptionally(Exception e) {}
    }

    /** Save a chunk to disk. */
    record SaveChunkTask(ChunkCoord coord, AlphaLevelFormat.SaveTask saveTask,
                         CompletableFuture<Void> future) implements IOTask {
        @Override
        public void execute() throws IOException {
            try {
                saveTask.writeToDisk();
                future.complete(null);
            } catch (IOException e) {
                future.completeExceptionally(e);
                throw e;
            }
        }

        @Override
        public void completeExceptionally(Exception e) {
            future.completeExceptionally(e);
        }
    }

    /** Load a chunk from disk. */
    record LoadChunkTask(File worldDir, ChunkCoord coord,
                         CompletableFuture<AlphaChunk> future) implements IOTask {
        @Override
        public void execute() {
            try {
                AlphaChunk chunk = AlphaLevelFormat.loadChunk(worldDir, coord.getX(), coord.getZ());
                future.complete(chunk); // null if not on disk
            } catch (IOException e) {
                System.err.println("[ChunkIO] Failed to load chunk " + coord + ": " + e.getMessage());
                future.complete(null); // treat load failure as "not on disk"
            }
        }

        @Override
        public void completeExceptionally(Exception e) {
            future.complete(null);
        }
    }

    /** Generic write task for world/player saves. */
    record GenericWriteTask(Runnable action) implements IOTask {
        @Override
        public void execute() {
            action.run();
        }
    }
}
