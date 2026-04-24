package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class ExplosionPrimeEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public ExplosionPrimeEvent(org.bukkit.entity.Entity arg0, float arg1, boolean arg2) { super((org.bukkit.entity.Entity) null); }
    public ExplosionPrimeEvent(org.bukkit.entity.Explosive arg0) { super((org.bukkit.entity.Entity) null); }
    public ExplosionPrimeEvent() { super((org.bukkit.entity.Entity) null); }
    public float getRadius() {
        return 0.0f;
    }
    public void setRadius(float arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.ExplosionPrimeEvent.setRadius(F)V");
    }
    public boolean getFire() {
        return false;
    }
    public void setFire(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.ExplosionPrimeEvent.setFire(Z)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.ExplosionPrimeEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
