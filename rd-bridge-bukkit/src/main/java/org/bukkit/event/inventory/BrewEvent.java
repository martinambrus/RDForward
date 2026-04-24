package org.bukkit.event.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class BrewEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    public BrewEvent(org.bukkit.block.Block arg0, org.bukkit.inventory.BrewerInventory arg1, java.util.List arg2, int arg3) { super((org.bukkit.block.Block) null); }
    public BrewEvent() { super((org.bukkit.block.Block) null); }
    public org.bukkit.inventory.BrewerInventory getContents() {
        return null;
    }
    public java.util.List getResults() {
        return java.util.Collections.emptyList();
    }
    public int getFuelLevel() {
        return 0;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.inventory.BrewEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
