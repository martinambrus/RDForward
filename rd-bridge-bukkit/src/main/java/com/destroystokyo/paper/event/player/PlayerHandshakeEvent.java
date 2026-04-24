package com.destroystokyo.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerHandshakeEvent extends org.bukkit.event.Event implements org.bukkit.event.Cancellable {
    public PlayerHandshakeEvent(java.lang.String arg0, boolean arg1) {}
    public PlayerHandshakeEvent(java.lang.String arg0, java.lang.String arg1, boolean arg2) {}
    public PlayerHandshakeEvent() {}
    public java.lang.String getOriginalHandshake() {
        return null;
    }
    public java.lang.String getOriginalSocketAddressHostname() {
        return null;
    }
    public java.lang.String getServerHostname() {
        return null;
    }
    public void setServerHostname(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.player.PlayerHandshakeEvent.setServerHostname(Ljava/lang/String;)V");
    }
    public java.lang.String getSocketAddressHostname() {
        return null;
    }
    public void setSocketAddressHostname(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.player.PlayerHandshakeEvent.setSocketAddressHostname(Ljava/lang/String;)V");
    }
    public java.util.UUID getUniqueId() {
        return null;
    }
    public void setUniqueId(java.util.UUID arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.player.PlayerHandshakeEvent.setUniqueId(Ljava/util/UUID;)V");
    }
    public java.lang.String getPropertiesJson() {
        return null;
    }
    public boolean isFailed() {
        return false;
    }
    public void setFailed(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.player.PlayerHandshakeEvent.setFailed(Z)V");
    }
    public void setPropertiesJson(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.player.PlayerHandshakeEvent.setPropertiesJson(Ljava/lang/String;)V");
    }
    public net.kyori.adventure.text.Component failMessage() {
        return null;
    }
    public void failMessage(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.player.PlayerHandshakeEvent.failMessage(Lnet/kyori/adventure/text/Component;)V");
    }
    public java.lang.String getFailMessage() {
        return null;
    }
    public void setFailMessage(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.player.PlayerHandshakeEvent.setFailMessage(Ljava/lang/String;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.player.PlayerHandshakeEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
