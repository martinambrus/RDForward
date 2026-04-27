package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.StubEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Smoke test for {@link StubEntity} — VanishNoPacket calls
 * {@code spawnEntity(...).getUniqueId()} per /vanish toggle so the
 * proxy MUST return a non-null UUID, and the same UUID across repeated
 * calls (Vanish stores it in a Set then later checks contains).
 */
class StubEntityTest {

    @Test
    void getUniqueIdReturnsNonNull() {
        Entity stub = StubEntity.create(EntityType.BAT, null);
        assertNotNull(stub);
        UUID uuid = stub.getUniqueId();
        assertNotNull(uuid);
    }

    @Test
    void sameStubReturnsSameUuidAcrossCalls() {
        Entity stub = StubEntity.create(EntityType.BAT, null);
        UUID first = stub.getUniqueId();
        UUID second = stub.getUniqueId();
        assertSame(first, second, "Stub UUID must be cached so Set lookups match");
    }

    @Test
    void distinctStubsGetDistinctUuids() {
        Entity a = StubEntity.create(EntityType.BAT, null);
        Entity b = StubEntity.create(EntityType.BAT, null);
        assertNotSame(a.getUniqueId(), b.getUniqueId());
    }

    @Test
    void uuidUsableAsHashSetKey() {
        Set<UUID> bats = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            bats.add(StubEntity.create(EntityType.BAT, null).getUniqueId());
        }
        assertEquals(10, bats.size(), "Each stub must yield a distinct UUID for HashSet");
    }

    @Test
    void getTypeReturnsRequestedType() {
        Entity stub = StubEntity.create(EntityType.BAT, null);
        assertEquals(EntityType.BAT, stub.getType());
    }

    @Test
    void unhandledMethodReturnsSafeDefault() {
        Entity stub = StubEntity.create(EntityType.BAT, null);
        assertFalse(stub.isValid());
        assertTrue(stub.isDead());
        assertFalse(stub.isOnGround());
    }

    @Test
    void voidMethodIsNoOp() {
        Entity stub = StubEntity.create(EntityType.BAT, null);
        stub.remove();
        // Reaching this line means no exception was thrown.
    }

    @Test
    void getWorldReturnsNullWhenLocationIsNull() {
        Entity stub = StubEntity.create(EntityType.BAT, null);
        assertNull(stub.getWorld());
    }
}
