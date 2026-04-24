package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerInteractEntityEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerInteractEntityEvent(org.bukkit.entity.Player arg0, org.bukkit.entity.Entity arg1) { super((org.bukkit.entity.Player) null); }
    public PlayerInteractEntityEvent(org.bukkit.entity.Player arg0, org.bukkit.entity.Entity arg1, org.bukkit.inventory.EquipmentSlot arg2) { super((org.bukkit.entity.Player) null); }
    public PlayerInteractEntityEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.entity.Entity getRightClicked() {
        return null;
    }
    public org.bukkit.inventory.EquipmentSlot getHand() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerInteractEntityEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
