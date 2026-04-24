package com.github.martinambrus.rdforward.client.api;

import com.github.martinambrus.rdforward.api.event.EventOwnership;
import com.github.martinambrus.rdforward.api.mod.ResourceSweeper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * KeyBindingRegistry is a static singleton. {@link KeyBindingRegistry#tick(long)}
 * needs an active GLFW window so it's only covered indirectly through the
 * window-handle guard; registration, ownership tracking, and sweep hooks are
 * covered here.
 */
class KeyBindingRegistryTest {

    @Test
    void registerAppendsToList() {
        int before = KeyBindingRegistry.getBindings().size();
        KeyBinding b = new KeyBinding("test-append", 65, () -> {});
        KeyBindingRegistry.register(b);
        assertEquals(before + 1, KeyBindingRegistry.getBindings().size());
        assertTrue(KeyBindingRegistry.getBindings().contains(b));
        // Clean up for the next test.
        KeyBindingRegistry.unregisterByOwner(KeyBindingRegistry.SERVER_OWNER);
    }

    @Test
    void getBindingsListIsUnmodifiable() {
        List<KeyBinding> view = KeyBindingRegistry.getBindings();
        assertThrows(UnsupportedOperationException.class,
                () -> view.add(new KeyBinding("x", 0, () -> {})),
                "returned list must not allow mutation — registry owns state");
    }

    @Test
    void tickWithZeroWindowIsEarlyReturnNoThrow() {
        // Even with registered bindings, a window handle of 0 must short-circuit
        // (game loop may tick before setWindow is called during startup).
        KeyBinding dormant = new KeyBinding("dormant", 71, () -> {
            throw new AssertionError("must not fire while window handle is 0");
        });
        KeyBindingRegistry.register(dormant);
        assertDoesNotThrow(() -> KeyBindingRegistry.tick(0L));
        KeyBindingRegistry.unregisterByOwner(KeyBindingRegistry.SERVER_OWNER);
    }

    @Test
    void setWindowAcceptsHandleWithoutSideEffectsOnBindings() {
        int before = KeyBindingRegistry.getBindings().size();
        KeyBindingRegistry.setWindow(0xdeadbeefL);
        assertEquals(before, KeyBindingRegistry.getBindings().size(),
                "setWindow must not touch the bindings list");
    }

    @Test
    void registerTagsCurrentOwnerFromThreadLocal() {
        KeyBinding b = new KeyBinding("owner-scoped", 42, () -> {});
        EventOwnership.withOwner("mod-alpha", () -> KeyBindingRegistry.register(b));
        assertEquals("mod-alpha", KeyBindingRegistry.getOwner(b));
        assertEquals(1, KeyBindingRegistry.unregisterByOwner("mod-alpha"));
        assertFalse(KeyBindingRegistry.getBindings().contains(b));
        assertNull(KeyBindingRegistry.getOwner(b));
    }

    @Test
    void registerOutsideOwnerTagsServerOwner() {
        KeyBinding b = new KeyBinding("server-owned", 43, () -> {});
        KeyBindingRegistry.register(b);
        assertEquals(KeyBindingRegistry.SERVER_OWNER, KeyBindingRegistry.getOwner(b));
        KeyBindingRegistry.unregisterByOwner(KeyBindingRegistry.SERVER_OWNER);
    }

    @Test
    void resourceSweeperHookRemovesOnlyMatchingOwner() {
        KeyBinding a = new KeyBinding("modA-key", 44, () -> {});
        KeyBinding b = new KeyBinding("modB-key", 45, () -> {});
        EventOwnership.withOwner("mod-a", () -> KeyBindingRegistry.register(a));
        EventOwnership.withOwner("mod-b", () -> KeyBindingRegistry.register(b));

        int removed = ResourceSweeper.sweep("mod-a");
        assertTrue(removed >= 1, "sweep should report at least the 1 binding we just registered");
        assertFalse(KeyBindingRegistry.getBindings().contains(a));
        assertTrue(KeyBindingRegistry.getBindings().contains(b));

        KeyBindingRegistry.unregisterByOwner("mod-b");
    }
}
