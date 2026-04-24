package org.bukkit.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface FurnaceInventory extends org.bukkit.inventory.Inventory {
    org.bukkit.inventory.ItemStack getResult();
    org.bukkit.inventory.ItemStack getFuel();
    org.bukkit.inventory.ItemStack getSmelting();
    void setFuel(org.bukkit.inventory.ItemStack arg0);
    void setResult(org.bukkit.inventory.ItemStack arg0);
    void setSmelting(org.bukkit.inventory.ItemStack arg0);
    boolean isFuel(org.bukkit.inventory.ItemStack arg0);
    boolean canSmelt(org.bukkit.inventory.ItemStack arg0);
    org.bukkit.block.Furnace getHolder();
}
