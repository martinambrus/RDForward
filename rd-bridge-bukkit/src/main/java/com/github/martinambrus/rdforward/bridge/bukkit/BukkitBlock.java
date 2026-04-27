// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * Concrete {@link Block} implementation backed by a
 * (world, x, y, z, material) snapshot. Mutations forward through the
 * enclosing world's {@code setBlockType}, which routes to the rd-api
 * {@code World.setBlock} chokepoint where the BlockPolicy runs.
 */
public final class BukkitBlock implements Block {

    private final World world;
    private final int x;
    private final int y;
    private final int z;
    private Material type;

    public BukkitBlock(World world, int x, int y, int z, Material type) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
    }

    @Override public World getWorld() { return world; }
    @Override public int getX() { return x; }
    @Override public int getY() { return y; }
    @Override public int getZ() { return z; }
    @Override public Material getType() { return type; }

    @Override
    public void setType(Material type) {
        this.type = type;
        if (world != null) world.setBlockType(x, y, z, type);
    }

    @Override
    public Location getLocation() { return new Location(world, x, y, z); }
}
