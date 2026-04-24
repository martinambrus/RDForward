package com.github.martinambrus.rdforward.bridge.pocketmine.unit;

import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.bridge.pocketmine.PocketMineBridge;
import com.github.martinambrus.rdforward.bridge.pocketmine.fixtures.PocketMineTestServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pocketmine.Server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PocketMineBridgeIdempotencyTest {

    @BeforeEach
    void clean() {
        PocketMineBridge.uninstall();
        ServerEvents.clearAll();
    }

    @AfterEach
    void cleanAfter() {
        PocketMineBridge.uninstall();
        ServerEvents.clearAll();
    }

    @Test
    void installBindsStaticServerInstance() {
        PocketMineBridge.install(new PocketMineTestServer());
        assertTrue(PocketMineBridge.isInstalled());
        assertNotNull(Server.getInstance(), "static pocketmine.Server.getInstance() must resolve");
    }

    @Test
    void doubleInstallKeepsFirstInstance() {
        PocketMineBridge.install(new PocketMineTestServer());
        Server first = Server.getInstance();
        PocketMineBridge.install(new PocketMineTestServer());
        assertSame(first, Server.getInstance(),
                "second install is a no-op — the first bridge server instance is preserved");
    }

    @Test
    void uninstallClearsStaticServerAndFlag() {
        PocketMineBridge.install(new PocketMineTestServer());
        PocketMineBridge.uninstall();
        assertFalse(PocketMineBridge.isInstalled());
        assertNull(Server.getInstance());
    }

    @Test
    void uninstallWithoutInstallIsSafe() {
        PocketMineBridge.uninstall();
        assertFalse(PocketMineBridge.isInstalled());
        assertNull(Server.getInstance());
    }

    @Test
    void reinstallAfterUninstallWiresFreshServer() {
        PocketMineBridge.install(new PocketMineTestServer());
        Server first = Server.getInstance();
        PocketMineBridge.uninstall();
        PocketMineBridge.install(new PocketMineTestServer());
        Server second = Server.getInstance();
        assertTrue(PocketMineBridge.isInstalled());
        assertNotNull(second);
        assertFalse(first == second,
                "reinstall must produce a fresh BridgeServer bound to the new rd-api Server");
    }

    @Test
    void bridgeServerExposesPluginManager() {
        PocketMineBridge.install(new PocketMineTestServer());
        assertNotNull(Server.getInstance().getPluginManager(),
                "bridge Server must expose a non-null PluginManager for plugin registerEvents calls");
    }

    @Test
    void broadcastMessageDelegatesToRdServer() {
        PocketMineTestServer rd = new PocketMineTestServer();
        PocketMineBridge.install(rd);
        Server.getInstance().broadcastMessage("hello from pocketmine");
        assertTrue(rd.broadcasts.contains("hello from pocketmine"));
    }
}
