package com.github.martinambrus.rdforward.protocol.event;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    @FunctionalInterface
    interface SimpleCallback {
        void invoke();
    }

    @FunctionalInterface
    interface StringCallback {
        EventResult onMessage(String message);
    }

    @Test
    void emptyEventUsesEmptyInvoker() {
        AtomicInteger counter = new AtomicInteger(0);
        Event<SimpleCallback> event = Event.create(
                () -> counter.set(-1),
                listeners -> () -> listeners.forEach(SimpleCallback::invoke)
        );

        // Empty invoker should run
        event.invoker().invoke();
        assertEquals(-1, counter.get());
    }

    @Test
    void registeredListenerIsCalled() {
        AtomicInteger counter = new AtomicInteger(0);
        Event<SimpleCallback> event = Event.create(
                () -> {},
                listeners -> () -> listeners.forEach(SimpleCallback::invoke)
        );

        event.register(counter::incrementAndGet);
        event.invoker().invoke();
        assertEquals(1, counter.get());
    }

    @Test
    void multipleListenersCalledInOrder() {
        StringBuilder order = new StringBuilder();
        Event<SimpleCallback> event = Event.create(
                () -> {},
                listeners -> () -> listeners.forEach(SimpleCallback::invoke)
        );

        event.register(() -> order.append("A"));
        event.register(() -> order.append("B"));
        event.register(() -> order.append("C"));
        event.invoker().invoke();
        assertEquals("ABC", order.toString());
    }

    @Test
    void listenerCountTracked() {
        Event<SimpleCallback> event = Event.create(
                () -> {},
                listeners -> () -> listeners.forEach(SimpleCallback::invoke)
        );

        assertEquals(0, event.listenerCount());
        event.register(() -> {});
        assertEquals(1, event.listenerCount());
        event.register(() -> {});
        assertEquals(2, event.listenerCount());
    }

    @Test
    void cancellableEventStopsOnCancel() {
        AtomicReference<String> lastCalled = new AtomicReference<>("");
        Event<StringCallback> event = Event.create(
                msg -> EventResult.PASS,
                listeners -> msg -> {
                    for (StringCallback l : listeners) {
                        EventResult result = l.onMessage(msg);
                        if (result != EventResult.PASS) return result;
                    }
                    return EventResult.PASS;
                }
        );

        event.register(msg -> { lastCalled.set("first"); return EventResult.PASS; });
        event.register(msg -> { lastCalled.set("second"); return EventResult.CANCEL; });
        event.register(msg -> { lastCalled.set("third"); return EventResult.PASS; });

        EventResult result = event.invoker().onMessage("test");
        assertEquals(EventResult.CANCEL, result);
        assertEquals("second", lastCalled.get()); // third was never called
    }

    @Test
    void passResultContinuesToNextListener() {
        AtomicInteger callCount = new AtomicInteger(0);
        Event<StringCallback> event = Event.create(
                msg -> EventResult.PASS,
                listeners -> msg -> {
                    for (StringCallback l : listeners) {
                        EventResult result = l.onMessage(msg);
                        if (result != EventResult.PASS) return result;
                    }
                    return EventResult.PASS;
                }
        );

        event.register(msg -> { callCount.incrementAndGet(); return EventResult.PASS; });
        event.register(msg -> { callCount.incrementAndGet(); return EventResult.PASS; });

        EventResult result = event.invoker().onMessage("test");
        assertEquals(EventResult.PASS, result);
        assertEquals(2, callCount.get());
    }
}
