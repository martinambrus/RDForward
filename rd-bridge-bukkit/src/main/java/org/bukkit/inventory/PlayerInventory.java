package org.bukkit.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PlayerInventory extends org.bukkit.inventory.Inventory {
    org.bukkit.inventory.ItemStack[] getArmorContents();
    org.bukkit.inventory.ItemStack[] getExtraContents();
    org.bukkit.inventory.ItemStack getHelmet();
    org.bukkit.inventory.ItemStack getChestplate();
    org.bukkit.inventory.ItemStack getLeggings();
    org.bukkit.inventory.ItemStack getBoots();
    void setItem(int arg0, org.bukkit.inventory.ItemStack arg1);
    void setItem(org.bukkit.inventory.EquipmentSlot arg0, org.bukkit.inventory.ItemStack arg1);
    org.bukkit.inventory.ItemStack getItem(org.bukkit.inventory.EquipmentSlot arg0);
    void setArmorContents(org.bukkit.inventory.ItemStack[] arg0);
    void setExtraContents(org.bukkit.inventory.ItemStack[] arg0);
    void setHelmet(org.bukkit.inventory.ItemStack arg0);
    void setChestplate(org.bukkit.inventory.ItemStack arg0);
    void setLeggings(org.bukkit.inventory.ItemStack arg0);
    void setBoots(org.bukkit.inventory.ItemStack arg0);
    org.bukkit.inventory.ItemStack getItemInMainHand();
    void setItemInMainHand(org.bukkit.inventory.ItemStack arg0);
    org.bukkit.inventory.ItemStack getItemInOffHand();
    void setItemInOffHand(org.bukkit.inventory.ItemStack arg0);
    org.bukkit.inventory.ItemStack getItemInHand();
    void setItemInHand(org.bukkit.inventory.ItemStack arg0);
    int getHeldItemSlot();
    void setHeldItemSlot(int arg0);
    org.bukkit.entity.HumanEntity getHolder();
}
