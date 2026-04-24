package org.bukkit.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Furnace extends org.bukkit.block.Container {
    short getBurnTime();
    void setBurnTime(short arg0);
    short getCookTime();
    void setCookTime(short arg0);
    int getCookTimeTotal();
    void setCookTimeTotal(int arg0);
    java.util.Map getRecipesUsed();
    double getCookSpeedMultiplier();
    void setCookSpeedMultiplier(double arg0);
    int getRecipeUsedCount(org.bukkit.NamespacedKey arg0);
    boolean hasRecipeUsedCount(org.bukkit.NamespacedKey arg0);
    void setRecipeUsedCount(org.bukkit.inventory.CookingRecipe arg0, int arg1);
    void setRecipesUsed(java.util.Map arg0);
    org.bukkit.inventory.FurnaceInventory getInventory();
    org.bukkit.inventory.FurnaceInventory getSnapshotInventory();
}
