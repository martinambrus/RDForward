package com.github.martinambrus.rdforward.client.api;

import com.github.martinambrus.rdforward.api.event.EventOwnership;
import com.github.martinambrus.rdforward.api.mod.ResourceSweeper;
import com.github.martinambrus.rdforward.client.ui.GameOverlay;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * OverlayRegistry is a static singleton holding registered {@link GameOverlay}s.
 * Tests exercise registration, render-order, visibility gating, and cleanup —
 * no GL context needed because GameOverlay is a plain interface.
 *
 * <p>Note: the registry has no {@code clearAll()} — each test appends to a
 * growing list. Tests only assert on the delta they introduce.
 */
class OverlayRegistryTest {

    private static final class RecordingOverlay implements GameOverlay {
        final AtomicInteger renders = new AtomicInteger();
        final AtomicInteger cleanups = new AtomicInteger();
        boolean visible = true;
        int lastW = -1, lastH = -1;

        @Override public void render(int w, int h) { renders.incrementAndGet(); lastW = w; lastH = h; }
        @Override public boolean isVisible() { return visible; }
        @Override public void cleanup() { cleanups.incrementAndGet(); }
    }

    @Test
    void registerAppendsToList() {
        int before = OverlayRegistry.getOverlays().size();
        RecordingOverlay o = new RecordingOverlay();
        OverlayRegistry.register(o);
        assertEquals(before + 1, OverlayRegistry.getOverlays().size());
        assertTrue(OverlayRegistry.getOverlays().contains(o));
    }

    @Test
    void getOverlaysListIsUnmodifiable() {
        List<GameOverlay> view = OverlayRegistry.getOverlays();
        assertThrows(UnsupportedOperationException.class,
                () -> view.add(new RecordingOverlay()),
                "returned list must not allow mutation — registry owns state");
    }

    @Test
    void renderAllForwardsDimensionsToVisibleOverlays() {
        RecordingOverlay a = new RecordingOverlay();
        RecordingOverlay b = new RecordingOverlay();
        b.visible = false;
        OverlayRegistry.register(a);
        OverlayRegistry.register(b);

        OverlayRegistry.renderAll(800, 600);

        assertEquals(1, a.renders.get(), "visible overlay should render");
        assertEquals(0, b.renders.get(), "invisible overlay must be skipped");
        assertEquals(800, a.lastW);
        assertEquals(600, a.lastH);
    }

    @Test
    void cleanupAllCallsEveryOverlayExactlyOnce() {
        // Snapshot existing cleanups baseline so other tests' leftovers
        // can't sway the count.
        List<GameOverlay> before = new ArrayList<>(OverlayRegistry.getOverlays());
        RecordingOverlay x = new RecordingOverlay();
        RecordingOverlay y = new RecordingOverlay();
        OverlayRegistry.register(x);
        OverlayRegistry.register(y);

        OverlayRegistry.cleanupAll();

        assertEquals(1, x.cleanups.get());
        assertEquals(1, y.cleanups.get());
        // Sanity: cleanup doesn't remove entries (the registry is append-only).
        assertTrue(OverlayRegistry.getOverlays().containsAll(before));

        OverlayRegistry.unregisterByOwner(OverlayRegistry.SERVER_OWNER);
    }

    @Test
    void registerTagsCurrentOwnerFromThreadLocal() {
        RecordingOverlay o = new RecordingOverlay();
        EventOwnership.withOwner("mod-over", () -> OverlayRegistry.register(o));
        assertEquals("mod-over", OverlayRegistry.getOwner(o));
        assertEquals(1, OverlayRegistry.unregisterByOwner("mod-over"));
        assertFalse(OverlayRegistry.getOverlays().contains(o));
        assertNull(OverlayRegistry.getOwner(o));
        // unregisterByOwner runs cleanup() on the overlay.
        assertEquals(1, o.cleanups.get());
    }

    @Test
    void registerOutsideOwnerTagsServerOwner() {
        RecordingOverlay o = new RecordingOverlay();
        OverlayRegistry.register(o);
        assertEquals(OverlayRegistry.SERVER_OWNER, OverlayRegistry.getOwner(o));
        OverlayRegistry.unregisterByOwner(OverlayRegistry.SERVER_OWNER);
    }

    @Test
    void resourceSweeperHookRemovesOnlyMatchingOwner() {
        RecordingOverlay a = new RecordingOverlay();
        RecordingOverlay b = new RecordingOverlay();
        EventOwnership.withOwner("mod-p", () -> OverlayRegistry.register(a));
        EventOwnership.withOwner("mod-q", () -> OverlayRegistry.register(b));

        int removed = ResourceSweeper.sweep("mod-p");
        assertTrue(removed >= 1, "sweep should remove at least the 1 overlay mod-p registered");
        assertFalse(OverlayRegistry.getOverlays().contains(a));
        assertTrue(OverlayRegistry.getOverlays().contains(b));

        OverlayRegistry.unregisterByOwner("mod-q");
    }

    @Test
    void explicitUnregisterAlsoDropsOwner() {
        RecordingOverlay o = new RecordingOverlay();
        EventOwnership.withOwner("mod-u", () -> OverlayRegistry.register(o));
        assertEquals("mod-u", OverlayRegistry.getOwner(o));
        OverlayRegistry.unregister(o);
        assertFalse(OverlayRegistry.getOverlays().contains(o));
        assertNull(OverlayRegistry.getOwner(o));
    }
}
