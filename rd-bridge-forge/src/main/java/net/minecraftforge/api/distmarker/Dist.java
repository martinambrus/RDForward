// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.minecraftforge.api.distmarker;

/**
 * Stub of Forge's physical-side enum. {@code DEDICATED_SERVER} is the only
 * value RDForward emits; {@code CLIENT} exists so {@code Dist.CLIENT} / {@code Dist.DEDICATED_SERVER}
 * references in mods compile against the bridge.
 */
public enum Dist {
    CLIENT,
    DEDICATED_SERVER;

    public boolean isClient() { return this == CLIENT; }
    public boolean isDedicatedServer() { return this == DEDICATED_SERVER; }
}
