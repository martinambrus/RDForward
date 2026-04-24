package io.papermc.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class AbstractRespawnEvent extends org.bukkit.event.player.PlayerEvent {
    protected AbstractRespawnEvent(org.bukkit.entity.Player arg0, org.bukkit.Location arg1, boolean arg2, boolean arg3, boolean arg4, org.bukkit.event.player.PlayerRespawnEvent$RespawnReason arg5) { super((org.bukkit.entity.Player) null); }
    protected AbstractRespawnEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.Location getRespawnLocation() {
        return null;
    }
    public boolean isBedSpawn() {
        return false;
    }
    public boolean isAnchorSpawn() {
        return false;
    }
    public boolean isMissingRespawnBlock() {
        return false;
    }
    public org.bukkit.event.player.PlayerRespawnEvent$RespawnReason getRespawnReason() {
        return null;
    }
    public java.util.Set getRespawnFlags() {
        return java.util.Collections.emptySet();
    }
}
