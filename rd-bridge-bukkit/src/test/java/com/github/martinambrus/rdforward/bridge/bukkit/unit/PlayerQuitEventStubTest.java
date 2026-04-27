package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.api.stub.StubCallLog;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Mirror of PlayerJoinEventStubTest for the leave broadcast.
 */
class PlayerQuitEventStubTest {

    private static final String SIG =
            "org.bukkit.event.player.PlayerQuitEvent.setQuitMessage(Ljava/lang/String;)V";

    @BeforeEach
    void clearLog() {
        StubCallLog.resetForTests();
    }

    @AfterEach
    void cleanup() {
        StubCallLog.resetForTests();
    }

    @Test
    void getQuitMessageDefaultsToEmpty() {
        PlayerQuitEvent ev = new PlayerQuitEvent(null);
        assertNotNull(ev.getQuitMessage());
        assertEquals("", ev.getQuitMessage());
    }

    @Test
    void seededCtorPropagatesDefault() {
        PlayerQuitEvent ev = new PlayerQuitEvent(null, "Zathrus left the game");
        assertEquals("Zathrus left the game", ev.getQuitMessage());
    }

    @Test
    void setQuitMessageRoundTrips() {
        PlayerQuitEvent ev = new PlayerQuitEvent(null, "default");
        ev.setQuitMessage("see you later");
        assertEquals("see you later", ev.getQuitMessage());
    }

    @Test
    void setQuitMessageNullStoresEmpty() {
        PlayerQuitEvent ev = new PlayerQuitEvent(null, "default");
        ev.setQuitMessage(null);
        assertEquals("", ev.getQuitMessage());
    }

    @Test
    void setQuitMessageDoesNotEmitStubCallLog() {
        PlayerQuitEvent ev = new PlayerQuitEvent(null);
        ev.setQuitMessage(null);
        assertFalse(StubCallLog.hasLogged(null, SIG));
    }
}
