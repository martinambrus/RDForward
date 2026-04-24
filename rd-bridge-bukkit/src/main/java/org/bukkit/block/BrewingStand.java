package org.bukkit.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface BrewingStand extends org.bukkit.block.Container {
    int getBrewingTime();
    void setBrewingTime(int arg0);
    void setRecipeBrewTime(int arg0);
    int getRecipeBrewTime();
    int getFuelLevel();
    void setFuelLevel(int arg0);
    org.bukkit.inventory.BrewerInventory getInventory();
    org.bukkit.inventory.BrewerInventory getSnapshotInventory();
}
