package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pinning regression test for mcbans 4.3.5's {@code onEnable}, which
 * self-checks {@code if (!pm.isPluginEnabled(this)) return;} after
 * loading its config and before initialising its command handler. The
 * previous {@code isPluginEnabled} delegated to {@code
 * ModManager.isLoaded} which only flips to true AFTER the container's
 * onEnable transition completes -- so during the plugin's own onEnable
 * the check returned false and the plugin bailed early, leaving its
 * {@code commandHandler} null and every later command dispatch NPE'ing.
 *
 * <p>The bridge now reads the plugin's {@link
 * org.bukkit.plugin.Plugin#isEnabled()} flag directly when given a
 * Plugin reference, and consults the bridge's name-keyed plugin
 * registry for the by-name overload. Both answer correctly during the
 * plugin's own onEnable.
 */
class BukkitBridgeIsPluginEnabledTest {

    private static final String NAME = "is-enabled-probe";

    static final class FixturePlugin extends JavaPlugin {}

    @BeforeEach
    void clearBefore() {
        BukkitBridge.uninstall();
        BukkitBridge.unregisterPlugin(NAME);
    }

    @AfterEach
    void clearAfter() {
        BukkitBridge.uninstall();
        BukkitBridge.unregisterPlugin(NAME);
    }

    @Test
    void byPluginReadsLiveEnabledFlag() {
        BukkitBridge.install(new StubRdServer());
        FixturePlugin plugin = new FixturePlugin();
        BukkitBridge.registerPlugin(NAME, plugin);
        PluginManager pm = Bukkit.getServer().getPluginManager();

        assertTrue(pm.isPluginEnabled(plugin),
                "JavaPlugin defaults enabled=true; the by-Plugin overload must mirror that even"
                        + " before the mod container leaves the LOADING state");

        plugin.setEnabled(false);
        assertFalse(pm.isPluginEnabled(plugin),
                "by-Plugin overload must reflect a self-disabled plugin during onEnable");
    }

    @Test
    void byNameReadsBridgeRegistryDuringLoading() {
        BukkitBridge.install(new StubRdServer());
        FixturePlugin plugin = new FixturePlugin();
        BukkitBridge.registerPlugin(NAME, plugin);
        PluginManager pm = Bukkit.getServer().getPluginManager();

        // The mod container would still be in LOADING state during the
        // plugin's own onEnable -- so a ModManager.isLoaded(name) probe
        // would return false. The bridge falls back to its own registry
        // first (registered the moment the plugin instance is built),
        // so the by-name check returns true here.
        assertTrue(pm.isPluginEnabled(NAME),
                "by-name overload must return true while the plugin is registered + enabled");
    }

    @Test
    void byNameFollowsSelfDisable() {
        BukkitBridge.install(new StubRdServer());
        FixturePlugin plugin = new FixturePlugin();
        BukkitBridge.registerPlugin(NAME, plugin);
        plugin.setEnabled(false);
        PluginManager pm = Bukkit.getServer().getPluginManager();
        assertFalse(pm.isPluginEnabled(NAME),
                "by-name overload must read the live enabled flag, not just registry membership");
    }

    @Test
    void byNameFalseWhenNotRegistered() {
        BukkitBridge.install(new StubRdServer());
        PluginManager pm = Bukkit.getServer().getPluginManager();
        assertFalse(pm.isPluginEnabled("never-registered"),
                "unknown plugin name must return false");
    }

    @Test
    void byPluginNullSafe() {
        BukkitBridge.install(new StubRdServer());
        PluginManager pm = Bukkit.getServer().getPluginManager();
        assertFalse(pm.isPluginEnabled((org.bukkit.plugin.Plugin) null));
    }
}
