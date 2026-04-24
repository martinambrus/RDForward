package org.bukkit.event.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class InventoryDragEvent extends org.bukkit.event.inventory.InventoryInteractEvent {
    public InventoryDragEvent(org.bukkit.inventory.InventoryView arg0, org.bukkit.inventory.ItemStack arg1, org.bukkit.inventory.ItemStack arg2, boolean arg3, java.util.Map arg4) { super((org.bukkit.inventory.InventoryView) null); }
    public InventoryDragEvent() { super((org.bukkit.inventory.InventoryView) null); }
    public org.bukkit.event.inventory.DragType getType() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getCursor() {
        return null;
    }
    public void setCursor(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.inventory.InventoryDragEvent.setCursor(Lorg/bukkit/inventory/ItemStack;)V");
    }
    public org.bukkit.inventory.ItemStack getOldCursor() {
        return null;
    }
    public java.util.Map getNewItems() {
        return java.util.Collections.emptyMap();
    }
    public java.util.Set getRawSlots() {
        return java.util.Collections.emptySet();
    }
    public java.util.Set getInventorySlots() {
        return java.util.Collections.emptySet();
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
