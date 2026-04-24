package org.bukkit.event.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class InventoryPickupItemEvent extends org.bukkit.event.Event implements org.bukkit.event.Cancellable {
    public InventoryPickupItemEvent(org.bukkit.inventory.Inventory arg0, org.bukkit.entity.Item arg1) {}
    public InventoryPickupItemEvent() {}
    public org.bukkit.inventory.Inventory getInventory() {
        return null;
    }
    public org.bukkit.entity.Item getItem() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.inventory.InventoryPickupItemEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
