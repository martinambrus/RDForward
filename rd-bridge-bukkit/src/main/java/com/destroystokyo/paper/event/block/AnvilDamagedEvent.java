package com.destroystokyo.paper.event.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class AnvilDamagedEvent extends org.bukkit.event.inventory.InventoryEvent implements org.bukkit.event.Cancellable {
    public AnvilDamagedEvent(org.bukkit.inventory.InventoryView arg0, org.bukkit.block.data.BlockData arg1) { super((org.bukkit.inventory.InventoryView) null); }
    public AnvilDamagedEvent() { super((org.bukkit.inventory.InventoryView) null); }
    public org.bukkit.inventory.AnvilInventory getInventory() {
        return null;
    }
    public com.destroystokyo.paper.event.block.AnvilDamagedEvent$DamageState getDamageState() {
        return null;
    }
    public void setDamageState(com.destroystokyo.paper.event.block.AnvilDamagedEvent$DamageState arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.block.AnvilDamagedEvent.setDamageState(Lcom/destroystokyo/paper/event/block/AnvilDamagedEvent$DamageState;)V");
    }
    public boolean isBreaking() {
        return false;
    }
    public void setBreaking(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.block.AnvilDamagedEvent.setBreaking(Z)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.block.AnvilDamagedEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
