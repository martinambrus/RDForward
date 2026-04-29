// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

/**
 * Snapshot {@link BlockState} backed by a (world, x, y, z, material)
 * tuple. Used by {@link org.bukkit.event.block.BlockPlaceEvent} to
 * synthesize a "replaced state" stand-in for plugins (CoreProtect's
 * BlockPlaceListener) that call
 * {@code event.getBlockReplacedState().getType()} to recover what was
 * at the placement coordinates before the placement happened. RDForward
 * does not snapshot world state per-tick, so the synthesized state
 * represents AIR at the placement coords by default.
 */
public final class BukkitBlockState implements BlockState {

    private final World world;
    private final int x, y, z;
    private Material type;

    public BukkitBlockState(World world, int x, int y, int z, Material type) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type == null ? Material.AIR : type;
    }

    @Override public Block getBlock() { return new BukkitBlock(world, x, y, z, type); }
    @Override public org.bukkit.material.MaterialData getData() { return null; }
    @Override public org.bukkit.block.data.BlockData getBlockData() { return new BukkitBlockData(type); }
    @Override public BlockState copy() { return new BukkitBlockState(world, x, y, z, type); }
    @Override public BlockState copy(Location loc) {
        if (loc == null) return copy();
        return new BukkitBlockState(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), type);
    }
    @Override public Material getType() { return type; }
    @Override public byte getLightLevel() { return 15; }
    @Override public World getWorld() { return world; }
    @Override public int getX() { return x; }
    @Override public int getY() { return y; }
    @Override public int getZ() { return z; }
    @Override public Location getLocation() { return new Location(world, x, y, z); }
    @Override public Location getLocation(Location reuse) {
        if (reuse == null) return getLocation();
        reuse.setWorld(world);
        reuse.setX(x);
        reuse.setY(y);
        reuse.setZ(z);
        return reuse;
    }
    @Override public org.bukkit.Chunk getChunk() { return null; }
    @Override public void setData(org.bukkit.material.MaterialData arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.block.BlockState.setData");
    }
    @Override public void setBlockData(org.bukkit.block.data.BlockData arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.block.BlockState.setBlockData");
    }
    @Override public void setType(Material newType) { this.type = newType == null ? Material.AIR : newType; }
    @Override public boolean update() { return false; }
    @Override public boolean update(boolean force) { return false; }
    @Override public boolean update(boolean force, boolean applyPhysics) { return false; }
    @Override public byte getRawData() { return 0; }
    @Override public void setRawData(byte arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.block.BlockState.setRawData");
    }
    @Override public boolean isPlaced() { return true; }
    @Override public boolean isCollidable() { return type != Material.AIR; }
    @Override public java.util.Collection getDrops(org.bukkit.inventory.ItemStack arg0, org.bukkit.entity.Entity arg1) {
        return java.util.Collections.emptyList();
    }
    @Override public boolean isSuffocating() { return false; }

    @Override public void setMetadata(String key, org.bukkit.metadata.MetadataValue value) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.block.BlockState.setMetadata");
    }
    @Override public java.util.List getMetadata(String key) { return java.util.Collections.emptyList(); }
    @Override public boolean hasMetadata(String key) { return false; }
    @Override public void removeMetadata(String key, org.bukkit.plugin.Plugin plugin) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.block.BlockState.removeMetadata");
    }
}
