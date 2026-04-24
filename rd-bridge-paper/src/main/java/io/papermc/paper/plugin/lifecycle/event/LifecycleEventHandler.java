package io.papermc.paper.plugin.lifecycle.event;

/**
 * Listener callback registered against a {@code LifecycleEventType}.
 */
@FunctionalInterface
public interface LifecycleEventHandler<E extends LifecycleEvent> {
    void run(E event);
}
