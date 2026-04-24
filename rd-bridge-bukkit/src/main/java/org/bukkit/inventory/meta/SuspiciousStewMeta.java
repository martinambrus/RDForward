package org.bukkit.inventory.meta;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface SuspiciousStewMeta extends org.bukkit.inventory.meta.ItemMeta {
    boolean hasCustomEffects();
    java.util.List getCustomEffects();
    boolean addCustomEffect(org.bukkit.potion.PotionEffect arg0, boolean arg1);
    boolean addCustomEffect(io.papermc.paper.potion.SuspiciousEffectEntry arg0, boolean arg1);
    boolean removeCustomEffect(org.bukkit.potion.PotionEffectType arg0);
    boolean hasCustomEffect(org.bukkit.potion.PotionEffectType arg0);
    boolean clearCustomEffects();
    org.bukkit.inventory.meta.SuspiciousStewMeta clone();
}
