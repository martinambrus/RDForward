package com.github.martinambrus.rdforward.bridge.forge.unit;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import com.github.martinambrus.rdforward.bridge.forge.ForgeEventBus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForgeEventBusTest {

    public static final class InstanceHandler {
        public int calls = 0;
        public int lastX = -1;

        @SubscribeEvent
        public void onBreak(BlockEvent.BreakEvent e) {
            calls++;
            lastX = e.getX();
        }

        public void ignored(BlockEvent.BreakEvent e) {}
    }

    public static final class StaticHandler {
        public static int calls;

        @SubscribeEvent
        public static void onBreak(BlockEvent.BreakEvent e) { calls++; }
    }

    public static final class BadSignatureHandler {
        @SubscribeEvent public void noArgs() {}
        @SubscribeEvent public void wrongType(String s) {}
        @SubscribeEvent public void tooMany(BlockEvent.BreakEvent e, String extra) {}
    }

    public static final class PriorityHandler {
        public final List<String> seen = new ArrayList<>();

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void last(BlockEvent.BreakEvent e) { seen.add("lowest"); }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public void first(BlockEvent.BreakEvent e) { seen.add("highest"); }
    }

    public static final class ReceiveCanceledHandler {
        public int defaultCalls = 0;
        public int alwaysCalls = 0;

        @SubscribeEvent
        public void onDefault(BlockEvent.BreakEvent e) { defaultCalls++; }

        @SubscribeEvent(receiveCanceled = true)
        public void onAlways(BlockEvent.BreakEvent e) { alwaysCalls++; }
    }

    @Test
    void registerInstanceScansAnnotatedMethodsOnly() {
        ForgeEventBus bus = new ForgeEventBus("test");
        InstanceHandler h = new InstanceHandler();
        bus.register(h);
        assertEquals(1, bus.listenerCount(), "only @SubscribeEvent methods must be wired");
    }

    @Test
    void postDeliversToInstanceHandler() {
        ForgeEventBus bus = new ForgeEventBus("test");
        InstanceHandler h = new InstanceHandler();
        bus.register(h);
        bus.post(new BlockEvent.BreakEvent("alice", 1, 2, 3, 4));
        assertEquals(1, h.calls);
        assertEquals(1, h.lastX);
    }

    @Test
    void registerInstanceSkipsStaticMethods() {
        ForgeEventBus bus = new ForgeEventBus("test");
        StaticHandler.calls = 0;
        bus.register(new StaticHandler());
        bus.post(new BlockEvent.BreakEvent("a", 0, 0, 0, 0));
        assertEquals(0, StaticHandler.calls, "instance-registration path must ignore static handlers");
    }

    @Test
    void registerClassWiresStaticOnly() {
        ForgeEventBus bus = new ForgeEventBus("test");
        StaticHandler.calls = 0;
        bus.register(StaticHandler.class);
        bus.post(new BlockEvent.BreakEvent("a", 0, 0, 0, 0));
        assertEquals(1, StaticHandler.calls);
    }

    @Test
    void bindSkipsHandlersWithBadSignature() {
        ForgeEventBus bus = new ForgeEventBus("test");
        bus.register(new BadSignatureHandler());
        assertEquals(0, bus.listenerCount(), "zero, two, or wrong-typed args must all be rejected");
    }

    @Test
    void postFiresSuperTypeListenersForSubclassedEvents() {
        ForgeEventBus bus = new ForgeEventBus("test");
        AtomicInteger hits = new AtomicInteger();
        bus.addListener(EventPriority.NORMAL, false, BlockEvent.class, e -> hits.incrementAndGet());
        bus.post(new BlockEvent.BreakEvent("a", 0, 0, 0, 0));
        assertEquals(1, hits.get(), "BreakEvent extends BlockEvent — superclass listener must fire");
    }

    @Test
    void postSkipsListenersForUnrelatedEventTypes() {
        ForgeEventBus bus = new ForgeEventBus("test");
        AtomicInteger hits = new AtomicInteger();
        bus.addListener(EventPriority.NORMAL, false, BlockEvent.BreakEvent.class, e -> hits.incrementAndGet());
        bus.post(new TickEvent.ServerTickEvent(TickEvent.Phase.START));
        assertEquals(0, hits.get());
    }

    @Test
    void priorityOrderingFollowsOrdinalHighestFirst() {
        ForgeEventBus bus = new ForgeEventBus("test");
        List<String> order = new ArrayList<>();
        bus.addListener(EventPriority.LOWEST, false, BlockEvent.BreakEvent.class,
                e -> order.add("lowest"));
        bus.addListener(EventPriority.HIGHEST, false, BlockEvent.BreakEvent.class,
                e -> order.add("highest"));
        bus.addListener(EventPriority.NORMAL, false, BlockEvent.BreakEvent.class,
                e -> order.add("normal"));
        bus.post(new BlockEvent.BreakEvent("a", 0, 0, 0, 0));
        assertEquals(List.of("highest", "normal", "lowest"), order,
                "HIGHEST has ordinal 0, LOWEST has ordinal 4 — sort runs HIGHEST first");
    }

    @Test
    void canceledEventSkipsDefaultListenersButReachesReceiveCanceled() {
        ForgeEventBus bus = new ForgeEventBus("test");
        AtomicBoolean defaultHit = new AtomicBoolean();
        AtomicBoolean canceledHit = new AtomicBoolean();
        bus.addListener(EventPriority.NORMAL, false, BlockEvent.BreakEvent.class,
                e -> defaultHit.set(true));
        bus.addListener(EventPriority.NORMAL, true, BlockEvent.BreakEvent.class,
                e -> canceledHit.set(true));
        BlockEvent.BreakEvent ev = new BlockEvent.BreakEvent("a", 0, 0, 0, 0);
        ev.setCanceled(true);
        bus.post(ev);
        assertFalse(defaultHit.get(), "default listener must skip canceled events");
        assertTrue(canceledHit.get(), "receiveCanceled listener must still fire");
    }

    @Test
    void clearRemovesAllListeners() {
        ForgeEventBus bus = new ForgeEventBus("test");
        bus.register(new InstanceHandler());
        bus.addListener(EventPriority.NORMAL, false, BlockEvent.class, e -> {});
        assertEquals(2, bus.listenerCount());
        bus.clear();
        assertEquals(0, bus.listenerCount());
    }

    @Test
    void annotatedPriorityIsHonoured() {
        ForgeEventBus bus = new ForgeEventBus("test");
        PriorityHandler p = new PriorityHandler();
        bus.register(p);
        bus.post(new BlockEvent.BreakEvent("a", 0, 0, 0, 0));
        assertEquals(List.of("highest", "lowest"), p.seen);
    }

    @Test
    void receiveCanceledAnnotationFlagPassesThroughCanceledEvents() {
        ForgeEventBus bus = new ForgeEventBus("test");
        ReceiveCanceledHandler h = new ReceiveCanceledHandler();
        bus.register(h);
        BlockEvent.BreakEvent ev = new BlockEvent.BreakEvent("a", 0, 0, 0, 0);
        ev.setCanceled(true);
        bus.post(ev);
        assertEquals(0, h.defaultCalls);
        assertEquals(1, h.alwaysCalls);
    }
}
