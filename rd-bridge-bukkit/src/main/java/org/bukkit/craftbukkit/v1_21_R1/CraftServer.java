// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.craftbukkit.v1_21_R1;

import com.github.martinambrus.rdforward.api.server.Server;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge;

/**
 * Thin subclass whose sole purpose is to make
 * {@code Bukkit.getServer().getClass().getPackage().getName()} resolve to
 * {@code org.bukkit.craftbukkit.v1_21_R1}, matching real Paper/CraftBukkit
 * NMS layout. Plugins like Essentials derive the NMS version by splitting
 * that package on '.' and reading index 3, then validating against a regex
 * (v\d+_\d+_R\d+) and an enum whitelist of known NMS versions. v1_21_R1 is
 * the highest version Essentials 2.21.x recognises and is the conventional
 * compatibility-lie used by every Bukkit-emulator (Magma, Mohist, Folia).
 *
 * <p>No method overrides — all behaviour is inherited from
 * {@link BukkitBridge.BukkitServerAdapter}.
 */
public final class CraftServer extends BukkitBridge.BukkitServerAdapter {

    public CraftServer(Server rd) {
        super(rd);
    }
}
