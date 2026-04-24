package org.bukkit.inventory.view;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface EnchantmentView extends org.bukkit.inventory.InventoryView {
    org.bukkit.inventory.EnchantingInventory getTopInventory();
    int getEnchantmentSeed();
    void setEnchantmentSeed(int arg0);
    org.bukkit.enchantments.EnchantmentOffer[] getOffers();
    void setOffers(org.bukkit.enchantments.EnchantmentOffer[] arg0) throws java.lang.IllegalArgumentException;
}
