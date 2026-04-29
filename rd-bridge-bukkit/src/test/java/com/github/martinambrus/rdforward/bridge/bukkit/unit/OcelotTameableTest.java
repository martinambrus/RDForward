package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Tameable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the {@code Tameable} parent on the {@link Ocelot} interface.
 * Pre-1.14 Bukkit had {@code Ocelot extends Tameable}; the rewrite that
 * split cats out moved the taming surface to {@code Cat}, leaving
 * Ocelot non-tameable in modern API. Essentials Pre-2.14's
 * {@code Commandkittycannon} still calls {@code ocelot.setTamed(true)}
 * directly, so the symbolic link must continue to resolve.
 *
 * <p>This is a pure type-system contract — the stub {@code World.spawn}
 * proxy already satisfies the runtime call (logged once and returning).
 */
class OcelotTameableTest {

    @Test
    void ocelotImplementsTameable() {
        assertTrue(Tameable.class.isAssignableFrom(Ocelot.class),
                "Ocelot must extend Tameable so Essentials's /kittycannon "
                + "ocelot.setTamed(true) call resolves at link time");
    }
}
