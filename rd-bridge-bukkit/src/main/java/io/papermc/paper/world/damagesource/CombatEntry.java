package io.papermc.paper.world.damagesource;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface CombatEntry {
    org.bukkit.damage.DamageSource getDamageSource();
    float getDamage();
    io.papermc.paper.world.damagesource.FallLocationType getFallLocationType();
    float getFallDistance();
    static io.papermc.paper.world.damagesource.CombatEntry combatEntry(org.bukkit.entity.LivingEntity arg0, org.bukkit.damage.DamageSource arg1, float arg2) {
        return null;
    }
    static io.papermc.paper.world.damagesource.CombatEntry combatEntry(org.bukkit.damage.DamageSource arg0, float arg1, io.papermc.paper.world.damagesource.FallLocationType arg2, float arg3) {
        return null;
    }
}
