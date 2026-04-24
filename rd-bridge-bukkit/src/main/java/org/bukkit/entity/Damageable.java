package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Damageable extends org.bukkit.entity.Entity {
    void damage(double arg0);
    void damage(double arg0, org.bukkit.entity.Entity arg1);
    void damage(double arg0, org.bukkit.damage.DamageSource arg1);
    double getHealth();
    void setHealth(double arg0);
    default void heal(double arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Damageable.heal(D)V");
    }
    void heal(double arg0, org.bukkit.event.entity.EntityRegainHealthEvent$RegainReason arg1);
    double getAbsorptionAmount();
    void setAbsorptionAmount(double arg0);
    double getMaxHealth();
    void setMaxHealth(double arg0);
    void resetMaxHealth();
}
