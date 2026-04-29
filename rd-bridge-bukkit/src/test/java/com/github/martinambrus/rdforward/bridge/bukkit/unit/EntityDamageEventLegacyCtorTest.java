package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.event.entity.EntityDamageEvent;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Pins the legacy {@code (Entity, DamageCause, int)} constructor on
 * {@link EntityDamageEvent}. Essentials's {@code Commandsuicide}
 * constructs the event via this CB-1.x signature before calling
 * {@code player.setHealth(0)}; without it the command throws
 * {@link NoSuchMethodError} before the no-op damage path even runs.
 */
class EntityDamageEventLegacyCtorTest {

    @Test
    void legacyIntDamageCtorIsPresent() throws Exception {
        Constructor<?> ctor = EntityDamageEvent.class.getConstructor(
                org.bukkit.entity.Entity.class,
                org.bukkit.event.entity.EntityDamageEvent$DamageCause.class,
                int.class);
        assertNotNull(ctor, "legacy (Entity, DamageCause, int) ctor must exist for /suicide");
    }

    @Test
    void canInstantiateLegacyCtorWithoutThrowing() throws Exception {
        // Real-world call shape: Entity == null is fine since the stub
        // ctor passes null to super; what matters is the symbolic link
        // resolves. Throwing here means /suicide would crash again.
        Constructor<?> ctor = EntityDamageEvent.class.getConstructor(
                org.bukkit.entity.Entity.class,
                org.bukkit.event.entity.EntityDamageEvent$DamageCause.class,
                int.class);
        Object event = ctor.newInstance(null,
                org.bukkit.event.entity.EntityDamageEvent$DamageCause.SUICIDE, 1000);
        assertNotNull(event);
    }
}
