package com.github.martinambrus.rdforward.render.desktop;

import com.github.martinambrus.rdforward.render.RDInput;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWKeyCallback;

/**
 * GLFW-based input implementation for the LWJGL 3 desktop backend.
 */
public class LwjglInput implements RDInput {

    private final long window;
    private volatile float mouseDX;
    private volatile float mouseDY;
    private double lastMouseX = Double.NaN;
    private double lastMouseY = Double.NaN;
    private boolean mouseGrabbed;

    private CharCallback charCallback;
    private KeyCallback keyCallback;

    // Hold references to prevent GC of the GLFW callbacks
    @SuppressWarnings("FieldCanBeLocal")
    private GLFWCharCallback glfwCharCallback;
    @SuppressWarnings("FieldCanBeLocal")
    private GLFWKeyCallback glfwKeyCallback;

    public LwjglInput(long window) {
        this.window = window;

        // Cursor position → accumulate delta
        GLFW.glfwSetCursorPosCallback(window, (w, xpos, ypos) -> {
            if (Double.isNaN(lastMouseX)) {
                lastMouseX = xpos;
                lastMouseY = ypos;
            }
            mouseDX += (float) (xpos - lastMouseX);
            mouseDY += (float) (ypos - lastMouseY);
            lastMouseX = xpos;
            lastMouseY = ypos;
        });

        // Character input → forward to user callback
        glfwCharCallback = GLFW.glfwSetCharCallback(window, (w, codepoint) -> {
            CharCallback cb = charCallback;
            if (cb != null) cb.onChar(codepoint);
        });

        // Key events → forward to user callback
        glfwKeyCallback = GLFW.glfwSetKeyCallback(window, (w, key, scancode, action, mods) -> {
            KeyCallback cb = keyCallback;
            if (cb != null) cb.onKey(key, action, mods);
        });
    }

    // ── Keyboard ───────────────────────────────────────────────────────

    @Override
    public boolean isKeyDown(int keyCode) {
        return GLFW.glfwGetKey(window, keyCode) == GLFW.GLFW_PRESS;
    }

    @Override
    public void setCharCallback(CharCallback callback) {
        this.charCallback = callback;
    }

    @Override
    public void setKeyCallback(KeyCallback callback) {
        this.keyCallback = callback;
    }

    // ── Mouse ──────────────────────────────────────────────────────────

    @Override
    public float consumeMouseDX() {
        float dx = mouseDX;
        mouseDX = 0;
        return dx;
    }

    @Override
    public float consumeMouseDY() {
        float dy = mouseDY;
        mouseDY = 0;
        return dy;
    }

    @Override
    public boolean isMouseButtonDown(int button) {
        return GLFW.glfwGetMouseButton(window, button) == GLFW.GLFW_PRESS;
    }

    @Override
    public void grabMouse() {
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
        mouseGrabbed = true;
        // Reset delta accumulators on grab to avoid jumps
        lastMouseX = Double.NaN;
        lastMouseY = Double.NaN;
        mouseDX = 0;
        mouseDY = 0;
    }

    @Override
    public void releaseMouse() {
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        mouseGrabbed = false;
    }

    @Override
    public boolean isMouseGrabbed() {
        return mouseGrabbed;
    }

    // ── Screen ─────────────────────────────────────────────────────────

    @Override
    public int getScreenWidth() {
        int[] w = new int[1];
        GLFW.glfwGetWindowSize(window, w, new int[1]);
        return w[0];
    }

    @Override
    public int getScreenHeight() {
        int[] h = new int[1];
        GLFW.glfwGetWindowSize(window, new int[1], h);
        return h[0];
    }
}
