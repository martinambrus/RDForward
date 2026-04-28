package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Both chat events expose a mutable {@code getRecipients()}: Essentials's
 * {@code /ignore} pipeline removes ignored recipients from this set,
 * which the bridge later diffs against the original online roster to
 * forward the exclusion to the chat broadcaster. A read-only or empty
 * set silently breaks {@code /ignore}.
 */
class ChatEventRecipientsTest {

    @Test
    void asyncRecipientsIsMutableAndStable() {
        AsyncPlayerChatEvent ev = new AsyncPlayerChatEvent((Player) null, "hi");
        Set<Player> r1 = ev.getRecipients();
        Set<Player> r2 = ev.getRecipients();
        assertNotNull(r1);
        assertSame(r1, r2, "getRecipients must return the same backing set");
        // Empty by default — bridge prepopulates from the live online roster.
        assertTrue(r1.isEmpty());
    }

    @Test
    void asyncRecipientsAcceptMutation() {
        AsyncPlayerChatEvent ev = new AsyncPlayerChatEvent((Player) null, "hi");
        Player p = makeStubPlayer();
        ev.getRecipients().add(p);
        assertEquals(1, ev.getRecipients().size());
        ev.getRecipients().remove(p);
        assertTrue(ev.getRecipients().isEmpty());
    }

    @Test
    void asyncRecipientsCtorSeedsFromArg() {
        Player p = makeStubPlayer();
        Set<Player> initial = new HashSet<>();
        initial.add(p);
        AsyncPlayerChatEvent ev = new AsyncPlayerChatEvent(true, null, "hi", initial);
        assertEquals(1, ev.getRecipients().size());
        assertTrue(ev.getRecipients().contains(p));
    }

    @Test
    void legacyRecipientsIsMutableAndStable() {
        PlayerChatEvent ev = new PlayerChatEvent((Player) null, "hi");
        Set<Player> r1 = ev.getRecipients();
        Set<Player> r2 = ev.getRecipients();
        assertNotNull(r1);
        assertSame(r1, r2, "legacy getRecipients must return the same backing set");
        assertTrue(r1.isEmpty());
    }

    @Test
    void legacyRecipientsAcceptMutation() {
        PlayerChatEvent ev = new PlayerChatEvent((Player) null, "hi");
        Player p = makeStubPlayer();
        ev.getRecipients().add(p);
        assertEquals(1, ev.getRecipients().size());
    }

    @Test
    void legacyCtorWithRecipientArgPopulates() {
        Player p = makeStubPlayer();
        Set<Player> initial = new HashSet<>();
        initial.add(p);
        PlayerChatEvent ev = new PlayerChatEvent(null, "hi", "<%1$s> %2$s", initial);
        assertEquals(1, ev.getRecipients().size());
        assertTrue(ev.getRecipients().contains(p));
    }

    private static Player makeStubPlayer() {
        com.github.martinambrus.rdforward.api.world.Location loc =
                new com.github.martinambrus.rdforward.api.world.Location("stub", 0, 0, 0, 0f, 0f);
        com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer.StubRdPlayer backing =
                new com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer.StubRdPlayer("chat-r-test", loc);
        return com.github.martinambrus.rdforward.bridge.bukkit.BukkitPlayer.create("chat-r-test", backing, null);
    }
}
