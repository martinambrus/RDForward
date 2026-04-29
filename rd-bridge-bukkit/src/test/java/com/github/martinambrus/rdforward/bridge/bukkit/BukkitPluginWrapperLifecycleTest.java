package com.github.martinambrus.rdforward.bridge.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the {@link Bukkit#INSIDE_PLUGIN_LIFECYCLE} ThreadLocal contract:
 * during a plugin's {@code onLoad/onEnable/onDisable} the plugin must
 * observe {@link Bukkit#isPrimaryThread()} as {@code true}, otherwise
 * CoreProtect's {@code Config.parseConfig} deadlocks (it schedules a
 * task to the primary thread and joins on the future, but the tick
 * loop has not started yet during plugin enable).
 *
 * <p>The flag must also be cleared when the lifecycle method returns,
 * including the exception path — otherwise unrelated worker threads
 * inherit it via {@link InheritableThreadLocal}-style leaks (we use a
 * plain {@link ThreadLocal} so the leak only matters within the lifecycle
 * thread itself, but the contract is still: clear on return).
 */
class BukkitPluginWrapperLifecycleTest {

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

    /** Captures the value of Bukkit.isPrimaryThread() observed inside
     *  each lifecycle method. */
    public static class CaptureLifecyclePlugin extends JavaPlugin {
        public volatile boolean primaryDuringLoad;
        public volatile boolean primaryDuringEnable;
        public volatile boolean primaryDuringDisable;

        @Override public void onLoad() { primaryDuringLoad = Bukkit.isPrimaryThread(); }
        @Override public void onEnable() { primaryDuringEnable = Bukkit.isPrimaryThread(); }
        @Override public void onDisable() { primaryDuringDisable = Bukkit.isPrimaryThread(); }
    }

    /** Lifecycle plugin that throws from onEnable to verify the
     *  {@code finally} block clears the ThreadLocal. */
    public static class ThrowingPlugin extends JavaPlugin {
        @Override public void onEnable() { throw new RuntimeException("boom"); }
    }

    @Test
    void onEnableSeesPrimaryThreadTrueAndCleansUpAfterReturn() {
        // Sanity: outside a lifecycle call, the test thread is not primary.
        assertFalse(Bukkit.isPrimaryThread(),
                "test thread is not the tick loop and no ThreadLocal set");

        CaptureLifecyclePlugin plugin = new CaptureLifecyclePlugin();
        BukkitPluginWrapper wrapper = new BukkitPluginWrapper(plugin, "capture");
        try {
            wrapper.onEnable(null);
        } catch (RuntimeException ignored) {
            // PluginSelfDisabledException / NPE without a Server is fine —
            // we only care about what the plugin observed.
        }

        assertTrue(plugin.primaryDuringLoad,
                "plugin.onLoad must see Bukkit.isPrimaryThread()=true");
        assertTrue(plugin.primaryDuringEnable,
                "plugin.onEnable must see Bukkit.isPrimaryThread()=true");
        assertFalse(Bukkit.isPrimaryThread(),
                "ThreadLocal must be cleared after onEnable returns");
    }

    @Test
    void onDisableSeesPrimaryThreadTrueAndCleansUpAfterReturn() {
        CaptureLifecyclePlugin plugin = new CaptureLifecyclePlugin();
        BukkitPluginWrapper wrapper = new BukkitPluginWrapper(plugin, "capture");

        wrapper.onDisable();

        assertTrue(plugin.primaryDuringDisable,
                "plugin.onDisable must see Bukkit.isPrimaryThread()=true");
        assertFalse(Bukkit.isPrimaryThread(),
                "ThreadLocal must be cleared after onDisable returns");
    }

    @Test
    void exceptionFromOnEnableStillClearsThreadLocal() {
        // The finally block in onEnable must clear the ThreadLocal even
        // if the plugin throws — without this, the next lifecycle call
        // on the same thread starts with stale state.
        ThrowingPlugin plugin = new ThrowingPlugin();
        BukkitPluginWrapper wrapper = new BukkitPluginWrapper(plugin, "throwing");
        try {
            wrapper.onEnable(null);
        } catch (RuntimeException ignored) {
            // Expected.
        }
        assertFalse(Bukkit.isPrimaryThread(),
                "exception from onEnable must not leak ThreadLocal=true");
    }
}
