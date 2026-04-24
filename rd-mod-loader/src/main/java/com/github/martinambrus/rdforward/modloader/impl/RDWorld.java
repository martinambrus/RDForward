package com.github.martinambrus.rdforward.modloader.impl;

import com.github.martinambrus.rdforward.api.world.Block;
import com.github.martinambrus.rdforward.api.world.BlockType;
import com.github.martinambrus.rdforward.api.world.World;
import com.github.martinambrus.rdforward.server.ServerWorld;

/** Adapter from {@link ServerWorld} to {@link World}. */
public final class RDWorld implements World {

    private final ServerWorld world;

    public RDWorld(ServerWorld world) {
        this.world = world;
    }

    public ServerWorld delegate() { return world; }

    @Override
    public String getName() { return "overworld"; }

    @Override
    public int getWidth() { return world.getWidth(); }

    @Override
    public int getHeight() { return world.getHeight(); }

    @Override
    public int getDepth() { return world.getDepth(); }

    @Override
    public Block getBlockAt(int x, int y, int z) {
        if (!isInBounds(x, y, z)) return null;
        return new RDBlock(this, x, y, z, world.getBlock(x, y, z));
    }

    @Override
    public boolean setBlock(int x, int y, int z, BlockType type) {
        if (!isInBounds(x, y, z)) return false;
        return world.setBlock(x, y, z, (byte) type.getId());
    }

    @Override
    public boolean isInBounds(int x, int y, int z) {
        return x >= 0 && x < world.getWidth()
                && y >= 0 && y < world.getHeight()
                && z >= 0 && z < world.getDepth();
    }

    @Override
    public long getTime() { return world.getWorldTime(); }

    @Override
    public void setTime(long time) { world.setWorldTime(time); }
}
