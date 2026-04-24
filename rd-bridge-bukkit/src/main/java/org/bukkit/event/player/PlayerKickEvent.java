package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerKickEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerKickEvent(org.bukkit.entity.Player arg0, java.lang.String arg1, java.lang.String arg2) { super((org.bukkit.entity.Player) null); }
    public PlayerKickEvent(org.bukkit.entity.Player arg0, net.kyori.adventure.text.Component arg1, net.kyori.adventure.text.Component arg2) { super((org.bukkit.entity.Player) null); }
    public PlayerKickEvent(org.bukkit.entity.Player arg0, net.kyori.adventure.text.Component arg1, net.kyori.adventure.text.Component arg2, org.bukkit.event.player.PlayerKickEvent$Cause arg3) { super((org.bukkit.entity.Player) null); }
    public PlayerKickEvent() { super((org.bukkit.entity.Player) null); }
    public net.kyori.adventure.text.Component reason() {
        return null;
    }
    public void reason(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerKickEvent.reason(Lnet/kyori/adventure/text/Component;)V");
    }
    public java.lang.String getReason() {
        return null;
    }
    public void setReason(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerKickEvent.setReason(Ljava/lang/String;)V");
    }
    public net.kyori.adventure.text.Component leaveMessage() {
        return null;
    }
    public void leaveMessage(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerKickEvent.leaveMessage(Lnet/kyori/adventure/text/Component;)V");
    }
    public java.lang.String getLeaveMessage() {
        return null;
    }
    public void setLeaveMessage(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerKickEvent.setLeaveMessage(Ljava/lang/String;)V");
    }
    public org.bukkit.event.player.PlayerKickEvent$Cause getCause() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerKickEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
