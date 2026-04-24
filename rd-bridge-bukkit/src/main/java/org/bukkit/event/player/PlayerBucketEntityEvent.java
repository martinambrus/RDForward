package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerBucketEntityEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerBucketEntityEvent(org.bukkit.entity.Player arg0, org.bukkit.entity.Entity arg1, org.bukkit.inventory.ItemStack arg2, org.bukkit.inventory.ItemStack arg3, org.bukkit.inventory.EquipmentSlot arg4) { super((org.bukkit.entity.Player) null); }
    public PlayerBucketEntityEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.entity.Entity getEntity() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getOriginalBucket() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getEntityBucket() {
        return null;
    }
    public org.bukkit.inventory.EquipmentSlot getHand() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerBucketEntityEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
