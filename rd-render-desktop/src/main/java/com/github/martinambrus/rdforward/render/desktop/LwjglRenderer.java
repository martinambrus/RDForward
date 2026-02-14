package com.github.martinambrus.rdforward.render.desktop;

import com.github.martinambrus.rdforward.render.RDGraphics;
import com.github.martinambrus.rdforward.render.RDInput;
import com.github.martinambrus.rdforward.render.RDRenderer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

/**
 * LWJGL 3 / GLFW desktop backend for {@link RDRenderer}.
 * <p>
 * This wraps the same OpenGL 1.x fixed-function pipeline that the original
 * RubyDung code used, but behind the platform-independent interface.
 */
public class LwjglRenderer implements RDRenderer {

    private long window;
    private int width;
    private int height;
    private final LwjglGraphics graphics = new LwjglGraphics();
    private LwjglInput input;

    @Override
    public void init(int width, int height, String title) {
        this.width = width;
        this.height = height;

        GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialise GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);

        window = GLFW.glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }

        GLFW.glfwMakeContextCurrent(window);
        GL.createCapabilities();
        GLFW.glfwSwapInterval(1); // v-sync
        GLFW.glfwShowWindow(window);

        input = new LwjglInput(window);
    }

    @Override
    public boolean shouldClose() {
        return GLFW.glfwWindowShouldClose(window);
    }

    @Override
    public void pollEvents() {
        GLFW.glfwPollEvents();
    }

    @Override
    public void swapBuffers() {
        GLFW.glfwSwapBuffers(window);
    }

    @Override
    public void dispose() {
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
        GLFWErrorCallback cb = GLFW.glfwSetErrorCallback(null);
        if (cb != null) cb.free();
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public long getWindowHandle() {
        return window;
    }

    @Override
    public RDGraphics graphics() {
        return graphics;
    }

    @Override
    public RDInput input() {
        return input;
    }
}
