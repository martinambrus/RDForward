package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitEventAdapter;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitPluginWrapper;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the contract that {@link PluginEnableEvent} fires AFTER a plugin's
 * {@code onEnable()} returns, so a listener the plugin registers DURING
 * its own onEnable receives its own enable event. Essentials's
 * {@code EssentialsPluginListener.onPluginEnable} catches this and runs
 * {@code permissionsHandler.checkPermissions()} — without this dispatch,
 * Essentials's handler stays on {@code NullPermissionsHandler} (which
 * denies every {@code hasPermission} call) and OP players get
 * "denied access to command" on every Essentials command.
 *
 * <p>{@link PluginDisableEvent} fires AFTER {@code onDisable()} returns
 * to mirror real Bukkit's order, so listeners observing dependency
 * unloads still see the plugin in its disabled state.
 */
class PluginEnableEventDispatchTest {

    @BeforeEach
    void clear() {
        Bukkit.INSIDE_PLUGIN_LIFECYCLE.remove();
        BukkitBridge.uninstall();
        BukkitEventAdapter.clearAll();
    }

    @AfterEach
    void clearAfter() {
        Bukkit.INSIDE_PLUGIN_LIFECYCLE.remove();
        BukkitBridge.uninstall();
        BukkitEventAdapter.clearAll();
    }

    /** Plugin that registers a self-listener during {@code onEnable} and
     *  records every enable/disable event it receives. */
    public static class SelfListeningPlugin extends JavaPlugin implements Listener {
        public final java.util.List<PluginEnableEvent> enables = new java.util.ArrayList<>();
        public final java.util.List<PluginDisableEvent> disables = new java.util.ArrayList<>();
        public volatile boolean enableReturnedBeforeDispatch = false;
        public volatile boolean dispatchSeenInsideOnEnable = false;

        @Override
        public void onEnable() {
            // Snapshot dispatched count BEFORE any registration so we can
            // verify the listener receives the event AFTER onEnable returns
            // (not synchronously during onEnable).
            int before = enables.size();
            registerListener(this);
            dispatchSeenInsideOnEnable = enables.size() > before;
        }

        @EventHandler
        public void onPluginEnable(PluginEnableEvent ev) {
            enables.add(ev);
            // If onEnable still has stack frames at this point, this flag
            // would not yet be set — see assertion below.
        }

        @EventHandler
        public void onPluginDisable(PluginDisableEvent ev) {
            disables.add(ev);
        }
    }

    @Test
    void pluginEnableEventReachesSelfRegisteredListenerAfterOnEnableReturns() {
        SelfListeningPlugin plugin = new SelfListeningPlugin();
        BukkitPluginWrapper wrapper = new BukkitPluginWrapper(plugin, "selflisten");

        StubRdServer rd = new StubRdServer();
        BukkitBridge.install(rd);

        wrapper.onEnable(rd);

        assertEquals(1, plugin.enables.size(),
                "PluginEnableEvent must dispatch exactly once after onEnable returns");
        assertSame(plugin, plugin.enables.get(0).getPlugin(),
                "event.getPlugin() must carry the enabling plugin reference");
        assertTrue(!plugin.dispatchSeenInsideOnEnable,
                "dispatch must happen AFTER onEnable returns — not synchronously during it");
    }

    @Test
    void pluginDisableEventReachesSelfRegisteredListenerAfterOnDisableReturns() {
        SelfListeningPlugin plugin = new SelfListeningPlugin();
        BukkitPluginWrapper wrapper = new BukkitPluginWrapper(plugin, "selflisten");

        StubRdServer rd = new StubRdServer();
        BukkitBridge.install(rd);
        wrapper.onEnable(rd);

        // Sanity: enable already fired once; clear so disable assertion is clean.
        plugin.disables.clear();

        wrapper.onDisable();

        assertEquals(1, plugin.disables.size(),
                "PluginDisableEvent must dispatch exactly once after onDisable returns");
        assertSame(plugin, plugin.disables.get(0).getPlugin(),
                "event.getPlugin() must carry the disabling plugin reference");
    }
}
