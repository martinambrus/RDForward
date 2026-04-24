package org.bukkit.event.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class InventoryOpenEvent extends org.bukkit.event.inventory.InventoryEvent implements org.bukkit.event.Cancellable {
    public InventoryOpenEvent(org.bukkit.inventory.InventoryView arg0) { super((org.bukkit.inventory.InventoryView) null); }
    public InventoryOpenEvent() { super((org.bukkit.inventory.InventoryView) null); }
    public final org.bukkit.entity.HumanEntity getPlayer() {
        return null;
    }
    public net.kyori.adventure.text.Component titleOverride() {
        return null;
    }
    public void titleOverride(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.inventory.InventoryOpenEvent.titleOverride(Lnet/kyori/adventure/text/Component;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.inventory.InventoryOpenEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
