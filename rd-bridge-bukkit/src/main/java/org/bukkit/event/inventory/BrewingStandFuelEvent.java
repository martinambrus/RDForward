package org.bukkit.event.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class BrewingStandFuelEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    public BrewingStandFuelEvent(org.bukkit.block.Block arg0, org.bukkit.inventory.ItemStack arg1, int arg2) { super((org.bukkit.block.Block) null); }
    public BrewingStandFuelEvent() { super((org.bukkit.block.Block) null); }
    public org.bukkit.inventory.ItemStack getFuel() {
        return null;
    }
    public int getFuelPower() {
        return 0;
    }
    public void setFuelPower(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.inventory.BrewingStandFuelEvent.setFuelPower(I)V");
    }
    public boolean isConsuming() {
        return false;
    }
    public void setConsuming(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.inventory.BrewingStandFuelEvent.setConsuming(Z)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.inventory.BrewingStandFuelEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
