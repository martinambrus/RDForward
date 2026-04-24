package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerShearEntityEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerShearEntityEvent(org.bukkit.entity.Player arg0, org.bukkit.entity.Entity arg1, org.bukkit.inventory.ItemStack arg2, org.bukkit.inventory.EquipmentSlot arg3, java.util.List arg4) { super((org.bukkit.entity.Player) null); }
    public PlayerShearEntityEvent(org.bukkit.entity.Player arg0, org.bukkit.entity.Entity arg1) { super((org.bukkit.entity.Player) null); }
    public PlayerShearEntityEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.entity.Entity getEntity() {
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
    public void setDrops(java.util.List arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerShearEntityEvent.setDrops(Ljava/util/List;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerShearEntityEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
