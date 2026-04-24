package net.fabricmc.fabric.api.client.rendering.v1.tooltip;

import com.github.martinambrus.rdforward.api.event.Event;

/**
 * Noop stub: RDForward has no tooltip layout pipeline. The event exists
 * so mods compile against it; listeners never fire.
 */
@FunctionalInterface
public interface TooltipComponentCallback {

    /** Noop callback — return value ignored because the event never fires. */
    Object getComponent(Object tooltipData);

    /** Noop event — accepts listeners, never invokes them. */
    Event<TooltipComponentCallback> EVENT = Event.create(
            data -> null,
            listeners -> data -> null
    );
}
