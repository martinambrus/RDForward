package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confirms {@link JavaPlugin#setEnabled(boolean)} flips
 * {@link JavaPlugin#isEnabled()} so plugins that self-disable on a
 * version mismatch (VanishNoPacket 3.14+ calls {@code setEnabled(false)}
 * when its CraftBukkit detection fails) no longer NoSuchMethodError
 * during onEnable.
 */
class JavaPluginEnabledTest {

    static final class TestPlugin extends JavaPlugin {}

    @Test
    void newPluginIsEnabledByDefault() {
        TestPlugin p = new TestPlugin();
        assertTrue(p.isEnabled());
    }

    @Test
    void setEnabledFalseTurnsOff() {
        TestPlugin p = new TestPlugin();
        p.setEnabled(false);
        assertFalse(p.isEnabled());
    }

    @Test
    void setEnabledTrueRestores() {
        TestPlugin p = new TestPlugin();
        p.setEnabled(false);
        p.setEnabled(true);
        assertTrue(p.isEnabled());
    }
}
