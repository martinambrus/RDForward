package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pins {@link ConnectedPlayer#setFlySpeed} / {@link ConnectedPlayer#setWalkSpeed}
 * range validation and stored-value semantics. Does NOT exercise the
 * dispatcher (channel is null in this fixture, so the no-op dispatcher
 * resolves and {@code sendPacket} short-circuits).
 */
class ConnectedPlayerSpeedTest {

    private static ConnectedPlayer newPlayer(ProtocolVersion v) {
        return new ConnectedPlayer((byte) 1, "tester", "00000000-0000-0000-0000-000000000001", null, v);
    }

    @Test
    void defaultsMatchBukkitVanilla() {
        ConnectedPlayer p = newPlayer(ProtocolVersion.RELEASE_1_21_5);
        // Bukkit's API defaults — half of these are sent on the wire.
        assertEquals(0.1f, p.getFlySpeed(), 1e-6f);
        assertEquals(0.2f, p.getWalkSpeed(), 1e-6f);
    }

    @Test
    void setFlySpeedStoresAndReadsBack() {
        ConnectedPlayer p = newPlayer(ProtocolVersion.RELEASE_1_21_5);
        p.setFlySpeed(0.4f);
        assertEquals(0.4f, p.getFlySpeed(), 1e-6f);
    }

    @Test
    void setWalkSpeedStoresAndReadsBack() {
        ConnectedPlayer p = newPlayer(ProtocolVersion.RELEASE_1_21_5);
        p.setWalkSpeed(0.3f);
        assertEquals(0.3f, p.getWalkSpeed(), 1e-6f);
    }

    @Test
    void outOfRangeFlySpeedThrows() {
        ConnectedPlayer p = newPlayer(ProtocolVersion.RELEASE_1_21_5);
        assertThrows(IllegalArgumentException.class, () -> p.setFlySpeed(1.5f));
        assertThrows(IllegalArgumentException.class, () -> p.setFlySpeed(-1.5f));
    }

    @Test
    void outOfRangeWalkSpeedThrows() {
        ConnectedPlayer p = newPlayer(ProtocolVersion.RELEASE_1_21_5);
        assertThrows(IllegalArgumentException.class, () -> p.setWalkSpeed(1.5f));
        assertThrows(IllegalArgumentException.class, () -> p.setWalkSpeed(-1.5f));
    }

    @Test
    void boundaryValuesAccepted() {
        ConnectedPlayer p = newPlayer(ProtocolVersion.RELEASE_1_21_5);
        p.setFlySpeed(1.0f);
        assertEquals(1.0f, p.getFlySpeed(), 1e-6f);
        p.setFlySpeed(-1.0f);
        assertEquals(-1.0f, p.getFlySpeed(), 1e-6f);
        p.setWalkSpeed(1.0f);
        assertEquals(1.0f, p.getWalkSpeed(), 1e-6f);
        p.setWalkSpeed(-1.0f);
        assertEquals(-1.0f, p.getWalkSpeed(), 1e-6f);
    }
}
