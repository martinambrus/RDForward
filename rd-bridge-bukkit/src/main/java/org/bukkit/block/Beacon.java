package org.bukkit.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Beacon extends io.papermc.paper.block.LockableTileState {
    java.util.Collection getEntitiesInRange();
    int getTier();
    org.bukkit.potion.PotionEffect getPrimaryEffect();
    void setPrimaryEffect(org.bukkit.potion.PotionEffectType arg0);
    org.bukkit.potion.PotionEffect getSecondaryEffect();
    void setSecondaryEffect(org.bukkit.potion.PotionEffectType arg0);
    double getEffectRange();
    void setEffectRange(double arg0);
    void resetEffectRange();
}
