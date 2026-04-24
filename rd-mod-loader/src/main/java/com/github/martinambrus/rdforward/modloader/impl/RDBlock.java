package com.github.martinambrus.rdforward.modloader.impl;

import com.github.martinambrus.rdforward.api.world.Block;
import com.github.martinambrus.rdforward.api.world.BlockType;
import com.github.martinambrus.rdforward.api.world.BlockTypes;
import com.github.martinambrus.rdforward.api.world.World;

/** Lightweight {@code api.Block} snapshot — captures id and location at lookup time. */
public final class RDBlock implements Block {

    private final World world;
    private final int x;
    private final int y;
    private final int z;
    private final byte blockId;

    public RDBlock(World world, int x, int y, int z, byte blockId) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockId = blockId;
    }

    @Override
    public World getWorld() { return world; }

    @Override
    public int getX() { return x; }

    @Override
    public int getY() { return y; }

    @Override
    public int getZ() { return z; }

    @Override
    public BlockType getType() {
        return BlockTypes.byId(blockId & 0xFF);
    }

    @Override
    public boolean setType(BlockType type) {
        return world.setBlock(x, y, z, type);
    }

    public byte getBlockId() { return blockId; }
}
