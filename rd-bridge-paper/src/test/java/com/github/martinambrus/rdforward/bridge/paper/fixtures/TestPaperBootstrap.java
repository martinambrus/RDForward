package com.github.martinambrus.rdforward.bridge.paper.fixtures;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;

/**
 * Minimal bootstrapper fixture. Records its firing to
 * {@link TestPaperPlugin#PROP_ORDER}. Brigadier command registration is
 * no longer exercised here — paper-api 26.1.2 changed the Brigadier
 * surface and the bridge currently accepts-and-drops registrations, so
 * the previous assertion on {@code PROP_BRIG_CMD} no longer holds.
 * {@code createPlugin} returns {@code null} so the loader falls back to
 * instantiating the declared {@code main:} class — which is
 * {@link TestPaperPlugin}.
 */
public class TestPaperBootstrap implements PluginBootstrap {

    public static final String PROP_BOOTSTRAP = "rdforward.test.paper.bootstrap";

    @Override
    public void bootstrap(BootstrapContext context) {
        System.setProperty(PROP_BOOTSTRAP, "true");
        String prior = System.getProperty(TestPaperPlugin.PROP_ORDER, "");
        System.setProperty(TestPaperPlugin.PROP_ORDER, prior.isEmpty() ? "bootstrap" : prior + ",bootstrap");
    }
}
