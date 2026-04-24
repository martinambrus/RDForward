package io.papermc.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerFailMoveEvent extends org.bukkit.event.player.PlayerEvent {
    public PlayerFailMoveEvent(org.bukkit.entity.Player arg0, io.papermc.paper.event.player.PlayerFailMoveEvent$FailReason arg1, boolean arg2, boolean arg3, org.bukkit.Location arg4, org.bukkit.Location arg5) { super((org.bukkit.entity.Player) null); }
    public PlayerFailMoveEvent() { super((org.bukkit.entity.Player) null); }
    public io.papermc.paper.event.player.PlayerFailMoveEvent$FailReason getFailReason() {
        return null;
    }
    public org.bukkit.Location getFrom() {
        return null;
    }
    public org.bukkit.Location getTo() {
        return null;
    }
    public boolean isAllowed() {
        return false;
    }
    public void setAllowed(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerFailMoveEvent.setAllowed(Z)V");
    }
    public boolean getLogWarning() {
        return false;
    }
    public void setLogWarning(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerFailMoveEvent.setLogWarning(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
