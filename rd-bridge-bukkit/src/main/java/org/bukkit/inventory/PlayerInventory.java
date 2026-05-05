// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar plus
 *  hand-tuned legacy overloads. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PlayerInventory extends org.bukkit.inventory.Inventory {

    /** Pre-Bukkit-1.x legacy form. Jail 2.1's
     *  {@code JailZoneCreation.selectstart} calls
     *  {@code player.getInventory().contains(int)} with a raw material
     *  id read from its config (default 280 = stick). Resolves the id
     *  to a {@link org.bukkit.Material} via the legacy id table and
     *  delegates to {@link #contains(org.bukkit.Material)}. Returns
     *  {@code false} when the id is unknown so callers don't NPE. */
    default boolean contains(int materialId) {
        org.bukkit.Material mat = org.bukkit.Material.getMaterial(materialId);
        return mat != null && contains(mat);
    }
    default boolean contains(int materialId, int amount) {
        org.bukkit.Material mat = org.bukkit.Material.getMaterial(materialId);
        return mat != null && contains(mat, amount);
    }
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
