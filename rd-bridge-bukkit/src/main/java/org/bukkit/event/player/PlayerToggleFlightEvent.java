package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerToggleFlightEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerToggleFlightEvent(org.bukkit.entity.Player arg0, boolean arg1) { super((org.bukkit.entity.Player) null); }
    public PlayerToggleFlightEvent() { super((org.bukkit.entity.Player) null); }
    public boolean isFlying() {
        return false;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerToggleFlightEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
