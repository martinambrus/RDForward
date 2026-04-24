package org.bukkit.event.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class BlockDamageEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    public BlockDamageEvent(org.bukkit.entity.Player arg0, org.bukkit.block.Block arg1, org.bukkit.inventory.ItemStack arg2, boolean arg3) { super((org.bukkit.block.Block) null); }
    public BlockDamageEvent(org.bukkit.entity.Player arg0, org.bukkit.block.Block arg1, org.bukkit.block.BlockFace arg2, org.bukkit.inventory.ItemStack arg3, boolean arg4) { super((org.bukkit.block.Block) null); }
    public BlockDamageEvent() { super((org.bukkit.block.Block) null); }
    public org.bukkit.entity.Player getPlayer() {
        return null;
    }
    public boolean getInstaBreak() {
        return false;
    }
    public void setInstaBreak(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.BlockDamageEvent.setInstaBreak(Z)V");
    }
    public org.bukkit.inventory.ItemStack getItemInHand() {
        return null;
    }
    public org.bukkit.block.BlockFace getBlockFace() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.BlockDamageEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
