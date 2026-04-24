// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.neoforged.fml;

/**
 * Minimal NeoForge-shaped mod container. NeoForge mod constructors may
 * declare this as a parameter; the loader injects an instance carrying
 * the mod's id and version so the mod can report them.
 */
public class ModContainer {

    private final String modId;
    private final String version;

    public ModContainer(String modId, String version) {
        this.modId = modId;
        this.version = version;
    }

    public String getModId() { return modId; }
    public String getVersion() { return version; }
}
