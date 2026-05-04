package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.compat.LegacyPluginClassLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies LegacyPluginClassLoader.allLoaders() returns a snapshot of
 * active loaders. YamlConfiguration.loadFromString uses this to resolve
 * SnakeYAML !! class tags from plugin data files.
 */
class LegacyPluginClassLoaderAllLoadersTest {

    @Test
    void allLoadersIncludesCreatedLoader(@TempDir Path dir) throws Exception {
        Path jar = dir.resolve("empty.jar");
        java.util.jar.JarOutputStream jos = new java.util.jar.JarOutputStream(java.nio.file.Files.newOutputStream(jar));
        jos.close();
        LegacyPluginClassLoader loader = new LegacyPluginClassLoader(
                new URL[]{ jar.toUri().toURL() }, getClass().getClassLoader());
        try {
            List<?> loaders = LegacyPluginClassLoader.allLoaders();
            assertTrue(loaders.contains(loader), "allLoaders must include the newly created loader");
        } finally {
            loader.close();
        }
    }

    @Test
    void allLoadersRemovesClosedLoader(@TempDir Path dir) throws Exception {
        Path jar = dir.resolve("empty.jar");
        java.util.jar.JarOutputStream jos = new java.util.jar.JarOutputStream(java.nio.file.Files.newOutputStream(jar));
        jos.close();
        LegacyPluginClassLoader loader = new LegacyPluginClassLoader(
                new URL[]{ jar.toUri().toURL() }, getClass().getClassLoader());
        loader.close();
        List<?> loaders = LegacyPluginClassLoader.allLoaders();
        assertFalse(loaders.contains(loader), "closed loader must not appear in allLoaders");
    }

    @Test
    void allLoadersReturnsSnapshot(@TempDir Path dir) throws Exception {
        List<?> before = LegacyPluginClassLoader.allLoaders();
        Path jar = dir.resolve("empty.jar");
        java.util.jar.JarOutputStream jos = new java.util.jar.JarOutputStream(java.nio.file.Files.newOutputStream(jar));
        jos.close();
        LegacyPluginClassLoader loader = new LegacyPluginClassLoader(
                new URL[]{ jar.toUri().toURL() }, getClass().getClassLoader());
        try {
            assertEquals(before.size(), LegacyPluginClassLoader.allLoaders().size() - 1);
            // The 'before' snapshot must not have changed
            assertThrows(UnsupportedOperationException.class, () -> ((java.util.List) before).add("x"),
                    "allLoaders must return an unmodifiable snapshot");
        } finally {
            loader.close();
        }
    }
}
