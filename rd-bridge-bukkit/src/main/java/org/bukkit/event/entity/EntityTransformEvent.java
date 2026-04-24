package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityTransformEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public EntityTransformEvent(org.bukkit.entity.Entity arg0, java.util.List arg1, org.bukkit.event.entity.EntityTransformEvent$TransformReason arg2) { super((org.bukkit.entity.Entity) null); }
    public EntityTransformEvent() { super((org.bukkit.entity.Entity) null); }
    public java.util.List getTransformedEntities() {
        return java.util.Collections.emptyList();
    }
    public org.bukkit.entity.Entity getTransformedEntity() {
        return null;
    }
    public org.bukkit.event.entity.EntityTransformEvent$TransformReason getTransformReason() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityTransformEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
