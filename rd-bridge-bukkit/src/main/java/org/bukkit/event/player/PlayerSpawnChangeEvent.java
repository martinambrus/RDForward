package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerSpawnChangeEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerSpawnChangeEvent(org.bukkit.entity.Player arg0, org.bukkit.Location arg1, boolean arg2, org.bukkit.event.player.PlayerSpawnChangeEvent$Cause arg3) { super((org.bukkit.entity.Player) null); }
    public PlayerSpawnChangeEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.Location getNewSpawn() {
        return null;
    }
    public void setNewSpawn(org.bukkit.Location arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerSpawnChangeEvent.setNewSpawn(Lorg/bukkit/Location;)V");
    }
    public org.bukkit.event.player.PlayerSpawnChangeEvent$Cause getCause() {
        return null;
    }
    public boolean isForced() {
        return false;
    }
    public void setForced(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerSpawnChangeEvent.setForced(Z)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerSpawnChangeEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
