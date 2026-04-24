package org.bukkit.event.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class InventoryMoveItemEvent extends org.bukkit.event.Event implements org.bukkit.event.Cancellable {
    public InventoryMoveItemEvent(org.bukkit.inventory.Inventory arg0, org.bukkit.inventory.ItemStack arg1, org.bukkit.inventory.Inventory arg2, boolean arg3) {}
    public InventoryMoveItemEvent() {}
    public org.bukkit.inventory.Inventory getSource() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getItem() {
        return null;
    }
    public void setItem(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.inventory.InventoryMoveItemEvent.setItem(Lorg/bukkit/inventory/ItemStack;)V");
    }
    public org.bukkit.inventory.Inventory getDestination() {
        return null;
    }
    public org.bukkit.inventory.Inventory getInitiator() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.inventory.InventoryMoveItemEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
