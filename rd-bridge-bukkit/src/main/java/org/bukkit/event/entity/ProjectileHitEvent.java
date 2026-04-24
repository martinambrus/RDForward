package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class ProjectileHitEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public ProjectileHitEvent(org.bukkit.entity.Projectile arg0) { super((org.bukkit.entity.Entity) null); }
    public ProjectileHitEvent(org.bukkit.entity.Projectile arg0, org.bukkit.entity.Entity arg1) { super((org.bukkit.entity.Entity) null); }
    public ProjectileHitEvent(org.bukkit.entity.Projectile arg0, org.bukkit.block.Block arg1) { super((org.bukkit.entity.Entity) null); }
    public ProjectileHitEvent(org.bukkit.entity.Projectile arg0, org.bukkit.entity.Entity arg1, org.bukkit.block.Block arg2) { super((org.bukkit.entity.Entity) null); }
    public ProjectileHitEvent(org.bukkit.entity.Projectile arg0, org.bukkit.entity.Entity arg1, org.bukkit.block.Block arg2, org.bukkit.block.BlockFace arg3) { super((org.bukkit.entity.Entity) null); }
    public ProjectileHitEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.Projectile getEntity() {
        return null;
    }
    public org.bukkit.entity.Entity getHitEntity() {
        return null;
    }
    public org.bukkit.block.Block getHitBlock() {
        return null;
    }
    public org.bukkit.block.BlockFace getHitBlockFace() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.ProjectileHitEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
