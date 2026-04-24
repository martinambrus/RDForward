package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerAnimationEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerAnimationEvent(org.bukkit.entity.Player arg0) { super((org.bukkit.entity.Player) null); }
    public PlayerAnimationEvent(org.bukkit.entity.Player arg0, org.bukkit.event.player.PlayerAnimationType arg1) { super((org.bukkit.entity.Player) null); }
    public PlayerAnimationEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.event.player.PlayerAnimationType getAnimationType() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerAnimationEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
