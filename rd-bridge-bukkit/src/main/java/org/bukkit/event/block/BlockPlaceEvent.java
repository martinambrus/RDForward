// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event.block;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class BlockPlaceEvent extends Event {
    private final Player player;
    private final int x, y, z, newBlockType;

    public BlockPlaceEvent(Player player, int x, int y, int z, int newBlockType) {
        this.player = player;
        this.x = x;
        this.y = y;
        this.z = z;
        this.newBlockType = newBlockType;
    }

    public BlockPlaceEvent(Block placedAgainst, BlockState replacedState, Block block,
                           ItemStack itemInHand, Player player, boolean canBuild) {
        this(placedAgainst, replacedState, block, itemInHand, player, canBuild, null);
    }

    public BlockPlaceEvent(Block placedAgainst, BlockState replacedState, Block block,
                           ItemStack itemInHand, Player player, boolean canBuild,
                           EquipmentSlot hand) {
        this.player = player;
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.newBlockType = 0;
    }

    public Player getPlayer() { return player; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getNewBlockType() { return newBlockType; }
}
