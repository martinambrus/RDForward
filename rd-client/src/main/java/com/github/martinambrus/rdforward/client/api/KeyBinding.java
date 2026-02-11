package com.github.martinambrus.rdforward.client.api;

/**
 * A registered key binding that mods can use to add custom key handlers.
 *
 * Key bindings track press/release state and fire callbacks on press.
 * The binding system handles edge detection (fires once per press, not per frame).
 *
 * Example:
 * <pre>
 *   KeyBinding flyKey = new KeyBinding("Fly Mode", GLFW.GLFW_KEY_G, () -> {
 *       System.out.println("Fly mode toggled!");
 *   });
 *   KeyBindingRegistry.register(flyKey);
 * </pre>
 */
public class KeyBinding {

    private final String name;
    private final int keyCode;
    private final Runnable onPress;
    private boolean wasPressed = false;

    /**
     * @param name    display name for the binding (e.g., "Fly Mode")
     * @param keyCode GLFW key code (e.g., GLFW_KEY_G)
     * @param onPress callback to invoke when the key is pressed (edge-triggered)
     */
    public KeyBinding(String name, int keyCode, Runnable onPress) {
        this.name = name;
        this.keyCode = keyCode;
        this.onPress = onPress;
    }

    /**
     * Update the key state and fire the callback if freshly pressed.
     * Called by the registry each frame.
     *
     * @param isPressed true if the key is currently held down
     */
    public void update(boolean isPressed) {
        if (isPressed && !wasPressed) {
            onPress.run();
        }
        wasPressed = isPressed;
    }

    public String getName() { return name; }
    public int getKeyCode() { return keyCode; }
}
