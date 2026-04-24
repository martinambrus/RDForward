package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ThrownPotion extends org.bukkit.entity.ThrowableProjectile {
    java.util.Collection getEffects();
    org.bukkit.inventory.ItemStack getItem();
    void setItem(org.bukkit.inventory.ItemStack arg0);
    org.bukkit.inventory.meta.PotionMeta getPotionMeta();
    void setPotionMeta(org.bukkit.inventory.meta.PotionMeta arg0);
    void splash();
}
