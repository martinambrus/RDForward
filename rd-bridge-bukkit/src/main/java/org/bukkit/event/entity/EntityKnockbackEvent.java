package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityKnockbackEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public EntityKnockbackEvent(org.bukkit.entity.LivingEntity arg0, org.bukkit.event.entity.EntityKnockbackEvent$KnockbackCause arg1, double arg2, org.bukkit.util.Vector arg3, org.bukkit.util.Vector arg4) { super((org.bukkit.entity.Entity) null); }
    public EntityKnockbackEvent() { super((org.bukkit.entity.Entity) null); }
    public org.bukkit.entity.LivingEntity getEntity() {
        return null;
    }
    public org.bukkit.event.entity.EntityKnockbackEvent$KnockbackCause getCause() {
        return null;
    }
    public double getForce() {
        return 0.0;
    }
    public org.bukkit.util.Vector getKnockback() {
        return null;
    }
    public org.bukkit.util.Vector getFinalKnockback() {
        return null;
    }
    public void setFinalKnockback(org.bukkit.util.Vector arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityKnockbackEvent.setFinalKnockback(Lorg/bukkit/util/Vector;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityKnockbackEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
