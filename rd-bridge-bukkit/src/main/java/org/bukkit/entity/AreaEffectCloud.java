package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface AreaEffectCloud extends org.bukkit.entity.Entity {
    int getDuration();
    void setDuration(int arg0);
    int getWaitTime();
    void setWaitTime(int arg0);
    int getReapplicationDelay();
    void setReapplicationDelay(int arg0);
    int getDurationOnUse();
    void setDurationOnUse(int arg0);
    float getRadius();
    void setRadius(float arg0);
    float getRadiusOnUse();
    void setRadiusOnUse(float arg0);
    float getRadiusPerTick();
    void setRadiusPerTick(float arg0);
    org.bukkit.Particle getParticle();
    void setParticle(org.bukkit.Particle arg0);
    void setParticle(org.bukkit.Particle arg0, java.lang.Object arg1);
    void setBasePotionData(org.bukkit.potion.PotionData arg0);
    org.bukkit.potion.PotionData getBasePotionData();
    void setBasePotionType(org.bukkit.potion.PotionType arg0);
    org.bukkit.potion.PotionType getBasePotionType();
    boolean hasCustomEffects();
    java.util.List getCustomEffects();
    boolean addCustomEffect(org.bukkit.potion.PotionEffect arg0, boolean arg1);
    boolean removeCustomEffect(org.bukkit.potion.PotionEffectType arg0);
    boolean hasCustomEffect(org.bukkit.potion.PotionEffectType arg0);
    void clearCustomEffects();
    org.bukkit.Color getColor();
    void setColor(org.bukkit.Color arg0);
    org.bukkit.projectiles.ProjectileSource getSource();
    void setSource(org.bukkit.projectiles.ProjectileSource arg0);
    java.util.UUID getOwnerUniqueId();
    void setOwnerUniqueId(java.util.UUID arg0);
}
