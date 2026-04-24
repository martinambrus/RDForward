package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityPortalEnterEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public EntityPortalEnterEvent(org.bukkit.entity.Entity arg0, org.bukkit.Location arg1) { super((org.bukkit.entity.Entity) null); }
    public EntityPortalEnterEvent(org.bukkit.entity.Entity arg0, org.bukkit.Location arg1, org.bukkit.PortalType arg2) { super((org.bukkit.entity.Entity) null); }
    public EntityPortalEnterEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.Location getLocation() {
        return null;
    }
    public org.bukkit.PortalType getPortalType() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityPortalEnterEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
