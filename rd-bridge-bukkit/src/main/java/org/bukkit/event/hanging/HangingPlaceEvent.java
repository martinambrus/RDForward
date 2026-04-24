package org.bukkit.event.hanging;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class HangingPlaceEvent extends org.bukkit.event.hanging.HangingEvent implements org.bukkit.event.Cancellable {
    public HangingPlaceEvent(org.bukkit.entity.Hanging arg0, org.bukkit.entity.Player arg1, org.bukkit.block.Block arg2, org.bukkit.block.BlockFace arg3, org.bukkit.inventory.EquipmentSlot arg4) { super((org.bukkit.entity.Hanging) null); }
    public HangingPlaceEvent(org.bukkit.entity.Hanging arg0, org.bukkit.entity.Player arg1, org.bukkit.block.Block arg2, org.bukkit.block.BlockFace arg3, org.bukkit.inventory.EquipmentSlot arg4, org.bukkit.inventory.ItemStack arg5) { super((org.bukkit.entity.Hanging) null); }
    public HangingPlaceEvent() { super((org.bukkit.entity.Hanging) null); }
    public org.bukkit.entity.Player getPlayer() {
        return null;
    }
    public org.bukkit.block.Block getBlock() {
        return null;
    }
    public org.bukkit.block.BlockFace getBlockFace() {
        return null;
    }
    public org.bukkit.inventory.EquipmentSlot getHand() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getItemStack() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.hanging.HangingPlaceEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
