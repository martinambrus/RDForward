// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit.compat;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Resolves Maven coordinates declared in a plugin's {@code libraries:} list
 * (Paper's mechanism) using Eclipse Aether for full transitive dependency
 * resolution, matching Paper's own {@code LibraryLoader} behavior.
 *
 * <p>Downloads from Maven Central on first use and caches in a shared
 * {@code libraries/} directory so subsequent server starts skip the
 * network call.
 */
public final class PluginLibraryResolver {

    private static final Logger LOG = Logger.getLogger("RDForward/LibraryLoader");
    private static final String MAVEN_CENTRAL = "https://repo.maven.apache.org/maven2/";

    private PluginLibraryResolver() {}

    /**
     * Resolve a list of Maven coordinates to local jar URLs, including
     * all transitive dependencies. Each coordinate must be in
     * {@code group:artifact:version} format.
     *
     * <p>Failed resolutions are skipped; the plugin will fail later with
     * a clear {@link ClassNotFoundException} identifying the missing class.
     */
    public static URL[] resolve(List<String> coordinates) {
        if (coordinates == null || coordinates.isEmpty()) return new URL[0];

        RepositorySystem system = newRepositorySystem();
        if (system == null) {
            LOG.warning("Maven Resolver not available — cannot download plugin libraries");
            return new URL[0];
        }
        Path cacheDir = Path.of("libraries");
        RepositorySystemSession session = newSession(system, cacheDir);
        RemoteRepository central = new RemoteRepository.Builder("central", "default", MAVEN_CENTRAL).build();

        List<URL> urls = new ArrayList<>();
        for (String coord : coordinates) {
            try {
                Dependency dep = new Dependency(new DefaultArtifact(coord), null);
                CollectRequest collectRequest = new CollectRequest(dep, List.of(central));
                DependencyRequest depRequest = new DependencyRequest(collectRequest, null);
                DependencyResult result = system.resolveDependencies(session, depRequest);
                for (ArtifactResult ar : result.getArtifactResults()) {
                    URL url = ar.getArtifact().getFile().toURI().toURL();
                    if (!urls.contains(url)) urls.add(url);
                }
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Failed to resolve library " + coord + ": " + e.getMessage(), e);
            }
        }
        return urls.toArray(new URL[0]);
    }

    /** Package-private for testing. */
    static RepositorySystem newRepositorySystem() {
        DefaultServiceLocator locator = new DefaultServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        return locator.getService(RepositorySystem.class);
    }

    private static RepositorySystemSession newSession(RepositorySystem system, Path cacheDir) {
        DefaultRepositorySystemSession session = new DefaultRepositorySystemSession();
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, new LocalRepository(cacheDir.toFile())));
        return session;
    }
}
