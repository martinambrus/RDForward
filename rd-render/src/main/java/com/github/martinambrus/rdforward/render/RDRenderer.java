package com.github.martinambrus.rdforward.render;

/**
 * Main rendering system lifecycle. Each platform backend (LWJGL 3, libGDX,
 * Android) provides an implementation. The game code uses this interface
 * instead of making direct GL or GLFW calls.
 */
public interface RDRenderer {

    /** Initialise the window / surface and OpenGL context. */
    void init(int width, int height, String title);

    /** @return true when the user has requested the window to close. */
    boolean shouldClose();

    /** Poll windowing-system events (keyboard, mouse, resize, close). */
    void pollEvents();

    /** Swap front/back buffers (present the frame). */
    void swapBuffers();

    /** Release all GPU resources and destroy the window. */
    void dispose();

    /** Current framebuffer width in pixels. */
    int getWidth();

    /** Current framebuffer height in pixels. */
    int getHeight();

    /**
     * Opaque window handle for platform-specific use. On LWJGL 3 this is
     * the GLFW window pointer; on Android it is 0.
     */
    long getWindowHandle();

    /** Access the low-level graphics operations. */
    RDGraphics graphics();

    /** Access the input system. */
    RDInput input();
}
