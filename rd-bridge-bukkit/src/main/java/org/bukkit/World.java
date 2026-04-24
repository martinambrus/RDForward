package org.bukkit;

import org.bukkit.block.Block;

/**
 * Bukkit-shaped {@code World} facade. Backed by an rd-api
 * {@link com.github.martinambrus.rdforward.api.world.World} via
 * {@link com.github.martinambrus.rdforward.bridge.bukkit.BukkitWorldAdapter}.
 * Methods beyond what RDForward surfaces (weather, biomes, entity queries)
 * return sensible defaults or noop.
 */
public interface World {

    String getName();

    /** @return a Bukkit {@link Block} view of the world block at {@code (x,y,z)}, or null if out of bounds. */
    Block getBlockAt(int x, int y, int z);

    /** Set the block type at {@code (x,y,z)}. @return true if the placement succeeded. */
    boolean setBlockType(int x, int y, int z, Material type);

    int getMaxHeight();

    long getTime();

    /** Noop for RDForward — surfaced so plugins that toggle time of day compile. */
    void setTime(long time);
}
