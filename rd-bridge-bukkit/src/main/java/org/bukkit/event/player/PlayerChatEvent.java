package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerChatEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerChatEvent(org.bukkit.entity.Player arg0, java.lang.String arg1) { super((org.bukkit.entity.Player) null); }
    public PlayerChatEvent(org.bukkit.entity.Player arg0, java.lang.String arg1, java.lang.String arg2, java.util.Set arg3) { super((org.bukkit.entity.Player) null); }
    public PlayerChatEvent() { super((org.bukkit.entity.Player) null); }
    public java.lang.String getMessage() {
        return null;
    }
    public void setMessage(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerChatEvent.setMessage(Ljava/lang/String;)V");
    }
    public void setPlayer(org.bukkit.entity.Player arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerChatEvent.setPlayer(Lorg/bukkit/entity/Player;)V");
    }
    public java.lang.String getFormat() {
        return null;
    }
    public void setFormat(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerChatEvent.setFormat(Ljava/lang/String;)V");
    }
    public java.util.Set getRecipients() {
        return java.util.Collections.emptySet();
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerChatEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
