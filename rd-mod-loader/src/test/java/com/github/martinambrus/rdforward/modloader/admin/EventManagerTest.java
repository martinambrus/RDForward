package com.github.martinambrus.rdforward.modloader.admin;

import com.github.martinambrus.rdforward.api.event.EventPriority;
import com.github.martinambrus.rdforward.api.event.EventResult;
import com.github.martinambrus.rdforward.api.event.PrioritizedEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventManagerTest {

    @FunctionalInterface
    interface Callback { EventResult run(List<String> trace); }

    static final class Holder {
        public static final PrioritizedEvent<Callback> SAMPLE = PrioritizedEvent.create(
                t -> EventResult.PASS,
                (dispatch, monitor) -> trace -> {
                    EventResult outcome = EventResult.PASS;
                    for (Callback c : dispatch) {
                        EventResult r = c.run(trace);
                        if (r != EventResult.PASS) { outcome = r; break; }
                    }
                    for (Callback c : monitor) c.run(trace);
                    return outcome;
                });
    }

    @BeforeEach
    void setUp() {
        Holder.SAMPLE.clearListeners();
    }

    @AfterEach
    void tearDown() {
        EventManager.clearAll();
        Holder.SAMPLE.clearListeners();
        // dispatchGate is left pointing at EventManager::allow — disabledKeys
        // has been emptied so the gate is effectively a no-op for later tests.
    }

    @Test
    void installDiscoversStaticPrioritizedEventFields(@TempDir Path dir) {
        EventManager.install(List.of(Holder.class), dir.resolve("ev.json"));
        assertTrue(EventManager.eventIds().contains("Holder#SAMPLE"));
    }

    @Test
    void snapshotListsEveryRegisteredListener(@TempDir Path dir) {
        EventManager.install(List.of(Holder.class), dir.resolve("ev.json"));
        Callback l = t -> EventResult.PASS;
        Holder.SAMPLE.register(EventPriority.NORMAL, l, "modA");

        List<EventManager.Entry> snap = EventManager.snapshot();
        assertEquals(1, snap.size());
        assertEquals("Holder#SAMPLE", snap.get(0).eventId());
        assertEquals("modA", snap.get(0).listener().modId());
        assertFalse(snap.get(0).disabled());
    }

    @Test
    void disableSkipsListenerOnNextDispatch(@TempDir Path dir) {
        EventManager.install(List.of(Holder.class), dir.resolve("ev.json"));
        Callback l = t -> { t.add("fired"); return EventResult.PASS; };
        Holder.SAMPLE.register(EventPriority.NORMAL, l, "modA");

        int disabled = EventManager.disable("Holder#SAMPLE", "modA", EventPriority.NORMAL, l.getClass().getName());
        assertEquals(1, disabled);

        List<String> trace = new ArrayList<>();
        Holder.SAMPLE.invoker().run(trace);
        assertTrue(trace.isEmpty());
    }

    @Test
    void enableReActivatesDisabledListener(@TempDir Path dir) {
        EventManager.install(List.of(Holder.class), dir.resolve("ev.json"));
        Callback l = t -> { t.add("x"); return EventResult.PASS; };
        Holder.SAMPLE.register(EventPriority.NORMAL, l, "modA");

        EventManager.disable("Holder#SAMPLE", "modA", EventPriority.NORMAL, l.getClass().getName());
        int re = EventManager.enable("Holder#SAMPLE", "modA", EventPriority.NORMAL, l.getClass().getName());
        assertEquals(1, re);

        List<String> trace = new ArrayList<>();
        Holder.SAMPLE.invoker().run(trace);
        assertEquals(List.of("x"), trace);
    }

    @Test
    void disablePersistsToFile(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("ev.json");
        EventManager.install(List.of(Holder.class), file);
        Callback l = t -> EventResult.PASS;
        Holder.SAMPLE.register(EventPriority.NORMAL, l, "modA");

        EventManager.disable("Holder#SAMPLE", "modA", EventPriority.NORMAL, l.getClass().getName());
        String json = Files.readString(file);
        assertTrue(json.contains("Holder#SAMPLE"));
        assertTrue(json.contains("modA"));
    }

    @Test
    void clearAllReactivatesEverything(@TempDir Path dir) {
        EventManager.install(List.of(Holder.class), dir.resolve("ev.json"));
        Callback l = t -> { t.add("ok"); return EventResult.PASS; };
        Holder.SAMPLE.register(EventPriority.NORMAL, l, "modA");
        EventManager.disable("Holder#SAMPLE", "modA", EventPriority.NORMAL, l.getClass().getName());

        EventManager.clearAll();
        List<String> trace = new ArrayList<>();
        Holder.SAMPLE.invoker().run(trace);
        assertEquals(List.of("ok"), trace);
    }

    @Test
    void unknownEventIdDisableReturnsZero(@TempDir Path dir) {
        EventManager.install(List.of(Holder.class), dir.resolve("ev.json"));
        int n = EventManager.disable("does_not_exist", "x", EventPriority.NORMAL, "y");
        assertEquals(0, n);
    }

    @Test
    void applyOverridesDropsOverridesForMissingMods(@TempDir Path dir) {
        EventManager.install(List.of(Holder.class), dir.resolve("ev.json"));
        Callback l = t -> EventResult.PASS;
        Holder.SAMPLE.register(EventPriority.NORMAL, l, "gone-mod");
        assertTrue(EventManager.setPriority("Holder#SAMPLE", "gone-mod", EventPriority.HIGH));
        assertFalse(EventManager.overridesSnapshot().isEmpty());

        // Simulate restart: mod no longer loaded.
        EventManager.applyOverrides(id -> false);

        assertTrue(EventManager.overridesSnapshot().isEmpty(),
                "overrides for missing mods should be dropped on reconciliation");
    }

    @Test
    void applyOverridesDropsOverridesForStaleListeners(@TempDir Path dir) {
        EventManager.install(List.of(Holder.class), dir.resolve("ev.json"));
        Callback l = t -> EventResult.PASS;
        Holder.SAMPLE.register(EventPriority.NORMAL, l, "modA");
        assertTrue(EventManager.setPriority("Holder#SAMPLE", "modA", EventPriority.HIGH));

        Holder.SAMPLE.clearListeners();
        EventManager.applyOverrides(id -> true);

        assertTrue(EventManager.overridesSnapshot().isEmpty(),
                "overrides should be dropped when the listener is no longer registered");
    }

    @Test
    void applyOverridesReapplySurvivingOverride(@TempDir Path dir) {
        EventManager.install(List.of(Holder.class), dir.resolve("ev.json"));
        Callback l = t -> EventResult.PASS;
        Holder.SAMPLE.register(EventPriority.NORMAL, l, "modA");
        assertTrue(EventManager.setPriority("Holder#SAMPLE", "modA", EventPriority.HIGH));

        Holder.SAMPLE.clearListeners();
        Holder.SAMPLE.register(EventPriority.NORMAL, l, "modA");
        EventManager.applyOverrides(id -> true);

        assertEquals(EventPriority.HIGH, Holder.SAMPLE.findPriority("modA", null),
                "surviving override must be re-applied after reconciliation");
        assertEquals(1, EventManager.overridesSnapshot().size());
    }

    @Test
    void overridesReloadFromFile(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("ev.json");
        EventManager.install(List.of(Holder.class), file);
        Callback l = t -> { t.add("fired"); return EventResult.PASS; };
        Holder.SAMPLE.register(EventPriority.NORMAL, l, "modA");
        EventManager.disable("Holder#SAMPLE", "modA", EventPriority.NORMAL, l.getClass().getName());

        Holder.SAMPLE.clearListeners();
        EventManager.install(List.of(Holder.class), file);
        Holder.SAMPLE.register(EventPriority.NORMAL, l, "modA");

        List<String> trace = new ArrayList<>();
        Holder.SAMPLE.invoker().run(trace);
        assertTrue(trace.isEmpty(), "persisted override should still block listener after re-install");
    }
}
