package com.github.martinambrus.rdforward.render;

/**
 * Global access point for the active rendering backend. The game code uses
 * this to obtain the renderer without knowing the specific implementation.
 * <p>
 * At startup, the platform launcher (desktop or Android) calls
 * {@link #init(RDRenderer)} to install the backend. Game code then
 * accesses it via {@code RenderSystem.renderer()} and friends.
 *
 * <pre>
 * // Desktop startup (in NativeLauncher or FabricNativeLauncher):
 * RenderSystem.init(new LwjglRenderer());
 *
 * // Android startup (in AndroidLauncher):
 * RenderSystem.init(new LibGDXRenderer());
 *
 * // Game code (in Tesselator, Chunk, etc.):
 * RenderSystem.graphics().bindTexture(texId);
 * RenderSystem.meshBuilder().vertex(x, y, z);
 * </pre>
 */
public final class RenderSystem {

    private static volatile RDRenderer renderer;

    private RenderSystem() {}

    /** Install the active renderer. Called once at startup. */
    public static void init(RDRenderer renderer) {
        RenderSystem.renderer = renderer;
    }

    /** @return the active renderer, or null if not yet initialised. */
    public static RDRenderer renderer() {
        return renderer;
    }

    /** Shorthand for {@code renderer().graphics()}. */
    public static RDGraphics graphics() {
        return renderer.graphics();
    }

    /** Shorthand for {@code renderer().input()}. */
    public static RDInput input() {
        return renderer.input();
    }

    /** Shorthand for {@code renderer().graphics().meshBuilder()}. */
    public static RDMeshBuilder meshBuilder() {
        return renderer.graphics().meshBuilder();
    }

    /** @return true if a renderer has been initialised. */
    public static boolean isInitialized() {
        return renderer != null;
    }
}
