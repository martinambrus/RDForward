package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Arrow extends org.bukkit.entity.AbstractArrow {
    void setBasePotionData(org.bukkit.potion.PotionData arg0);
    org.bukkit.potion.PotionData getBasePotionData();
    void setBasePotionType(org.bukkit.potion.PotionType arg0);
    org.bukkit.potion.PotionType getBasePotionType();
    org.bukkit.Color getColor();
    void setColor(org.bukkit.Color arg0);
    boolean hasCustomEffects();
    java.util.List getCustomEffects();
    boolean addCustomEffect(org.bukkit.potion.PotionEffect arg0, boolean arg1);
    boolean removeCustomEffect(org.bukkit.potion.PotionEffectType arg0);
    boolean hasCustomEffect(org.bukkit.potion.PotionEffectType arg0);
    void clearCustomEffects();
}
