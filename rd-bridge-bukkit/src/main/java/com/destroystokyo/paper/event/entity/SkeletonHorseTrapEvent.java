package com.destroystokyo.paper.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class SkeletonHorseTrapEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public SkeletonHorseTrapEvent(org.bukkit.entity.SkeletonHorse arg0, java.util.List arg1) { super((org.bukkit.entity.Entity) null); }
    public SkeletonHorseTrapEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.SkeletonHorse getEntity() {
        return null;
    }
    public java.util.List getEligibleHumans() {
        return java.util.Collections.emptyList();
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.entity.SkeletonHorseTrapEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
