package org.bukkit.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Campfire extends org.bukkit.block.TileState {
    int getSize();
    org.bukkit.inventory.ItemStack getItem(int arg0);
    void setItem(int arg0, org.bukkit.inventory.ItemStack arg1);
    int getCookTime(int arg0);
    void setCookTime(int arg0, int arg1);
    int getCookTimeTotal(int arg0);
    void setCookTimeTotal(int arg0, int arg1);
    void stopCooking();
    void startCooking();
    boolean stopCooking(int arg0);
    boolean startCooking(int arg0);
    boolean isCookingDisabled(int arg0);
}
