package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.Bukkit;
import org.bukkit.UnsafeValues;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Confirms {@code Bukkit.getUnsafe()} returns a non-null stub. EssentialsX's
 * shaded {@code BukkitComponentSerializer.<clinit>} resolves this method
 * the first time the bridge sends a chat / join message; a missing method
 * poisons the class with {@code ExceptionInInitializerError} and every
 * subsequent {@code User.sendComponent} fires
 * {@code NoClassDefFoundError: Could not initialize class
 * ...BukkitComponentSerializer}.
 */
class BukkitGetUnsafeTest {

    @Test
    void getUnsafeReturnsNonNullStub() {
        UnsafeValues unsafe = Bukkit.getUnsafe();
        assertNotNull(unsafe, "Bukkit.getUnsafe() must return non-null");
    }

    @Test
    void stubReturnsNullForObjectMethods() {
        // Adventure's fallback path null-checks each serializer return and
        // substitutes its own defaults — null is the right default.
        UnsafeValues unsafe = Bukkit.getUnsafe();
        assertNull(unsafe.legacyComponentSerializer());
        assertNull(unsafe.gsonComponentSerializer());
        assertNull(unsafe.plainTextSerializer());
        assertNull(unsafe.componentFlattener());
    }

    @Test
    void stubReturnsZeroForPrimitiveMethods() {
        UnsafeValues unsafe = Bukkit.getUnsafe();
        assertEquals(0, unsafe.getDataVersion());
        assertEquals(0, unsafe.getProtocolVersion());
        assertEquals(0, unsafe.nextEntityId());
        assertFalse(unsafe.isSupportedApiVersion("1.0"));
    }

    @Test
    void stubIsStableAcrossCalls() {
        // Same proxy instance — avoids re-initialising on every chat message.
        // Use assertSame: the proxy intercepts equals() and returns its own
        // primitive default (false), so assertEquals would always fail.
        assertSame(Bukkit.getUnsafe(), Bukkit.getUnsafe());
    }
}
