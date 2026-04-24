package com.destroystokyo.paper.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityZapEvent extends org.bukkit.event.entity.EntityTransformEvent implements org.bukkit.event.Cancellable {
    public EntityZapEvent(org.bukkit.entity.Entity arg0, org.bukkit.entity.LightningStrike arg1, org.bukkit.entity.Entity arg2) { super((org.bukkit.entity.Entity) null, java.util.Collections.emptyList(), (org.bukkit.event.entity.EntityTransformEvent$TransformReason) null); }
    public EntityZapEvent() { super((org.bukkit.entity.Entity) null, java.util.Collections.emptyList(), (org.bukkit.event.entity.EntityTransformEvent$TransformReason) null); }
    public org.bukkit.entity.LightningStrike getBolt() {
        return null;
    }
    public org.bukkit.entity.Entity getReplacementEntity() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.entity.EntityZapEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
