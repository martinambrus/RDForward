package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerVelocityEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerVelocityEvent(org.bukkit.entity.Player arg0, org.bukkit.util.Vector arg1) { super((org.bukkit.entity.Player) null); }
    public PlayerVelocityEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.util.Vector getVelocity() {
        return null;
    }
    public void setVelocity(org.bukkit.util.Vector arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerVelocityEvent.setVelocity(Lorg/bukkit/util/Vector;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerVelocityEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
