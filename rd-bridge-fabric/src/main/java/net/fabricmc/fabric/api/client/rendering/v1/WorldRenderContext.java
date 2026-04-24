// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.fabricmc.fabric.api.client.rendering.v1;

/**
 * Stub view of Fabric's {@code WorldRenderContext}. RDForward's world
 * render pass has no matrix stack / frustum / tessellator abstractions
 * to expose, so this type carries only {@code tickDelta}.
 *
 * <p>Fabric mods that call unmodeled getters will see {@code null}; that
 * is the documented noop contract for APIs beyond RDForward's capabilities.
 */
public final class WorldRenderContext {

    private final float tickDelta;

    public WorldRenderContext(float tickDelta) {
        this.tickDelta = tickDelta;
    }

    public float tickDelta() {
        return tickDelta;
    }

    /** Noop getter retained so mods that call it compile. */
    public Object matrixStack() { return null; }

    /** Noop getter retained so mods that call it compile. */
    public Object consumers() { return null; }

    /** Noop getter retained so mods that call it compile. */
    public Object camera() { return null; }
}
