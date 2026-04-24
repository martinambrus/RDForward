package io.papermc.paper;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface InternalAPIBridge {
    static io.papermc.paper.InternalAPIBridge get() {
        return null;
    }
    org.bukkit.damage.DamageEffect getDamageEffect(java.lang.String arg0);
    org.bukkit.block.Biome constructLegacyCustomBiome();
    io.papermc.paper.world.damagesource.CombatEntry createCombatEntry(org.bukkit.entity.LivingEntity arg0, org.bukkit.damage.DamageSource arg1, float arg2);
    io.papermc.paper.world.damagesource.CombatEntry createCombatEntry(org.bukkit.damage.DamageSource arg0, float arg1, io.papermc.paper.world.damagesource.FallLocationType arg2, float arg3);
    java.util.function.Predicate restricted(java.util.function.Predicate arg0);
    io.papermc.paper.datacomponent.item.ResolvableProfile defaultMannequinProfile();
    com.destroystokyo.paper.SkinParts$Mutable allSkinParts();
    net.kyori.adventure.text.Component defaultMannequinDescription();
    org.bukkit.GameRule legacyGameRuleBridge(org.bukkit.GameRule arg0, java.util.function.Function arg1, java.util.function.Function arg2, java.lang.Class arg3);
    java.util.Set validMannequinPoses();
}
