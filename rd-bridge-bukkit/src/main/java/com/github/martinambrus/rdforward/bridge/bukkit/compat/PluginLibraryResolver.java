// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit.compat;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Resolves Maven coordinates declared in a plugin's {@code libraries:} list
 * (Paper's mechanism) to local jar URLs. Downloads from Maven Central on
 * first use and caches in a shared directory so subsequent server starts
 * skip the network call.
 */
public final class PluginLibraryResolver {

    private static final Logger LOG = Logger.getLogger("RDForward/LibraryLoader");
    static final String MAVEN_CENTRAL = "https://repo.maven.apache.org/maven2/";

    private PluginLibraryResolver() {}

    /**
     * Resolve a list of Maven coordinates to local jar URLs.
     * Each coordinate must be in {@code group:artifact:version} format.
     * Failed downloads are skipped; the plugin will fail later with a
     * clear {@link ClassNotFoundException} identifying the missing class.
     */
    public static URL[] resolve(List<String> coordinates) {
        return resolve(coordinates, Path.of("libraries"));
    }

    /** Package-private overload for testing with a custom cache dir. */
    static URL[] resolve(List<String> coordinates, Path cacheDir) {
        if (coordinates == null || coordinates.isEmpty()) return new URL[0];
        List<URL> urls = new ArrayList<>();
        for (String coord : coordinates) {
            try {
                urls.add(resolveOne(coord, cacheDir));
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Failed to resolve library " + coord + ": " + e.getMessage(), e);
            }
        }
        return urls.toArray(new URL[0]);
    }

    private static URL resolveOne(String coord, Path cacheDir) throws IOException {
        String[] parts = coord.split(":");
        if (parts.length != 3) throw new IllegalArgumentException("expected group:artifact:version");
        String group = parts[0];
        String artifact = parts[1];
        String version = parts[2];

        String groupPath = group.replace('.', '/');
        String fileName = artifact + "-" + version + ".jar";
        Path cached = cacheDir.resolve(fileName);

        if (Files.exists(cached)) {
            LOG.info("Library " + coord + " loaded from cache");
            return cached.toUri().toURL();
        }

        String remoteUrl = MAVEN_CENTRAL + groupPath + "/" + artifact + "/" + version + "/" + fileName;
        LOG.info("Downloading library " + coord);
        Files.createDirectories(cacheDir);
        try (InputStream in = new URL(remoteUrl).openStream()) {
            Files.copy(in, cached);
        } catch (IOException e) {
            Files.deleteIfExists(cached);
            throw e;
        }
        return cached.toUri().toURL();
    }

    /** Build the expected remote URL for a Maven coordinate. Visible for testing. */
    static String remoteUrlFor(String coord) {
        String[] parts = coord.split(":");
        if (parts.length != 3) throw new IllegalArgumentException("expected group:artifact:version");
        return MAVEN_CENTRAL + parts[0].replace('.', '/') + "/" + parts[1] + "/" + parts[2]
                + "/" + parts[1] + "-" + parts[2] + ".jar";
    }
}
