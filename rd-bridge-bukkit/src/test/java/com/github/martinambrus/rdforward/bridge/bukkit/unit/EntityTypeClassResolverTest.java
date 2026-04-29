package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.EntityTypeClassResolver;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Snowman;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Direct tests for {@link EntityTypeClassResolver}. The resolver is
 * exercised end-to-end through {@link org.bukkit.entity.EntityType#getEntityClass()}
 * but the indirect path doesn't pin the cache or the alias-precedence
 * branches.
 */
class EntityTypeClassResolverTest {

    @Test
    void simpleNameResolvesToInterface() {
        assertSame(Pig.class, EntityTypeClassResolver.resolve("PIG"));
    }

    @Test
    void snakeCaseConvertsToCamelCase() {
        // IRON_GOLEM -> IronGolem
        assertSame(IronGolem.class, EntityTypeClassResolver.resolve("IRON_GOLEM"));
    }

    @Test
    void aliasTakesPrecedenceOverNameDerivation() {
        // PIG_ZOMBIE would CamelCase to PigZombie naturally — the alias
        // map confirms it. ZOMBIFIED_PIGLIN, however, would CamelCase
        // to ZombifiedPiglin (which doesn't exist as a stub) — the alias
        // redirects it to PigZombie. Both return the same backing class.
        assertSame(PigZombie.class, EntityTypeClassResolver.resolve("PIG_ZOMBIE"));
        assertSame(PigZombie.class, EntityTypeClassResolver.resolve("ZOMBIFIED_PIGLIN"));
    }

    @Test
    void mooshroomAliasResolvesToMushroomCow() {
        assertSame(MushroomCow.class, EntityTypeClassResolver.resolve("MOOSHROOM"));
        assertSame(MushroomCow.class, EntityTypeClassResolver.resolve("MUSHROOM_COW"));
    }

    @Test
    void snowGolemAliasResolvesToSnowman() {
        assertSame(Snowman.class, EntityTypeClassResolver.resolve("SNOW_GOLEM"));
        assertSame(Snowman.class, EntityTypeClassResolver.resolve("SNOWMAN"));
    }

    @Test
    void unknownEnumExplicitlyReturnsNull() {
        // UNKNOWN is the no-spawn marker; even though "Unknown" isn't a
        // class on the bridge classpath, the resolver must short-circuit
        // via the alias-empty-string sentinel rather than reflectively
        // attempting Class.forName.
        assertNull(EntityTypeClassResolver.resolve("UNKNOWN"));
    }

    @Test
    void unmappedNameReturnsNull() {
        // No org.bukkit.entity.Definitelynotaclass interface exists;
        // resolver must catch the ClassNotFoundException and return null
        // rather than letting it propagate.
        assertNull(EntityTypeClassResolver.resolve("DEFINITELYNOTACLASS"));
    }

    @Test
    void nullAndEmptyAreSafe() {
        assertNull(EntityTypeClassResolver.resolve(null));
        assertNull(EntityTypeClassResolver.resolve(""));
    }

    @Test
    void resolvedClassIsAlwaysEntityRooted() {
        // The resolver only returns classes assignable to Entity — a
        // stray match against a non-Entity class (e.g. some hypothetical
        // org.bukkit.entity.* utility) must not pollute the spawn path.
        Class<?> c = EntityTypeClassResolver.resolve("PIG");
        assertTrue(Entity.class.isAssignableFrom(c),
                "resolver must only return Entity-rooted classes");
    }

    @Test
    void resolveIsCachedAcrossCalls() {
        // Repeated lookups must return the same Class instance — both
        // because Class.forName is itself cached by the classloader and
        // because the resolver memoizes per-name. We can't directly
        // observe the memo, but referential equality is the contract.
        Class<?> first = EntityTypeClassResolver.resolve("PIG");
        Class<?> second = EntityTypeClassResolver.resolve("PIG");
        assertSame(first, second);
        // Negative results must also be stable.
        assertSame(EntityTypeClassResolver.resolve("DEFINITELYNOTACLASS"),
                EntityTypeClassResolver.resolve("DEFINITELYNOTACLASS"));
    }
}
