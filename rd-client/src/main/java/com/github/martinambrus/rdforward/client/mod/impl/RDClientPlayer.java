package com.github.martinambrus.rdforward.client.mod.impl;

import com.github.martinambrus.rdforward.api.client.ClientPlayer;
import com.mojang.rubydung.Player;

/**
 * Read-only adapter exposing the local {@link Player} through the
 * {@link ClientPlayer} API. Positional fields follow the internal convention
 * (Y at eye level = feet + 1.62; yaw in Classic degrees where 0 = North).
 */
public final class RDClientPlayer implements ClientPlayer {

    private final Player player;
    private final String name;

    public RDClientPlayer(Player player, String name) {
        this.player = player;
        this.name = name;
    }

    @Override public String getName() { return name; }
    @Override public double getX() { return player.x; }
    @Override public double getY() { return player.y; }
    @Override public double getZ() { return player.z; }
    @Override public float getYaw() { return player.yRot; }
    @Override public float getPitch() { return player.xRot; }
}
