package com.github.martinambrus.rdforward.bridge.bukkit.compat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies {@link PluginLibraryResolver} parses Maven coordinates,
 * builds correct remote URLs, and reuses cached jars.
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
        assertEquals(0, PluginLibraryResolver.resolve(List.of("bad-format")).length);
    }

    @Test
    void remoteUrlForBuildsCorrectPath() {
        assertEquals(
                "https://repo.maven.apache.org/maven2/ch/jalu/injector/1.0/injector-1.0.jar",
                PluginLibraryResolver.remoteUrlFor("ch.jalu:injector:1.0"));
        assertEquals(
                "https://repo.maven.apache.org/maven2/org/bstats/bstats-bukkit/3.1.0/bstats-bukkit-3.1.0.jar",
                PluginLibraryResolver.remoteUrlFor("org.bstats:bstats-bukkit:3.1.0"));
    }

    @Test
    void cachedJarIsReusedWithoutDownload(@TempDir Path cacheDir) throws Exception {
        // Pre-create a fake cached jar
        Path cached = cacheDir.resolve("fake-lib-1.0.jar");
        Files.write(cached, new byte[]{0x50, 0x4B});

        URL[] urls = PluginLibraryResolver.resolve(
                List.of("com.example:fake-lib:1.0"), cacheDir);

        assertEquals(1, urls.length);
        assertTrue(urls[0].toString().endsWith("fake-lib-1.0.jar"));
        // File content should be unchanged (not overwritten by download)
        assertEquals(2, Files.size(cached));
    }

    @Test
    void mixedCachedAndMissingSkipsFailures(@TempDir Path cacheDir) throws Exception {
        // Cache one lib, leave another missing (will fail download in test env)
        Path cached = cacheDir.resolve("cached-lib-2.0.jar");
        Files.write(cached, new byte[]{0x50, 0x4B});

        URL[] urls = PluginLibraryResolver.resolve(List.of(
                "com.example:cached-lib:2.0",
                "com.example:missing-lib:3.0"
        ), cacheDir);

        // At least the cached one should resolve; the missing one may fail
        // in test env without network
        assertTrue(urls.length >= 1, "cached lib should resolve");
        assertTrue(urls[0].toString().endsWith("cached-lib-2.0.jar"));
    }
}
