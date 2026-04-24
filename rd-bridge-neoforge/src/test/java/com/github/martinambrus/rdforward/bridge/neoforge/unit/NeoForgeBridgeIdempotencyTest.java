package com.github.martinambrus.rdforward.bridge.neoforge.unit;

import com.github.martinambrus.rdforward.api.event.EventResult;
import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.bridge.forge.ForgeBridge;
import com.github.martinambrus.rdforward.bridge.forge.ForgeEventBus;
import com.github.martinambrus.rdforward.bridge.neoforge.NeoForgeBridge;
import com.github.martinambrus.rdforward.bridge.neoforge.fixtures.NeoForgeTestServer;
import net.minecraftforge.common.MinecraftForge;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraftforge.eventbus.api.EventPriority;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NeoForgeBridgeIdempotencyTest {

    @BeforeEach
    void clean() {
        NeoForgeBridge.uninstall();
        ServerEvents.clearAll();
        ((ForgeEventBus) MinecraftForge.EVENT_BUS).clear();
    }

    @AfterEach
    void cleanAfter() {
        NeoForgeBridge.uninstall();
        ServerEvents.clearAll();
        ((ForgeEventBus) MinecraftForge.EVENT_BUS).clear();
    }

    @Test
    void installSetsFlag() {
        NeoForgeBridge.install(new NeoForgeTestServer());
        assertTrue(NeoForgeBridge.isInstalled());
    }

    @Test
    void installAlsoInstallsForgeBridge() {
        NeoForgeBridge.install(new NeoForgeTestServer());
        assertTrue(ForgeBridge.isInstalled(),
                "NeoForge bridge delegates to Forge — Forge must also register as installed");
    }

    @Test
    void uninstallClearsBoth() {
        NeoForgeBridge.install(new NeoForgeTestServer());
        NeoForgeBridge.uninstall();
        assertFalse(NeoForgeBridge.isInstalled());
        assertFalse(ForgeBridge.isInstalled());
    }

    @Test
    void doubleInstallIsNoOp() {
        NeoForgeBridge.install(new NeoForgeTestServer());
        int first = ((ForgeEventBus) MinecraftForge.EVENT_BUS).listenerCount();
        NeoForgeBridge.install(new NeoForgeTestServer());
        int second = ((ForgeEventBus) MinecraftForge.EVENT_BUS).listenerCount();
        assertEquals(first, second, "double install must not wire duplicate forwarders");
    }

    @Test
    void uninstallWithoutInstallIsSafe() {
        NeoForgeBridge.uninstall();
        assertFalse(NeoForgeBridge.isInstalled());
    }

    @Test
    void reinstallAfterUninstallWorks() {
        NeoForgeBridge.install(new NeoForgeTestServer());
        NeoForgeBridge.uninstall();
        NeoForgeBridge.install(new NeoForgeTestServer());
        assertTrue(NeoForgeBridge.isInstalled());
    }

    @Test
    void neoForgeEventBusIsSameInstanceAsMinecraftForgeBus() {
        assertSame(MinecraftForge.EVENT_BUS, NeoForge.EVENT_BUS,
                "NeoForge.EVENT_BUS must alias MinecraftForge.EVENT_BUS — listeners see each other");
    }

    @Test
    void rdApiBlockBreakReachesNeoForgeBus() {
        NeoForgeBridge.install(new NeoForgeTestServer());
        AtomicInteger hits = new AtomicInteger();
        NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false,
                net.minecraftforge.event.level.BlockEvent.BreakEvent.class,
                e -> hits.incrementAndGet());
        ServerEvents.BLOCK_BREAK.invoker().onBlockBreak("alice", 1, 2, 3, 4);
        assertEquals(1, hits.get(),
                "rd-api BLOCK_BREAK posts through shared bus, reaching NeoForge listeners");
    }

    @Test
    void forgeListenerCancellationBubblesBackToRdApi() {
        NeoForgeBridge.install(new NeoForgeTestServer());
        NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false,
                net.minecraftforge.event.level.BlockEvent.BreakEvent.class,
                e -> e.setCanceled(true));
        EventResult r = ServerEvents.BLOCK_BREAK.invoker().onBlockBreak("alice", 1, 2, 3, 4);
        assertEquals(EventResult.CANCEL, r);
    }
}
