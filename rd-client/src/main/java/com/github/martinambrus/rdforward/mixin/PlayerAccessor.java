package com.github.martinambrus.rdforward.mixin;

import com.mojang.rubydung.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Mixin accessor interface to expose Player's position and rotation fields.
 *
 * RubyDung's Player has package-private/private position fields.
 * This accessor lets the multiplayer code read them without reflection.
 *
 * Usage: ((PlayerAccessor) player).getX()
 */
@Mixin(Player.class)
public interface PlayerAccessor {

    @Accessor("x")
    float getX();

    @Accessor("y")
    float getY();

    @Accessor("z")
    float getZ();

    @Accessor("xRot")
    float getXRot();

    @Accessor("yRot")
    float getYRot();
}
