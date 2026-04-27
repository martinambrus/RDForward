package com.github.martinambrus.rdforward.bridge.bukkit.fixtures;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Fixture for {@code SelfDisableLinkageTest} -- mirrors the mcbans v3.8
 * self-disable pattern: looks itself up via the plugin manager and
 * passes the result back to {@code disablePlugin}. The wrapper must
 * detect the resulting {@code isEnabled() == false} on return from
 * {@code onEnable} and throw {@code PluginSelfDisabledException}
 * instead of completing the enable chain.
 */
public class SelfDisableProbePlugin extends JavaPlugin {

    public static final String PLUGIN_NAME = "SelfDisableProbe";

    @Override
    public void onEnable() {
        PluginManager pm = Bukkit.getServer().getPluginManager();
        pm.disablePlugin(pm.getPlugin(PLUGIN_NAME));
    }
}
