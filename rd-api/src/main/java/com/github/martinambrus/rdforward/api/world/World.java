package com.github.martinambrus.rdforward.api.world;

/**
 * Server-side world view. Mods obtain instances via {@code Server.getWorld()}.
 */
public interface World {

    /** Stable name of the world, e.g. "overworld". */
    String getName();

    int getWidth();
    int getHeight();
    int getDepth();

    /** Returns null if out of bounds. */
    Block getBlockAt(int x, int y, int z);

    /** Place a block. Returns false if out of bounds or rejected by the server. */
    boolean setBlock(int x, int y, int z, BlockType type);

    boolean isInBounds(int x, int y, int z);

    /** World time in ticks. */
    long getTime();

    void setTime(long time);
}
