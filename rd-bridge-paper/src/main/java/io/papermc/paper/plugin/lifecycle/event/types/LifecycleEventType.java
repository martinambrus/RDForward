package io.papermc.paper.plugin.lifecycle.event.types;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;

/**
 * Typed constant identifying a Paper lifecycle event class. The bridge uses
 * {@link #getName()} as a dispatch key; the generic parameters are compile-
 * time only.
 */
public final class LifecycleEventType<E extends LifecycleEvent, O extends LifecycleEventOwner> {

    private final String name;

    public LifecycleEventType(String name) {
        this.name = name;
    }

    public String getName() { return name; }
}
