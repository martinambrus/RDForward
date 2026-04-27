package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confirms {@link JavaPlugin#setNaggable(boolean)} round-trips through
 * {@link JavaPlugin#isNaggable()}. BenCmd v1.3.5 calls {@code
 * setNaggable(false)} from {@code onEnable} to silence its own
 * {@link org.bukkit.plugin.AuthorNagException} warnings; without the
 * method the JVM throws {@link NoSuchMethodError} and the boot aborts.
 *
 * <p>RDForward has no nag pipeline so the flag is purely a stored
 * round-trip — accessor pin protects against a future cleanup pass
 * deleting the field as "unused".
 */
class JavaPluginNaggableTest {

    static final class TestPlugin extends JavaPlugin {}

    @Test
    void newPluginIsNaggableByDefault() {
        TestPlugin p = new TestPlugin();
        assertTrue(p.isNaggable(),
                "real paper-api defaults naggable=true; pre-1.x plugins rely on this initial state");
    }

    @Test
    void setNaggableFalseFlipsFlag() {
        TestPlugin p = new TestPlugin();
        p.setNaggable(false);
        assertFalse(p.isNaggable());
    }

    @Test
    void setNaggableTrueRestores() {
        TestPlugin p = new TestPlugin();
        p.setNaggable(false);
        p.setNaggable(true);
        assertTrue(p.isNaggable());
    }
}
