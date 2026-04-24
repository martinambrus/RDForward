package org.bukkit.event.block;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public final class BlockPlaceEvent extends Event {
    private final Player player;
    private final int x, y, z, newBlockType;

    public BlockPlaceEvent(Player player, int x, int y, int z, int newBlockType) {
        this.player = player;
        this.x = x;
        this.y = y;
        this.z = z;
        this.newBlockType = newBlockType;
    }

    public Player getPlayer() { return player; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getNewBlockType() { return newBlockType; }
}
