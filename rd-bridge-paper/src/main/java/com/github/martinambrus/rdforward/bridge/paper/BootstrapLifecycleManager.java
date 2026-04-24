package com.github.martinambrus.rdforward.bridge.paper;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventHandler;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEvents;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEventType;
import io.papermc.paper.command.brigadier.Commands;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects lifecycle handlers registered during bootstrap and replays them
 * on demand. In Paper, {@code LifecycleEvents.COMMANDS} fires at server
 * load; in the bridge, {@link PaperPluginWrapper#onEnable} synthesises the
 * corresponding event and invokes the stored handlers.
 *
 * <p>Only {@link LifecycleEvents#COMMANDS} is currently wired. Other event
 * types still accept registrations but never fire.
 */
public final class BootstrapLifecycleManager implements LifecycleEventManager<BootstrapContext> {

    private final List<LifecycleEventHandler<? super ReloadableRegistrarEvent<Commands>>> commandHandlers = new ArrayList<>();

    @Override
    @SuppressWarnings("unchecked")
    public <E extends LifecycleEvent> void registerEventHandler(
            LifecycleEventType<E, BootstrapContext> type,
            LifecycleEventHandler<? super E> handler) {
        if (type == LifecycleEvents.COMMANDS) {
            commandHandlers.add((LifecycleEventHandler<? super ReloadableRegistrarEvent<Commands>>) handler);
        }
    }

    public void fireCommands(Commands registrar) {
        ReloadableRegistrarEvent<Commands> event = () -> registrar;
        for (LifecycleEventHandler<? super ReloadableRegistrarEvent<Commands>> h : commandHandlers) {
            h.run(event);
        }
    }
}
