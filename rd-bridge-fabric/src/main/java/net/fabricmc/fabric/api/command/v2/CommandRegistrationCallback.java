// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.fabricmc.fabric.api.command.v2;

import com.github.martinambrus.rdforward.api.command.CommandRegistry;
import com.github.martinambrus.rdforward.api.event.Event;

/**
 * Fabric-compatible command registration hook. Upstream Fabric delivers a
 * Brigadier {@code CommandDispatcher<ServerCommandSource>}; RDForward has
 * no Brigadier runtime, so the callback receives the rd-api
 * {@link CommandRegistry} directly.
 *
 * <p>Fabric mods that only use Brigadier's command builder will not compile
 * against this stub — the signature deviates from upstream intentionally so
 * mods get a type error rather than a silent noop. Mods ported to RDForward
 * call {@code registry.register(modId, name, description, handler)} to
 * install commands.
 *
 * <p>{@code FabricServerBridge} fires {@link #EVENT} once the server has a
 * real {@link CommandRegistry} wired up, so listeners that register at
 * {@code onInitialize} time see the registry when the host is ready.
 */
@FunctionalInterface
public interface CommandRegistrationCallback {

    void register(CommandRegistry registry);

    /** Fires when the host is ready to accept command registrations. */
    Event<CommandRegistrationCallback> EVENT = Event.create(
            registry -> {},
            listeners -> registry -> {
                for (CommandRegistrationCallback l : listeners) l.register(registry);
            }
    );
}
