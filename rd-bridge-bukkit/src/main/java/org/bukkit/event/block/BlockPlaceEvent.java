// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event.block;

import com.github.martinambrus.rdforward.api.world.BlockTypes;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBlock;
import com.github.martinambrus.rdforward.bridge.bukkit.MaterialMapper;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class BlockPlaceEvent extends Event {
    private final Player player;
    private final int x, y, z, newBlockType;
    private final Block placedBlock;

    public BlockPlaceEvent(Player player, int x, int y, int z, int newBlockType) {
        this.player = player;
        this.x = x;
        this.y = y;
        this.z = z;
        this.newBlockType = newBlockType;
        // Synthesize the placed block so Essentials's
        // {@code event.getBlockPlaced().getType()} round-trip works.
        // World comes from the placer; the rd-api block id round-trips
        // through MaterialMapper to recover the Bukkit Material.
        World world = player == null ? null : player.getWorld();
        Material mat = MaterialMapper.fromApi(BlockTypes.byId(newBlockType));
        this.placedBlock = new BukkitBlock(world, x, y, z, mat);
    }

    public BlockPlaceEvent(Block placedAgainst, BlockState replacedState, Block block,
                           ItemStack itemInHand, Player player, boolean canBuild) {
        this(placedAgainst, replacedState, block, itemInHand, player, canBuild, null);
    }

    public BlockPlaceEvent(Block placedAgainst, BlockState replacedState, Block block,
                           ItemStack itemInHand, Player player, boolean canBuild,
                           EquipmentSlot hand) {
        this.player = player;
        this.x = block == null ? 0 : block.getX();
        this.y = block == null ? 0 : block.getY();
        this.z = block == null ? 0 : block.getZ();
        this.newBlockType = 0;
        this.placedBlock = block;
    }

    public Player getPlayer() { return player; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getNewBlockType() { return newBlockType; }
    public Block getBlockPlaced() { return placedBlock; }
    public Block getBlock() { return placedBlock; }
}
