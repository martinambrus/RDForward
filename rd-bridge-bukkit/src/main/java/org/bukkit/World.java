// @rdforward:preserve - hand-tuned facade, do not regenerate
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

    /** Pre-Flattening numeric block id at {@code (x,y,z)}. WorldEdit
     *  5.6.1's {@code BukkitWorld.getBlockType} reads the source block
     *  through this signature on every {@code //set} pass — without it
     *  WE bails with {@link NoSuchMethodError} before any coercion runs. */
    default int getBlockTypeIdAt(int x, int y, int z) {
        Block b = getBlockAt(x, y, z);
        if (b == null) return 0;
        Material t = b.getType();
        return t == null ? 0 : t.getId();
    }

    /** Pre-Flattening data nibble at {@code (x,y,z)}. RDForward does
     *  not model block data — return 0. WE 5.6.1's {@code
     *  LocalWorld.getBlock} reads this when its NMS-direct path fails
     *  (which it always does here, since we have no NMS layer). */
    default byte getBlockData(int x, int y, int z) {
        return 0;
    }

    /** WorldEdit 5.6.1's {@code BukkitWorld.checkLoadedChunk} guards
     *  every {@code getBlock} call with this — RDForward holds the
     *  whole world in memory and treats every chunk as loaded. */
    default boolean isChunkLoaded(int chunkX, int chunkZ) {
        return true;
    }

    int getMaxHeight();

    long getTime();

    /** Noop for RDForward — surfaced so plugins that toggle time of day compile. */
    void setTime(long time);

    /** @return {@link World$Environment#NORMAL}. RDForward only models a
     *  single overworld dimension, but LuckPerms's
     *  {@code BukkitPlayerCalculator.calculate} reads
     *  {@code player.getWorld().getEnvironment()} on every permission
     *  check — and a {@link NoSuchMethodError} there cascades through
     *  {@code QueryOptionsCache.supply}, denying every {@code
     *  hasPermission} call and (because LoginSecurity routes its
     *  {@code onAuthChange} through {@code hasPermission}) kicking the
     *  player off the server. */
    default World$Environment getEnvironment() {
        return World$Environment.NORMAL;
    }
}
