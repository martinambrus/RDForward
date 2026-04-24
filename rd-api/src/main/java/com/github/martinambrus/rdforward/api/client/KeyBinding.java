package com.github.martinambrus.rdforward.api.client;

/**
 * Named keyboard shortcut registered via {@code KeyBindingRegistry}. The
 * registry polls the pressed state each frame and calls {@link #update(boolean)};
 * implementations are responsible for edge-detecting press/release and
 * firing their own callback once per press.
 *
 * <p>GLFW key codes (e.g. {@code GLFW_KEY_F3}) are used for {@link #getKeyCode()}.
 */
public interface KeyBinding {

    String getName();

    int getKeyCode();

    void update(boolean isPressed);
}
