package net.fabricmc.fabric.api.client.rendering.v1;

/**
 * Noop stub: RDForward does not expose an entity renderer pipeline, so
 * {@link #register} accepts any factory without doing work. Keeps Fabric
 * client mods that register custom entity renderers compiling and running.
 */
public final class EntityRendererRegistry {

    private EntityRendererRegistry() {}

    /** Noop — RDForward does not render custom entity models. */
    public static <T> void register(Object entityType, Object factory) {
        // intentional noop
    }
}
