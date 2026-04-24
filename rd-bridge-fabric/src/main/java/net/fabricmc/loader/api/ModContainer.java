package net.fabricmc.loader.api;

import com.github.martinambrus.rdforward.api.mod.ModDescriptor;

/**
 * Fabric-compatible view of a loaded mod. Upstream exposes {@code ModMetadata}
 * and a filesystem root; RDForward does not ship a filesystem abstraction, so
 * {@code findPath} is a noop and {@code getRootPath} returns null.
 *
 * <p>Wraps an rd-api {@link ModDescriptor} — mods that just want id /
 * version / name reads work without change.
 */
public final class ModContainer {

    private final ModDescriptor descriptor;

    public ModContainer(ModDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    /** @return mod id (matches upstream Fabric's ModMetadata.getId). */
    public String getId() {
        return descriptor.id();
    }

    /** @return human-readable mod name. */
    public String getName() {
        return descriptor.name();
    }

    /** @return mod version string. */
    public String getVersion() {
        return descriptor.version();
    }

    /** @return wrapped rd-api descriptor for callers that need more fields. */
    public ModDescriptor descriptor() {
        return descriptor;
    }

    /** Noop — RDForward does not expose per-mod file roots. Always null. */
    public Object findPath(String relativePath) {
        return null;
    }

    /** Noop — RDForward does not expose per-mod file roots. Always null. */
    public Object getRootPath() {
        return null;
    }
}
