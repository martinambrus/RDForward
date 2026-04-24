package org.bukkit.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

/**
 * Bukkit-shaped {@code Block}. Wraps a position + world + material
 * snapshot. Mutations flow through
 * {@link com.github.martinambrus.rdforward.bridge.bukkit.BukkitWorldAdapter}
 * which forwards to the rd-api {@code World.setBlock}.
 */
public class Block {

    private final World world;
    private final int x;
    private final int y;
    private final int z;
    private Material type;

    public Block(World world, int x, int y, int z, Material type) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
    }

    public World getWorld() { return world; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }

    public Material getType() { return type; }

    /** Set the block type. Forwards through the enclosing world. */
    public void setType(Material type) {
        this.type = type;
        if (world != null) world.setBlockType(x, y, z, type);
    }

    public Location getLocation() { return new Location(world, x, y, z); }

    public boolean isEmpty() { return type == null || type == Material.AIR; }
}
