package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerBedLeaveEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerBedLeaveEvent(org.bukkit.entity.Player arg0, org.bukkit.block.Block arg1, boolean arg2) { super((org.bukkit.entity.Player) null); }
    public PlayerBedLeaveEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.block.Block getBed() {
        return null;
    }
    public boolean shouldSetSpawnLocation() {
        return false;
    }
    public void setSpawnLocation(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerBedLeaveEvent.setSpawnLocation(Z)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerBedLeaveEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
