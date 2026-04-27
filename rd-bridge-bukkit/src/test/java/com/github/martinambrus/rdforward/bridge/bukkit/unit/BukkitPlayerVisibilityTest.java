package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitPlayer;
import com.github.martinambrus.rdforward.bridge.bukkit.PlayerVisibilityRegistry;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that {@code Player.hidePlayer} / {@code showPlayer} /
 * {@code canSee} round-trip through the registry the visibility filter
 * consults. Vanish 3.22 calls these from its toggle handler; without
 * the registry round-trip the per-recipient broadcast filter has no
 * data to act on and the vanished player still appears to viewers.
 */
class BukkitPlayerVisibilityTest {

    @BeforeEach
    void clearRegistry() {
        PlayerVisibilityRegistry.resetForTests();
    }

    @AfterEach
    void wipe() {
        BukkitPlayer.evict("alice");
        BukkitPlayer.evict("bob");
        PlayerVisibilityRegistry.resetForTests();
    }

    @Test
    void hidePlayerPopulatesRegistry() {
        Player alice = newPlayer("alice");
        Player bob = newPlayer("bob");
        alice.hidePlayer(bob);
        assertTrue(PlayerVisibilityRegistry.isHidden("alice", "bob"),
                "alice hid bob -> registry must show bob hidden from alice");
        assertFalse(PlayerVisibilityRegistry.isHidden("bob", "alice"),
                "hide is one-directional");
    }

    @Test
    void showPlayerRemovesFromRegistry() {
        Player alice = newPlayer("alice");
        Player bob = newPlayer("bob");
        alice.hidePlayer(bob);
        alice.showPlayer(bob);
        assertFalse(PlayerVisibilityRegistry.isHidden("alice", "bob"));
    }

    @Test
    void canSeeReflectsRegistry() {
        Player alice = newPlayer("alice");
        Player bob = newPlayer("bob");
        assertTrue(alice.canSee(bob), "default state: visible");
        alice.hidePlayer(bob);
        assertFalse(alice.canSee(bob));
        alice.showPlayer(bob);
        assertTrue(alice.canSee(bob));
    }

    @Test
    void evictClearsBothDirections() {
        Player alice = newPlayer("alice");
        Player bob = newPlayer("bob");
        alice.hidePlayer(bob);
        BukkitPlayer.evict("alice");
        assertFalse(PlayerVisibilityRegistry.isHidden("alice", "bob"),
                "evict must clear hidden state for the recipient");
    }

    @Test
    void evictedSenderRemovedFromOthersHiddenSets() {
        Player alice = newPlayer("alice");
        Player bob = newPlayer("bob");
        alice.hidePlayer(bob);
        BukkitPlayer.evict("bob");
        assertFalse(PlayerVisibilityRegistry.isHidden("alice", "bob"),
                "evict must also clear the sender from other recipients' sets");
    }

    @Test
    void modernPluginPlayerVariantDelegatesToLegacyForm() {
        // VanishNoPacket 3.22 calls Player.hidePlayer(Plugin, Player) — the
        // modern API shape introduced when the legacy single-arg form was
        // deprecated. Player.java's default method must delegate to the
        // legacy form so the bridge's registry update fires.
        Player alice = newPlayer("alice");
        Player bob = newPlayer("bob");
        alice.hidePlayer(null, bob);
        assertTrue(PlayerVisibilityRegistry.isHidden("alice", "bob"),
                "(Plugin, Player) overload must delegate to the bridge-intercepted single-arg form");
        alice.showPlayer(null, bob);
        assertFalse(PlayerVisibilityRegistry.isHidden("alice", "bob"));
    }

    private static Player newPlayer(String name) {
        com.github.martinambrus.rdforward.api.world.Location loc =
                new com.github.martinambrus.rdforward.api.world.Location("stub", 0, 0, 0, 0f, 0f);
        StubRdServer.StubRdPlayer backing = new StubRdServer.StubRdPlayer(name, loc);
        return BukkitPlayer.create(name, backing, null);
    }
}
