package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityDamageEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public EntityDamageEvent(org.bukkit.entity.Entity arg0, org.bukkit.event.entity.EntityDamageEvent$DamageCause arg1, double arg2) { super((org.bukkit.entity.Entity) null); }
    public EntityDamageEvent(org.bukkit.entity.Entity arg0, org.bukkit.event.entity.EntityDamageEvent$DamageCause arg1, org.bukkit.damage.DamageSource arg2, double arg3) { super((org.bukkit.entity.Entity) null); }
    public EntityDamageEvent(org.bukkit.entity.Entity arg0, org.bukkit.event.entity.EntityDamageEvent$DamageCause arg1, java.util.Map arg2, java.util.Map arg3) { super((org.bukkit.entity.Entity) null); }
    public EntityDamageEvent(org.bukkit.entity.Entity arg0, org.bukkit.event.entity.EntityDamageEvent$DamageCause arg1, org.bukkit.damage.DamageSource arg2, java.util.Map arg3, java.util.Map arg4) { super((org.bukkit.entity.Entity) null); }
    public EntityDamageEvent() { super((org.bukkit.entity.Entity) null); }
    public double getOriginalDamage(org.bukkit.event.entity.EntityDamageEvent$DamageModifier arg0) throws java.lang.IllegalArgumentException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityDamageEvent.getOriginalDamage(Lorg/bukkit/event/entity/EntityDamageEvent$DamageModifier;)D");
        return 0.0;
    }
    public void setDamage(org.bukkit.event.entity.EntityDamageEvent$DamageModifier arg0, double arg1) throws java.lang.IllegalArgumentException, java.lang.UnsupportedOperationException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityDamageEvent.setDamage(Lorg/bukkit/event/entity/EntityDamageEvent$DamageModifier;D)V");
    }
    public double getDamage(org.bukkit.event.entity.EntityDamageEvent$DamageModifier arg0) throws java.lang.IllegalArgumentException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityDamageEvent.getDamage(Lorg/bukkit/event/entity/EntityDamageEvent$DamageModifier;)D");
        return 0.0;
    }
    public boolean isApplicable(org.bukkit.event.entity.EntityDamageEvent$DamageModifier arg0) throws java.lang.IllegalArgumentException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityDamageEvent.isApplicable(Lorg/bukkit/event/entity/EntityDamageEvent$DamageModifier;)Z");
        return false;
    }
    public double getDamage() {
        return 0.0;
    }
    public final double getFinalDamage() {
        return 0.0;
    }
    public void setDamage(double arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityDamageEvent.setDamage(D)V");
    }
    public org.bukkit.event.entity.EntityDamageEvent$DamageCause getCause() {
        return null;
    }
    public org.bukkit.damage.DamageSource getDamageSource() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.EntityDamageEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
