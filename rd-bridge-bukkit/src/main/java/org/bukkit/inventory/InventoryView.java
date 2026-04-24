package org.bukkit.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface InventoryView {
    public static final int OUTSIDE = -999;
    org.bukkit.inventory.Inventory getTopInventory();
    org.bukkit.inventory.Inventory getBottomInventory();
    org.bukkit.entity.HumanEntity getPlayer();
    org.bukkit.event.inventory.InventoryType getType();
    void setItem(int arg0, org.bukkit.inventory.ItemStack arg1);
    org.bukkit.inventory.ItemStack getItem(int arg0);
    void setCursor(org.bukkit.inventory.ItemStack arg0);
    org.bukkit.inventory.ItemStack getCursor();
    org.bukkit.inventory.Inventory getInventory(int arg0);
    int convertSlot(int arg0);
    org.bukkit.event.inventory.InventoryType$SlotType getSlotType(int arg0);
    void open();
    void close();
    int countSlots();
    boolean setProperty(org.bukkit.inventory.InventoryView$Property arg0, int arg1);
    default net.kyori.adventure.text.Component title() {
        return null;
    }
    java.lang.String getTitle();
    java.lang.String getOriginalTitle();
    void setTitle(java.lang.String arg0);
    org.bukkit.inventory.MenuType getMenuType();
}
