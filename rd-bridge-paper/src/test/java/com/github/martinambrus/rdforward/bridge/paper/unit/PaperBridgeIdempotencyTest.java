package com.github.martinambrus.rdforward.bridge.paper.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge;
import com.github.martinambrus.rdforward.bridge.paper.fixtures.PaperTestServer;
import com.github.martinambrus.rdforward.bridge.paper.PaperBridge;
import org.bukkit.Bukkit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperBridgeIdempotencyTest {

    @BeforeEach
    void clean() { PaperBridge.uninstall(); BukkitBridge.uninstall(); }

    @AfterEach
    void cleanAfter() { PaperBridge.uninstall(); BukkitBridge.uninstall(); }

    @Test
    void installDelegatesToBukkit() {
        PaperBridge.install(new PaperTestServer());
        assertTrue(PaperBridge.isInstalled());
        assertTrue(BukkitBridge.isInstalled(), "Paper bridge must install the Bukkit facade");
        assertNotNull(Bukkit.getServer());
    }

    @Test
    void doubleInstallKeepsFirstInstance() {
        PaperBridge.install(new PaperTestServer());
        org.bukkit.Server first = Bukkit.getServer();
        PaperBridge.install(new PaperTestServer());
        assertSame(first, Bukkit.getServer());
    }

    @Test
    void uninstallRevertsBukkitFacade() {
        PaperBridge.install(new PaperTestServer());
        PaperBridge.uninstall();
        assertFalse(PaperBridge.isInstalled());
        assertFalse(BukkitBridge.isInstalled());
        assertNull(Bukkit.getServer());
    }

    @Test
    void uninstallWithoutInstallIsSafe() {
        PaperBridge.uninstall();
        assertFalse(PaperBridge.isInstalled());
    }

    @Test
    void reinstallAfterUninstall() {
        PaperBridge.install(new PaperTestServer());
        PaperBridge.uninstall();
        PaperBridge.install(new PaperTestServer());
        assertTrue(PaperBridge.isInstalled());
        assertNotNull(Bukkit.getServer());
    }
}
