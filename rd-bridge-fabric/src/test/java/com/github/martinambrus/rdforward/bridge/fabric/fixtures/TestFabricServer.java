package com.github.martinambrus.rdforward.bridge.fabric.fixtures;

import net.fabricmc.api.DedicatedServerModInitializer;

public class TestFabricServer implements DedicatedServerModInitializer {

    public static final String PROP_SERVER = "rdforward.test.fabric.server";

    @Override
    public void onInitializeServer() {
        System.setProperty(PROP_SERVER, "true");
    }
}
