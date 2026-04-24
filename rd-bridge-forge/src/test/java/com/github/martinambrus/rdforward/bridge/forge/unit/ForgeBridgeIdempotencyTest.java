package com.github.martinambrus.rdforward.bridge.forge.unit;

import com.github.martinambrus.rdforward.api.event.EventResult;
import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.bridge.forge.ForgeBridge;
import com.github.martinambrus.rdforward.bridge.forge.ForgeEventBus;
import com.github.martinambrus.rdforward.bridge.forge.fixtures.ForgeTestServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForgeBridgeIdempotencyTest {

    @BeforeEach
    void clean() {
        ForgeBridge.uninstall();
        ServerEvents.clearAll();
        ((ForgeEventBus) MinecraftForge.EVENT_BUS).clear();
    }

    @AfterEach
    void cleanAfter() {
        ForgeBridge.uninstall();
        ServerEvents.clearAll();
        ((ForgeEventBus) MinecraftForge.EVENT_BUS).clear();
    }

    @Test
    void installSetsFlag() {
        ForgeBridge.install(new ForgeTestServer());
        assertTrue(ForgeBridge.isInstalled());
    }

    @Test
    void uninstallClearsFlag() {
        ForgeBridge.install(new ForgeTestServer());
        ForgeBridge.uninstall();
        assertFalse(ForgeBridge.isInstalled());
    }

    @Test
    void doubleInstallIsNoOp() {
        ForgeBridge.install(new ForgeTestServer());
        int listenersAfterFirst = ((ForgeEventBus) MinecraftForge.EVENT_BUS).listenerCount();
        ForgeBridge.install(new ForgeTestServer());
        int listenersAfterSecond = ((ForgeEventBus) MinecraftForge.EVENT_BUS).listenerCount();
        assertEquals(listenersAfterFirst, listenersAfterSecond,
                "second install must not wire duplicate forwarders");
    }

    @Test
    void uninstallWithoutInstallIsSafe() {
        ForgeBridge.uninstall();
        assertFalse(ForgeBridge.isInstalled());
    }

    @Test
    void reinstallAfterUninstall() {
        ForgeBridge.install(new ForgeTestServer());
        ForgeBridge.uninstall();
        ForgeBridge.install(new ForgeTestServer());
        assertTrue(ForgeBridge.isInstalled());
    }

    @Test
    void rdApiBlockBreakReachesForgeBus() {
        ForgeBridge.install(new ForgeTestServer());
        AtomicInteger hits = new AtomicInteger();
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false,
                BlockEvent.BreakEvent.class, e -> hits.incrementAndGet());
        ServerEvents.BLOCK_BREAK.invoker().onBlockBreak("alice", 1, 2, 3, 4);
        assertEquals(1, hits.get(), "rd-api BLOCK_BREAK must post through to Forge bus");
    }

    @Test
    void forgeListenerCancellationBubblesBackToRdApi() {
        ForgeBridge.install(new ForgeTestServer());
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false,
                BlockEvent.BreakEvent.class, e -> e.setCanceled(true));
        EventResult r = ServerEvents.BLOCK_BREAK.invoker().onBlockBreak("alice", 1, 2, 3, 4);
        assertEquals(EventResult.CANCEL, r);
    }

    @Test
    void rdApiChatReachesServerChatEvent() {
        ForgeBridge.install(new ForgeTestServer());
        AtomicInteger hits = new AtomicInteger();
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false,
                ServerChatEvent.class, e -> hits.incrementAndGet());
        ServerEvents.CHAT.invoker().onChat("alice", "hi");
        assertEquals(1, hits.get());
    }

    @Test
    void uninstallStopsForwardingToForgeBus() {
        ForgeBridge.install(new ForgeTestServer());
        AtomicInteger hits = new AtomicInteger();
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false,
                BlockEvent.BreakEvent.class, e -> hits.incrementAndGet());
        ForgeBridge.uninstall();
        ((ForgeEventBus) MinecraftForge.EVENT_BUS).clear();
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false,
                BlockEvent.BreakEvent.class, e -> hits.incrementAndGet());
        ServerEvents.BLOCK_BREAK.invoker().onBlockBreak("alice", 0, 0, 0, 0);
        assertEquals(0, hits.get(),
                "after uninstall, rd-api events must no longer reach Forge bus");
    }
}
