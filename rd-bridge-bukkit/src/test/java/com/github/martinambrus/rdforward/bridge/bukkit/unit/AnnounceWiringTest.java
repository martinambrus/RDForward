package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.api.version.ProtocolVersion;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitEventAdapter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * End-to-end wiring check for the new announce events.
 *
 * <p>The bridge must thread the host's default broadcast message into
 * the seeded {@code PlayerJoinEvent} ctor, dispatch the event to plugin
 * listeners, then return whatever {@code joinMessage} the listeners left
 * behind. {@code rd-server.PlayerManager.announceJoinBroadcast} relies
 * on this so a Vanish-style {@code setJoinMessage("")} call actually
 * suppresses the in-game broadcast. Same shape for quit.
 */
class AnnounceWiringTest {

    @BeforeEach
    void clear() {
        ServerEvents.clearAll();
        BukkitEventAdapter.clearAll();
    }

    @AfterEach
    void clearAfter() {
        ServerEvents.clearAll();
        BukkitEventAdapter.clearAll();
    }

    static final class MessageRewritingListener implements Listener {
        @EventHandler
        public void onJoin(PlayerJoinEvent e) {
            // Confirm the bridge seeded the event with the host's default.
            assertNotNull(e.getJoinMessage());
            e.setJoinMessage("*** " + e.getJoinMessage() + " ***");
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent e) {
            assertNotNull(e.getQuitMessage());
            e.setQuitMessage("");
        }
    }

    @Test
    void setJoinMessageRewritePropagatesThroughAnnounce() {
        BukkitEventAdapter.register(new MessageRewritingListener(), "test");
        String result = ServerEvents.PLAYER_JOIN_ANNOUNCE.invoker()
                .onAnnounce("Z", (ProtocolVersion) null, "Z joined the game");
        assertEquals("*** Z joined the game ***", result);
    }

    @Test
    void emptyQuitMessageSuppressesBroadcast() {
        BukkitEventAdapter.register(new MessageRewritingListener(), "test");
        String result = ServerEvents.PLAYER_LEAVE_ANNOUNCE.invoker()
                .onAnnounce("Z", "Z left the game");
        assertEquals("", result, "PlayerManager.announceLeaveBroadcast skips broadcast on empty");
    }

    static final class SilentVanishListener implements Listener {
        @EventHandler
        public void onJoin(PlayerJoinEvent e) {
            e.setJoinMessage("");
        }
    }

    @Test
    void vanishSilentJoinSuppresses() {
        BukkitEventAdapter.register(new SilentVanishListener(), "vanish");
        String result = ServerEvents.PLAYER_JOIN_ANNOUNCE.invoker()
                .onAnnounce("Z", (ProtocolVersion) null, "Z joined the game");
        assertEquals("", result);
    }

    static final class NoMessageListener implements Listener {
        @EventHandler
        public void onJoin(PlayerJoinEvent e) {
            // Do nothing — default message must pass through unchanged.
        }
    }

    @Test
    void listenerThatLeavesMessageAloneReturnsDefault() {
        BukkitEventAdapter.register(new NoMessageListener(), "test");
        String result = ServerEvents.PLAYER_JOIN_ANNOUNCE.invoker()
                .onAnnounce("Z", (ProtocolVersion) null, "Z joined the game");
        assertEquals("Z joined the game", result);
    }
}
