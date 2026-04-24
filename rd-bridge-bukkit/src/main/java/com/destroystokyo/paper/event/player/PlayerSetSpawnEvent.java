package com.destroystokyo.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerSetSpawnEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerSetSpawnEvent(org.bukkit.entity.Player arg0, com.destroystokyo.paper.event.player.PlayerSetSpawnEvent$Cause arg1, org.bukkit.Location arg2, boolean arg3, boolean arg4, net.kyori.adventure.text.Component arg5) { super((org.bukkit.entity.Player) null); }
    public PlayerSetSpawnEvent() { super((org.bukkit.entity.Player) null); }
    public com.destroystokyo.paper.event.player.PlayerSetSpawnEvent$Cause getCause() {
        return null;
    }
    public org.bukkit.Location getLocation() {
        return null;
    }
    public void setLocation(org.bukkit.Location arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.player.PlayerSetSpawnEvent.setLocation(Lorg/bukkit/Location;)V");
    }
    public boolean isForced() {
        return false;
    }
    public void setForced(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.player.PlayerSetSpawnEvent.setForced(Z)V");
    }
    public boolean willNotifyPlayer() {
        return false;
    }
    public void setNotifyPlayer(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.player.PlayerSetSpawnEvent.setNotifyPlayer(Z)V");
    }
    public net.kyori.adventure.text.Component getNotification() {
        return null;
    }
    public void setNotification(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.player.PlayerSetSpawnEvent.setNotification(Lnet/kyori/adventure/text/Component;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.player.PlayerSetSpawnEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
