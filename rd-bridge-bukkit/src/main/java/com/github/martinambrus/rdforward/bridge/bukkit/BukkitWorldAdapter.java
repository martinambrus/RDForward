// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * Wraps an rd-api {@link com.github.martinambrus.rdforward.api.world.World}
 * as a Bukkit-shaped {@link World}. Block reads/writes translate through
 * {@link MaterialMapper}.
 */
public final class BukkitWorldAdapter implements World {

    private final com.github.martinambrus.rdforward.api.world.World backing;

    public BukkitWorldAdapter(com.github.martinambrus.rdforward.api.world.World backing) {
        this.backing = backing;
    }

    @Override public String getName() { return backing.getName(); }

    @Override
    public Block getBlockAt(int x, int y, int z) {
        com.github.martinambrus.rdforward.api.world.Block b = backing.getBlockAt(x, y, z);
        if (b == null) return null;
        return new Block(this, b.getX(), b.getY(), b.getZ(), MaterialMapper.fromApi(b.getType()));
    }

    @Override
    public boolean setBlockType(int x, int y, int z, Material type) {
        return backing.setBlock(x, y, z, MaterialMapper.toApi(type));
    }

    @Override public int getMaxHeight() { return backing.getHeight(); }
    @Override public long getTime() { return backing.getTime(); }
    @Override public void setTime(long time) { backing.setTime(time); }
}
