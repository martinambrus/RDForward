package org.bukkit.event.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class FurnaceBurnEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    public FurnaceBurnEvent(org.bukkit.block.Block arg0, org.bukkit.inventory.ItemStack arg1, int arg2) { super((org.bukkit.block.Block) null); }
    public FurnaceBurnEvent() { super((org.bukkit.block.Block) null); }
    public org.bukkit.inventory.ItemStack getFuel() {
        return null;
    }
    public int getBurnTime() {
        return 0;
    }
    public void setBurnTime(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.inventory.FurnaceBurnEvent.setBurnTime(I)V");
    }
    public boolean isBurning() {
        return false;
    }
    public void setBurning(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.inventory.FurnaceBurnEvent.setBurning(Z)V");
    }
    public boolean willConsumeFuel() {
        return false;
    }
    public void setConsumeFuel(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.inventory.FurnaceBurnEvent.setConsumeFuel(Z)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.inventory.FurnaceBurnEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
