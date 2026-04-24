package org.bukkit.inventory.meta;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface EnchantmentStorageMeta extends org.bukkit.inventory.meta.ItemMeta {
    boolean hasStoredEnchants();
    boolean hasStoredEnchant(org.bukkit.enchantments.Enchantment arg0);
    int getStoredEnchantLevel(org.bukkit.enchantments.Enchantment arg0);
    java.util.Map getStoredEnchants();
    boolean addStoredEnchant(org.bukkit.enchantments.Enchantment arg0, int arg1, boolean arg2);
    boolean removeStoredEnchant(org.bukkit.enchantments.Enchantment arg0) throws java.lang.IllegalArgumentException;
    boolean hasConflictingStoredEnchant(org.bukkit.enchantments.Enchantment arg0);
    org.bukkit.inventory.meta.EnchantmentStorageMeta clone();
}
