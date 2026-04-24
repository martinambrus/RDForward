package org.bukkit.event.block;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public final class BlockBreakEvent extends Event {
    private final Player player;
    private final int x, y, z, blockType;

    public BlockBreakEvent(Player player, int x, int y, int z, int blockType) {
        this.player = player;
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockType = blockType;
    }

    public Player getPlayer() { return player; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getBlockType() { return blockType; }
}
