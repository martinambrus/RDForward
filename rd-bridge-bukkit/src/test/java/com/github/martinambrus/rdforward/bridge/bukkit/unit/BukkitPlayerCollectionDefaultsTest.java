package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitPlayer;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Real Bukkit's collection-returning Entity/Player methods never return
 * null. VanishNoPacket's join handler calls
 * {@code Player.getNearbyEntities(...).iterator()} unconditionally, so a
 * null List default crashed the listener and skipped per-player setup.
 * The same shape applies to other collection getters reflectively
 * dispatched through the generated proxy. Empty collections satisfy the
 * iteration contract without requiring a real entity backing.
 */
class BukkitPlayerCollectionDefaultsTest {

    @AfterEach
    void wipeCache() {
        BukkitPlayer.evict("coll-test");
    }

    @Test
    void getNearbyEntitiesReturnsEmptyList() {
        Player p = newPlayer();
        java.util.List<org.bukkit.entity.Entity> near = p.getNearbyEntities(1.0, 1.0, 1.0);
        assertNotNull(near, "getNearbyEntities must never return null");
        assertTrue(near.isEmpty(), "no real entity backing -> empty list");
        // Iteration must succeed (this is the exact pattern Vanish uses).
        for (org.bukkit.entity.Entity ignored : near) {
            // unreachable: list is empty
        }
    }

    @Test
    void getPassengersReturnsEmptyList() {
        Player p = newPlayer();
        java.util.List<org.bukkit.entity.Entity> passengers = p.getPassengers();
        assertNotNull(passengers);
        assertTrue(passengers.isEmpty());
    }

    @Test
    void getTrackedBySetIsNonNullEmpty() {
        Player p = newPlayer();
        java.util.Set<?> tracked = p.getTrackedBy();
        assertNotNull(tracked, "Set-returning stubs must never return null");
        assertTrue(tracked.isEmpty());
    }

    @Test
    void getScoreboardTagsReturnsEmptySet() {
        Player p = newPlayer();
        java.util.Set<String> tags = p.getScoreboardTags();
        assertNotNull(tags);
        assertTrue(tags.isEmpty());
    }

    private static Player newPlayer() {
        com.github.martinambrus.rdforward.api.world.Location loc =
                new com.github.martinambrus.rdforward.api.world.Location("stub", 0, 0, 0, 0f, 0f);
        StubRdServer.StubRdPlayer backing = new StubRdServer.StubRdPlayer("coll-test", loc);
        return BukkitPlayer.create("coll-test", backing, null);
    }
}
