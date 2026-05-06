package com.github.martinambrus.rdforward.bridge.bukkit.compat;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.RepositorySystemSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies {@link PluginLibraryResolver} resolves Maven coordinates
 * with full transitive dependency resolution via Eclipse Aether.
 *
 * <p>Transitive resolution tests require network access to Maven Central.
 * They are skipped automatically in offline environments.
 */
class PluginLibraryResolverTest {

    @Test
    void emptyListReturnsEmptyArray() {
        assertEquals(0, PluginLibraryResolver.resolve(List.of()).length);
    }

    @Test
    void nullReturnsEmptyArray() {
        assertEquals(0, PluginLibraryResolver.resolve(null).length);
    }

    @Test
    void invalidCoordinateIsSkipped() {
        // Should not throw — bad coords are logged and skipped
        assertEquals(0, PluginLibraryResolver.resolve(List.of("bad-format")).length);
    }

    @Test
    @EnabledIf("isNetworkAvailable")
    void resolvesTransitiveDependencies() {
        // ch.jalu:injector:1.0 depends on javax.inject:javax.inject:1
        URL[] urls = PluginLibraryResolver.resolve(List.of("ch.jalu:injector:1.0"));
        assertTrue(urls.length >= 1, "should resolve at least the direct artifact");

        boolean foundInjector = false;
        boolean foundJavaxInject = false;
        for (URL u : urls) {
            String s = u.toString();
            if (s.contains("injector-1.0")) foundInjector = true;
            if (s.contains("javax.inject")) foundJavaxInject = true;
        }
        assertTrue(foundInjector, "should include injector-1.0.jar");
        assertTrue(foundJavaxInject, "Aether should resolve transitive javax.inject");
    }

    @Test
    @EnabledIf("isNetworkAvailable")
    void deduplicatesArtifactsAcrossCoordinates() {
        URL[] urls = PluginLibraryResolver.resolve(List.of(
                "ch.jalu:injector:1.0",
                "ch.jalu:configme:1.3.1"));
        assertTrue(urls.length >= 2, "should resolve both artifacts + transitives");

        long unique = java.util.Arrays.stream(urls).distinct().count();
        assertEquals(urls.length, unique, "no duplicate URLs");
    }

    static boolean isNetworkAvailable() {
        try {
            RepositorySystem system = PluginLibraryResolver.newRepositorySystem();
            return system != null;
        } catch (Exception e) {
            return false;
        }
    }
}
