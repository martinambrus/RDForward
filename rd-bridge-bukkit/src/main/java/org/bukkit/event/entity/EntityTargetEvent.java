package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityTargetEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public EntityTargetEvent(org.bukkit.entity.Entity arg0, org.bukkit.entity.Entity arg1, org.bukkit.event.entity.EntityTargetEvent$TargetReason arg2) { super((org.bukkit.entity.Entity) null); }
    public EntityTargetEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.event.entity.EntityTargetEvent$TargetReason getReason() {
        return null;
    }
    public org.bukkit.entity.Entity getTarget() {
        return null;
    }
    public void setTarget(org.bukkit.entity.Entity arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityTargetEvent.setTarget(Lorg/bukkit/entity/Entity;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityTargetEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
