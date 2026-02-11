package com.github.martinambrus.rdforward.mixin;

import com.mojang.rubydung.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Mixin accessor interface to expose Timer's internal state.
 *
 * RubyDung's Timer calculates the number of game ticks per render frame
 * and provides the partial tick value for smooth rendering interpolation.
 * This accessor lets the multiplayer code use the actual tick rate (20 TPS)
 * instead of a frame-based counter for position updates.
 *
 * Usage: ((TimerAccessor) timer).getTicks()
 */
@Mixin(Timer.class)
public interface TimerAccessor {

    @Accessor("ticks")
    int getTicks();

    @Accessor("a")
    float getPartialTick();

    @Accessor("passedTime")
    float getPassedTime();
}
