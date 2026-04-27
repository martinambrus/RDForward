package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.Sound;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the ASM-injected legacy Sound symbols. Pre-Bukkit-1.9
 * plugins (VanishNoPacket 3.15's "fake explosion" effect) reference
 * short flat field names like {@code Sound.EXPLODE} that were renamed
 * to {@code ENTITY_GENERIC_EXPLODE} in 1.9; without the legacy alias
 * those plugins crash with {@link NoSuchFieldError} on the modern
 * stub.
 */
class LegacySoundFieldTest {

    @Test
    void legacyExplodeIsResolvable() throws Exception {
        Field f = Sound.class.getDeclaredField("EXPLODE");
        int mods = f.getModifiers();
        assertTrue(Modifier.isPublic(mods));
        assertTrue(Modifier.isStatic(mods));
        assertTrue(Modifier.isFinal(mods));
        assertEquals(Sound.class, f.getType());
        assertNull(f.get(null),
                "legacy Sound aliases default to null — plugins forward the value to "
                        + "playSound/playEffect which already StubCallLog the no-op");
    }
}
