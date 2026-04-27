package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.api.stub.StubCallLog;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.bukkit.Bukkit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BukkitBridgeIdempotencyTest {

    @BeforeEach
    void clean() { BukkitBridge.uninstall(); }

    @AfterEach
    void cleanAfter() { BukkitBridge.uninstall(); }

    @Test
    void installWiresBukkitFacade() {
        StubRdServer rd = new StubRdServer();
        BukkitBridge.install(rd);
        assertTrue(BukkitBridge.isInstalled());
        assertNotNull(Bukkit.getServer(), "Bukkit.setServer should have been wired");
    }

    @Test
    void doubleInstallKeepsFirstServerInstance() {
        StubRdServer rd1 = new StubRdServer();
        StubRdServer rd2 = new StubRdServer();
        BukkitBridge.install(rd1);
        org.bukkit.Server first = Bukkit.getServer();
        BukkitBridge.install(rd2);
        assertSame(first, Bukkit.getServer(),
                "second install must not swap the live facade");
    }

    @Test
    void uninstallRevertsFacade() {
        BukkitBridge.install(new StubRdServer());
        assertTrue(BukkitBridge.isInstalled());
        BukkitBridge.uninstall();
        assertFalse(BukkitBridge.isInstalled());
        assertNull(Bukkit.getServer());
    }

    @Test
    void uninstallWithoutInstallIsSafe() {
        BukkitBridge.uninstall();
        assertFalse(BukkitBridge.isInstalled());
    }

    @Test
    void installDoesNotWireStubCallBroadcastSink() {
        StubCallLog.resetForTests();
        StubRdServer rd = new StubRdServer();
        BukkitBridge.install(rd);

        // First-hit StubCallLog from a stub method must reach the JUL
        // log (covered by StubCallLogTest) but MUST NOT broadcast to
        // every online player — operators audit gaps from the console,
        // not by spamming chat each time a plugin hits an unsupported
        // method. If a future change re-installs the broadcast sink
        // this assertion fires.
        StubCallLog.logOnce("demo-plugin", "org.bukkit.World.unsupportedMethod()V");
        assertEquals(0, rd.broadcasts.size(),
                "Bukkit bridge must not install StubCallLog broadcast sink");
    }

    @Test
    void reinstallAfterUninstallProducesFreshFacade() {
        BukkitBridge.install(new StubRdServer());
        org.bukkit.Server first = Bukkit.getServer();
        BukkitBridge.uninstall();
        BukkitBridge.install(new StubRdServer());
        assertTrue(BukkitBridge.isInstalled());
        assertNotNull(Bukkit.getServer());
        assertFalse(first == Bukkit.getServer(),
                "re-install must create a new adapter");
    }
}
