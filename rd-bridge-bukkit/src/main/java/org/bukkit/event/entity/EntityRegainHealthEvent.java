package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityRegainHealthEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public EntityRegainHealthEvent(org.bukkit.entity.Entity arg0, double arg1, org.bukkit.event.entity.EntityRegainHealthEvent$RegainReason arg2) { super((org.bukkit.entity.Entity) null); }
    public EntityRegainHealthEvent(org.bukkit.entity.Entity arg0, double arg1, org.bukkit.event.entity.EntityRegainHealthEvent$RegainReason arg2, boolean arg3) { super((org.bukkit.entity.Entity) null); }
    public EntityRegainHealthEvent() { super((org.bukkit.entity.Entity) null); }
    public double getAmount() {
        return 0.0;
    }
    public void setAmount(double arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityRegainHealthEvent.setAmount(D)V");
    }
    public org.bukkit.event.entity.EntityRegainHealthEvent$RegainReason getRegainReason() {
        return null;
    }
    public boolean isFastRegen() {
        return false;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityRegainHealthEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
