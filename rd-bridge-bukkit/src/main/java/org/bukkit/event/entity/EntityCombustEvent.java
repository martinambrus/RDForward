package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityCombustEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public EntityCombustEvent(org.bukkit.entity.Entity arg0, int arg1) { super((org.bukkit.entity.Entity) null); }
    public EntityCombustEvent(org.bukkit.entity.Entity arg0, float arg1) { super((org.bukkit.entity.Entity) null); }
    public EntityCombustEvent() { super((org.bukkit.entity.Entity) null); }
    public float getDuration() {
        return 0.0f;
    }
    public void setDuration(float arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityCombustEvent.setDuration(F)V");
    }
    public void setDuration(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityCombustEvent.setDuration(I)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityCombustEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
