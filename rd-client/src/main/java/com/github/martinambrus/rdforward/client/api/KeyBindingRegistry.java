package com.github.martinambrus.rdforward.client.api;

import com.github.martinambrus.rdforward.api.event.EventOwnership;
import com.github.martinambrus.rdforward.api.mod.ResourceSweeper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for client-side key bindings. Mods register custom keys here;
 * the game loop polls all bindings each frame via {@link #tick(long)}.
 *
 * <p>Every registration is tagged with an owner id read from
 * {@link EventOwnership#currentOwner()} (or {@code __server__} for core
 * registrations). When a mod unloads, {@link ResourceSweeper#sweep(String)}
 * invokes {@link #unregisterByOwner(String)} to drop every binding that
 * mod registered.
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

    public static final String SERVER_OWNER = "__server__";

    private static final List<KeyBinding> bindings = new ArrayList<>();
    private static final Map<KeyBinding, String> owners = new IdentityHashMap<>();
    private static long windowHandle = 0;

    static {
        ResourceSweeper.register(KeyBindingRegistry::unregisterByOwner);
    }

    /**
     * Set the GLFW window handle (required for key state polling).
     * Called once during game initialization.
     */
    public static void setWindow(long window) {
        windowHandle = window;
    }

    /**
     * Register a new key binding under the current thread-local owner
     * (or {@link #SERVER_OWNER} if none).
     */
    public static synchronized void register(KeyBinding binding) {
        String owner = EventOwnership.currentOwner();
        register(owner == null ? SERVER_OWNER : owner, binding);
    }

    /** Register a key binding explicitly under {@code modId}. */
    public static synchronized void register(String modId, KeyBinding binding) {
        bindings.add(binding);
        owners.put(binding, modId);
    }

    /**
     * Remove every binding owned by {@code modId}. Returns the count removed.
     * Called by {@link ResourceSweeper} when a mod unloads.
     */
    public static synchronized int unregisterByOwner(String modId) {
        int removed = 0;
        var it = bindings.iterator();
        while (it.hasNext()) {
            KeyBinding b = it.next();
            if (modId.equals(owners.get(b))) {
                it.remove();
                owners.remove(b);
                removed++;
            }
        }
        return removed;
    }

    /**
     * Poll all registered key bindings. Called once per frame from the render loop.
     * Uses GLFW to check key state directly.
     */
    public static void tick(long window) {
        if (window == 0) return;
        List<KeyBinding> snapshot;
        synchronized (KeyBindingRegistry.class) {
            snapshot = new ArrayList<>(bindings);
        }
        for (KeyBinding binding : snapshot) {
            boolean pressed = org.lwjgl.glfw.GLFW.glfwGetKey(window, binding.getKeyCode())
                    == org.lwjgl.glfw.GLFW.GLFW_PRESS;
            binding.update(pressed);
        }
    }

    /**
     * Get all registered bindings (unmodifiable).
     */
    public static synchronized List<KeyBinding> getBindings() {
        return Collections.unmodifiableList(new ArrayList<>(bindings));
    }

    /** @return owner id for the given binding, or {@code null} if unregistered. */
    public static synchronized String getOwner(KeyBinding binding) {
        return owners.get(binding);
    }
}
