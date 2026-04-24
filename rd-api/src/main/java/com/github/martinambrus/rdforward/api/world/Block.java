package com.github.martinambrus.rdforward.api.world;

/**
 * Block in the world at a specific position.
 */
public interface Block {

    BlockType getType();

    int getX();
    int getY();
    int getZ();

    /** Owning world. */
    World getWorld();

    /** Replace this block's type. */
    boolean setType(BlockType type);
}
