package com.destroystokyo.paper.event.server;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PaperServerListPingEvent extends org.bukkit.event.server.ServerListPingEvent implements org.bukkit.event.Cancellable {
    public PaperServerListPingEvent(com.destroystokyo.paper.network.StatusClient arg0, net.kyori.adventure.text.Component arg1, int arg2, int arg3, java.lang.String arg4, int arg5, org.bukkit.util.CachedServerIcon arg6) { super((java.lang.String) null, (java.net.InetAddress) null, (net.kyori.adventure.text.Component) null, 0, 0); }
    public PaperServerListPingEvent() { super((java.lang.String) null, (java.net.InetAddress) null, (net.kyori.adventure.text.Component) null, 0, 0); }
    public com.destroystokyo.paper.network.StatusClient getClient() {
        return null;
    }
    public int getNumPlayers() {
        return 0;
    }
    public void setNumPlayers(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.server.PaperServerListPingEvent.setNumPlayers(I)V");
    }
    public int getMaxPlayers() {
        return 0;
    }
    public boolean shouldHidePlayers() {
        return false;
    }
    public void setHidePlayers(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.server.PaperServerListPingEvent.setHidePlayers(Z)V");
    }
    public java.util.List getListedPlayers() {
        return java.util.Collections.emptyList();
    }
    public java.util.List getPlayerSample() {
        return java.util.Collections.emptyList();
    }
    public java.lang.String getVersion() {
        return null;
    }
    public void setVersion(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.server.PaperServerListPingEvent.setVersion(Ljava/lang/String;)V");
    }
    public int getProtocolVersion() {
        return 0;
    }
    public void setProtocolVersion(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.server.PaperServerListPingEvent.setProtocolVersion(I)V");
    }
    public org.bukkit.util.CachedServerIcon getServerIcon() {
        return null;
    }
    public void setServerIcon(org.bukkit.util.CachedServerIcon arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.server.PaperServerListPingEvent.setServerIcon(Lorg/bukkit/util/CachedServerIcon;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.server.PaperServerListPingEvent.setCancelled(Z)V");
    }
    public java.util.Iterator iterator() {
        return null;
    }
    protected java.lang.Object[] getOnlinePlayers() {
        return new java.lang.Object[0];
    }
    protected org.bukkit.entity.Player getBukkitPlayer(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.server.PaperServerListPingEvent.getBukkitPlayer(Ljava/lang/Object;)Lorg/bukkit/entity/Player;");
        return null;
    }
}
