package org.bukkit.event.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class InventoryInteractEvent extends org.bukkit.event.inventory.InventoryEvent implements org.bukkit.event.Cancellable {
    public InventoryInteractEvent(org.bukkit.inventory.InventoryView arg0) { super((org.bukkit.inventory.InventoryView) null); }
    protected InventoryInteractEvent() { super((org.bukkit.inventory.InventoryView) null); }
    public org.bukkit.entity.HumanEntity getWhoClicked() {
        return null;
    }
    public void setResult(org.bukkit.event.Event$Result arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.inventory.InventoryInteractEvent.setResult(Lorg/bukkit/event/Event$Result;)V");
    }
    public org.bukkit.event.Event$Result getResult() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.inventory.InventoryInteractEvent.setCancelled(Z)V");
    }
}
