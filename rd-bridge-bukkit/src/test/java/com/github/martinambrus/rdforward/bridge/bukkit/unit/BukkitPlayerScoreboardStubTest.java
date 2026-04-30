package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitPlayer;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EssentialsChat's lowest-priority listener does
 * {@code player.getScoreboard().getPlayerTeam(player)} on every chat
 * message. Real Bukkit always hands plugins a non-null scoreboard
 * (the main scoreboard if no per-player one is set), so a null here
 * NPEs the whole chat pipeline.
 *
 * <p>The {@code BukkitPlayer} stub now returns a shared no-op
 * {@link Scoreboard} proxy: lookups return null (semantically "not
 * registered"), collection accessors return empty Sets/Lists, and
 * the proxy reference is stable across calls so plugins can keep
 * an instance reference without it going stale.
 */
class BukkitPlayerScoreboardStubTest {

    @AfterEach
    void wipeCache() {
        BukkitPlayer.evict("scoreboard-test");
    }

    @Test
    void getScoreboardIsNonNull() {
        Player p = newPlayer();
        assertNotNull(p.getScoreboard(),
                "Player.getScoreboard must never return null — chat dispatch dereferences it unconditionally");
    }

    @Test
    void scoreboardReferenceIsStableAcrossCalls() {
        Player p = newPlayer();
        // Plugins (EssentialsChat) often grab the scoreboard once
        // per event and pass it around — if every call minted a
        // new proxy, identity-keyed caches in plugin code would
        // break.
        assertSame(p.getScoreboard(), p.getScoreboard());
    }

    @Test
    void getPlayerTeamReturnsNullForUnaffiliatedPlayer() {
        Player p = newPlayer();
        Scoreboard sb = p.getScoreboard();
        // Real Bukkit returns null when the queried player is on no
        // team. EssentialsChat checks for null before formatting.
        assertNull(sb.getPlayerTeam(p));
        assertNull(sb.getEntryTeam(p.getName()));
        assertNull(sb.getTeam("anything"));
    }

    @Test
    void collectionAccessorsReturnEmptyContainers() {
        // Empty (non-null) collection results match real Bukkit's
        // contract for "no objectives / teams / entries registered"
        // and let plugin code iterate without a null guard.
        Player p = newPlayer();
        Scoreboard sb = p.getScoreboard();
        assertNotNull(sb.getTeams());
        assertTrue(sb.getTeams().isEmpty());
        assertNotNull(sb.getObjectives());
        assertTrue(sb.getObjectives().isEmpty());
        assertNotNull(sb.getEntries());
        assertTrue(sb.getEntries().isEmpty());
    }

    @Test
    void setScoreboardIsAcceptedSilently() {
        // Bukkit allows {@code Player.setScoreboard(otherBoard)} to
        // swap the active board. RDForward has no real scoreboard
        // backing, so the call is a no-op — but it MUST NOT throw.
        Player p = newPlayer();
        p.setScoreboard(p.getScoreboard());
    }

    private static Player newPlayer() {
        com.github.martinambrus.rdforward.api.world.Location loc =
                new com.github.martinambrus.rdforward.api.world.Location("stub", 0, 0, 0, 0f, 0f);
        StubRdServer.StubRdPlayer backing = new StubRdServer.StubRdPlayer("scoreboard-test", loc);
        return BukkitPlayer.create("scoreboard-test", backing, null);
    }
}
