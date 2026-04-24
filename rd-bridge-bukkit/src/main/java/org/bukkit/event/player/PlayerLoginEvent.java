package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerLoginEvent extends org.bukkit.event.player.PlayerEvent {
    public PlayerLoginEvent(org.bukkit.entity.Player arg0, java.lang.String arg1, java.net.InetAddress arg2, java.net.InetAddress arg3) { super((org.bukkit.entity.Player) null); }
    public PlayerLoginEvent(org.bukkit.entity.Player arg0, java.lang.String arg1, java.net.InetAddress arg2) { super((org.bukkit.entity.Player) null); }
    public PlayerLoginEvent(org.bukkit.entity.Player arg0, java.lang.String arg1, java.net.InetAddress arg2, org.bukkit.event.player.PlayerLoginEvent$Result arg3, java.lang.String arg4, java.net.InetAddress arg5) { super((org.bukkit.entity.Player) null); }
    public PlayerLoginEvent(org.bukkit.entity.Player arg0, java.lang.String arg1, java.net.InetAddress arg2, org.bukkit.event.player.PlayerLoginEvent$Result arg3, net.kyori.adventure.text.Component arg4, java.net.InetAddress arg5) { super((org.bukkit.entity.Player) null); }
    public PlayerLoginEvent() { super((org.bukkit.entity.Player) null); }
    public java.lang.String getHostname() {
        return null;
    }
    public java.net.InetAddress getAddress() {
        return null;
    }
    public java.net.InetAddress getRealAddress() {
        return null;
    }
    public org.bukkit.event.player.PlayerLoginEvent$Result getResult() {
        return null;
    }
    public void setResult(org.bukkit.event.player.PlayerLoginEvent$Result arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerLoginEvent.setResult(Lorg/bukkit/event/player/PlayerLoginEvent$Result;)V");
    }
    public net.kyori.adventure.text.Component kickMessage() {
        return null;
    }
    public void kickMessage(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerLoginEvent.kickMessage(Lnet/kyori/adventure/text/Component;)V");
    }
    public java.lang.String getKickMessage() {
        return null;
    }
    public void setKickMessage(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerLoginEvent.setKickMessage(Ljava/lang/String;)V");
    }
    public void allow() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerLoginEvent.allow()V");
    }
    public void disallow(org.bukkit.event.player.PlayerLoginEvent$Result arg0, java.lang.String arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerLoginEvent.disallow(Lorg/bukkit/event/player/PlayerLoginEvent$Result;Ljava/lang/String;)V");
    }
    public void disallow(org.bukkit.event.player.PlayerLoginEvent$Result arg0, net.kyori.adventure.text.Component arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerLoginEvent.disallow(Lorg/bukkit/event/player/PlayerLoginEvent$Result;Lnet/kyori/adventure/text/Component;)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
