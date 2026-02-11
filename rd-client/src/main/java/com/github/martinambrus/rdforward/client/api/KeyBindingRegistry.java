package com.github.martinambrus.rdforward.client.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registry for client-side key bindings. Mods register custom keys here;
 * the game loop polls all bindings each frame via {@link #tick(long)}.
 *
 * Example:
 * <pre>
 *   KeyBindingRegistry.register(new KeyBinding("Fly Mode", GLFW_KEY_G, () -> {
 *       System.out.println("Fly mode toggled!");
 *   }));
 * </pre>
 */
public final class KeyBindingRegistry {

    private KeyBindingRegistry() {}

    private static final List<KeyBinding> bindings = new ArrayList<>();
    private static long windowHandle = 0;

    /**
     * Set the GLFW window handle (required for key state polling).
     * Called once during game initialization.
     */
    public static void setWindow(long window) {
        windowHandle = window;
    }

    /**
     * Register a new key binding.
     */
    public static void register(KeyBinding binding) {
        bindings.add(binding);
    }

    /**
     * Poll all registered key bindings. Called once per frame from the render loop.
     * Uses GLFW to check key state directly.
     */
    public static void tick(long window) {
        if (window == 0) return;
        for (KeyBinding binding : bindings) {
            boolean pressed = org.lwjgl.glfw.GLFW.glfwGetKey(window, binding.getKeyCode())
                    == org.lwjgl.glfw.GLFW.GLFW_PRESS;
            binding.update(pressed);
        }
    }

    /**
     * Get all registered bindings (unmodifiable).
     */
    public static List<KeyBinding> getBindings() {
        return Collections.unmodifiableList(bindings);
    }
}
