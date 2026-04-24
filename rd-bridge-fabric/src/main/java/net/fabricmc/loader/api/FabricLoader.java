// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.fabricmc.loader.api;

import com.github.martinambrus.rdforward.api.mod.ModDescriptor;
import com.github.martinambrus.rdforward.api.mod.ModManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Fabric-compatible loader introspection facade. Mods call
 * {@link #getInstance()} to query whether other mods are loaded and to
 * enumerate the active mod set.
 *
 * <p>Backed by rd-api {@link ModManager}; the host wires the real
 * {@code ModManager} once the mod loader finishes boot by calling
 * {@link #bind(ModManager, EnvType, boolean)}. Before binding, the facade
 * reports an empty mod set — same as an unstarted Fabric loader.
 */
public final class FabricLoader {

    /** Fabric's environment enum; RDForward surfaces only the two sides it cares about. */
    public enum EnvType { SERVER, CLIENT }

    private static final FabricLoader INSTANCE = new FabricLoader();

    private volatile ModManager modManager;
    private volatile EnvType envType = EnvType.SERVER;
    private volatile boolean developmentEnvironment;

    private FabricLoader() {}

    /** @return the singleton facade; identical across calls. */
    public static FabricLoader getInstance() {
        return INSTANCE;
    }

    /**
     * Host-side hook. Called once by the mod loader once the real
     * {@link ModManager} is ready so mod queries return accurate results.
     */
    public void bind(ModManager manager, EnvType type, boolean devEnvironment) {
        this.modManager = manager;
        this.envType = type;
        this.developmentEnvironment = devEnvironment;
    }

    /** True if a mod with the given id is currently loaded. */
    public boolean isModLoaded(String modId) {
        ModManager mm = modManager;
        return mm != null && mm.isLoaded(modId);
    }

    /** @return container for the given mod, or empty if not loaded. */
    public Optional<ModContainer> getModContainer(String modId) {
        ModManager mm = modManager;
        if (mm == null) return Optional.empty();
        ModDescriptor d = mm.get(modId);
        return d == null ? Optional.empty() : Optional.of(new ModContainer(d));
    }

    /** @return an immutable snapshot of every loaded mod as a {@link ModContainer}. */
    public Collection<ModContainer> getAllMods() {
        ModManager mm = modManager;
        if (mm == null) return Collections.emptyList();
        List<ModContainer> out = new ArrayList<>();
        for (ModDescriptor d : mm.all()) out.add(new ModContainer(d));
        return Collections.unmodifiableList(out);
    }

    /** @return whether the loader is running in a dev environment (dev-run vs shipped). */
    public boolean isDevelopmentEnvironment() {
        return developmentEnvironment;
    }

    /** @return the side this loader is running on. */
    public EnvType getEnvironmentType() {
        return envType;
    }
}
