package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityUnleashEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public EntityUnleashEvent(org.bukkit.entity.Entity arg0, org.bukkit.event.entity.EntityUnleashEvent$UnleashReason arg1) { super((org.bukkit.entity.Entity) null); }
    public EntityUnleashEvent(org.bukkit.entity.Entity arg0, org.bukkit.event.entity.EntityUnleashEvent$UnleashReason arg1, boolean arg2) { super((org.bukkit.entity.Entity) null); }
    public EntityUnleashEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.event.entity.EntityUnleashEvent$UnleashReason getReason() {
        return null;
    }
    public boolean isDropLeash() {
        return false;
    }
    public void setDropLeash(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityUnleashEvent.setDropLeash(Z)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityUnleashEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
