package com.destroystokyo.paper.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class ProjectileCollideEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public ProjectileCollideEvent(org.bukkit.entity.Projectile arg0, org.bukkit.entity.Entity arg1) { super((org.bukkit.entity.Entity) null); }
    public ProjectileCollideEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.Projectile getEntity() {
        return null;
    }
    public org.bukkit.entity.Entity getCollidedWith() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.entity.ProjectileCollideEvent.setCancelled(Z)V");
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
}
