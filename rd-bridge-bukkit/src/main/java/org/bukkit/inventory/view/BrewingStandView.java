package org.bukkit.inventory.view;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface BrewingStandView extends org.bukkit.inventory.InventoryView {
    org.bukkit.inventory.BrewerInventory getTopInventory();
    int getFuelLevel();
    int getBrewingTicks();
    void setFuelLevel(int arg0) throws java.lang.IllegalArgumentException;
    void setBrewingTicks(int arg0) throws java.lang.IllegalArgumentException;
    void setRecipeBrewTime(int arg0);
    int getRecipeBrewTime();
}
