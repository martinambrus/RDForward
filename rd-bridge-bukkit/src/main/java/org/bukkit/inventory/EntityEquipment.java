package org.bukkit.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface EntityEquipment {
    void setItem(org.bukkit.inventory.EquipmentSlot arg0, org.bukkit.inventory.ItemStack arg1);
    void setItem(org.bukkit.inventory.EquipmentSlot arg0, org.bukkit.inventory.ItemStack arg1, boolean arg2);
    org.bukkit.inventory.ItemStack getItem(org.bukkit.inventory.EquipmentSlot arg0);
    org.bukkit.inventory.ItemStack getItemInMainHand();
    void setItemInMainHand(org.bukkit.inventory.ItemStack arg0);
    void setItemInMainHand(org.bukkit.inventory.ItemStack arg0, boolean arg1);
    org.bukkit.inventory.ItemStack getItemInOffHand();
    void setItemInOffHand(org.bukkit.inventory.ItemStack arg0);
    void setItemInOffHand(org.bukkit.inventory.ItemStack arg0, boolean arg1);
    org.bukkit.inventory.ItemStack getItemInHand();
    void setItemInHand(org.bukkit.inventory.ItemStack arg0);
    org.bukkit.inventory.ItemStack getHelmet();
    void setHelmet(org.bukkit.inventory.ItemStack arg0);
    void setHelmet(org.bukkit.inventory.ItemStack arg0, boolean arg1);
    org.bukkit.inventory.ItemStack getChestplate();
    void setChestplate(org.bukkit.inventory.ItemStack arg0);
    void setChestplate(org.bukkit.inventory.ItemStack arg0, boolean arg1);
    org.bukkit.inventory.ItemStack getLeggings();
    void setLeggings(org.bukkit.inventory.ItemStack arg0);
    void setLeggings(org.bukkit.inventory.ItemStack arg0, boolean arg1);
    org.bukkit.inventory.ItemStack getBoots();
    void setBoots(org.bukkit.inventory.ItemStack arg0);
    void setBoots(org.bukkit.inventory.ItemStack arg0, boolean arg1);
    org.bukkit.inventory.ItemStack[] getArmorContents();
    void setArmorContents(org.bukkit.inventory.ItemStack[] arg0);
    void clear();
    float getItemInHandDropChance();
    void setItemInHandDropChance(float arg0);
    float getItemInMainHandDropChance();
    void setItemInMainHandDropChance(float arg0);
    float getItemInOffHandDropChance();
    void setItemInOffHandDropChance(float arg0);
    float getHelmetDropChance();
    void setHelmetDropChance(float arg0);
    float getChestplateDropChance();
    void setChestplateDropChance(float arg0);
    float getLeggingsDropChance();
    void setLeggingsDropChance(float arg0);
    float getBootsDropChance();
    void setBootsDropChance(float arg0);
    org.bukkit.entity.Entity getHolder();
    float getDropChance(org.bukkit.inventory.EquipmentSlot arg0);
    void setDropChance(org.bukkit.inventory.EquipmentSlot arg0, float arg1);
}
