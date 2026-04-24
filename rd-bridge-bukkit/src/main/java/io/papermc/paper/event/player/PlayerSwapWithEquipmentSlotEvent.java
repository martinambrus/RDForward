package io.papermc.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerSwapWithEquipmentSlotEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerSwapWithEquipmentSlotEvent(org.bukkit.entity.Player arg0, org.bukkit.inventory.ItemStack arg1, org.bukkit.inventory.EquipmentSlot arg2, org.bukkit.inventory.ItemStack arg3) { super((org.bukkit.entity.Player) null); }
    public PlayerSwapWithEquipmentSlotEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.inventory.ItemStack getItemInHand() {
        return null;
    }
    public org.bukkit.inventory.EquipmentSlot getSlot() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getItemToSwap() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerSwapWithEquipmentSlotEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
