// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.craftbukkit.v1_6_R3;

import net.minecraft.server.v1_6_R3.WorldServer;

/** CraftBukkit CraftWorld stub for ClearLag v2.6.0 class-loading compatibility. */
public class CraftWorld {

    public WorldServer getHandle() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null,
            "org.bukkit.craftbukkit.v1_6_R3.CraftWorld.getHandle");
        return new WorldServer();
    }
}
