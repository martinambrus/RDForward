package org.bukkit.inventory.view;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface FurnaceView extends org.bukkit.inventory.InventoryView {
    org.bukkit.inventory.FurnaceInventory getTopInventory();
    float getCookTime();
    float getBurnTime();
    boolean isBurning();
    void setCookTime(int arg0, int arg1);
    void setBurnTime(int arg0, int arg1);
}
