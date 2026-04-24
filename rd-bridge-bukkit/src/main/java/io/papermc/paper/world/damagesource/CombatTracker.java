package io.papermc.paper.world.damagesource;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface CombatTracker {
    org.bukkit.entity.LivingEntity getEntity();
    java.util.List getCombatEntries();
    void setCombatEntries(java.util.List arg0);
    io.papermc.paper.world.damagesource.CombatEntry computeMostSignificantFall();
    boolean isInCombat();
    boolean isTakingDamage();
    int getCombatDuration();
    void addCombatEntry(io.papermc.paper.world.damagesource.CombatEntry arg0);
    net.kyori.adventure.text.Component getDeathMessage();
    void resetCombatState();
    io.papermc.paper.world.damagesource.FallLocationType calculateFallLocationType();
}
