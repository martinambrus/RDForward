package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the {@code World.playSound} overloads needed by Vanish 3.15's
 * "fake explosion" /vanish cue ({@code world.playSound(loc,
 * Sound.EXPLODE, 4.0f, 0.7f)}) and the modern String-named variant.
 *
 * <p>The methods are declarative no-op stubs that just call
 * {@link com.github.martinambrus.rdforward.api.stub.StubCallLog#logOnce}.
 * StubCallLog itself is covered by {@code StubCallLogTest}; this test
 * only guards the signatures so an accidental rename on
 * {@code World.java} surfaces immediately rather than as a runtime
 * NoSuchMethodError when a plugin calls it.
 */
class WorldPlaySoundStubTest {

    @Test
    void worldExposesEnumPlaySoundOverload() throws Exception {
        assertNotNull(World.class.getDeclaredMethod(
                "playSound", Location.class, Sound.class, float.class, float.class));
    }

    @Test
    void worldExposesStringPlaySoundOverload() throws Exception {
        assertNotNull(World.class.getDeclaredMethod(
                "playSound", Location.class, String.class, float.class, float.class));
    }

    @Test
    void overloadsAreDefaultMethods() throws Exception {
        // Default methods so any World impl inherits the no-op without
        // having to override; this is what lets old stubs that never
        // touched playSound keep working untouched.
        assertTrue(World.class.getDeclaredMethod(
                "playSound", Location.class, Sound.class, float.class, float.class).isDefault());
        assertTrue(World.class.getDeclaredMethod(
                "playSound", Location.class, String.class, float.class, float.class).isDefault());
    }
}
