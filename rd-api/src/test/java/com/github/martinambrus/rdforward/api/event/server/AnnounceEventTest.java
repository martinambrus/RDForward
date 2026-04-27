package com.github.martinambrus.rdforward.api.event.server;

import com.github.martinambrus.rdforward.api.version.ProtocolVersion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Verifies the chained-listener semantics for
 * {@link ServerEvents#PLAYER_JOIN_ANNOUNCE} and
 * {@link ServerEvents#PLAYER_LEAVE_ANNOUNCE}: each listener sees the
 * message returned by the previous one and the final return value is
 * what the host uses for the broadcast (or skips entirely on null/empty).
 * The Bukkit bridge depends on this contract — a single PJE-dispatching
 * listener returns the rewritten {@code joinMessage} and rd-server's
 * {@code PlayerManager.announceJoinBroadcast} suppresses the broadcast
 * when the chain ends in an empty string.
 */
class AnnounceEventTest {

    @BeforeEach
    void clearListeners() {
        ServerEvents.PLAYER_JOIN_ANNOUNCE.clearListeners();
        ServerEvents.PLAYER_LEAVE_ANNOUNCE.clearListeners();
    }

    @AfterEach
    void cleanup() {
        ServerEvents.PLAYER_JOIN_ANNOUNCE.clearListeners();
        ServerEvents.PLAYER_LEAVE_ANNOUNCE.clearListeners();
    }

    @Test
    void noListenersReturnsDefault() {
        String result = ServerEvents.PLAYER_JOIN_ANNOUNCE.invoker()
                .onAnnounce("Z", (ProtocolVersion) null, "Z joined the game");
        assertEquals("Z joined the game", result);
    }

    @Test
    void singleListenerCanSuppressByReturningEmpty() {
        ServerEvents.PLAYER_JOIN_ANNOUNCE.register((name, v, defaultMsg) -> "");
        String result = ServerEvents.PLAYER_JOIN_ANNOUNCE.invoker()
                .onAnnounce("Z", (ProtocolVersion) null, "Z joined the game");
        assertEquals("", result);
    }

    @Test
    void singleListenerCanRewriteMessage() {
        ServerEvents.PLAYER_JOIN_ANNOUNCE.register((name, v, defaultMsg) -> "*** " + name + " arrived ***");
        String result = ServerEvents.PLAYER_JOIN_ANNOUNCE.invoker()
                .onAnnounce("Z", (ProtocolVersion) null, "Z joined the game");
        assertEquals("*** Z arrived ***", result);
    }

    @Test
    void chainedListenersThreadMessageThrough() {
        ServerEvents.PLAYER_JOIN_ANNOUNCE.register((name, v, defaultMsg) -> "[v1] " + defaultMsg);
        ServerEvents.PLAYER_JOIN_ANNOUNCE.register((name, v, defaultMsg) -> defaultMsg + " [v2]");
        String result = ServerEvents.PLAYER_JOIN_ANNOUNCE.invoker()
                .onAnnounce("Z", (ProtocolVersion) null, "joined");
        assertEquals("[v1] joined [v2]", result);
    }

    @Test
    void chainedListenerCanNullOutMessage() {
        ServerEvents.PLAYER_JOIN_ANNOUNCE.register((name, v, defaultMsg) -> "interim");
        ServerEvents.PLAYER_JOIN_ANNOUNCE.register((name, v, defaultMsg) -> null);
        String result = ServerEvents.PLAYER_JOIN_ANNOUNCE.invoker()
                .onAnnounce("Z", (ProtocolVersion) null, "joined");
        assertNull(result, "null at any point should propagate as the final return");
    }

    @Test
    void leaveAnnounceChainsLikeJoin() {
        ServerEvents.PLAYER_LEAVE_ANNOUNCE.register((name, defaultMsg) -> defaultMsg + " (rd)");
        ServerEvents.PLAYER_LEAVE_ANNOUNCE.register((name, defaultMsg) -> "[" + name + "] " + defaultMsg);
        String result = ServerEvents.PLAYER_LEAVE_ANNOUNCE.invoker()
                .onAnnounce("Z", "Z left the game");
        assertEquals("[Z] Z left the game (rd)", result);
    }

    @Test
    void leaveAnnounceSuppressesOnEmpty() {
        ServerEvents.PLAYER_LEAVE_ANNOUNCE.register((name, defaultMsg) -> "");
        String result = ServerEvents.PLAYER_LEAVE_ANNOUNCE.invoker()
                .onAnnounce("Z", "Z left the game");
        assertEquals("", result);
    }

    @Test
    void clearAllResetsAnnounceListeners() {
        ServerEvents.PLAYER_JOIN_ANNOUNCE.register((name, v, defaultMsg) -> "x");
        ServerEvents.PLAYER_LEAVE_ANNOUNCE.register((name, defaultMsg) -> "x");
        ServerEvents.clearAll();
        assertEquals("default", ServerEvents.PLAYER_JOIN_ANNOUNCE.invoker()
                .onAnnounce("Z", (ProtocolVersion) null, "default"));
        assertEquals("default", ServerEvents.PLAYER_LEAVE_ANNOUNCE.invoker()
                .onAnnounce("Z", "default"));
    }
}
