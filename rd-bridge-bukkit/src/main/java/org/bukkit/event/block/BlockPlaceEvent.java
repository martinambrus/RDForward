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

    /** @return synthesized BlockState representing what was at the
     *  placement coordinates BEFORE the placement. RDForward does not
     *  snapshot pre-placement state, so the stand-in is AIR at the
     *  placement coords. CoreProtect's BlockPlaceListener calls
     *  {@code event.getBlockReplacedState().getType()} to log the
     *  pre-placement material; AIR is correct for the common case
     *  (placing on empty space) and harmless when wrong. */
    public BlockState getBlockReplacedState() {
        World world = placedBlock == null ? null : placedBlock.getWorld();
        return new com.github.martinambrus.rdforward.bridge.bukkit.BukkitBlockState(world, x, y, z, Material.AIR);
    }

    public boolean canBuild() { return true; }
    public Block getBlockAgainst() { return placedBlock; }
    public ItemStack getItemInHand() { return null; }
    public EquipmentSlot getHand() { return EquipmentSlot.HAND; }
}
