package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityExhaustionEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public EntityExhaustionEvent(org.bukkit.entity.HumanEntity arg0, org.bukkit.event.entity.EntityExhaustionEvent$ExhaustionReason arg1, float arg2) { super((org.bukkit.entity.Entity) null); }
    public EntityExhaustionEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.HumanEntity getEntity() {
        return null;
    }
    public org.bukkit.event.entity.EntityExhaustionEvent$ExhaustionReason getExhaustionReason() {
        return null;
    }
    public float getExhaustion() {
        return 0.0f;
    }
    public void setExhaustion(float arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityExhaustionEvent.setExhaustion(F)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityExhaustionEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
