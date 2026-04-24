package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class PlayerBucketEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerBucketEvent(org.bukkit.entity.Player arg0, org.bukkit.block.Block arg1, org.bukkit.block.BlockFace arg2, org.bukkit.Material arg3, org.bukkit.inventory.ItemStack arg4) { super((org.bukkit.entity.Player) null); }
    public PlayerBucketEvent(org.bukkit.entity.Player arg0, org.bukkit.block.Block arg1, org.bukkit.block.Block arg2, org.bukkit.block.BlockFace arg3, org.bukkit.Material arg4, org.bukkit.inventory.ItemStack arg5) { super((org.bukkit.entity.Player) null); }
    public PlayerBucketEvent(org.bukkit.entity.Player arg0, org.bukkit.block.Block arg1, org.bukkit.block.Block arg2, org.bukkit.block.BlockFace arg3, org.bukkit.Material arg4, org.bukkit.inventory.ItemStack arg5, org.bukkit.inventory.EquipmentSlot arg6) { super((org.bukkit.entity.Player) null); }
    protected PlayerBucketEvent() { super((org.bukkit.entity.Player) null); }
    public final org.bukkit.block.Block getBlock() {
        return null;
    }
    public org.bukkit.block.Block getBlockClicked() {
        return null;
    }
    public org.bukkit.block.BlockFace getBlockFace() {
        return null;
    }
    public org.bukkit.Material getBucket() {
        return null;
    }
    public org.bukkit.inventory.EquipmentSlot getHand() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getItemStack() {
        return null;
    }
    public void setItemStack(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerBucketEvent.setItemStack(Lorg/bukkit/inventory/ItemStack;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerBucketEvent.setCancelled(Z)V");
    }
}
