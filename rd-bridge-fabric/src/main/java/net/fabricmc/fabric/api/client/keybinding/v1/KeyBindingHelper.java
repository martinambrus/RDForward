// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.fabricmc.fabric.api.client.keybinding.v1;

import com.github.martinambrus.rdforward.client.api.KeyBinding;
import com.github.martinambrus.rdforward.client.api.KeyBindingRegistry;

/**
 * Fabric-compatible key binding registration helper. Mods call
 * {@link #registerKeyBinding(KeyBinding)} to add a binding that the
 * client's render loop polls each frame and invokes on the press edge.
 *
 * <p>Upstream Fabric returns a Minecraft {@code net.minecraft.client.option.KeyBinding};
 * RDForward has no such type, so we use
 * {@link com.github.martinambrus.rdforward.client.api.KeyBinding}, the concrete
 * edge-detecting binding that {@code KeyBindingRegistry} polls each frame.
 * The returned binding is the same instance passed in — mods can hold on
 * to it for runtime inspection.
 */
public final class KeyBindingHelper {

    private KeyBindingHelper() {}

    public static KeyBinding registerKeyBinding(KeyBinding binding) {
        KeyBindingRegistry.register(binding);
        return binding;
    }
}
