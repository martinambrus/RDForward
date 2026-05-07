package com.github.martinambrus.rdforward.bridge.bukkit.fixtures;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Records {@code Bukkit.getServer().getVersion()} and
 * {@code getBukkitVersion()} into system properties keyed by the
 * plugin's declared name. The integration test loads multiple jars
 * with different plugin names so each plugin's view of the
 * version string can be asserted independently.
 *
 * <p>Property keys: {@code rdforward.mcprobe.<name>.version} and
 * {@code rdforward.mcprobe.<name>.bukkitVersion}.
 */
public class MCVersionProbePlugin extends JavaPlugin {

    public static final String PROP_PREFIX = "rdforward.mcprobe.";

    @Override
    public void onEnable() {
        String name = getName();
        System.setProperty(PROP_PREFIX + name + ".version",
                Bukkit.getServer().getVersion());
        System.setProperty(PROP_PREFIX + name + ".bukkitVersion",
                Bukkit.getServer().getBukkitVersion());
    }
}
