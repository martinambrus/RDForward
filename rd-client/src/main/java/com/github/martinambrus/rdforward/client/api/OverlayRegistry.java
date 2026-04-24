package com.github.martinambrus.rdforward.client.api;

import com.github.martinambrus.rdforward.api.event.EventOwnership;
import com.github.martinambrus.rdforward.api.mod.ResourceSweeper;
import com.github.martinambrus.rdforward.client.ui.GameOverlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for client-side render overlays. Mods register 2D overlays here;
 * the game loop renders all visible overlays each frame after the 3D scene.
 *
 * <p>Overlays are rendered in registration order (first registered = rendered
 * first, i.e., behind later overlays). Every registration is tagged with an
 * owner id read from {@link EventOwnership#currentOwner()} (or
 * {@code __server__} for core registrations). When a mod unloads,
 * {@link ResourceSweeper#sweep(String)} calls {@link #unregisterByOwner(String)}
 * to drop every overlay that mod registered.
 *
 * Example:
 * <pre>
 *   OverlayRegistry.register(new GameOverlay() {
 *       public void render(int screenWidth, int screenHeight) {
 *           // Draw custom HUD element
 *       }
 *       public boolean isVisible() { return true; }
 *       public void cleanup() {}
 *   });
 * </pre>
 */
public final class OverlayRegistry {

    private OverlayRegistry() {}

    public static final String SERVER_OWNER = "__server__";

    private static final List<GameOverlay> overlays = new ArrayList<>();
    private static final Map<GameOverlay, String> owners = new IdentityHashMap<>();

    static {
        ResourceSweeper.register(OverlayRegistry::unregisterByOwner);
    }

    /**
     * Register a new overlay under the current thread-local owner
     * (or {@link #SERVER_OWNER} if none).
     */
    public static synchronized void register(GameOverlay overlay) {
        String owner = EventOwnership.currentOwner();
        register(owner == null ? SERVER_OWNER : owner, overlay);
    }

    /** Register an overlay explicitly under {@code modId}. */
    public static synchronized void register(String modId, GameOverlay overlay) {
        overlays.add(overlay);
        owners.put(overlay, modId);
    }

    /** Unregister a specific overlay instance. No-op if not registered. */
    public static synchronized void unregister(GameOverlay overlay) {
        if (overlays.remove(overlay)) {
            owners.remove(overlay);
        }
    }

    /**
     * Remove every overlay owned by {@code modId}, running its
     * {@link GameOverlay#cleanup()} as it goes. Returns the count removed.
     */
    public static synchronized int unregisterByOwner(String modId) {
        int removed = 0;
        var it = overlays.iterator();
        while (it.hasNext()) {
            GameOverlay o = it.next();
            if (modId.equals(owners.get(o))) {
                try {
                    o.cleanup();
                } catch (RuntimeException ignored) {
                    // proceed with removal even if cleanup throws
                }
                it.remove();
                owners.remove(o);
                removed++;
            }
        }
        return removed;
    }

    /**
     * Render all visible overlays. Called once per frame from the render loop.
     */
    public static void renderAll(int screenWidth, int screenHeight) {
        List<GameOverlay> snapshot;
        synchronized (OverlayRegistry.class) {
            snapshot = new ArrayList<>(overlays);
        }
        for (GameOverlay overlay : snapshot) {
            if (overlay.isVisible()) {
                overlay.render(screenWidth, screenHeight);
            }
        }
    }

    /**
     * Clean up all registered overlays (e.g., on game shutdown).
     */
    public static void cleanupAll() {
        List<GameOverlay> snapshot;
        synchronized (OverlayRegistry.class) {
            snapshot = new ArrayList<>(overlays);
        }
        for (GameOverlay overlay : snapshot) {
            overlay.cleanup();
        }
    }

    /**
     * Get all registered overlays (unmodifiable snapshot).
     */
    public static synchronized List<GameOverlay> getOverlays() {
        return Collections.unmodifiableList(new ArrayList<>(overlays));
    }

    /** @return owner id for the given overlay, or {@code null} if unregistered. */
    public static synchronized String getOwner(GameOverlay overlay) {
        return owners.get(overlay);
    }
}
