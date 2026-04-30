// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event.player;

/**
 * Fired when a player respawns. EssentialsX's {@code /home} (via
 * {@code AsyncTeleport.respawnNow}) constructs this event with the
 * 3-arg form {@code new PlayerRespawnEvent(player, spawnLoc, false)},
 * fires it, and the listener's {@code onPlayerRespawn} reads
 * {@code event.getPlayer()} to look up the user — so the player
 * reference passed to the constructor MUST survive the call to
 * {@link io.papermc.paper.event.player.AbstractRespawnEvent}'s
 * super-ctor (and onward to {@link PlayerEvent}). The previous
 * auto-generated stub forwarded {@code null}, causing
 * {@code EssentialsPlayerListener.updateCompass} to NPE on every
 * respawn-shaped path.
 *
 * <p>{@link #setRespawnLocation} mutates the location field on the
 * abstract base so listener edits before {@code respawn()} take effect.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerRespawnEvent extends io.papermc.paper.event.player.AbstractRespawnEvent {

    public PlayerRespawnEvent(org.bukkit.entity.Player player,
                              org.bukkit.Location respawnLocation,
                              boolean bedSpawn) {
        super(player, respawnLocation, bedSpawn, false, false, null);
    }

    public PlayerRespawnEvent(org.bukkit.entity.Player player,
                              org.bukkit.Location respawnLocation,
                              boolean bedSpawn,
                              boolean anchorSpawn) {
        super(player, respawnLocation, bedSpawn, anchorSpawn, false, null);
    }

    public PlayerRespawnEvent(org.bukkit.entity.Player player,
                              org.bukkit.Location respawnLocation,
                              boolean bedSpawn,
                              boolean anchorSpawn,
                              boolean missingRespawnBlock,
                              org.bukkit.event.player.PlayerRespawnEvent$RespawnReason reason) {
        super(player, respawnLocation, bedSpawn, anchorSpawn, missingRespawnBlock, reason);
    }

    public PlayerRespawnEvent() {
        super();
    }

    public void setRespawnLocation(org.bukkit.Location loc) {
        this.respawnLocation = loc;
    }

    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
