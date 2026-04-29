// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event.block;

import com.github.martinambrus.rdforward.api.world.BlockTypes;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBlock;
import com.github.martinambrus.rdforward.bridge.bukkit.MaterialMapper;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * Block break event. Real Bukkit's signature is
 * {@code BlockBreakEvent(Block, Player)}; the prior RDForward stub
 * declared a {@code (Player, int, int, int, int)} form for the
 * adapter's broadcast pipeline. Both ctors are kept — the adapter
 * synthesises a {@link BukkitBlockHolder} for legacy listeners that
 * call {@link #getBlock()}, and Bukkit plugins (Essentials's
 * {@code Commandbreak}) that {@code new BlockBreakEvent(block, player)}
 * link cleanly via the canonical ctor.
 */
public final class BlockBreakEvent extends Event {

    private final Player player;
    private final Block block;
    private final int x, y, z, blockType;

    public BlockBreakEvent(Player player, int x, int y, int z, int blockType) {
        this.player = player;
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockType = blockType;
        // Synthesize the broken Block so listeners that call
        // {@code event.getBlock().getWorld()/getType()/getLocation()}
        // (CoreProtect's {@code BlockBreakListener}) don't NPE.
        World world = player == null ? null : player.getWorld();
        Material mat = MaterialMapper.fromApi(BlockTypes.byId(blockType));
        this.block = new BukkitBlock(world, x, y, z, mat);
    }

    /** Canonical Bukkit ctor. The prior 5-arg form used by the
     *  adapter's broadcast pipeline is preserved alongside. */
    public BlockBreakEvent(Block block, Player player) {
        this.player = player;
        this.block = block;
        this.x = block == null ? 0 : block.getX();
        this.y = block == null ? 0 : block.getY();
        this.z = block == null ? 0 : block.getZ();
        this.blockType = 0;
    }

    public Player getPlayer() { return player; }
    public Block getBlock() { return block; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getBlockType() { return blockType; }
}
