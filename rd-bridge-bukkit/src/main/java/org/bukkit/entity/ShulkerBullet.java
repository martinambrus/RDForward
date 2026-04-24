package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ShulkerBullet extends org.bukkit.entity.Projectile {
    org.bukkit.entity.Entity getTarget();
    void setTarget(org.bukkit.entity.Entity arg0);
    org.bukkit.util.Vector getTargetDelta();
    void setTargetDelta(org.bukkit.util.Vector arg0);
    org.bukkit.block.BlockFace getCurrentMovementDirection();
    void setCurrentMovementDirection(org.bukkit.block.BlockFace arg0);
    int getFlightSteps();
    void setFlightSteps(int arg0);
}
