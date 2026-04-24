package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityExplodeEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public EntityExplodeEvent(org.bukkit.entity.Entity arg0, org.bukkit.Location arg1, java.util.List arg2, float arg3, org.bukkit.ExplosionResult arg4) { super((org.bukkit.entity.Entity) null); }
    public EntityExplodeEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.ExplosionResult getExplosionResult() {
        return null;
    }
    public java.util.List blockList() {
        return java.util.Collections.emptyList();
    }
    public org.bukkit.Location getLocation() {
        return null;
    }
    public float getYield() {
        return 0.0f;
    }
    public void setYield(float arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityExplodeEvent.setYield(F)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityExplodeEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
