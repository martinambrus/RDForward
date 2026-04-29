package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Pins the concrete {@link PotionEffectType} backing introduced for
 * Essentials's {@code /potion} command. Pre-fix, every modern constant
 * was {@code null}; Essentials's {@code Potions.<clinit>} populated its
 * lookup map with null values, then {@code Commandpotion} NPE'd on
 * {@code map.get(name).getName()}.
 *
 * <p>Contract validated:
 * <ul>
 *   <li>Modern constants are non-null and surface their canonical name.</li>
 *   <li>Pre-1.20.5 aliases (SLOW, FAST_DIGGING, ...) point to the same
 *       instance as the modern equivalent — both bindings let
 *       {@code Potions.<clinit>} populate its map with non-null
 *       references.</li>
 *   <li>{@code getByName} resolves both modern and legacy spellings.</li>
 * </ul>
 */
class PotionEffectTypeStubTest {

    @Test
    void modernConstantsAreNonNullAndCarryName() {
        assertNotNull(PotionEffectType.SPEED);
        assertEquals("SPEED", PotionEffectType.SPEED.getName());
        assertNotNull(PotionEffectType.SLOWNESS);
        assertEquals("SLOWNESS", PotionEffectType.SLOWNESS.getName());
        assertNotNull(PotionEffectType.NAUSEA);
        assertEquals("NAUSEA", PotionEffectType.NAUSEA.getName());
    }

    @Test
    void legacyAliasesPointToModernInstances() {
        // Same instance, second binding — Essentials's Potions <clinit>
        // reads both spellings into a map; both must store non-null
        // references for the downstream getName() call to succeed.
        assertSame(PotionEffectType.SLOWNESS, PotionEffectType.SLOW);
        assertSame(PotionEffectType.HASTE, PotionEffectType.FAST_DIGGING);
        assertSame(PotionEffectType.MINING_FATIGUE, PotionEffectType.SLOW_DIGGING);
        assertSame(PotionEffectType.STRENGTH, PotionEffectType.INCREASE_DAMAGE);
        assertSame(PotionEffectType.INSTANT_HEALTH, PotionEffectType.HEAL);
        assertSame(PotionEffectType.INSTANT_DAMAGE, PotionEffectType.HARM);
        assertSame(PotionEffectType.JUMP_BOOST, PotionEffectType.JUMP);
        assertSame(PotionEffectType.NAUSEA, PotionEffectType.CONFUSION);
        assertSame(PotionEffectType.RESISTANCE, PotionEffectType.DAMAGE_RESISTANCE);
    }

    @Test
    void getByNameResolvesModernSpellings() {
        assertSame(PotionEffectType.SPEED, PotionEffectType.getByName("SPEED"));
        assertSame(PotionEffectType.SPEED, PotionEffectType.getByName("speed"));
        assertSame(PotionEffectType.SLOWNESS, PotionEffectType.getByName("SLOWNESS"));
    }

    @Test
    void getByNameResolvesLegacyAliases() {
        // Essentials Pre-2.14 stores its lookup table by upper-cased
        // legacy name (SLOW, FAST_DIGGING, ...); the bridge's getByName
        // must accept those too so plugins pulling potion effects by
        // name don't fall through.
        assertSame(PotionEffectType.SLOWNESS, PotionEffectType.getByName("SLOW"));
        assertSame(PotionEffectType.HASTE, PotionEffectType.getByName("FAST_DIGGING"));
        assertSame(PotionEffectType.RESISTANCE, PotionEffectType.getByName("DAMAGE_RESISTANCE"));
        assertSame(PotionEffectType.INSTANT_HEALTH, PotionEffectType.getByName("HEAL"));
    }

    @Test
    void getByNameReturnsNullForUnknown() {
        assertNull(PotionEffectType.getByName("NOT_A_REAL_EFFECT"));
        assertNull(PotionEffectType.getByName(null));
    }
}
