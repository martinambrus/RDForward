// @rdforward:preserve - hand-tuned facade, do not regenerate
package io.papermc.paper.event.player;

/**
 * Common base for {@link org.bukkit.event.player.PlayerRespawnEvent}.
 * EssentialsX's {@code AsyncTeleport.respawnNow} constructs the event,
 * fires it, and {@code EssentialsPlayerListener.onPlayerRespawn} reads
 * {@code event.getPlayer()} via the inherited
 * {@link org.bukkit.event.player.PlayerEvent#getPlayer()} method to look
 * up the {@code User} and update the player's compass. The previous
 * auto-generated stub forwarded {@code null} to the {@code PlayerEvent}
 * super constructor, so {@code getPlayer()} returned {@code null} and
 * {@code updateCompass} NPE'd on every {@code /home} (and any other
 * Essentials path that respawns a player).
 *
 * <p>This rewrite preserves the player reference and the location/flag
 * payload so listeners read the values the caller actually passed in.
 * The location is held in a protected mutable field so
 * {@link org.bukkit.event.player.PlayerRespawnEvent#setRespawnLocation}
 * can override it.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class AbstractRespawnEvent extends org.bukkit.event.player.PlayerEvent {

    protected org.bukkit.Location respawnLocation;
    private final boolean bedSpawn;
    private final boolean anchorSpawn;
    private final boolean missingRespawnBlock;
    private final org.bukkit.event.player.PlayerRespawnEvent$RespawnReason reason;

    protected AbstractRespawnEvent(org.bukkit.entity.Player player,
                                   org.bukkit.Location respawnLocation,
                                   boolean bedSpawn,
                                   boolean anchorSpawn,
                                   boolean missingRespawnBlock,
                                   org.bukkit.event.player.PlayerRespawnEvent$RespawnReason reason) {
        super(player);
        this.respawnLocation = respawnLocation;
        this.bedSpawn = bedSpawn;
        this.anchorSpawn = anchorSpawn;
        this.missingRespawnBlock = missingRespawnBlock;
        this.reason = reason;
    }

    protected AbstractRespawnEvent() {
        super((org.bukkit.entity.Player) null);
        this.respawnLocation = null;
        this.bedSpawn = false;
        this.anchorSpawn = false;
        this.missingRespawnBlock = false;
        this.reason = null;
    }

    public org.bukkit.Location getRespawnLocation() {
        return respawnLocation;
    }

    public boolean isBedSpawn() {
        return bedSpawn;
    }

    public boolean isAnchorSpawn() {
        return anchorSpawn;
    }

    public boolean isMissingRespawnBlock() {
        return missingRespawnBlock;
    }

    public org.bukkit.event.player.PlayerRespawnEvent$RespawnReason getRespawnReason() {
        return reason;
    }

    public java.util.Set getRespawnFlags() {
        return java.util.Collections.emptySet();
    }
}
