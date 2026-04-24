package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerInteractEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerInteractEvent(org.bukkit.entity.Player arg0, org.bukkit.event.block.Action arg1, org.bukkit.inventory.ItemStack arg2, org.bukkit.block.Block arg3, org.bukkit.block.BlockFace arg4) { super((org.bukkit.entity.Player) null); }
    public PlayerInteractEvent(org.bukkit.entity.Player arg0, org.bukkit.event.block.Action arg1, org.bukkit.inventory.ItemStack arg2, org.bukkit.block.Block arg3, org.bukkit.block.BlockFace arg4, org.bukkit.inventory.EquipmentSlot arg5) { super((org.bukkit.entity.Player) null); }
    public PlayerInteractEvent(org.bukkit.entity.Player arg0, org.bukkit.event.block.Action arg1, org.bukkit.inventory.ItemStack arg2, org.bukkit.block.Block arg3, org.bukkit.block.BlockFace arg4, org.bukkit.inventory.EquipmentSlot arg5, org.bukkit.util.Vector arg6) { super((org.bukkit.entity.Player) null); }
    public PlayerInteractEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.event.block.Action getAction() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerInteractEvent.setCancelled(Z)V");
    }
    public org.bukkit.inventory.ItemStack getItem() {
        return null;
    }
    public org.bukkit.Material getMaterial() {
        return null;
    }
    public boolean hasBlock() {
        return false;
    }
    public boolean hasItem() {
        return false;
    }
    public boolean isBlockInHand() {
        return false;
    }
    public org.bukkit.block.Block getClickedBlock() {
        return null;
    }
    public org.bukkit.block.BlockFace getBlockFace() {
        return null;
    }
    public org.bukkit.inventory.EquipmentSlot getHand() {
        return null;
    }
    public org.bukkit.util.Vector getClickedPosition() {
        return null;
    }
    public org.bukkit.Location getInteractionPoint() {
        return null;
    }
    public org.bukkit.event.Event$Result useInteractedBlock() {
        return null;
    }
    public void setUseInteractedBlock(org.bukkit.event.Event$Result arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerInteractEvent.setUseInteractedBlock(Lorg/bukkit/event/Event$Result;)V");
    }
    public org.bukkit.event.Event$Result useItemInHand() {
        return null;
    }
    public void setUseItemInHand(org.bukkit.event.Event$Result arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerInteractEvent.setUseItemInHand(Lorg/bukkit/event/Event$Result;)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
