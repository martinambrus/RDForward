package com.github.martinambrus.rdforward.api.mod;

import java.util.Collection;

/**
 * Runtime query interface over the set of loaded mods. Obtained via
 * {@code Server.getModManager()}; the concrete implementation lives in
 * {@code rd-mod-loader}.
 *
 * <p>Mods use this to detect optional dependencies ({@link #isLoaded(String)}),
 * fetch metadata about peers ({@link #get(String)}), and enumerate what is
 * running ({@link #all()}). It is intentionally read-only — enabling,
 * disabling and reloading are admin operations exposed through commands,
 * not a modding API.
 */
public interface ModManager {

    /** @return descriptor for the mod with the given id, or {@code null} if not loaded. */
    ModDescriptor get(String modId);

    /** @return true if a mod with the given id is currently enabled. */
    boolean isLoaded(String modId);

    /** @return descriptors of every currently-enabled mod, in load order. */
    Collection<ModDescriptor> all();
}
