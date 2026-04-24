package com.github.martinambrus.rdforward.bridge.paper.fixtures;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Plain {@code JavaPlugin} fixture. Matches what a Paper mod author would
 * ship as the {@code main:} entrypoint of {@code paper-plugin.yml} — no
 * Paper-specific inheritance needed. The bootstrapper fixture
 * {@link TestPaperBootstrap} returns an instance of this class from
 * {@code createPlugin}.
 *
 * <p>The plugin records {@code onEnable} / {@code onDisable} to syspropys,
 * appending to {@link #PROP_ORDER} so tests can assert that bootstrap ran
 * before enable. Listener registration happens in {@code onEnable} so the
 * Paper bridge can pick up {@code AsyncChatEvent} handlers during its
 * listener walk.
 */
public class TestPaperPlugin extends JavaPlugin {

    public static final String PROP_ENABLE = "rdforward.test.paper.enable";
    public static final String PROP_DISABLE = "rdforward.test.paper.disable";
    public static final String PROP_ORDER = "rdforward.test.paper.order";
    public static final String PROP_CHAT = "rdforward.test.paper.chat";
    public static final String PROP_ADVENTURE_PLAIN = "rdforward.test.paper.adventure_plain";

    @Override
    public void onEnable() {
        System.setProperty(PROP_ENABLE, "true");
        String prior = System.getProperty(PROP_ORDER, "");
        System.setProperty(PROP_ORDER, prior.isEmpty() ? "enable" : prior + ",enable");
        registerListener(new TestPaperListener());
    }

    @Override
    public void onDisable() {
        System.setProperty(PROP_DISABLE, "true");
    }
}
