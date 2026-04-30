package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerRespawnEvent$RespawnReason;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EssentialsX 2.21.2's {@code AsyncTeleport.respawnNow} fires {@code
 * new PlayerRespawnEvent(player, spawnLoc, false)} and the listener's
 * {@code onPlayerRespawn} reads {@code event.getPlayer()} to look up
 * the user before calling {@code updateCompass}. The auto-generated
 * stub forwarded {@code null} to the {@link PlayerEvent} super-ctor,
 * so the player reference was lost — every {@code /home} hit a NPE
 * inside the listener.
 *
 * <p>Pin the constructor → accessor round-trip across all four
 * overloads so any future regen of these stubs gets caught.
 */
class PlayerRespawnEventTest {

    @Test
    void threeArgCtorPreservesPlayerLocationAndBedFlag() {
        Player p = BukkitPlayer.create("alice");
        Location loc = new Location(null, 1, 2, 3);
        PlayerRespawnEvent ev = new PlayerRespawnEvent(p, loc, true);
        assertSame(p, ev.getPlayer(),
                "player passed to ctor must be visible via PlayerEvent.getPlayer()");
        assertSame(loc, ev.getRespawnLocation());
        assertTrue(ev.isBedSpawn());
        assertFalse(ev.isAnchorSpawn());
        assertFalse(ev.isMissingRespawnBlock());
        assertNull(ev.getRespawnReason());
    }

    @Test
    void fourArgCtorPreservesAnchorFlag() {
        Player p = BukkitPlayer.create("bob");
        Location loc = new Location(null, 4, 5, 6);
        PlayerRespawnEvent ev = new PlayerRespawnEvent(p, loc, false, true);
        assertSame(p, ev.getPlayer());
        assertSame(loc, ev.getRespawnLocation());
        assertFalse(ev.isBedSpawn());
        assertTrue(ev.isAnchorSpawn());
    }

    @Test
    void sixArgCtorPreservesAllFlagsAndReason() {
        Player p = BukkitPlayer.create("carol");
        Location loc = new Location(null, 7, 8, 9);
        PlayerRespawnEvent ev = new PlayerRespawnEvent(
                p, loc, false, false, true, PlayerRespawnEvent$RespawnReason.PLUGIN);
        assertSame(p, ev.getPlayer());
        assertSame(loc, ev.getRespawnLocation());
        assertTrue(ev.isMissingRespawnBlock());
        assertSame(PlayerRespawnEvent$RespawnReason.PLUGIN, ev.getRespawnReason());
    }

    @Test
    void setRespawnLocationOverwritesField() {
        Player p = BukkitPlayer.create("dave");
        Location original = new Location(null, 0, 0, 0);
        Location replacement = new Location(null, 100, 64, 100);
        PlayerRespawnEvent ev = new PlayerRespawnEvent(p, original, false);
        assertSame(original, ev.getRespawnLocation());
        ev.setRespawnLocation(replacement);
        assertSame(replacement, ev.getRespawnLocation(),
                "listeners that mutate the location before respawn() runs must take effect");
    }
}
