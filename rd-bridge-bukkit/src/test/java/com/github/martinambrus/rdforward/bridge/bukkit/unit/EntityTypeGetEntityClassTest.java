package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Zombie;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins {@link EntityType#getEntityClass()} resolving to the matching
 * {@code org.bukkit.entity.<X>} interface. Essentials's {@code /spawnmob}
 * pipeline reads this and feeds the class into
 * {@code World.spawn(Location, Class)}, then immediately checkcasts to
 * {@link LivingEntity}. Without a non-null interface here, the spawn
 * proxy is typed only as raw {@link org.bukkit.entity.Entity} and the
 * cast throws {@link ClassCastException}.
 */
class EntityTypeGetEntityClassTest {

    @Test
    void pigResolvesToPigInterface() {
        assertSame(Pig.class, EntityType.PIG.getEntityClass());
    }

    @Test
    void zombieResolvesToZombieInterface() {
        assertSame(Zombie.class, EntityType.ZOMBIE.getEntityClass());
    }

    @Test
    void snakeCaseEnumNamesConvertToCamelCase() {
        // ENDER_DRAGON -> EnderDragon. Real check: EntityType.ENDER_DRAGON
        // resolves to a non-null Entity-rooted class.
        assertNotNull(EntityType.ENDER_DRAGON.getEntityClass(),
                "SNAKE_CASE -> CamelCase conversion must work for compound names");
    }

    @Test
    void legacyAliasesPointToLegacyInterfaces() {
        // Pre-1.13 plugins use these enum constants. Both legacy and
        // modern enum names should resolve to the bridge's legacy
        // interface stubs (the modern Mooshroom / ZombifiedPiglin /
        // SnowGolem types are not separately stubbed).
        assertSame(PigZombie.class, EntityType.PIG_ZOMBIE.getEntityClass());
        assertSame(MushroomCow.class, EntityType.MUSHROOM_COW.getEntityClass());
        assertSame(Snowman.class, EntityType.SNOWMAN.getEntityClass());
    }

    @Test
    void modernEnumNamesAlsoResolveToLegacyInterfaces() {
        assertSame(PigZombie.class, EntityType.ZOMBIFIED_PIGLIN.getEntityClass());
        assertSame(MushroomCow.class, EntityType.MOOSHROOM.getEntityClass());
        assertSame(Snowman.class, EntityType.SNOW_GOLEM.getEntityClass());
    }

    @Test
    void resolvedClassIsLivingEntityForCommonMobs() {
        // The cast Essentials does is to LivingEntity — pin that the
        // resolved class is in that hierarchy for representative mobs.
        assertTrue(LivingEntity.class.isAssignableFrom(EntityType.PIG.getEntityClass()));
        assertTrue(LivingEntity.class.isAssignableFrom(EntityType.ZOMBIE.getEntityClass()));
        assertTrue(LivingEntity.class.isAssignableFrom(EntityType.PIG_ZOMBIE.getEntityClass()));
    }

    @Test
    void unknownReturnsNull() {
        // EntityType.UNKNOWN is the no-spawn marker; getEntityClass must
        // not invent a class for it.
        org.junit.jupiter.api.Assertions.assertNull(EntityType.UNKNOWN.getEntityClass());
    }
}
