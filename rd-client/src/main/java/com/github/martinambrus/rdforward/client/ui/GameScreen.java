package com.github.martinambrus.rdforward.client.ui;

/**
 * Base interface for full-screen UI overlays that capture input.
 *
 * Unlike {@link GameOverlay}, a GameScreen takes over input handling:
 * the game world continues rendering in the background but the player
 * cannot move or interact with it. The mouse cursor is visible.
 *
 * Examples: server browser, settings menu, inventory screen.
 *
 * Only one GameScreen can be active at a time. Opening a new screen
 * closes the current one.
 *
 * This interface will be implemented when Alpha stage adds graphical
 * menus. For now it defines the contract so the mixin render hook
 * can check for active screens.
 */
public interface GameScreen {

    /**
     * Called when this screen is opened. Set up state, show cursor.
     *
     * @param window the GLFW window handle
     */
    void open(long window);

    /**
     * Called when this screen is closed. Restore cursor state.
     */
    void close();

    /**
     * Whether this screen is currently active and capturing input.
     */
    boolean isActive();

    /**
     * Render this screen's UI elements.
     *
     * @param screenWidth  current window width in pixels
     * @param screenHeight current window height in pixels
     */
    void render(int screenWidth, int screenHeight);

    /**
     * Handle a key press event while this screen is active.
     *
     * @param key      the GLFW key code
     * @param scancode the platform-specific scancode
     * @param action   GLFW_PRESS, GLFW_RELEASE, or GLFW_REPEAT
     * @param mods     bitfield of modifier keys (Shift, Ctrl, Alt, Super)
     * @return true if the event was consumed (should not propagate)
     */
    boolean handleKey(int key, int scancode, int action, int mods);

    /**
     * Handle a character input event (for text fields).
     *
     * @param codepoint the Unicode code point
     */
    void handleChar(int codepoint);

    /**
     * Handle a mouse click event.
     *
     * @param button the GLFW mouse button (0 = left, 1 = right, 2 = middle)
     * @param action GLFW_PRESS or GLFW_RELEASE
     * @param mouseX mouse X position in screen pixels
     * @param mouseY mouse Y position in screen pixels
     * @return true if the event was consumed
     */
    boolean handleClick(int button, int action, double mouseX, double mouseY);

    /**
     * Release any GL resources held by this screen.
     * Called on game shutdown.
     */
    void cleanup();
}
