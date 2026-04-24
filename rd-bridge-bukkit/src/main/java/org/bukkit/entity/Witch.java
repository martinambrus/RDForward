package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Witch extends org.bukkit.entity.Raider, com.destroystokyo.paper.entity.RangedEntity {
    boolean isDrinkingPotion();
    int getPotionUseTimeLeft();
    void setPotionUseTimeLeft(int arg0);
    org.bukkit.inventory.ItemStack getDrinkingPotion();
    void setDrinkingPotion(org.bukkit.inventory.ItemStack arg0);
}
