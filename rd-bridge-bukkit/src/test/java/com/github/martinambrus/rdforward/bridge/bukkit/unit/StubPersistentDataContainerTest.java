package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitPlayer;
import com.github.martinambrus.rdforward.bridge.bukkit.StubPersistentDataContainer;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Real Bukkit's {@link Player#getPersistentDataContainer()} never returns
 * null. VanishNoPacket calls {@code .set(NamespacedKey, type, value)}
 * inside its join handler to persist per-player vanish state. Without a
 * non-null in-memory container the listener NPEs and Vanish never
 * registers the player. The stub container round-trips writes for the
 * lifetime of the proxy (cross-restart persistence is out of scope) and
 * exposes a stable instance per proxy so writes performed in
 * onPlayerJoinEarly remain visible in onPlayerJoinLate.
 */
class StubPersistentDataContainerTest {

    @AfterEach
    void wipeCache() {
        BukkitPlayer.evict("pdc-test");
    }

    @Test
    void getPersistentDataContainerNonNull() {
        Player p = newPlayer();
        PersistentDataContainer pdc = p.getPersistentDataContainer();
        assertNotNull(pdc, "PDC must never be null — plugins call .set() unconditionally");
        assertTrue(pdc.isEmpty());
        assertEquals(0, pdc.getSize());
    }

    @Test
    void containerInstanceIsStableAcrossCalls() {
        Player p = newPlayer();
        // Both join handlers must observe the same container so writes
        // performed in onPlayerJoinEarly are visible in onPlayerJoinLate.
        assertSame(p.getPersistentDataContainer(), p.getPersistentDataContainer());
    }

    @Test
    void setRoundTripsThroughGet() {
        Player p = newPlayer();
        PersistentDataContainer pdc = p.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey("vanish", "state");
        pdc.set(key, null, "hidden");
        assertTrue(pdc.has(key));
        assertEquals("hidden", pdc.get(key, null));
        assertFalse(pdc.isEmpty());
        assertEquals(1, pdc.getSize());
        assertTrue(pdc.getKeys().contains(key));
    }

    @Test
    void getOrDefaultUsesFallbackWhenAbsent() {
        StubPersistentDataContainer pdc = new StubPersistentDataContainer();
        NamespacedKey key = new NamespacedKey("rd", "missing");
        assertEquals("fallback", pdc.getOrDefault(key, null, "fallback"));
        pdc.set(key, null, "stored");
        assertEquals("stored", pdc.getOrDefault(key, null, "fallback"));
    }

    @Test
    void removeDropsEntry() {
        StubPersistentDataContainer pdc = new StubPersistentDataContainer();
        NamespacedKey key = new NamespacedKey("rd", "drop");
        pdc.set(key, null, 42);
        pdc.remove(key);
        assertFalse(pdc.has(key));
        assertNull(pdc.get(key, null));
    }

    @Test
    void copyToReplacesOrPreservesByFlag() {
        StubPersistentDataContainer src = new StubPersistentDataContainer();
        StubPersistentDataContainer dst = new StubPersistentDataContainer();
        NamespacedKey key = new NamespacedKey("rd", "k");
        src.set(key, null, "src");
        dst.set(key, null, "dst");
        src.copyTo(dst, false);
        assertEquals("dst", dst.get(key, null), "replace=false must preserve dst entries");
        src.copyTo(dst, true);
        assertEquals("src", dst.get(key, null), "replace=true must overwrite dst entries");
    }

    @Test
    void serializeToBytesReturnsEmptyNonNull() {
        StubPersistentDataContainer pdc = new StubPersistentDataContainer();
        byte[] bytes = pdc.serializeToBytes();
        assertNotNull(bytes);
        assertEquals(0, bytes.length);
    }

    @Test
    void getAdapterContextNonNull() {
        StubPersistentDataContainer pdc = new StubPersistentDataContainer();
        assertNotNull(pdc.getAdapterContext());
        assertNotNull(pdc.getAdapterContext().newPersistentDataContainer());
    }

    private static Player newPlayer() {
        com.github.martinambrus.rdforward.api.world.Location loc =
                new com.github.martinambrus.rdforward.api.world.Location("stub", 0, 0, 0, 0f, 0f);
        StubRdServer.StubRdPlayer backing = new StubRdServer.StubRdPlayer("pdc-test", loc);
        return BukkitPlayer.create("pdc-test", backing, null);
    }
}
