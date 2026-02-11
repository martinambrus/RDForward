package com.github.martinambrus.rdforward.client.ui;

/**
 * Base interface for HUD overlays that render on top of the game world.
 *
 * Overlays are rendered every frame in the 2D screen-space pass
 * (after 3D world rendering, before buffer swap). They do not pause
 * the game or capture input exclusively — the game continues running
 * underneath.
 *
 * Examples: chat display, player list (Tab), health bar, hotbar.
 *
 * Implementations should follow the existing rendering pattern
 * (static methods, Java2D text rasterization to GL texture).
 * When Alpha stage is reached, these will be connected to the
 * Alpha client's rendering pipeline.
 */
public interface GameOverlay {

    /**
     * Render this overlay on screen.
     *
     * @param screenWidth  current window width in pixels
     * @param screenHeight current window height in pixels
     */
    void render(int screenWidth, int screenHeight);

    /**
     * Whether this overlay is currently visible and should be rendered.
     */
    boolean isVisible();

    /**
     * Release any GL resources held by this overlay.
     * Called on game shutdown.
     */
    void cleanup();
}
