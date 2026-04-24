package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ItemFrame extends org.bukkit.entity.Hanging {
    org.bukkit.inventory.ItemStack getItem();
    void setItem(org.bukkit.inventory.ItemStack arg0);
    void setItem(org.bukkit.inventory.ItemStack arg0, boolean arg1);
    float getItemDropChance();
    void setItemDropChance(float arg0);
    org.bukkit.Rotation getRotation();
    void setRotation(org.bukkit.Rotation arg0) throws java.lang.IllegalArgumentException;
    boolean isVisible();
    void setVisible(boolean arg0);
    boolean isFixed();
    void setFixed(boolean arg0);
}
