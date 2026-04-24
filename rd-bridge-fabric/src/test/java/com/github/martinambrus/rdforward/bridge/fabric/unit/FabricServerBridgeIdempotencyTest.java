package com.github.martinambrus.rdforward.bridge.fabric.unit;

import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.bridge.fabric.server.FabricServerBridge;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricServerBridgeIdempotencyTest {

    @BeforeEach
    void clean() { FabricServerBridge.uninstall(); ServerEvents.clearAll(); }

    @AfterEach
    void cleanAfter() { FabricServerBridge.uninstall(); ServerEvents.clearAll(); }

    @Test
    void installSetsFlag() {
        FabricServerBridge.install();
        assertTrue(FabricServerBridge.isInstalled());
    }

    @Test
    void installRegistersEachForwarderExactlyOnce() {
        FabricServerBridge.install();
        assertEquals(1, ServerEvents.SERVER_STARTED.listenerCount());
        assertEquals(1, ServerEvents.SERVER_STOPPING.listenerCount());
        assertEquals(1, ServerEvents.SERVER_TICK.listenerCount());
    }

    @Test
    void doubleInstallIsNoOp() {
        FabricServerBridge.install();
        FabricServerBridge.install();
        assertEquals(1, ServerEvents.SERVER_STARTED.listenerCount(),
                "second install must not re-register forwarders");
        assertEquals(1, ServerEvents.SERVER_TICK.listenerCount());
    }

    @Test
    void uninstallRemovesAllForwarders() {
        FabricServerBridge.install();
        FabricServerBridge.uninstall();
        assertFalse(FabricServerBridge.isInstalled());
        assertEquals(0, ServerEvents.SERVER_STARTED.listenerCount());
        assertEquals(0, ServerEvents.SERVER_STOPPING.listenerCount());
        assertEquals(0, ServerEvents.SERVER_TICK.listenerCount());
    }

    @Test
    void uninstallWithoutInstallIsSafe() {
        FabricServerBridge.uninstall();
        assertFalse(FabricServerBridge.isInstalled());
    }

    @Test
    void reinstallAfterUninstallRewiresForwarders() {
        FabricServerBridge.install();
        FabricServerBridge.uninstall();
        FabricServerBridge.install();
        assertTrue(FabricServerBridge.isInstalled());
        assertEquals(1, ServerEvents.SERVER_STARTED.listenerCount());
    }
}
