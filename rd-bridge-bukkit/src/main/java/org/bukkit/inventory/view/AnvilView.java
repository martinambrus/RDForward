package org.bukkit.inventory.view;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface AnvilView extends org.bukkit.inventory.InventoryView {
    org.bukkit.inventory.AnvilInventory getTopInventory();
    java.lang.String getRenameText();
    int getRepairItemCountCost();
    int getRepairCost();
    int getMaximumRepairCost();
    void setRepairItemCountCost(int arg0);
    void setRepairCost(int arg0);
    void setMaximumRepairCost(int arg0);
    boolean bypassesEnchantmentLevelRestriction();
    void bypassEnchantmentLevelRestriction(boolean arg0);
}
