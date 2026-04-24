package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Projectile extends org.bukkit.entity.Entity {
    org.bukkit.projectiles.ProjectileSource getShooter();
    void setShooter(org.bukkit.projectiles.ProjectileSource arg0);
    boolean doesBounce();
    void setBounce(boolean arg0);
    boolean hasLeftShooter();
    void setHasLeftShooter(boolean arg0);
    boolean hasBeenShot();
    void setHasBeenShot(boolean arg0);
    boolean canHitEntity(org.bukkit.entity.Entity arg0);
    void hitEntity(org.bukkit.entity.Entity arg0);
    void hitEntity(org.bukkit.entity.Entity arg0, org.bukkit.util.Vector arg1);
    java.util.UUID getOwnerUniqueId();
}
