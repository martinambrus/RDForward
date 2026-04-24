package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Chicken extends org.bukkit.entity.Animals {
    org.bukkit.entity.Chicken$Variant getVariant();
    void setVariant(org.bukkit.entity.Chicken$Variant arg0);
    org.bukkit.entity.Chicken$SoundVariant getSoundVariant();
    void setSoundVariant(org.bukkit.entity.Chicken$SoundVariant arg0);
    boolean isChickenJockey();
    void setIsChickenJockey(boolean arg0);
    int getEggLayTime();
    void setEggLayTime(int arg0);
}
