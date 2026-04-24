package com.github.martinambrus.rdforward.bridge.paper;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.handler.LifecycleEventHandler;
import io.papermc.paper.plugin.lifecycle.event.handler.configuration.LifecycleEventHandlerConfiguration;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEventType;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

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
@SuppressWarnings({"unchecked", "rawtypes"})
public final class BootstrapLifecycleManager implements LifecycleEventManager {

    private final List<LifecycleEventHandler> commandHandlers = new ArrayList<>();

    @Override
    public void registerEventHandler(LifecycleEventType type, LifecycleEventHandler handler) {
        if (type == LifecycleEvents.COMMANDS) {
            commandHandlers.add(handler);
        }
    }

    @Override
    public void registerEventHandler(LifecycleEventHandlerConfiguration config) {
        // Configuration-based registration is a no-op in the bridge;
        // plugins that rely on priorities/monitors degrade gracefully to
        // the direct handler path.
    }

    public void fireCommands(Commands registrar) {
        io.papermc.paper.plugin.lifecycle.event.registrar.Registrar registrarFacade =
                new io.papermc.paper.plugin.lifecycle.event.registrar.Registrar() {};
        ReloadableRegistrarEvent event = new ReloadableRegistrarEvent() {
            @Override public io.papermc.paper.plugin.lifecycle.event.registrar.Registrar registrar() {
                return registrarFacade;
            }
            @Override public io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent$Cause cause() {
                return null;
            }
        };
        for (LifecycleEventHandler h : commandHandlers) {
            h.run((LifecycleEvent) event);
        }
    }
}
