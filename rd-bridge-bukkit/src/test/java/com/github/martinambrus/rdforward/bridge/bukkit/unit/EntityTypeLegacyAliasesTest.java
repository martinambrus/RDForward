package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Pins the legacy CB-1.x {@link EntityType} aliases that pre-1.13
 * plugins still reference symbolically. Essentials's {@code /spawnmob}
 * command metadata enumerates {@code EntityType} values during command
 * registration; a single missing alias throws
 * {@link NoSuchFieldError} before any spawn happens.
 *
 * <p>The actual mob-spawn path is already a no-op via
 * {@code World.spawnEntity} returning a {@code StubEntity} proxy —
 * this test only proves the symbolic constants resolve.
 */
class EntityTypeLegacyAliasesTest {

    @Test
    void pigZombieIsPresent() {
        // Renamed to ZOMBIFIED_PIGLIN in MC 1.16.
        assertNotNull(EntityType.valueOf("PIG_ZOMBIE"));
    }

    @Test
    void mushroomCowIsPresent() {
        // Renamed to MOOSHROOM in modern API.
        assertNotNull(EntityType.valueOf("MUSHROOM_COW"));
    }

    @Test
    void snowmanIsPresent() {
        // Renamed to SNOW_GOLEM in modern API.
        assertNotNull(EntityType.valueOf("SNOWMAN"));
    }

    @Test
    void modernEquivalentsStillExist() {
        // Adding aliases must not displace the modern names — both
        // legacy and modern code paths must coexist.
        assertNotNull(EntityType.valueOf("ZOMBIFIED_PIGLIN"));
        assertNotNull(EntityType.valueOf("MOOSHROOM"));
        assertNotNull(EntityType.valueOf("SNOW_GOLEM"));
    }
}
