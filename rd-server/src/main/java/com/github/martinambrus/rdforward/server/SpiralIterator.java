package com.github.martinambrus.rdforward.server;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Pre-computes chunk coordinate offsets in outward-spiral order from (0,0).
 *
 * Ring 0 is the center chunk itself. Ring N covers the perimeter at
 * Chebyshev distance N. Within each ring, offsets proceed clockwise
 * starting from the top-left corner. This ordering ensures the closest
 * chunks are always sent first without requiring a per-player sort.
 *
 * Results are cached per view distance since the spiral pattern only
 * depends on radius.
 */
public final class SpiralIterator {

    private SpiralIterator() {} // utility class

    private static final ConcurrentHashMap<Integer, ChunkCoord[]> CACHE = new ConcurrentHashMap<>();

    /**
     * Get pre-computed spiral offsets for the given view distance (radius).
     * Returns (2*radius+1)^2 offsets starting from (0,0) and spiraling outward.
     */
    public static ChunkCoord[] computeOffsets(int radius) {
        return CACHE.computeIfAbsent(radius, SpiralIterator::buildSpiral);
    }

    private static ChunkCoord[] buildSpiral(int radius) {
        int side = 2 * radius + 1;
        ChunkCoord[] offsets = new ChunkCoord[side * side];
        int idx = 0;

        // Ring 0: center chunk
        offsets[idx++] = new ChunkCoord(0, 0);

        // Ring 1..radius: walk the perimeter of each ring
        for (int ring = 1; ring <= radius; ring++) {
            // Start at top-left corner of the ring: (-ring, -ring)
            int x = -ring;
            int z = -ring;

            // Top edge: left to right (exclusive of last corner)
            for (int i = 0; i < 2 * ring; i++) {
                offsets[idx++] = new ChunkCoord(x + i, z);
            }
            // Right edge: top to bottom
            x = ring;
            for (int i = 0; i < 2 * ring; i++) {
                offsets[idx++] = new ChunkCoord(x, z + i);
            }
            // Bottom edge: right to left
            z = ring;
            for (int i = 0; i < 2 * ring; i++) {
                offsets[idx++] = new ChunkCoord(x - i, z);
            }
            // Left edge: bottom to top
            x = -ring;
            for (int i = 0; i < 2 * ring; i++) {
                offsets[idx++] = new ChunkCoord(x, z - i);
            }
        }

        return offsets;
    }
}
