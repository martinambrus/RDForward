package io.papermc.paper.plugin.lifecycle.event;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEventType;

/**
 * Per-owner registration manager. Bootstrappers obtain it from
 * {@code BootstrapContext.getLifecycleManager()} and use it to register
 * handlers for specific {@link LifecycleEventType} constants.
 */
public interface LifecycleEventManager<O extends LifecycleEventOwner> {

    <E extends LifecycleEvent> void registerEventHandler(
            LifecycleEventType<E, O> type,
            LifecycleEventHandler<? super E> handler);
}
