package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerItemHeldEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerItemHeldEvent(org.bukkit.entity.Player arg0, int arg1, int arg2) { super((org.bukkit.entity.Player) null); }
    public PlayerItemHeldEvent() { super((org.bukkit.entity.Player) null); }
    public int getPreviousSlot() {
        return 0;
    }
    public int getNewSlot() {
        return 0;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerItemHeldEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
