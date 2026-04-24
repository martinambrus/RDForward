// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.fabricmc.fabric.api.client.rendering.v1;

/**
 * Noop stub: RDForward does not render armor layers, so {@link #register}
 * accepts any renderer without doing work. Keeps Fabric client mods that
 * register custom armor layers compiling and running.
 */
@FunctionalInterface
public interface ArmorRenderer {

    /** Upstream signature; RDForward never calls this because no layer invokes it. */
    void render(Object matrices, Object vertexConsumers, Object stack, Object entity,
                Object slot, int light, Object model);

    /** Noop — RDForward does not render armor. */
    static void register(ArmorRenderer renderer, Object... items) {
        // intentional noop
    }
}
