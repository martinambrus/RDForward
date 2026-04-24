package com.github.martinambrus.rdforward.api.event;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrioritizedEventTest {

    @FunctionalInterface
    interface Callback {
        EventResult run(List<String> trace);
    }

    private PrioritizedEvent<Callback> event;
    private BiPredicate<PrioritizedEvent<?>, ListenerInfo> savedGate;

    @BeforeEach
    void setUp() {
        savedGate = PrioritizedEvent.dispatchGate;
        event = PrioritizedEvent.create(
                trace -> EventResult.PASS,
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

    @AfterEach
    void tearDown() {
        PrioritizedEvent.dispatchGate = savedGate;
        event.clearListeners();
    }

    @Test
    void listenersFireInPriorityOrder() {
        event.register(EventPriority.HIGHEST, t -> { t.add("highest"); return EventResult.PASS; }, "a");
        event.register(EventPriority.LOWEST,  t -> { t.add("lowest"); return EventResult.PASS; }, "b");
        event.register(EventPriority.NORMAL,  t -> { t.add("normal"); return EventResult.PASS; }, "c");
        event.register(EventPriority.HIGH,    t -> { t.add("high"); return EventResult.PASS; }, "d");
        event.register(EventPriority.LOW,     t -> { t.add("low"); return EventResult.PASS; }, "e");

        List<String> trace = new ArrayList<>();
        EventResult result = event.invoker().run(trace);

        assertEquals(List.of("lowest", "low", "normal", "high", "highest"), trace);
        assertEquals(EventResult.PASS, result);
    }

    @Test
    void stopOnCancelSkipsRemainingDispatchButRunsMonitors() {
        event.register(EventPriority.LOWEST, t -> { t.add("lowest"); return EventResult.PASS; }, "a");
        event.register(EventPriority.NORMAL, t -> { t.add("normal"); return EventResult.FAIL; }, "b");
        event.register(EventPriority.HIGH,   t -> { t.add("high"); return EventResult.PASS; }, "c");
        event.register(EventPriority.MONITOR, t -> { t.add("monitor"); return EventResult.PASS; }, "d");

        List<String> trace = new ArrayList<>();
        EventResult result = event.invoker().run(trace);

        assertEquals(List.of("lowest", "normal", "monitor"), trace);
        assertEquals(EventResult.FAIL, result);
    }

    @Test
    void monitorsAlwaysRunEvenOnCancel() {
        event.register(EventPriority.LOWEST,  t -> { t.add("cancel"); return EventResult.FAIL; }, "a");
        event.register(EventPriority.MONITOR, t -> { t.add("mon1"); return EventResult.PASS; }, "b");
        event.register(EventPriority.MONITOR, t -> { t.add("mon2"); return EventResult.PASS; }, "c");

        List<String> trace = new ArrayList<>();
        event.invoker().run(trace);

        assertEquals(List.of("cancel", "mon1", "mon2"), trace);
    }

    @Test
    void unregisterByOwnerRemovesAcrossPriorities() {
        Callback keep = t -> { t.add("keep"); return EventResult.PASS; };
        event.register(EventPriority.LOWEST, t -> { t.add("a-low"); return EventResult.PASS; }, "modA");
        event.register(EventPriority.HIGH,   t -> { t.add("a-high"); return EventResult.PASS; }, "modA");
        event.register(EventPriority.NORMAL, keep, "modB");

        assertTrue(event.unregisterByOwner("modA"));

        List<String> trace = new ArrayList<>();
        event.invoker().run(trace);
        assertEquals(List.of("keep"), trace);
    }

    @Test
    void unregisterByOwnerReturnsFalseWhenNothingRemoved() {
        event.register(EventPriority.NORMAL, t -> EventResult.PASS, "modA");
        assertFalse(event.unregisterByOwner("nonexistent"));
    }

    @Test
    void unregisterAllByOwnerSweepsAcrossEvents() {
        PrioritizedEvent<Callback> other = PrioritizedEvent.create(
                t -> EventResult.PASS,
                (d, m) -> t -> EventResult.PASS);

        event.register(EventPriority.NORMAL, t -> EventResult.PASS, "target");
        other.register(EventPriority.NORMAL, t -> EventResult.PASS, "target");
        other.register(EventPriority.NORMAL, t -> EventResult.PASS, "keep");

        int affected = PrioritizedEvent.unregisterAllByOwner("target");
        assertEquals(2, affected);

        assertTrue(event.getListenerInfo().isEmpty());
        assertEquals(1, other.getListenerInfo().size());
        assertEquals("keep", other.getListenerInfo().get(0).modId());
        other.clearListeners();
    }

    @Test
    void dispatchGateCanFilterListeners() {
        event.register(EventPriority.NORMAL, t -> { t.add("allowed"); return EventResult.PASS; }, "ok");
        event.register(EventPriority.NORMAL, t -> { t.add("blocked"); return EventResult.PASS; }, "banned");

        PrioritizedEvent.dispatchGate = (ev, info) -> !"banned".equals(info.modId());
        event.refresh();

        List<String> trace = new ArrayList<>();
        event.invoker().run(trace);

        assertEquals(List.of("allowed"), trace);
    }

    @Test
    void emptyInvokerWhenNoListeners() {
        List<String> trace = new ArrayList<>();
        EventResult result = event.invoker().run(trace);
        assertTrue(trace.isEmpty());
        assertEquals(EventResult.PASS, result);
    }

    @Test
    void getListenerInfoIncludesOwnerAndPriority() {
        event.register(EventPriority.HIGH, t -> EventResult.PASS, "modX");
        event.register(EventPriority.MONITOR, t -> EventResult.PASS, "modY");

        List<ListenerInfo> info = event.getListenerInfo();
        assertEquals(2, info.size());
        assertTrue(info.stream().anyMatch(i -> i.modId().equals("modX") && i.priority() == EventPriority.HIGH));
        assertTrue(info.stream().anyMatch(i -> i.modId().equals("modY") && i.priority() == EventPriority.MONITOR));
    }
}
