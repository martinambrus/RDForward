package org.bukkit.event.server;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class ServerListPingEvent extends org.bukkit.event.server.ServerEvent implements java.lang.Iterable {
    public ServerListPingEvent(java.lang.String arg0, java.net.InetAddress arg1, java.lang.String arg2, int arg3, int arg4) {}
    protected ServerListPingEvent(java.lang.String arg0, java.net.InetAddress arg1, java.lang.String arg2, int arg3) {}
    public ServerListPingEvent(java.net.InetAddress arg0, net.kyori.adventure.text.Component arg1, int arg2, int arg3) {}
    public ServerListPingEvent(java.lang.String arg0, java.net.InetAddress arg1, net.kyori.adventure.text.Component arg2, int arg3, int arg4) {}
    protected ServerListPingEvent(java.net.InetAddress arg0, net.kyori.adventure.text.Component arg1, int arg2) {}
    protected ServerListPingEvent(java.lang.String arg0, java.net.InetAddress arg1, net.kyori.adventure.text.Component arg2, int arg3) {}
    public ServerListPingEvent() {}
    public java.lang.String getHostname() {
        return null;
    }
    public java.net.InetAddress getAddress() {
        return null;
    }
    public net.kyori.adventure.text.Component motd() {
        return null;
    }
    public void motd(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.server.ServerListPingEvent.motd(Lnet/kyori/adventure/text/Component;)V");
    }
    public java.lang.String getMotd() {
        return null;
    }
    public void setMotd(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.server.ServerListPingEvent.setMotd(Ljava/lang/String;)V");
    }
    public int getNumPlayers() {
        return 0;
    }
    public int getMaxPlayers() {
        return 0;
    }
    public void setMaxPlayers(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.server.ServerListPingEvent.setMaxPlayers(I)V");
    }
    public boolean shouldSendChatPreviews() {
        return false;
    }
    public void setServerIcon(org.bukkit.util.CachedServerIcon arg0) throws java.lang.IllegalArgumentException, java.lang.UnsupportedOperationException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.server.ServerListPingEvent.setServerIcon(Lorg/bukkit/util/CachedServerIcon;)V");
    }
    public java.util.Iterator iterator() throws java.lang.UnsupportedOperationException {
        return null;
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
