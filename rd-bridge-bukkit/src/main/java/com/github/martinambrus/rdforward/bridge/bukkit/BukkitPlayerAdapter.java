// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit;

import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Wraps an rd-api {@link com.github.martinambrus.rdforward.api.player.Player}
 * as a Bukkit-shaped {@link Player}. The wrapper threads the backing
 * reference through so {@code sendMessage}, {@code teleport}, {@code kickPlayer},
 * and {@code getLocation} forward to the real session.
 */
public final class BukkitPlayerAdapter {

    private BukkitPlayerAdapter() {}

    public static Player wrap(com.github.martinambrus.rdforward.api.player.Player backing, World world) {
        if (backing == null) return null;
        return BukkitPlayer.create(backing.getName(), backing, world);
    }
}
