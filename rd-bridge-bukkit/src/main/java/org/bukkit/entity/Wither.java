package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Wither extends org.bukkit.entity.Monster, org.bukkit.entity.Boss, com.destroystokyo.paper.entity.RangedEntity {
    void setTarget(org.bukkit.entity.LivingEntity arg0);
    void setTarget(org.bukkit.entity.Wither$Head arg0, org.bukkit.entity.LivingEntity arg1);
    org.bukkit.entity.LivingEntity getTarget(org.bukkit.entity.Wither$Head arg0);
    int getInvulnerabilityTicks();
    void setInvulnerabilityTicks(int arg0);
    boolean isCharged();
    int getInvulnerableTicks();
    void setInvulnerableTicks(int arg0);
    boolean canTravelThroughPortals();
    void setCanTravelThroughPortals(boolean arg0);
    void enterInvulnerabilityPhase();
}
