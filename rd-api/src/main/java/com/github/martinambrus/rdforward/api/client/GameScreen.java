package com.github.martinambrus.rdforward.api.client;

/**
 * Full-screen UI that captures input exclusively. While a screen is active
 * the cursor is released, the 3D world stops receiving input, and all
 * keyboard / mouse events flow through the screen's handlers.
 *
 * <p>Managed by {@code ScreenManager}; only one screen is active at a time.
 */
public interface GameScreen {

    /** Called by the screen manager when this screen is opened. */
    void open(long window);

    /** Called by the screen manager when this screen is closed. */
    void close();

    boolean isActive();

    void render(int screenWidth, int screenHeight);

    /** @return true if the key event was consumed (not propagated to the game). */
    boolean handleKey(int key, int scancode, int action, int mods);

    void handleChar(int codepoint);

    /** @return true if the click was consumed. */
    boolean handleClick(int button, int action, double mouseX, double mouseY);

    /** Release any GL resources owned by this screen. */
    void cleanup();
}
