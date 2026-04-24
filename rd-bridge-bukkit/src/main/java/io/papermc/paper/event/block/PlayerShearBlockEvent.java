package io.papermc.paper.event.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerShearBlockEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerShearBlockEvent(org.bukkit.entity.Player arg0, org.bukkit.block.Block arg1, org.bukkit.inventory.ItemStack arg2, org.bukkit.inventory.EquipmentSlot arg3, java.util.List arg4) { super((org.bukkit.entity.Player) null); }
    public PlayerShearBlockEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.block.Block getBlock() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getItem() {
        return null;
    }
    public org.bukkit.inventory.EquipmentSlot getHand() {
        return null;
    }
    public java.util.List getDrops() {
        return java.util.Collections.emptyList();
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.block.PlayerShearBlockEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
