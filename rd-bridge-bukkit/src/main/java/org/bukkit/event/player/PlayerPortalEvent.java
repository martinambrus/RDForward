package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerPortalEvent extends org.bukkit.event.player.PlayerTeleportEvent {
    public PlayerPortalEvent(org.bukkit.entity.Player arg0, org.bukkit.Location arg1, org.bukkit.Location arg2) { super((org.bukkit.entity.Player) null, (org.bukkit.Location) null, (org.bukkit.Location) null); }
    public PlayerPortalEvent(org.bukkit.entity.Player arg0, org.bukkit.Location arg1, org.bukkit.Location arg2, org.bukkit.event.player.PlayerTeleportEvent$TeleportCause arg3) { super((org.bukkit.entity.Player) null, (org.bukkit.Location) null, (org.bukkit.Location) null); }
    public PlayerPortalEvent(org.bukkit.entity.Player arg0, org.bukkit.Location arg1, org.bukkit.Location arg2, org.bukkit.event.player.PlayerTeleportEvent$TeleportCause arg3, int arg4, boolean arg5, int arg6) { super((org.bukkit.entity.Player) null, (org.bukkit.Location) null, (org.bukkit.Location) null); }
    public PlayerPortalEvent() { super((org.bukkit.entity.Player) null, (org.bukkit.Location) null, (org.bukkit.Location) null); }
    public org.bukkit.Location getTo() {
        return null;
    }
    public void setTo(org.bukkit.Location arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerPortalEvent.setTo(Lorg/bukkit/Location;)V");
    }
    public void setSearchRadius(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerPortalEvent.setSearchRadius(I)V");
    }
    public int getSearchRadius() {
        return 0;
    }
    public boolean getCanCreatePortal() {
        return false;
    }
    public void setCanCreatePortal(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerPortalEvent.setCanCreatePortal(Z)V");
    }
    public void setCreationRadius(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerPortalEvent.setCreationRadius(I)V");
    }
    public int getCreationRadius() {
        return 0;
    }
    public boolean willDismountPlayer() {
        return false;
    }
    public java.util.Set getRelativeTeleportationFlags() {
        return java.util.Collections.emptySet();
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
