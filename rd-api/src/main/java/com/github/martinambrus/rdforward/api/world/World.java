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

    /** Place a block. Returns false if out of bounds or rejected by the server.
     *
     *  <p>Implementations may apply a {@link BlockPolicy} to {@code type}
     *  before storage (e.g. RubyDung worlds substitute cobblestone /
     *  grass per position; older-version worlds map "future" blocks
     *  down to their nearest equivalent). The return value reflects
     *  whether the post-coercion block actually changed what was
     *  there — callers that need certainty should re-query via
     *  {@link #getBlockAt(int, int, int)}.
     */
    boolean setBlock(int x, int y, int z, BlockType type);

    boolean isInBounds(int x, int y, int z);

    /** World time in ticks. */
    long getTime();

    void setTime(long time);
}
