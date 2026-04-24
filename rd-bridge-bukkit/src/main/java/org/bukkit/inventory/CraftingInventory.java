package org.bukkit.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface CraftingInventory extends org.bukkit.inventory.Inventory {
    org.bukkit.inventory.ItemStack getResult();
    org.bukkit.inventory.ItemStack[] getMatrix();
    void setResult(org.bukkit.inventory.ItemStack arg0);
    void setMatrix(org.bukkit.inventory.ItemStack[] arg0);
    org.bukkit.inventory.Recipe getRecipe();
}
