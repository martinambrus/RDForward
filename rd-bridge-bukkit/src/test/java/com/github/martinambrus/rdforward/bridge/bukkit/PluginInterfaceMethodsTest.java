package com.github.martinambrus.rdforward.bridge.bukkit;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Covers config and lifecycle methods on the {@link Plugin} interface.
 * CS-CoreLib calls {@code plugin.saveConfig()} through the Plugin interface
 * (not JavaPlugin), so these methods must be visible there.
 */
class PluginInterfaceMethodsTest {

    /** Minimal JavaPlugin subclass for testing. */
    static class TestPlugin extends JavaPlugin {
        @Override public void onEnable() {}
    }

    private final Plugin plugin = new TestPlugin();

    @Test
    void pluginInterfaceDeclaresSaveConfig() {
        assertDoesNotThrow(() -> plugin.saveConfig(),
                "Plugin.saveConfig() must resolve without NoSuchMethodError");
    }

    @Test
    void pluginInterfaceDeclaresSaveDefaultConfig() {
        assertDoesNotThrow(() -> plugin.saveDefaultConfig(),
                "Plugin.saveDefaultConfig() must resolve without NoSuchMethodError");
    }

    @Test
    void pluginInterfaceDeclaresReloadConfig() {
        assertDoesNotThrow(() -> plugin.reloadConfig(),
                "Plugin.reloadConfig() must resolve without NoSuchMethodError");
    }

    @Test
    void saveConfigPersistsToDisk() {
        plugin.reloadConfig();
        plugin.getConfig().set("test-key", "test-value");
        plugin.saveConfig();

        File configFile = new File(plugin.getDataFolder(), "config.yml");
        assertDoesNotThrow(() -> plugin.reloadConfig());
        assertEquals("test-value", plugin.getConfig().getString("test-key"));
    }

    @Test
    void getDataFolderReturnsNonNullDirectory() {
        assertNotNull(plugin.getDataFolder());
    }

    @Test
    void getConfigReturnsNonNull() {
        assertNotNull(plugin.getConfig());
    }

    @Test
    void getResourceReturnsNullForMissingResource() {
        assertNull(plugin.getResource("nonexistent-file.yml"));
    }
}
