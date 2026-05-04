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

    @Test
    void boatAliasIsPresent() {
        // Renamed to per-wood OAK_BOAT / BIRCH_BOAT / ... in modern API.
        // Essentials Pre-2.14's Mob.<clinit> reads BOAT directly.
        assertNotNull(EntityType.valueOf("BOAT"));
    }

    @Test
    void enderCrystalAliasIsPresent() {
        // Renamed to END_CRYSTAL in modern API.
        assertNotNull(EntityType.valueOf("ENDER_CRYSTAL"));
    }

    @Test
    void minecartLegacyAliasesArePresent() {
        // Pre-1.13 names: MINECART_CHEST etc. — modern names append
        // _MINECART (CHEST_MINECART, FURNACE_MINECART, ...).
        assertNotNull(EntityType.valueOf("MINECART_CHEST"));
        assertNotNull(EntityType.valueOf("MINECART_FURNACE"));
        assertNotNull(EntityType.valueOf("MINECART_HOPPER"));
        assertNotNull(EntityType.valueOf("MINECART_MOB_SPAWNER"));
        assertNotNull(EntityType.valueOf("MINECART_TNT"));
    }

    @Test
    void modernBoatAndMinecartNamesStillExist() {
        // Aliases must not displace the modern names.
        assertNotNull(EntityType.valueOf("OAK_BOAT"));
        assertNotNull(EntityType.valueOf("END_CRYSTAL"));
        assertNotNull(EntityType.valueOf("CHEST_MINECART"));
        assertNotNull(EntityType.valueOf("FURNACE_MINECART"));
        assertNotNull(EntityType.valueOf("TNT_MINECART"));
    }

    @Test
    void pre1_11EntityAliases() {
        // WorldGuard 6.2's Materials.<clinit> references these pre-1.11 names.
        assertNotNull(EntityType.valueOf("THROWN_EXP_BOTTLE"));
        assertNotNull(EntityType.valueOf("PRIMED_TNT"));
        assertNotNull(EntityType.valueOf("FIREWORK"));
        assertNotNull(EntityType.valueOf("MINECART_COMMAND"));
    }
}
