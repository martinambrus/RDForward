package com.github.martinambrus.rdforward.modloader.fixtures;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Trivial Bukkit fixture used by {@code ModLoaderBridgeWiringTest}. The
 * test packages this class into a synthetic plugin jar (alongside a
 * generated {@code plugin.yml}) so that the full
 * {@code BridgeRegistry.detect → BukkitPluginLoader.load → ModContainer}
 * chain runs against a real {@link JavaPlugin}.
 *
 * <p>{@code onEnable} / {@code onDisable} flip a system property so the
 * tests can verify lifecycle dispatch through {@code BukkitPluginWrapper}.
 */
public class BridgeFixturePlugin extends JavaPlugin {

    public static final String PROP_ENABLED = "rdforward.test.bridge.fixture.enabled";
    public static final String PROP_DISABLED = "rdforward.test.bridge.fixture.disabled";

    @Override
    public void onEnable() {
        System.setProperty(PROP_ENABLED, "true");
    }

    @Override
    public void onDisable() {
        System.setProperty(PROP_DISABLED, "true");
    }
}
