package com.github.martinambrus.rdforward.render;

/**
 * Platform-independent input abstraction.
 * <p>
 * Key codes use GLFW constants on desktop and are mapped from Android/libGDX
 * key codes on mobile. Mouse input is synthesised from touch events on Android.
 */
public interface RDInput {

    // ── Keyboard ───────────────────────────────────────────────────────

    /** @return true while the given key is held down. */
    boolean isKeyDown(int keyCode);

    /**
     * Install a character-typed callback (Unicode codepoints, for chat input).
     * Pass {@code null} to remove the callback.
     */
    void setCharCallback(CharCallback callback);

    /**
     * Install a key-event callback (for special keys like Enter, Escape).
     * Pass {@code null} to remove the callback.
     */
    void setKeyCallback(KeyCallback callback);

    // ── Mouse / Touch ──────────────────────────────────────────────────

    /** Accumulated horizontal mouse movement since last call (resets). */
    float consumeMouseDX();

    /** Accumulated vertical mouse movement since last call (resets). */
    float consumeMouseDY();

    /** @return true if the given mouse button is currently pressed. */
    boolean isMouseButtonDown(int button);

    /** Hide the cursor and lock it to the window centre (FPS mode). */
    void grabMouse();

    /** Show the cursor and allow free movement (UI mode). */
    void releaseMouse();

    /** @return true if the mouse is currently grabbed (FPS mode). */
    boolean isMouseGrabbed();

    // ── Screen ─────────────────────────────────────────────────────────

    /** @return current screen / surface width in pixels. */
    int getScreenWidth();

    /** @return current screen / surface height in pixels. */
    int getScreenHeight();

    // ── Callback interfaces ────────────────────────────────────────────

    @FunctionalInterface
    interface CharCallback {
        void onChar(int codepoint);
    }

    @FunctionalInterface
    interface KeyCallback {
        /**
         * @param key    the key code (GLFW constant on desktop)
         * @param action 0 = release, 1 = press, 2 = repeat
         * @param mods   modifier flags (shift, ctrl, alt, super)
         */
        void onKey(int key, int action, int mods);
    }
}
