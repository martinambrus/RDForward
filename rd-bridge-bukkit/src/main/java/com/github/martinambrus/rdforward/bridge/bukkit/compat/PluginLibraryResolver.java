// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit.compat;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.supplier.RepositorySystemSupplier;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferListener;
import org.eclipse.aether.util.filter.ScopeDependencyFilter;
import org.eclipse.aether.util.graph.selector.ScopeDependencySelector;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Resolves Maven coordinates declared in a plugin's {@code libraries:} list
 * (Paper's mechanism) using Eclipse Aether for full transitive dependency
 * resolution, matching Paper's own {@code LibraryLoader} behavior.
 *
 * <p>Only resolves compile/runtime-scoped deps (excludes test/provided).
 * Downloads from Maven Central on first use and caches in a shared
 * {@code libraries/} directory.
 */
public final class PluginLibraryResolver {

    private static final Logger LOG = Logger.getLogger("RDForward/LibraryLoader");
    private static final String MAVEN_CENTRAL = "https://repo.maven.apache.org/maven2/";

    private PluginLibraryResolver() {}

    /**
     * Resolve a list of Maven coordinates to local jar URLs, including
     * all transitive dependencies. Each coordinate must be in
     * {@code group:artifact:version} format.
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
        // Disable snapshots — prevents Aether from hunting for SNAPSHOT versions
        // in remote repos (major source of slowness and failures).
        org.eclipse.aether.repository.RepositoryPolicy never = new org.eclipse.aether.repository.RepositoryPolicy(
                false, org.eclipse.aether.repository.RepositoryPolicy.UPDATE_POLICY_NEVER,
                org.eclipse.aether.repository.RepositoryPolicy.CHECKSUM_POLICY_FAIL);
        RemoteRepository central = new RemoteRepository.Builder("central", "default", MAVEN_CENTRAL)
                .setPolicy(new org.eclipse.aether.repository.RepositoryPolicy(
                        true, org.eclipse.aether.repository.RepositoryPolicy.UPDATE_POLICY_NEVER,
                        org.eclipse.aether.repository.RepositoryPolicy.CHECKSUM_POLICY_FAIL))
                .setSnapshotPolicy(never)
                .build();

        // Session selectors prune test/provided at collection time, but
        // optional compile-scoped deps (e.g. injector declares junit as
        // compile+optional) still leak through. Apply a result filter too.
        DependencyFilter filter = (node, parents) -> {
            if (node == null || node.getDependency() == null) return true;
            if (node.getDependency().isOptional()) return false;
            String scope = node.getDependency().getScope();
            return !"test".equals(scope) && !"provided".equals(scope);
        };

        List<URL> urls = new ArrayList<>();
        int totalCoords = coordinates.size();
        int coordIdx = 0;
        for (String coord : coordinates) {
            coordIdx++;
            try {
                LOG.info("[" + coordIdx + "/" + totalCoords + "] Resolving " + coord + "...");
                Dependency dep = new Dependency(new DefaultArtifact(coord), "runtime");
                CollectRequest collectRequest = new CollectRequest(dep, List.of(central));
                DependencyRequest depRequest = new DependencyRequest(collectRequest, filter);
                DependencyResult result = system.resolveDependencies(session, depRequest);
                int added = 0;
                for (ArtifactResult ar : result.getArtifactResults()) {
                    URL url = ar.getArtifact().getFile().toURI().toURL();
                    if (!urls.contains(url)) { urls.add(url); added++; }
                }
                LOG.info("[" + coordIdx + "/" + totalCoords + "] " + coord + " -> " + added + " jars");
            } catch (Exception e) {
                LOG.log(Level.WARNING, "[" + coordIdx + "/" + totalCoords + "] Failed: " + coord + ": " + e.getMessage());
            }
        }
        LOG.info("Library resolution complete: " + urls.size() + " total jars");
        return urls.toArray(new URL[0]);
    }

    static RepositorySystem newRepositorySystem() {
        try {
            return new RepositorySystemSupplier().get();
        } catch (NoClassDefFoundError e) {
            return null;
        }
    }

    private static RepositorySystemSession newSession(RepositorySystem system, Path cacheDir) {
        DefaultRepositorySystemSession session = new DefaultRepositorySystemSession();
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, new LocalRepository(cacheDir.toFile())));
        session.setSystemProperties(System.getProperties());
        session.setUserProperties(System.getProperties());
        session.setTransferListener(new ProgressListener());
        // Prune test/provided/optional deps at collection time — prevents
        // Aether from downloading their POMs entirely. This is the key
        // difference vs using only a DependencyFilter (which downloads all
        // POMs first, then filters the final jars).
        session.setDependencySelector(
                new org.eclipse.aether.util.graph.selector.AndDependencySelector(
                        new ScopeDependencySelector("test", "provided"),
                        new org.eclipse.aether.util.graph.selector.OptionalDependencySelector()));
        return session;
    }

    /** Logs download progress at INFO level so the console shows activity. */
    private static final class ProgressListener implements TransferListener {
        private final AtomicInteger downloaded = new AtomicInteger();
        private final AtomicInteger total = new AtomicInteger();

        @Override public void transferInitiated(TransferEvent e) {
            if (e.getRequestType() == TransferEvent.RequestType.GET) {
                int t = total.incrementAndGet();
                LOG.info("  Downloading [" + t + "] " + shortName(e.getResource()));
            }
        }

        @Override public void transferSucceeded(TransferEvent e) {
            if (e.getRequestType() == TransferEvent.RequestType.GET) {
                int d = downloaded.incrementAndGet();
                LOG.info("  Done [" + d + "/" + total.get() + "] " + shortName(e.getResource()));
            }
        }

        @Override public void transferFailed(TransferEvent e) {
            if (e.getRequestType() == TransferEvent.RequestType.GET) {
                LOG.warning("  Failed: " + shortName(e.getResource()) + " - " + e.getException().getMessage());
            }
        }

        @Override public void transferProgressed(TransferEvent e) {}
        @Override public void transferStarted(TransferEvent e) {}
        @Override public void transferCorrupted(TransferEvent e) {
            LOG.warning("  Corrupted: " + shortName(e.getResource()));
        }

        private static String shortName(org.eclipse.aether.transfer.TransferResource r) {
            String name = r.getResourceName();
            // Strip parent dirs: "ch/jalu/injector/1.0/injector-1.0.jar" -> "injector-1.0.jar"
            int slash = name.lastIndexOf('/');
            return slash >= 0 ? name.substring(slash + 1) : name;
        }
    }
}
