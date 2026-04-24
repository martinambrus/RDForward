package com.github.martinambrus.rdforward.client.mod.impl;

import com.github.martinambrus.rdforward.api.client.ClientWorld;
import com.mojang.rubydung.level.Level;

/**
 * Read-only adapter that exposes a {@link Level} through the
 * {@link ClientWorld} API. Held per-mod at client-ready time and handed to
 * listeners of {@code ClientEvents.RENDER_WORLD} / {@code RENDER_HUD}
 * that need to query world state (raycasts, waypoint visibility, etc.).
 */
public final class RDClientWorld implements ClientWorld {

    private final Level level;

    public RDClientWorld(Level level) {
        this.level = level;
    }

    @Override public int getWidth() { return level.width; }
    @Override public int getHeight() { return level.depth; }
    @Override public int getDepth() { return level.height; }

    @Override
    public boolean isSolid(int x, int y, int z) {
        return isInBounds(x, y, z) && level.isSolidTile(x, y, z);
    }

    @Override
    public boolean isInBounds(int x, int y, int z) {
        return x >= 0 && y >= 0 && z >= 0
                && x < level.width && y < level.depth && z < level.height;
    }
}
