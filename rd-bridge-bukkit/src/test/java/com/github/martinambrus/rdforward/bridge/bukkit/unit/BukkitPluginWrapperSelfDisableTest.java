package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitEventAdapter;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitPluginWrapper;
import com.github.martinambrus.rdforward.bridge.bukkit.PluginSelfDisabledException;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pinning regression test for the mcbans v3.8 / VanishNoPacket boot
 * pattern: a plugin that fails its own pre-flight check (online-mode
 * mismatch, CraftBukkit-version mismatch) calls {@code setEnabled(false)}
 * or {@code Bukkit.getPluginManager().disablePlugin(this)} during
 * {@code onEnable} and returns normally. Without the post-enable
 * isEnabled() gate the bridge would wire its half-initialized
 * listeners and commands and the mod loader would log "Enabled X" /
 * still call onDisable on shutdown -- the operator-visible "semi-loaded"
 * state we want to avoid.
 */
class BukkitPluginWrapperSelfDisableTest {

    @BeforeEach
    void clearBefore() {
        BukkitEventAdapter.resetWarnedPlugins();
        BukkitBridge.uninstall();
        BukkitBridge.unregisterPlugin("self-disable-probe");
    }

    @AfterEach
    void clearAfter() {
        BukkitEventAdapter.resetWarnedPlugins();
        BukkitBridge.uninstall();
        BukkitBridge.unregisterPlugin("self-disable-probe");
    }

    static final class SelfDisablingPlugin extends JavaPlugin {
        @Override
        public void onEnable() {
            setEnabled(false);
            registerListener(new Listener() {
                @EventHandler public void onJoin(PlayerJoinEvent ev) {}
            });
        }
    }

    static final class HealthyPlugin extends JavaPlugin {
        @Override
        public void onEnable() {
            registerListener(new Listener() {
                @EventHandler public void onJoin(PlayerJoinEvent ev) {}
            });
        }
    }

    @Test
    void wrapperThrowsWhenPluginSelfDisablesInOnEnable() {
        StubRdServer rd = new StubRdServer();
        BukkitBridge.install(rd);
        SelfDisablingPlugin plugin = new SelfDisablingPlugin();
        BukkitBridge.registerPlugin("self-disable-probe", plugin);
        BukkitPluginWrapper wrapper = new BukkitPluginWrapper(plugin, "self-disable-probe");

        PluginSelfDisabledException ex = assertThrows(PluginSelfDisabledException.class,
                () -> wrapper.onEnable(rd));
        org.junit.jupiter.api.Assertions.assertTrue(
                ex.getMessage().contains("self-disable-probe"),
                "exception must name the plugin so operators see which plugin failed");
    }

    @Test
    void selfDisablingPluginIsRemovedFromRegistry() {
        StubRdServer rd = new StubRdServer();
        BukkitBridge.install(rd);
        SelfDisablingPlugin plugin = new SelfDisablingPlugin();
        BukkitBridge.registerPlugin("self-disable-probe", plugin);
        assertSame(plugin, BukkitBridge.lookupPlugin("self-disable-probe"));

        BukkitPluginWrapper wrapper = new BukkitPluginWrapper(plugin, "self-disable-probe");
        assertThrows(PluginSelfDisabledException.class, () -> wrapper.onEnable(rd));

        assertNull(BukkitBridge.lookupPlugin("self-disable-probe"),
                "wrapper.onEnable's finally must drop the registry entry on failure");
    }

    @Test
    void healthyPluginStaysRegistered() {
        StubRdServer rd = new StubRdServer();
        BukkitBridge.install(rd);
        HealthyPlugin plugin = new HealthyPlugin();
        BukkitBridge.registerPlugin("self-disable-probe", plugin);
        BukkitPluginWrapper wrapper = new BukkitPluginWrapper(plugin, "self-disable-probe");
        wrapper.onEnable(rd);
        assertSame(plugin, BukkitBridge.lookupPlugin("self-disable-probe"),
                "successful enable leaves the plugin in the registry");
    }

    @Test
    void onDisableUnregistersFromRegistry() {
        StubRdServer rd = new StubRdServer();
        BukkitBridge.install(rd);
        HealthyPlugin plugin = new HealthyPlugin();
        BukkitBridge.registerPlugin("self-disable-probe", plugin);
        BukkitPluginWrapper wrapper = new BukkitPluginWrapper(plugin, "self-disable-probe");
        wrapper.onEnable(rd);

        wrapper.onDisable();
        assertNull(BukkitBridge.lookupPlugin("self-disable-probe"),
                "onDisable must unregister so a stale plugin reference can't leak");
    }

    @Test
    void disablePluginViaPluginManagerFlipsEnabledFlag() {
        // Mirrors the mcbans v3.8 pattern: pm.disablePlugin(pm.getPlugin("mcbans")).
        // Composes the registry, getPlugin, and disablePlugin paths to
        // confirm the full chain triggers the post-enable detection.
        StubRdServer rd = new StubRdServer();
        BukkitBridge.install(rd);
        SelfDisablingPluginViaManager plugin = new SelfDisablingPluginViaManager();
        BukkitBridge.registerPlugin("self-disable-probe", plugin);
        BukkitPluginWrapper wrapper = new BukkitPluginWrapper(plugin, "self-disable-probe");

        assertThrows(PluginSelfDisabledException.class, () -> wrapper.onEnable(rd));
        assertEquals(false, plugin.isEnabled());
    }

    static final class SelfDisablingPluginViaManager extends JavaPlugin {
        @Override
        public void onEnable() {
            org.bukkit.plugin.PluginManager pm = org.bukkit.Bukkit.getServer().getPluginManager();
            pm.disablePlugin(pm.getPlugin("self-disable-probe"));
        }
    }
}
