package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerTeleportEvent extends org.bukkit.event.player.PlayerMoveEvent {
    public PlayerTeleportEvent(org.bukkit.entity.Player arg0, org.bukkit.Location arg1, org.bukkit.Location arg2) { super((org.bukkit.entity.Player) null, (org.bukkit.Location) null, (org.bukkit.Location) null); }
    public PlayerTeleportEvent(org.bukkit.entity.Player arg0, org.bukkit.Location arg1, org.bukkit.Location arg2, org.bukkit.event.player.PlayerTeleportEvent$TeleportCause arg3) { super((org.bukkit.entity.Player) null, (org.bukkit.Location) null, (org.bukkit.Location) null); }
    public PlayerTeleportEvent(org.bukkit.entity.Player arg0, org.bukkit.Location arg1, org.bukkit.Location arg2, org.bukkit.event.player.PlayerTeleportEvent$TeleportCause arg3, java.util.Set arg4) { super((org.bukkit.entity.Player) null, (org.bukkit.Location) null, (org.bukkit.Location) null); }
    public PlayerTeleportEvent() { super((org.bukkit.entity.Player) null, (org.bukkit.Location) null, (org.bukkit.Location) null); }
    public org.bukkit.event.player.PlayerTeleportEvent$TeleportCause getCause() {
        return null;
    }
    public java.util.Set getRelativeTeleportationFlags() {
        return java.util.Collections.emptySet();
    }
    public boolean willDismountPlayer() {
        return false;
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
