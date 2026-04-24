package com.destroystokyo.paper.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface RangedEntity extends org.bukkit.entity.Mob {
    void rangedAttack(org.bukkit.entity.LivingEntity arg0, float arg1);
    void setChargingAttack(boolean arg0);
    default boolean isChargingAttack() {
        return false;
    }
}
