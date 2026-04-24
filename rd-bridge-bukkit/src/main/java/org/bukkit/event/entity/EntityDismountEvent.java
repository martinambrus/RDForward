package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityDismountEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public EntityDismountEvent(org.bukkit.entity.Entity arg0, org.bukkit.entity.Entity arg1) { super((org.bukkit.entity.Entity) null); }
    public EntityDismountEvent(org.bukkit.entity.Entity arg0, org.bukkit.entity.Entity arg1, boolean arg2) { super((org.bukkit.entity.Entity) null); }
    public EntityDismountEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.Entity getDismounted() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityDismountEvent.setCancelled(Z)V");
    }
    public boolean isCancellable() {
        return false;
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
