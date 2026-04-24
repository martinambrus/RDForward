package io.papermc.paper.plugin.lifecycle.event;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEventType;

/**
 * Registry of Paper's built-in lifecycle event types. The bridge only wires
 * {@link #COMMANDS} at present.
 */
public final class LifecycleEvents {

    public static final LifecycleEventType<ReloadableRegistrarEvent<Commands>, BootstrapContext> COMMANDS =
            new LifecycleEventType<>("commands");

    private LifecycleEvents() {}
}
