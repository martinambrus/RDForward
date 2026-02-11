package com.github.martinambrus.rdforward.client.api;

import com.github.martinambrus.rdforward.client.ui.GameOverlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registry for client-side render overlays. Mods register 2D overlays here;
 * the game loop renders all visible overlays each frame after the 3D scene.
 *
 * Overlays are rendered in registration order (first registered = rendered first,
 * i.e., behind later overlays).
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

    private static final List<GameOverlay> overlays = new ArrayList<>();

    /**
     * Register a new overlay.
     */
    public static void register(GameOverlay overlay) {
        overlays.add(overlay);
    }

    /**
     * Render all visible overlays. Called once per frame from the render loop.
     */
    public static void renderAll(int screenWidth, int screenHeight) {
        for (GameOverlay overlay : overlays) {
            if (overlay.isVisible()) {
                overlay.render(screenWidth, screenHeight);
            }
        }
    }

    /**
     * Clean up all registered overlays (e.g., on game shutdown).
     */
    public static void cleanupAll() {
        for (GameOverlay overlay : overlays) {
            overlay.cleanup();
        }
    }

    /**
     * Get all registered overlays (unmodifiable).
     */
    public static List<GameOverlay> getOverlays() {
        return Collections.unmodifiableList(overlays);
    }
}
