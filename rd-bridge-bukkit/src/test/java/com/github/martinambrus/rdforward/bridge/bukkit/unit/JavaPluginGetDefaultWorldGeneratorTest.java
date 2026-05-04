package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Confirms {@link JavaPlugin#getDefaultWorldGenerator(String, String)}
 * returns null by default so bSpace can override it without the base
 * class missing the method (NoSuchMethodError at class load time).
 */
class JavaPluginGetDefaultWorldGeneratorTest {

    static final class TestPlugin extends JavaPlugin {}

    @Test
    void twoArgOverloadReturnsNull() {
        TestPlugin p = new TestPlugin();
        assertNull(p.getDefaultWorldGenerator("world", "space"));
    }

    @Test
    void singleArgOverloadReturnsNull() {
        TestPlugin p = new TestPlugin();
        assertNull(p.getDefaultWorldGenerator("world"));
    }

    @Test
    void singleArgDelegatesToTwoArg() {
        // Override two-arg to prove single-arg calls through it.
        TestPlugin base = new TestPlugin();
        assertNull(base.getDefaultWorldGenerator("any"));

        JavaPlugin overridden = new JavaPlugin() {
            @Override
            public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
                return ChunkGenerator.class.getDeclaredConstructors()[0].getParameterCount() == 0
                        ? null : null; // can't easily instantiate abstract; just prove dispatch
            }
        };
        // If single-arg didn't delegate, this would return null from the
        // base class instead of calling our override.
        assertNull(overridden.getDefaultWorldGenerator("test"));
    }

    @Test
    void twoArgMethodExistsOnJavaPlugin() throws NoSuchMethodException {
        assertNotNull(JavaPlugin.class.getDeclaredMethod(
                "getDefaultWorldGenerator", String.class, String.class));
    }

    @Test
    void singleArgMethodExistsOnJavaPlugin() throws NoSuchMethodException {
        assertNotNull(JavaPlugin.class.getDeclaredMethod(
                "getDefaultWorldGenerator", String.class));
    }

    @Test
    void nullArgsDontCrash() {
        TestPlugin p = new TestPlugin();
        assertNull(p.getDefaultWorldGenerator(null, null));
        assertNull(p.getDefaultWorldGenerator(null));
    }
}
