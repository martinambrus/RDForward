package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class AsyncPlayerPreLoginEvent extends org.bukkit.event.Event {
    public AsyncPlayerPreLoginEvent(java.lang.String arg0, java.net.InetAddress arg1) {}
    public AsyncPlayerPreLoginEvent(java.lang.String arg0, java.net.InetAddress arg1, java.util.UUID arg2) {}
    public AsyncPlayerPreLoginEvent(java.lang.String arg0, java.net.InetAddress arg1, java.util.UUID arg2, boolean arg3) {}
    public AsyncPlayerPreLoginEvent(java.lang.String arg0, java.net.InetAddress arg1, java.util.UUID arg2, boolean arg3, com.destroystokyo.paper.profile.PlayerProfile arg4) {}
    public AsyncPlayerPreLoginEvent(java.lang.String arg0, java.net.InetAddress arg1, java.net.InetAddress arg2, java.util.UUID arg3, boolean arg4, com.destroystokyo.paper.profile.PlayerProfile arg5) {}
    public AsyncPlayerPreLoginEvent(java.lang.String arg0, java.net.InetAddress arg1, java.net.InetAddress arg2, java.util.UUID arg3, boolean arg4, com.destroystokyo.paper.profile.PlayerProfile arg5, java.lang.String arg6, io.papermc.paper.connection.PlayerLoginConnection arg7) {}
    public AsyncPlayerPreLoginEvent() {}
    public org.bukkit.event.player.AsyncPlayerPreLoginEvent$Result getLoginResult() {
        return null;
    }
    public org.bukkit.event.player.PlayerPreLoginEvent$Result getResult() {
        return null;
    }
    public void setLoginResult(org.bukkit.event.player.AsyncPlayerPreLoginEvent$Result arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.AsyncPlayerPreLoginEvent.setLoginResult(Lorg/bukkit/event/player/AsyncPlayerPreLoginEvent$Result;)V");
    }
    public void setResult(org.bukkit.event.player.PlayerPreLoginEvent$Result arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.AsyncPlayerPreLoginEvent.setResult(Lorg/bukkit/event/player/PlayerPreLoginEvent$Result;)V");
    }
    public net.kyori.adventure.text.Component kickMessage() {
        return null;
    }
    public void kickMessage(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.AsyncPlayerPreLoginEvent.kickMessage(Lnet/kyori/adventure/text/Component;)V");
    }
    public void disallow(org.bukkit.event.player.AsyncPlayerPreLoginEvent$Result arg0, net.kyori.adventure.text.Component arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.AsyncPlayerPreLoginEvent.disallow(Lorg/bukkit/event/player/AsyncPlayerPreLoginEvent$Result;Lnet/kyori/adventure/text/Component;)V");
    }
    public void disallow(org.bukkit.event.player.PlayerPreLoginEvent$Result arg0, net.kyori.adventure.text.Component arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.AsyncPlayerPreLoginEvent.disallow(Lorg/bukkit/event/player/PlayerPreLoginEvent$Result;Lnet/kyori/adventure/text/Component;)V");
    }
    public java.lang.String getKickMessage() {
        return null;
    }
    public void setKickMessage(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.AsyncPlayerPreLoginEvent.setKickMessage(Ljava/lang/String;)V");
    }
    public void allow() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.AsyncPlayerPreLoginEvent.allow()V");
    }
    public void disallow(org.bukkit.event.player.AsyncPlayerPreLoginEvent$Result arg0, java.lang.String arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.AsyncPlayerPreLoginEvent.disallow(Lorg/bukkit/event/player/AsyncPlayerPreLoginEvent$Result;Ljava/lang/String;)V");
    }
    public void disallow(org.bukkit.event.player.PlayerPreLoginEvent$Result arg0, java.lang.String arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.AsyncPlayerPreLoginEvent.disallow(Lorg/bukkit/event/player/PlayerPreLoginEvent$Result;Ljava/lang/String;)V");
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
    public com.destroystokyo.paper.profile.PlayerProfile getPlayerProfile() {
        return null;
    }
    public void setPlayerProfile(com.destroystokyo.paper.profile.PlayerProfile arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.AsyncPlayerPreLoginEvent.setPlayerProfile(Lcom/destroystokyo/paper/profile/PlayerProfile;)V");
    }
    public java.net.InetAddress getRawAddress() {
        return null;
    }
    public java.lang.String getHostname() {
        return null;
    }
    public boolean isTransferred() {
        return false;
    }
    public io.papermc.paper.connection.PlayerLoginConnection getConnection() {
        return null;
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
