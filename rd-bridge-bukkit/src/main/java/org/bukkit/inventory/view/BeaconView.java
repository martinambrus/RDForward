package org.bukkit.inventory.view;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface BeaconView extends org.bukkit.inventory.InventoryView {
    org.bukkit.inventory.BeaconInventory getTopInventory();
    int getTier();
    org.bukkit.potion.PotionEffectType getPrimaryEffect();
    org.bukkit.potion.PotionEffectType getSecondaryEffect();
    void setPrimaryEffect(org.bukkit.potion.PotionEffectType arg0);
    void setSecondaryEffect(org.bukkit.potion.PotionEffectType arg0);
}
