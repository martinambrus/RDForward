package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.OfflinePlayer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Pins the UUID-keyed offline-player stub used by the new
 * {@code Server.getOfflinePlayer(UUID)} default. Essentials's
 * {@code com.earth2me.essentials.OfflinePlayer.<init>(UUID,Server)}
 * calls the UUID overload for every {@code UserMap} cache miss; pre-fix,
 * the overload did not exist and {@code /balancetop} threw
 * {@link NoSuchMethodError} mid-tick.
 *
 * <p>Reaches into {@code ServerSupport.offlinePlayerStubByUuid} directly
 * — same factory the production {@code Server} default invokes — so the
 * proxy contract is exercised without spinning up the full Server stub.
 */
class OfflinePlayerByUuidTest {

    @Test
    void byUuidStubCarriesUuidAndNullName() {
        UUID id = UUID.fromString("12345678-1234-5678-1234-567812345678");
        OfflinePlayer offline = invokeStubByUuid(id);
        assertNotNull(offline);
        assertEquals(id, offline.getUniqueId());
        // Real Bukkit returns a null name when the UUID has never been
        // seen on the server. Match that contract — Essentials handles
        // the null path, real-Bukkit does too, and we have no UUID-name
        // cache to consult.
        assertNull(offline.getName());
    }

    @Test
    void byUuidStubReportsOffline() {
        UUID id = UUID.randomUUID();
        OfflinePlayer offline = invokeStubByUuid(id);
        assertFalse(offline.isOnline());
        assertFalse(offline.isConnected());
    }

    @Test
    void byUuidStubReturnsNullPlayer() {
        // OfflinePlayer.getPlayer() returns null when not online; the stub
        // mirrors that — Essentials reads it through this path during
        // /balancetop iteration.
        OfflinePlayer offline = invokeStubByUuid(UUID.randomUUID());
        assertNull(offline.getPlayer());
    }

    private static OfflinePlayer invokeStubByUuid(UUID id) {
        try {
            Class<?> support = Class.forName("org.bukkit.ServerSupport");
            Method m = support.getDeclaredMethod("offlinePlayerStubByUuid", UUID.class);
            m.setAccessible(true);
            return (OfflinePlayer) m.invoke(null, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
