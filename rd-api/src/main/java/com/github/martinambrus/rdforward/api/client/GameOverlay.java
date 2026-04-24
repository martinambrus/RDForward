package com.github.martinambrus.rdforward.api.client;

/**
 * Persistent HUD element drawn every frame by {@code OverlayRegistry}.
 * Overlays render after the built-in HUD and before {@code RENDER_HUD}
 * events. Use raw GL or GLFW calls here — this runs on the render thread
 * inside the existing matrix state.
 */
public interface GameOverlay {

    void render(int screenWidth, int screenHeight);

    boolean isVisible();

    /** Release any GL resources (textures, VBOs) owned by this overlay. */
    void cleanup();
}
