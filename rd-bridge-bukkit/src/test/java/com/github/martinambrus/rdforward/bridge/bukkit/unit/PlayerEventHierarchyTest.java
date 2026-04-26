package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pinning regression test for LoginSecurity, whose {@code PlayerListener}
 * compiles against the Bukkit contract {@code AsyncPlayerChatEvent
 * extends PlayerEvent} and ships bytecode that hands the event to a
 * helper method typed against {@code PlayerEvent}. The previous stub
 * extended {@code Event} directly, causing the JVM verifier to reject
 * the call with {@code VerifyError: Bad type on operand stack}.
 *
 * <p>The {@link PlayerEvent#getPlayer()} accessor is {@code final} in
 * paper-api, so the subclass cannot override it; this test also pins the
 * round-trip from {@code super(player)} to {@code getPlayer()} so the
 * carried-player contract isn't lost.
 */
class PlayerEventHierarchyTest {

    @Test
    void asyncChatEventIsAPlayerEvent() {
        AsyncPlayerChatEvent ev = new AsyncPlayerChatEvent(com.github.martinambrus.rdforward.bridge.bukkit.BukkitPlayer.create("alice"), "hi");
        assertTrue(ev instanceof PlayerEvent,
                "AsyncPlayerChatEvent must extend PlayerEvent — required by plugin verifiers");
    }

    @Test
    void getPlayerRoundTripsThroughPlayerEvent() {
        Player p = com.github.martinambrus.rdforward.bridge.bukkit.BukkitPlayer.create("alice");
        AsyncPlayerChatEvent ev = new AsyncPlayerChatEvent(p, "hi");
        assertSame(p, ev.getPlayer(),
                "PlayerEvent.getPlayer() must return the player passed to the subclass ctor");
    }

    @Test
    void asyncChatMessageRoundTrips() {
        AsyncPlayerChatEvent ev = new AsyncPlayerChatEvent(com.github.martinambrus.rdforward.bridge.bukkit.BukkitPlayer.create("alice"), "hello");
        assertEquals("hello", ev.getMessage());
        ev.setMessage("world");
        assertEquals("world", ev.getMessage());
    }
}
