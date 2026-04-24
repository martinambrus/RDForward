package com.github.martinambrus.rdforward.bridge.pocketmine.fixtures;

import pocketmine.plugin.PluginBase;

/**
 * Fixture PocketMine plugin. Records each lifecycle hook's firing order
 * to a system property so the integration test can assert
 * {@code onLoad} fires before {@code onEnable}, which fires before
 * {@code onDisable}.
 */
public class TestPocketMinePlugin extends PluginBase {

    public static final String PROP_ORDER = "rdforward.test.pocketmine.order";

    public TestPocketMinePlugin() {
        System.clearProperty(PROP_ORDER);
    }

    @Override
    public void onLoad() {
        append("load");
    }

    @Override
    public void onEnable() {
        append("enable");
    }

    @Override
    public void onDisable() {
        append("disable");
    }

    private static void append(String step) {
        String prior = System.getProperty(PROP_ORDER, "");
        System.setProperty(PROP_ORDER, prior.isEmpty() ? step : prior + "," + step);
    }
}
