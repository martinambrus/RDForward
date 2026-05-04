package org.bukkit.craftbukkit.v1_4_R1;

import com.github.martinambrus.rdforward.api.server.Server;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge;
import org.bukkit.command.SimpleCommandMap;

/** CraftBukkit v1_4_R1 CraftServer stub. HomeSpawnPlus 1.7.4 casts
 *  {@code Bukkit.getServer()} to this type to access the command map. */
public final class CraftServer extends BukkitBridge.BukkitServerAdapter {

    private final SimpleCommandMap commandMap = new SimpleCommandMap();

    public CraftServer(Server rd) {
        super(rd);
    }

    public SimpleCommandMap getCommandMap() {
        return commandMap;
    }
}
