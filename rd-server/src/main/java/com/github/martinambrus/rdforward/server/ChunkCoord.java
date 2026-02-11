package com.github.martinambrus.rdforward.server;

/**
 * Immutable chunk coordinate pair, used as a HashMap key for
 * tracking loaded chunks.
 *
 * Chunk coordinates are block coordinates divided by 16 (>> 4).
 * Two ChunkCoords are equal if both x and z match.
 */
public final class ChunkCoord {

    private final int x;
    private final int z;

    public ChunkCoord(int x, int z) {
        this.x = x;
        this.z = z;
    }

    /**
     * Create a ChunkCoord from block coordinates.
     * Uses arithmetic shift (>> 4) to handle negative coordinates correctly.
     */
    public static ChunkCoord fromBlock(int blockX, int blockZ) {
        return new ChunkCoord(blockX >> 4, blockZ >> 4);
    }

    public int getX() { return x; }
    public int getZ() { return z; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChunkCoord)) return false;
        ChunkCoord that = (ChunkCoord) o;
        return x == that.x && z == that.z;
    }

    @Override
    public int hashCode() {
        // Spread the bits to reduce collisions for nearby chunks
        return 31 * x + z;
    }

    @Override
    public String toString() {
        return "ChunkCoord(" + x + ", " + z + ")";
    }
}
