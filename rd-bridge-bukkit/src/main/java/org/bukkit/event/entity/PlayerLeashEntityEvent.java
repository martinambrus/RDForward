package org.bukkit.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerLeashEntityEvent extends org.bukkit.event.Event implements org.bukkit.event.Cancellable {
    public PlayerLeashEntityEvent(org.bukkit.entity.Entity arg0, org.bukkit.entity.Entity arg1, org.bukkit.entity.Player arg2, org.bukkit.inventory.EquipmentSlot arg3) {}
    public PlayerLeashEntityEvent(org.bukkit.entity.Entity arg0, org.bukkit.entity.Entity arg1, org.bukkit.entity.Player arg2) {}
    public PlayerLeashEntityEvent() {}
    public org.bukkit.entity.Entity getLeashHolder() {
        return null;
    }
    public org.bukkit.entity.Entity getEntity() {
        return null;
    }
    public final org.bukkit.entity.Player getPlayer() {
        return null;
    }
    public org.bukkit.inventory.EquipmentSlot getHand() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.entity.PlayerLeashEntityEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
