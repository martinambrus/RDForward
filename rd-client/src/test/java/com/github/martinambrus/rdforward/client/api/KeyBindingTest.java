package com.github.martinambrus.rdforward.client.api;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeyBindingTest {

    @Test
    void firesOnceOnPressEdge() {
        AtomicInteger fired = new AtomicInteger();
        KeyBinding b = new KeyBinding("fly", 42, fired::incrementAndGet);

        b.update(false);
        assertEquals(0, fired.get());

        b.update(true);
        assertEquals(1, fired.get(), "callback must fire on the press edge");
    }

    @Test
    void heldKeyDoesNotRefireUntilReleased() {
        AtomicInteger fired = new AtomicInteger();
        KeyBinding b = new KeyBinding("fly", 42, fired::incrementAndGet);

        b.update(true);
        b.update(true);
        b.update(true);
        b.update(true);
        assertEquals(1, fired.get(), "holding the key must not refire the callback each frame");
    }

    @Test
    void retriggerAfterReleaseFiresAgain() {
        AtomicInteger fired = new AtomicInteger();
        KeyBinding b = new KeyBinding("fly", 42, fired::incrementAndGet);

        b.update(true);
        b.update(false);
        b.update(true);
        assertEquals(2, fired.get(), "release -> press cycle must fire a second time");
    }

    @Test
    void releaseWithoutPressIsIgnored() {
        AtomicInteger fired = new AtomicInteger();
        KeyBinding b = new KeyBinding("fly", 42, fired::incrementAndGet);

        b.update(false);
        b.update(false);
        assertEquals(0, fired.get());
    }

    @Test
    void accessorsReturnConstructorValues() {
        KeyBinding b = new KeyBinding("Fly Mode", 71, () -> {});
        assertEquals("Fly Mode", b.getName());
        assertEquals(71, b.getKeyCode());
    }
}
