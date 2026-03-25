package com.github.martinambrus.rdforward.world;

import java.io.ByteArrayOutputStream;
import java.util.ArrayDeque;

/**
 * Thread-local object pools for chunk serialization to reduce GC pressure.
 *
 * The serializeForV*Protocol() methods on AlphaChunk create many short-lived
 * objects per call: ByteArrayOutputStream, long[] for bit-packed data arrays,
 * and int[] for palette building. These are allocated on hot paths (every chunk
 * send to every player on cache miss) and immediately discarded after copy.
 *
 * Each pool uses ThreadLocal&lt;ArrayDeque&gt; with a capacity cap. When the pool
 * is empty, fresh objects are allocated (never blocks). When capacity is exceeded,
 * returned objects are discarded (never grows unbounded).
 *
 * Usage pattern:
 * <pre>
 *   ByteArrayOutputStream baos = ChunkSerializationPool.borrowBAOS();
 *   try {
 *       // ... write to baos ...
 *       byte[] result = baos.toByteArray();
 *       return result;
 *   } finally {
 *       ChunkSerializationPool.returnBAOS(baos);
 *   }
 * </pre>
 */
public final class ChunkSerializationPool {

    private ChunkSerializationPool() {} // utility class

    /** Max pooled objects per type per thread. */
    private static final int POOL_CAPACITY = 64;

    // --- ByteArrayOutputStream pool (initial capacity 16384) ---

    private static final ThreadLocal<ArrayDeque<ByteArrayOutputStream>> BAOS_POOL =
            ThreadLocal.withInitial(ArrayDeque::new);

    public static ByteArrayOutputStream borrowBAOS() {
        ByteArrayOutputStream baos = BAOS_POOL.get().pollFirst();
        if (baos != null) {
            baos.reset();
            return baos;
        }
        return new ByteArrayOutputStream(16384);
    }

    public static void returnBAOS(ByteArrayOutputStream baos) {
        if (baos == null) return;
        ArrayDeque<ByteArrayOutputStream> pool = BAOS_POOL.get();
        if (pool.size() < POOL_CAPACITY) {
            pool.addFirst(baos);
        }
    }

    // --- long[1024] pool (bit-packed data arrays, max size for non-spanning 15-bit) ---

    private static final ThreadLocal<ArrayDeque<long[]>> LONG_ARRAY_POOL =
            ThreadLocal.withInitial(ArrayDeque::new);

    /**
     * Borrow a zeroed long[1024] for bit-packed block state data.
     * Callers using fewer than 1024 entries should only read/write
     * the first N elements and pass the full array to returnLongArray().
     */
    public static long[] borrowLongArray() {
        long[] arr = LONG_ARRAY_POOL.get().pollFirst();
        if (arr != null) {
            java.util.Arrays.fill(arr, 0L);
            return arr;
        }
        return new long[1024];
    }

    public static void returnLongArray(long[] arr) {
        if (arr == null || arr.length != 1024) return;
        ArrayDeque<long[]> pool = LONG_ARRAY_POOL.get();
        if (pool.size() < POOL_CAPACITY) {
            pool.addFirst(arr);
        }
    }

    // --- byte[8192] pool (bulk long write buffers for CanonicalSectionWriter) ---

    private static final ThreadLocal<ArrayDeque<byte[]>> LONG_WRITE_BUF_POOL =
            ThreadLocal.withInitial(ArrayDeque::new);

    /** Borrow a byte[8192] for bulk long-to-byte conversion (max 1024 longs * 8 bytes). */
    public static byte[] borrowLongWriteBuf() {
        byte[] buf = LONG_WRITE_BUF_POOL.get().pollFirst();
        return buf != null ? buf : new byte[8192];
    }

    public static void returnLongWriteBuf(byte[] buf) {
        if (buf == null || buf.length != 8192) return;
        ArrayDeque<byte[]> pool = LONG_WRITE_BUF_POOL.get();
        if (pool.size() < POOL_CAPACITY) {
            pool.addFirst(buf);
        }
    }

    // --- int[4096] pool (per-section block state arrays for palette building) ---

    private static final ThreadLocal<ArrayDeque<int[]>> INT_ARRAY_POOL =
            ThreadLocal.withInitial(ArrayDeque::new);

    /** Borrow a zeroed int[4096] for section block state palette building. */
    public static int[] borrowBlockStateArray() {
        int[] arr = INT_ARRAY_POOL.get().pollFirst();
        if (arr != null) {
            java.util.Arrays.fill(arr, 0);
            return arr;
        }
        return new int[4096];
    }

    public static void returnBlockStateArray(int[] arr) {
        if (arr == null || arr.length != 4096) return;
        ArrayDeque<int[]> pool = INT_ARRAY_POOL.get();
        if (pool.size() < POOL_CAPACITY) {
            pool.addFirst(arr);
        }
    }
}
