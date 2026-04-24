package io.papermc.paper.plugin.lifecycle.event.registrar;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;

/**
 * Lifecycle event that exposes a registrar of type {@code R} (for example
 * {@link io.papermc.paper.command.brigadier.Commands}). Handlers call
 * {@link #registrar()} to get an object to register entries on.
 */
public interface ReloadableRegistrarEvent<R> extends LifecycleEvent {
    R registrar();
}
