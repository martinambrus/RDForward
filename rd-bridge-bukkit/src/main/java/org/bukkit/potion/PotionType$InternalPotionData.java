package org.bukkit.potion;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PotionType$InternalPotionData {
    org.bukkit.potion.PotionEffectType getEffectType();
    java.util.List getPotionEffects();
    boolean isInstant();
    boolean isUpgradeable();
    boolean isExtendable();
    int getMaxLevel();
}
