package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerHarvestBlockEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerHarvestBlockEvent(org.bukkit.entity.Player arg0, org.bukkit.block.Block arg1, org.bukkit.inventory.EquipmentSlot arg2, java.util.List arg3) { super((org.bukkit.entity.Player) null); }
    public PlayerHarvestBlockEvent(org.bukkit.entity.Player arg0, org.bukkit.block.Block arg1, java.util.List arg2) { super((org.bukkit.entity.Player) null); }
    public PlayerHarvestBlockEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.block.Block getHarvestedBlock() {
        return null;
    }
    public org.bukkit.inventory.EquipmentSlot getHand() {
        return null;
    }
    public java.util.List getItemsHarvested() {
        return java.util.Collections.emptyList();
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerHarvestBlockEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
