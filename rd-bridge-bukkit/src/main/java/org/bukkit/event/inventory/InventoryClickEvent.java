package org.bukkit.event.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class InventoryClickEvent extends org.bukkit.event.inventory.InventoryInteractEvent {
    public InventoryClickEvent(org.bukkit.inventory.InventoryView arg0, org.bukkit.event.inventory.InventoryType$SlotType arg1, int arg2, org.bukkit.event.inventory.ClickType arg3, org.bukkit.event.inventory.InventoryAction arg4) { super((org.bukkit.inventory.InventoryView) null); }
    public InventoryClickEvent(org.bukkit.inventory.InventoryView arg0, org.bukkit.event.inventory.InventoryType$SlotType arg1, int arg2, org.bukkit.event.inventory.ClickType arg3, org.bukkit.event.inventory.InventoryAction arg4, int arg5) { super((org.bukkit.inventory.InventoryView) null); }
    public InventoryClickEvent() { super((org.bukkit.inventory.InventoryView) null); }
    public org.bukkit.event.inventory.InventoryType$SlotType getSlotType() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getCursor() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getCurrentItem() {
        return null;
    }
    public boolean isRightClick() {
        return false;
    }
    public boolean isLeftClick() {
        return false;
    }
    public boolean isShiftClick() {
        return false;
    }
    public void setCursor(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.inventory.InventoryClickEvent.setCursor(Lorg/bukkit/inventory/ItemStack;)V");
    }
    public void setCurrentItem(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.inventory.InventoryClickEvent.setCurrentItem(Lorg/bukkit/inventory/ItemStack;)V");
    }
    public org.bukkit.inventory.Inventory getClickedInventory() {
        return null;
    }
    public int getSlot() {
        return 0;
    }
    public int getRawSlot() {
        return 0;
    }
    public int getHotbarButton() {
        return 0;
    }
    public org.bukkit.event.inventory.InventoryAction getAction() {
        return null;
    }
    public org.bukkit.event.inventory.ClickType getClick() {
        return null;
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
