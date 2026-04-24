package io.papermc.paper.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface MovingPiston extends org.bukkit.block.TileState {
    org.bukkit.block.data.BlockData getMovingBlock();
    org.bukkit.block.BlockFace getDirection();
    boolean isExtending();
    boolean isPistonHead();
}
