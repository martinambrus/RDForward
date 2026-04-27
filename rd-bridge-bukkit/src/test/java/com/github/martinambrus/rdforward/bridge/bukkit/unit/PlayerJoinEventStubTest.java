package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.api.stub.StubCallLog;
import org.bukkit.event.player.PlayerJoinEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Vanish-style "silent join" plugins call
 * {@link PlayerJoinEvent#setJoinMessage(String)} to suppress the
 * broadcast. The setter no longer routes through {@link StubCallLog}
 * because the override is now actually honoured: the bridge reads the
 * message back from {@code PLAYER_JOIN_ANNOUNCE} and rd-server skips
 * the broadcast when the result is empty. The setter must therefore
 * round-trip cleanly without emitting "unsupported in RDForward"
 * warnings.
 */
class PlayerJoinEventStubTest {

    private static final String SIG =
            "org.bukkit.event.player.PlayerJoinEvent.setJoinMessage(Ljava/lang/String;)V";

    @BeforeEach
    void clearLog() {
        StubCallLog.resetForTests();
    }

    @AfterEach
    void cleanup() {
        StubCallLog.resetForTests();
    }

    @Test
    void getJoinMessageDefaultsToEmpty() {
        PlayerJoinEvent ev = new PlayerJoinEvent(null);
        assertNotNull(ev.getJoinMessage());
        assertEquals("", ev.getJoinMessage());
    }

    @Test
    void seededCtorPropagatesDefault() {
        PlayerJoinEvent ev = new PlayerJoinEvent(null, "Zathrus joined the game");
        assertEquals("Zathrus joined the game", ev.getJoinMessage());
    }

    @Test
    void seededCtorNullStoresEmpty() {
        PlayerJoinEvent ev = new PlayerJoinEvent(null, null);
        assertEquals("", ev.getJoinMessage());
    }

    @Test
    void setJoinMessageRoundTrips() {
        PlayerJoinEvent ev = new PlayerJoinEvent(null, "default");
        ev.setJoinMessage("welcome back");
        assertEquals("welcome back", ev.getJoinMessage());
    }

    @Test
    void setJoinMessageNullStoresEmpty() {
        PlayerJoinEvent ev = new PlayerJoinEvent(null, "default");
        ev.setJoinMessage(null);
        assertEquals("", ev.getJoinMessage());
    }

    @Test
    void setJoinMessageDoesNotEmitStubCallLog() {
        PlayerJoinEvent ev = new PlayerJoinEvent(null);
        ev.setJoinMessage("hidden");
        assertFalse(StubCallLog.hasLogged(null, SIG),
                "setJoinMessage is now honoured by the host — no StubCallLog warning expected");
    }
}
