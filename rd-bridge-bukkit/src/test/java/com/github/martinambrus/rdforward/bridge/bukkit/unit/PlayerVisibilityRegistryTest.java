package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.PlayerVisibilityRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The hidden-set used by {@code Player.hidePlayer/showPlayer} feeds the
 * rd-api {@code PlayerVisibilityFilter} the bridge installs on boot.
 * Names must match case-insensitively (Bukkit's identity convention)
 * and must not leak across reconnects (the bridge clears entries on
 * quit so a rejoin starts fresh).
 */
class PlayerVisibilityRegistryTest {

    @BeforeEach
    void clearBefore() {
        PlayerVisibilityRegistry.resetForTests();
    }

    @AfterEach
    void clearAfter() {
        PlayerVisibilityRegistry.resetForTests();
    }

    @Test
    void hideMakesIsHiddenTrue() {
        PlayerVisibilityRegistry.hide("Alice", "Bob");
        assertTrue(PlayerVisibilityRegistry.isHidden("Alice", "Bob"));
        assertFalse(PlayerVisibilityRegistry.isHidden("Bob", "Alice"),
                "hide is per-pair, not bidirectional");
    }

    @Test
    void caseInsensitiveLookup() {
        PlayerVisibilityRegistry.hide("ALICE", "bob");
        assertTrue(PlayerVisibilityRegistry.isHidden("alice", "BOB"));
    }

    @Test
    void selfHideIsRejected() {
        PlayerVisibilityRegistry.hide("Alice", "Alice");
        assertFalse(PlayerVisibilityRegistry.isHidden("Alice", "Alice"),
                "a player can't hide themselves from themselves");
    }

    @Test
    void nullInputsAreSafe() {
        PlayerVisibilityRegistry.hide(null, "Bob");
        PlayerVisibilityRegistry.hide("Alice", null);
        assertFalse(PlayerVisibilityRegistry.isHidden(null, "Bob"));
        assertFalse(PlayerVisibilityRegistry.isHidden("Alice", null));
    }

    @Test
    void showRemovesPair() {
        PlayerVisibilityRegistry.hide("Alice", "Bob");
        assertTrue(PlayerVisibilityRegistry.show("Alice", "Bob"));
        assertFalse(PlayerVisibilityRegistry.isHidden("Alice", "Bob"));
    }

    @Test
    void showOnNonHiddenReturnsFalse() {
        assertFalse(PlayerVisibilityRegistry.show("Alice", "Bob"));
    }

    @Test
    void clearRecipientDropsAllSendersHiddenForRecipient() {
        PlayerVisibilityRegistry.hide("Alice", "Bob");
        PlayerVisibilityRegistry.hide("Alice", "Carol");
        PlayerVisibilityRegistry.hide("Dave", "Bob");
        PlayerVisibilityRegistry.clearRecipient("Alice");
        assertFalse(PlayerVisibilityRegistry.isHidden("Alice", "Bob"));
        assertFalse(PlayerVisibilityRegistry.isHidden("Alice", "Carol"));
        assertTrue(PlayerVisibilityRegistry.isHidden("Dave", "Bob"),
                "clearRecipient only affects the named recipient");
    }

    @Test
    void clearSenderRemovesSenderFromAllRecipients() {
        PlayerVisibilityRegistry.hide("Alice", "Bob");
        PlayerVisibilityRegistry.hide("Carol", "Bob");
        PlayerVisibilityRegistry.hide("Alice", "Dave");
        PlayerVisibilityRegistry.clearSender("Bob");
        assertFalse(PlayerVisibilityRegistry.isHidden("Alice", "Bob"));
        assertFalse(PlayerVisibilityRegistry.isHidden("Carol", "Bob"));
        assertTrue(PlayerVisibilityRegistry.isHidden("Alice", "Dave"),
                "other senders untouched");
    }

    @Test
    void hiddenForSnapshotIsImmutable() {
        PlayerVisibilityRegistry.hide("Alice", "Bob");
        PlayerVisibilityRegistry.hide("Alice", "Carol");
        java.util.Set<String> snapshot = PlayerVisibilityRegistry.hiddenFor("Alice");
        assertTrue(snapshot.contains("bob"));
        assertTrue(snapshot.contains("carol"));
        try {
            snapshot.add("dave");
            org.junit.jupiter.api.Assertions.fail("snapshot must be immutable");
        } catch (UnsupportedOperationException expected) {
            // ok
        }
    }
}
