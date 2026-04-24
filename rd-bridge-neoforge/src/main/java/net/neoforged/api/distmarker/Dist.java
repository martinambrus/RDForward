// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.neoforged.api.distmarker;

/**
 * Stub of NeoForge's physical-side enum. Mirrors Forge's
 * {@link net.minecraftforge.api.distmarker.Dist}.
 */
public enum Dist {
    CLIENT,
    DEDICATED_SERVER;

    public boolean isClient() { return this == CLIENT; }
    public boolean isDedicatedServer() { return this == DEDICATED_SERVER; }

    public static net.minecraftforge.api.distmarker.Dist toForge(Dist d) {
        return d == CLIENT
                ? net.minecraftforge.api.distmarker.Dist.CLIENT
                : net.minecraftforge.api.distmarker.Dist.DEDICATED_SERVER;
    }
}
