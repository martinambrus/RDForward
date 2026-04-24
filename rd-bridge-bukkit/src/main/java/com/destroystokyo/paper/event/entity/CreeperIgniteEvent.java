package com.destroystokyo.paper.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class CreeperIgniteEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public CreeperIgniteEvent(org.bukkit.entity.Creeper arg0, boolean arg1) { super((org.bukkit.entity.Entity) null); }
    public CreeperIgniteEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.Creeper getEntity() {
        return null;
    }
    public boolean isIgnited() {
        return false;
    }
    public void setIgnited(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.entity.CreeperIgniteEvent.setIgnited(Z)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.entity.CreeperIgniteEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
