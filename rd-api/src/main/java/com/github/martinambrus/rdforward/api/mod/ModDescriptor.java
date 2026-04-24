package com.github.martinambrus.rdforward.api.mod;

import java.util.List;
import java.util.Map;

/**
 * Parsed contents of a mod's {@code rdmod.json} (or {@code .yml} / {@code .toml})
 * manifest. The descriptor is populated by the mod loader's parser and handed
 * to the loader, admin tooling, and {@link ModManager} queries.
 *
 * @param id              unique mod id; matches the namespace used for commands,
 *                        permissions, config files and event-ownership tags
 * @param name            human-readable display name
 * @param version         mod version string (semver recommended but not enforced)
 * @param description     short description, may be empty
 * @param authors         author names, may be empty
 * @param apiVersion      required {@code rd-api} version (semver range)
 * @param entrypoints     fully-qualified class names by side: keys are
 *                        {@code "server"} and/or {@code "client"}
 * @param dependencies    hard dependencies (modId -&gt; version range); missing
 *                        or out-of-range entries abort the load
 * @param softDependencies optional dependencies that merely affect load order
 * @param permissions     permission nodes the mod declares it uses
 * @param reloadable      whether the mod opts into hot-reload; also requires
 *                        the entrypoint to implement {@link Reloadable}
 * @param minProtocol     minimum {@code ProtocolVersion.name()} the mod supports,
 *                        or {@code null} for no lower bound
 * @param maxProtocol     maximum {@code ProtocolVersion.name()} the mod supports,
 *                        or {@code "LATEST"} / {@code null} for no upper bound
 */
public record ModDescriptor(
        String id,
        String name,
        String version,
        String description,
        List<String> authors,
        String apiVersion,
        Map<String, String> entrypoints,
        Map<String, String> dependencies,
        Map<String, String> softDependencies,
        List<String> permissions,
        boolean reloadable,
        String minProtocol,
        String maxProtocol
) {

    /** Key used in {@link #entrypoints} for the server-side class. */
    public static final String ENTRYPOINT_SERVER = "server";

    /** Key used in {@link #entrypoints} for the client-side class. */
    public static final String ENTRYPOINT_CLIENT = "client";

    public ModDescriptor {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("mod id is required");
        }
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("mod version is required");
        }
        authors = authors == null ? List.of() : List.copyOf(authors);
        entrypoints = entrypoints == null ? Map.of() : Map.copyOf(entrypoints);
        dependencies = dependencies == null ? Map.of() : Map.copyOf(dependencies);
        softDependencies = softDependencies == null ? Map.of() : Map.copyOf(softDependencies);
        permissions = permissions == null ? List.of() : List.copyOf(permissions);
    }

    /** @return class name for the server entrypoint, or {@code null} if the mod is client-only. */
    public String serverEntrypoint() {
        return entrypoints.get(ENTRYPOINT_SERVER);
    }

    /** @return class name for the client entrypoint, or {@code null} if the mod is server-only. */
    public String clientEntrypoint() {
        return entrypoints.get(ENTRYPOINT_CLIENT);
    }
}
