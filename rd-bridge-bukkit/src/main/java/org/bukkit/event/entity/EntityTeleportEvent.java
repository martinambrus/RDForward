package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityTeleportEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public EntityTeleportEvent(org.bukkit.entity.Entity arg0, org.bukkit.Location arg1, org.bukkit.Location arg2) { super((org.bukkit.entity.Entity) null); }
    public EntityTeleportEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.Location getFrom() {
        return null;
    }
    public void setFrom(org.bukkit.Location arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityTeleportEvent.setFrom(Lorg/bukkit/Location;)V");
    }
    public org.bukkit.Location getTo() {
        return null;
    }
    public void setTo(org.bukkit.Location arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityTeleportEvent.setTo(Lorg/bukkit/Location;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityTeleportEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
