package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface EnderDragon extends org.bukkit.entity.ComplexLivingEntity, org.bukkit.entity.Boss, org.bukkit.entity.Mob, org.bukkit.entity.Enemy {
    org.bukkit.entity.EnderDragon$Phase getPhase();
    void setPhase(org.bukkit.entity.EnderDragon$Phase arg0);
    org.bukkit.boss.DragonBattle getDragonBattle();
    int getDeathAnimationTicks();
    org.bukkit.Location getPodium();
    void setPodium(org.bukkit.Location arg0);
}
