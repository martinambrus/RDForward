package com.github.martinambrus.rdforward.modloader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that {@link BridgeRegistry#scanKinds(List)} detects bridge kinds
 * from jar directories without loading any plugin classes. This is the
 * pre-scan used by {@code ModSystem.boot()} to install bridges before
 * plugin classloading triggers static initialisers.
 */
class BridgeRegistryScanKindsTest {

    @Test
    void emptyDirReturnsEmptySet(@TempDir Path dir) throws IOException {
        Files.createDirectories(dir.resolve("empty"));
        Set<BridgeKind> kinds = BridgeRegistry.scanKinds(List.of(dir.resolve("empty")));
        assertTrue(kinds.isEmpty());
    }

    @Test
    void bukkitJarDetected(@TempDir Path dir) throws IOException {
        Path plugins = dir.resolve("plugins");
        Files.createDirectories(plugins);
        writeJar(plugins.resolve("test.jar"),
                Map.of("plugin.yml", "name: T\nversion: 1\nmain: x.X\n"));
        Set<BridgeKind> kinds = BridgeRegistry.scanKinds(List.of(plugins));
        assertEquals(Set.of(BridgeKind.BUKKIT), kinds);
    }

    @Test
    void multipleKindsCollected(@TempDir Path dir) throws IOException {
        Path plugins = dir.resolve("plugins");
        Files.createDirectories(plugins);
        writeJar(plugins.resolve("bukkit.jar"),
                Map.of("plugin.yml", "name: B\nversion: 1\nmain: x.X\n"));
        writeJar(plugins.resolve("fabric.jar"),
                Map.of("fabric.mod.json", "{\"id\":\"f\",\"version\":\"1\"}"));
        Set<BridgeKind> kinds = BridgeRegistry.scanKinds(List.of(plugins));
        assertEquals(Set.of(BridgeKind.BUKKIT, BridgeKind.FABRIC), kinds);
    }

    @Test
    void nonExistentDirSkipped(@TempDir Path dir) throws IOException {
        Set<BridgeKind> kinds = BridgeRegistry.scanKinds(
                List.of(dir.resolve("nonexistent")));
        assertTrue(kinds.isEmpty());
    }

    @Test
    void twoDirsScanned(@TempDir Path dir) throws IOException {
        Path mods = dir.resolve("mods");
        Path plugins = dir.resolve("plugins");
        Files.createDirectories(mods);
        Files.createDirectories(plugins);
        writeJar(mods.resolve("forge.jar"),
                Map.of("META-INF/mods.toml", "modLoader=\"javafml\"\n"));
        writeJar(plugins.resolve("bukkit.jar"),
                Map.of("plugin.yml", "name: B\nversion: 1\nmain: x.X\n"));
        Set<BridgeKind> kinds = BridgeRegistry.scanKinds(List.of(mods, plugins));
        assertEquals(Set.of(BridgeKind.FORGE, BridgeKind.BUKKIT), kinds);
    }

    @Test
    void nativeRdmodIgnored(@TempDir Path dir) throws IOException {
        Path mods = dir.resolve("mods");
        Files.createDirectories(mods);
        writeJar(mods.resolve("native.jar"),
                Map.of("rdmod.json", "{\"id\":\"x\",\"version\":\"1\"}"));
        Set<BridgeKind> kinds = BridgeRegistry.scanKinds(List.of(mods));
        assertTrue(kinds.isEmpty(), "rdmod jars must not register a bridge kind");
    }

    private static Path writeJar(Path target, Map<String, String> entries) throws IOException {
        Files.createDirectories(target.getParent());
        try (OutputStream fos = Files.newOutputStream(target);
             JarOutputStream jos = new JarOutputStream(fos)) {
            for (Map.Entry<String, String> e : entries.entrySet()) {
                jos.putNextEntry(new JarEntry(e.getKey()));
                jos.write(e.getValue().getBytes(StandardCharsets.UTF_8));
                jos.closeEntry();
            }
        }
        return target;
    }
}
