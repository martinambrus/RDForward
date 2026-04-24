package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerAttemptPickupItemEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerAttemptPickupItemEvent(org.bukkit.entity.Player arg0, org.bukkit.entity.Item arg1) { super((org.bukkit.entity.Player) null); }
    public PlayerAttemptPickupItemEvent(org.bukkit.entity.Player arg0, org.bukkit.entity.Item arg1, int arg2) { super((org.bukkit.entity.Player) null); }
    public PlayerAttemptPickupItemEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.entity.Item getItem() {
        return null;
    }
    public int getRemaining() {
        return 0;
    }
    public void setFlyAtPlayer(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerAttemptPickupItemEvent.setFlyAtPlayer(Z)V");
    }
    public boolean getFlyAtPlayer() {
        return false;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerAttemptPickupItemEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
