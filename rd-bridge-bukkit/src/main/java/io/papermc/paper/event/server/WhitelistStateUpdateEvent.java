package io.papermc.paper.event.server;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class WhitelistStateUpdateEvent extends org.bukkit.event.Event implements org.bukkit.event.Cancellable {
    public WhitelistStateUpdateEvent(com.destroystokyo.paper.profile.PlayerProfile arg0, io.papermc.paper.event.server.WhitelistStateUpdateEvent$WhitelistStatus arg1) {}
    public WhitelistStateUpdateEvent() {}
    public org.bukkit.OfflinePlayer getPlayer() {
        return null;
    }
    public com.destroystokyo.paper.profile.PlayerProfile getPlayerProfile() {
        return null;
    }
    public io.papermc.paper.event.server.WhitelistStateUpdateEvent$WhitelistStatus getStatus() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.server.WhitelistStateUpdateEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
