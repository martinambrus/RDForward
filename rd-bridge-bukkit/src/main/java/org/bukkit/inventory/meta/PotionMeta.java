package org.bukkit.inventory.meta;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PotionMeta extends org.bukkit.inventory.meta.ItemMeta {
    void setBasePotionData(org.bukkit.potion.PotionData arg0);
    org.bukkit.potion.PotionData getBasePotionData();
    void setBasePotionType(org.bukkit.potion.PotionType arg0);
    org.bukkit.potion.PotionType getBasePotionType();
    boolean hasBasePotionType();
    boolean hasCustomEffects();
    java.util.List getCustomEffects();
    java.util.List getAllEffects();
    boolean addCustomEffect(org.bukkit.potion.PotionEffect arg0, boolean arg1);
    boolean removeCustomEffect(org.bukkit.potion.PotionEffectType arg0);
    boolean hasCustomEffect(org.bukkit.potion.PotionEffectType arg0);
    boolean setMainEffect(org.bukkit.potion.PotionEffectType arg0);
    boolean clearCustomEffects();
    boolean hasColor();
    org.bukkit.Color getColor();
    void setColor(org.bukkit.Color arg0);
    org.bukkit.Color computeEffectiveColor();
    default boolean hasCustomName() {
        return false;
    }
    default java.lang.String getCustomName() {
        return null;
    }
    default void setCustomName(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.meta.PotionMeta.setCustomName(Ljava/lang/String;)V");
    }
    boolean hasCustomPotionName();
    java.lang.String getCustomPotionName();
    void setCustomPotionName(java.lang.String arg0);
    org.bukkit.inventory.meta.PotionMeta clone();
}
