package com.github.martinambrus.rdforward.mixin;

import com.mojang.rubydung.level.Level;
import com.mojang.rubydung.level.LevelListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.ArrayList;

/**
 * Mixin accessor interface to expose Level's internal state for multiplayer.
 *
 * Provides read/write access to the block array and listeners list
 * so the multiplayer code can replace the local world with server data
 * and trigger chunk rebuilds.
 *
 * Usage: ((LevelAccessor) level).getBlocks()
 */
@Mixin(Level.class)
public interface LevelAccessor {

    @Accessor("blocks")
    byte[] getBlocks();

    @Accessor("blocks")
    void setBlocks(byte[] blocks);

    @Accessor("levelListeners")
    ArrayList<LevelListener> getLevelListeners();
}
