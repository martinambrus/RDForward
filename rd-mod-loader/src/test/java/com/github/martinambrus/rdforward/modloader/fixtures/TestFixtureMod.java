package com.github.martinambrus.rdforward.modloader.fixtures;

import com.github.martinambrus.rdforward.api.event.EventPriority;
import com.github.martinambrus.rdforward.api.event.EventResult;
import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.api.mod.ServerMod;
import com.github.martinambrus.rdforward.api.server.Server;

public class TestFixtureMod implements ServerMod {

    public static final String PROP_ENABLED = "rdforward.test.fixture.enabled";
    public static final String PROP_DISABLED = "rdforward.test.fixture.disabled";
    public static final String PROP_FIRED = "rdforward.test.fixture.fired";
    public static final String MOD_ID = "testfixture";

    @Override
    public void onEnable(Server server) {
        System.setProperty(PROP_ENABLED, "true");
        ServerEvents.BLOCK_PLACE.register(
                EventPriority.NORMAL,
                (playerName, x, y, z, newBlockType) -> {
                    System.setProperty(PROP_FIRED,
                            playerName + "," + x + "," + y + "," + z + "," + newBlockType);
                    return EventResult.PASS;
                },
                MOD_ID);
    }

    @Override
    public void onDisable() {
        System.setProperty(PROP_DISABLED, "true");
    }
}
