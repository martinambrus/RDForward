package org.bukkit.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Conduit extends org.bukkit.block.TileState {
    boolean isActive();
    boolean isHunting();
    java.util.Collection getFrameBlocks();
    int getFrameBlockCount();
    int getRange();
    boolean setTarget(org.bukkit.entity.LivingEntity arg0);
    org.bukkit.entity.LivingEntity getTarget();
    boolean hasTarget();
    org.bukkit.util.BoundingBox getHuntingArea();
}
