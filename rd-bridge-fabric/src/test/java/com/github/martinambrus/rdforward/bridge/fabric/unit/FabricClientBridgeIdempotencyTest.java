package com.github.martinambrus.rdforward.bridge.fabric.unit;

import com.github.martinambrus.rdforward.api.client.ClientEvents;
import com.github.martinambrus.rdforward.bridge.fabric.client.FabricClientBridge;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricClientBridgeIdempotencyTest {

    @BeforeEach
    void clean() { reset(); }

    @AfterEach
    void cleanAfter() { reset(); }

    private static void reset() {
        FabricClientBridge.uninstall();
        ClientEvents.RENDER_HUD.clearListeners();
        ClientEvents.CLIENT_TICK.clearListeners();
        ClientEvents.CLIENT_READY.clearListeners();
        ClientEvents.CLIENT_STOPPING.clearListeners();
        ClientEvents.RENDER_WORLD.clearListeners();
        ClientEvents.SCREEN_OPEN.clearListeners();
        ClientEvents.SCREEN_CLOSE.clearListeners();
    }

    @Test
    void installSetsFlag() {
        FabricClientBridge.install();
        assertTrue(FabricClientBridge.isInstalled());
    }

    @Test
    void installRegistersOneForwarderPerClientEvent() {
        FabricClientBridge.install();
        assertEquals(1, ClientEvents.RENDER_HUD.listenerCount());
        assertEquals(1, ClientEvents.CLIENT_TICK.listenerCount());
        assertEquals(1, ClientEvents.CLIENT_READY.listenerCount());
        assertEquals(1, ClientEvents.CLIENT_STOPPING.listenerCount());
        assertEquals(1, ClientEvents.RENDER_WORLD.listenerCount());
        assertEquals(1, ClientEvents.SCREEN_OPEN.listenerCount());
        assertEquals(1, ClientEvents.SCREEN_CLOSE.listenerCount());
    }

    @Test
    void doubleInstallIsNoOp() {
        FabricClientBridge.install();
        FabricClientBridge.install();
        assertEquals(1, ClientEvents.RENDER_HUD.listenerCount(),
                "second install must not duplicate forwarders");
    }

    @Test
    void uninstallRemovesAllForwarders() {
        FabricClientBridge.install();
        FabricClientBridge.uninstall();
        assertFalse(FabricClientBridge.isInstalled());
        assertEquals(0, ClientEvents.RENDER_HUD.listenerCount());
        assertEquals(0, ClientEvents.CLIENT_TICK.listenerCount());
        assertEquals(0, ClientEvents.CLIENT_READY.listenerCount());
        assertEquals(0, ClientEvents.CLIENT_STOPPING.listenerCount());
        assertEquals(0, ClientEvents.RENDER_WORLD.listenerCount());
        assertEquals(0, ClientEvents.SCREEN_OPEN.listenerCount());
        assertEquals(0, ClientEvents.SCREEN_CLOSE.listenerCount());
    }

    @Test
    void uninstallWithoutInstallIsSafe() {
        FabricClientBridge.uninstall();
        assertFalse(FabricClientBridge.isInstalled());
    }

    @Test
    void reinstallAfterUninstallRewires() {
        FabricClientBridge.install();
        FabricClientBridge.uninstall();
        FabricClientBridge.install();
        assertTrue(FabricClientBridge.isInstalled());
        assertEquals(1, ClientEvents.RENDER_HUD.listenerCount());
    }
}
