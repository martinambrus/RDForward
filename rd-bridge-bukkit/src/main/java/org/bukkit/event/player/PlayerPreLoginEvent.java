package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerPreLoginEvent extends org.bukkit.event.Event {
    public PlayerPreLoginEvent(java.lang.String arg0, java.net.InetAddress arg1) {}
    public PlayerPreLoginEvent(java.lang.String arg0, java.net.InetAddress arg1, java.util.UUID arg2) {}
    public PlayerPreLoginEvent() {}
    public org.bukkit.event.player.PlayerPreLoginEvent$Result getResult() {
        return null;
    }
    public void setResult(org.bukkit.event.player.PlayerPreLoginEvent$Result arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerPreLoginEvent.setResult(Lorg/bukkit/event/player/PlayerPreLoginEvent$Result;)V");
    }
    public net.kyori.adventure.text.Component kickMessage() {
        return null;
    }
    public void kickMessage(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerPreLoginEvent.kickMessage(Lnet/kyori/adventure/text/Component;)V");
    }
    public void disallow(org.bukkit.event.player.PlayerPreLoginEvent$Result arg0, net.kyori.adventure.text.Component arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerPreLoginEvent.disallow(Lorg/bukkit/event/player/PlayerPreLoginEvent$Result;Lnet/kyori/adventure/text/Component;)V");
    }
    public java.lang.String getKickMessage() {
        return null;
    }
    public void setKickMessage(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerPreLoginEvent.setKickMessage(Ljava/lang/String;)V");
    }
    public void allow() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerPreLoginEvent.allow()V");
    }
    public void disallow(org.bukkit.event.player.PlayerPreLoginEvent$Result arg0, java.lang.String arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerPreLoginEvent.disallow(Lorg/bukkit/event/player/PlayerPreLoginEvent$Result;Ljava/lang/String;)V");
    }
    public java.lang.String getName() {
        return null;
    }
    public java.net.InetAddress getAddress() {
        return null;
    }
    public java.util.UUID getUniqueId() {
        return null;
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
