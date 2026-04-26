package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pinning regression test for SimpleLogin's bootstrap, which calls
 * {@link JavaPlugin#getConfig()} from {@code onEnable} and stores the
 * result in a field. The accessor must therefore return a non-null
 * {@link FileConfiguration} stable across calls, and {@code reloadConfig}
 * must publish a fresh one rather than mutating the previous instance.
 */
class JavaPluginConfigTest {

    private static final class FixturePlugin extends JavaPlugin {}

    @Test
    void getConfigReturnsNonNullYamlConfiguration() {
        FixturePlugin p = new FixturePlugin();
        FileConfiguration cfg = p.getConfig();
        assertNotNull(cfg, "JavaPlugin.getConfig() must never return null");
        assertTrue(cfg instanceof YamlConfiguration,
                "stub config must be a YamlConfiguration so plugins that cast still work");
    }

    @Test
    void getConfigIsStableAcrossCalls() {
        FixturePlugin p = new FixturePlugin();
        assertSame(p.getConfig(), p.getConfig(),
                "successive calls must return the same instance — plugins cache the reference");
    }

    @Test
    void reloadConfigPublishesAFreshInstance() {
        FixturePlugin p = new FixturePlugin();
        FileConfiguration first = p.getConfig();
        p.reloadConfig();
        FileConfiguration second = p.getConfig();
        assertNotSame(first, second,
                "reloadConfig must swap in a new YamlConfiguration so callers re-read defaults");
        assertTrue(second instanceof YamlConfiguration);
    }
}
