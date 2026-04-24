package com.github.martinambrus.rdforward.api.event;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventOwnershipTest {

    @FunctionalInterface
    interface Callback { void run(); }

    private Event<Callback> event;

    @BeforeEach
    void setUp() {
        EventOwnership.install();
        event = Event.create(
                () -> {},
                listeners -> () -> listeners.forEach(Callback::run));
    }

    @AfterEach
    void tearDown() {
        EventOwnership.unregisterAllForMod("modA");
        EventOwnership.unregisterAllForMod("modB");
        EventOwnership.unregisterAllForMod("outer");
        EventOwnership.unregisterAllForMod("inner");
        event.clearListeners();
    }

    @Test
    void listenerRegisteredWithinWithOwnerIsTagged() {
        Callback l = () -> {};
        EventOwnership.withOwner("modA", () -> event.register(l));

        Map<String, List<ListenerInfo>> snap = EventOwnership.snapshot();
        assertTrue(snap.containsKey("modA"));
        assertEquals(1, snap.get("modA").size());
    }

    @Test
    void listenerRegisteredOutsideOwnerScopeIsUntagged() {
        event.register(() -> {});
        Map<String, List<ListenerInfo>> snap = EventOwnership.snapshot();
        assertFalse(snap.containsKey(null));
    }

    @Test
    void unregisterAllForModRemovesFromEvent() {
        Callback l1 = () -> {};
        Callback l2 = () -> {};
        Callback l3 = () -> {};

        EventOwnership.withOwner("modA", () -> { event.register(l1); event.register(l2); });
        EventOwnership.withOwner("modB", () -> event.register(l3));

        int removed = EventOwnership.unregisterAllForMod("modA");
        assertEquals(2, removed);
        assertEquals(1, event.listenerCount());
    }

    @Test
    void nestedWithOwnerRestoresPreviousAfterInnerExits() {
        AtomicReference<String> seen = new AtomicReference<>();
        EventOwnership.withOwner("outer", () -> {
            EventOwnership.withOwner("inner", () -> {
                assertEquals("inner", EventOwnership.currentOwner());
            });
            seen.set(EventOwnership.currentOwner());
        });
        assertEquals("outer", seen.get());
        assertNull(EventOwnership.currentOwner());
    }

    @Test
    void currentOwnerIsNullOutsideWrapper() {
        assertNull(EventOwnership.currentOwner());
    }

    @Test
    void installIsIdempotent() {
        EventOwnership.install();
        EventOwnership.install();
        Callback l = () -> {};
        EventOwnership.withOwner("modA", () -> event.register(l));
        assertEquals(1, EventOwnership.unregisterAllForMod("modA"));
    }
}
