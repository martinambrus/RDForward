package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.StubEntity;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Pig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins that {@link StubEntity} proxies returned from
 * {@code World.spawn(Location, Class)} satisfy {@code instanceof}
 * checks against every interface in the requested type's chain.
 *
 * <p>Essentials's {@code /spawnmob} dispatches via
 * {@code world.spawn(loc, Pig.class)} and immediately casts the
 * result to {@link LivingEntity}. Without explicit declaration of
 * the transitive super-interfaces, some JVMs raise
 * {@link ClassCastException} on the checkcast path even though
 * {@link Pig} extends {@link LivingEntity} symbolically.
 */
class StubEntityCastTest {

    @Test
    void pigStubIsAssignableToLivingEntity() {
        Entity stub = StubEntity.create(Pig.class, null, null);
        assertTrue(stub instanceof LivingEntity,
                "Pig stub must satisfy instanceof LivingEntity for Essentials's /spawnmob cast");
        // Mob is the direct parent of Pig's chain that introduces
        // LivingEntity — must also satisfy.
        assertTrue(stub instanceof Mob, "Pig stub must satisfy instanceof Mob");
    }

    @Test
    void pigStubLivingEntityCastDoesNotThrow() {
        Entity stub = StubEntity.create(Pig.class, null, null);
        assertDoesNotThrow(() -> {
            LivingEntity le = (LivingEntity) stub;
            assertNotNull(le.getUniqueId(),
                    "post-cast proxy must still answer the identity surface");
        });
    }

    @Test
    void pigStubStillRespectsPigInterface() {
        Entity stub = StubEntity.create(Pig.class, null, null);
        assertTrue(stub instanceof Pig,
                "narrowing the cast surface to LivingEntity must not break Pig");
    }
}
