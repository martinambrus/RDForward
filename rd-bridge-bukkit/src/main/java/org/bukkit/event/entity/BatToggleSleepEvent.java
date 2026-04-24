package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class BatToggleSleepEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public BatToggleSleepEvent(org.bukkit.entity.Bat arg0, boolean arg1) { super((org.bukkit.entity.Entity) null); }
    public BatToggleSleepEvent() { super((org.bukkit.entity.Entity) null); }
    public boolean isAwake() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.BatToggleSleepEvent.setCancelled(Z)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
