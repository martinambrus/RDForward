package org.bukkit.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface BrewerInventory extends org.bukkit.inventory.Inventory {
    org.bukkit.inventory.ItemStack getIngredient();
    void setIngredient(org.bukkit.inventory.ItemStack arg0);
    org.bukkit.inventory.ItemStack getFuel();
    void setFuel(org.bukkit.inventory.ItemStack arg0);
    org.bukkit.block.BrewingStand getHolder();
}
