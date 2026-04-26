package com.github.martinambrus.rdforward.modloader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link BridgeRegistry#detect(Path)}. Synthesises tiny jar
 * files in {@code @TempDir} so the tests run without any bridge module on
 * the classpath — the goal here is to lock down the manifest probe order
 * and the PocketMine vs Bukkit {@code plugin.yml} disambiguation, not to
 * exercise the bridge loaders themselves (covered by the per-bridge
 * integration tests + {@code ModLoaderBridgeWiringTest}).
 */
class BridgeRegistryDetectTest {

    @Test
    void detectsRdmodAsNullSoNativePathHandlesIt(@TempDir Path dir) throws IOException {
        Path jar = writeJar(dir.resolve("native.jar"),
                Map.of("rdmod.json", "{\"id\":\"x\",\"version\":\"1\"}"));
        // detect() only handles bridge formats; native rdmod is the caller's
        // responsibility (ModLoader probes rdmod.json before delegating here).
        assertNull(BridgeRegistry.detect(jar));
    }

    @Test
    void paperPluginYmlIsPaper(@TempDir Path dir) throws IOException {
        Path jar = writeJar(dir.resolve("paper.jar"),
                Map.of("paper-plugin.yml", "name: P\nversion: 1\nmain: x.X\n"));
        assertEquals(BridgeKind.PAPER, BridgeRegistry.detect(jar));
    }

    @Test
    void fabricModJsonIsFabric(@TempDir Path dir) throws IOException {
        Path jar = writeJar(dir.resolve("fabric.jar"),
                Map.of("fabric.mod.json", "{\"id\":\"f\",\"version\":\"1\"}"));
        assertEquals(BridgeKind.FABRIC, BridgeRegistry.detect(jar));
    }

    @Test
    void neoForgeManifestIsNeoForge(@TempDir Path dir) throws IOException {
        Path jar = writeJar(dir.resolve("neoforge.jar"),
                Map.of("META-INF/neoforge.mods.toml", "modLoader=\"javafml\"\n"));
        assertEquals(BridgeKind.NEOFORGE, BridgeRegistry.detect(jar));
    }

    @Test
    void forgeManifestIsForge(@TempDir Path dir) throws IOException {
        Path jar = writeJar(dir.resolve("forge.jar"),
                Map.of("META-INF/mods.toml", "modLoader=\"javafml\"\n"));
        assertEquals(BridgeKind.FORGE, BridgeRegistry.detect(jar));
    }

    @Test
    void neoForgeBeatsForgeWhenBothPresent(@TempDir Path dir) throws IOException {
        // NeoForge ships both files for backwards compat. Detect must pick
        // NeoForge so the right bridge handles it.
        Map<String, String> entries = new LinkedHashMap<>();
        entries.put("META-INF/mods.toml", "modLoader=\"javafml\"\n");
        entries.put("META-INF/neoforge.mods.toml", "modLoader=\"javafml\"\n");
        Path jar = writeJar(dir.resolve("hybrid.jar"), entries);
        assertEquals(BridgeKind.NEOFORGE, BridgeRegistry.detect(jar));
    }

    @Test
    void paperBeatsBukkitWhenBothPresent(@TempDir Path dir) throws IOException {
        // Paper plugins commonly ship plugin.yml as a fallback for older
        // Bukkit-only servers; on a Paper-aware host we want the Paper path.
        Map<String, String> entries = new LinkedHashMap<>();
        entries.put("plugin.yml", "name: B\nversion: 1\nmain: x.X\n");
        entries.put("paper-plugin.yml", "name: P\nversion: 1\nmain: x.X\n");
        Path jar = writeJar(dir.resolve("paperish.jar"), entries);
        assertEquals(BridgeKind.PAPER, BridgeRegistry.detect(jar));
    }

    @Test
    void pluginYmlWithoutApiIsBukkit(@TempDir Path dir) throws IOException {
        Path jar = writeJar(dir.resolve("bukkit.jar"),
                Map.of("plugin.yml", "name: B\nversion: 1\nmain: x.X\n"));
        assertEquals(BridgeKind.BUKKIT, BridgeRegistry.detect(jar));
    }

    @Test
    void pluginYmlWithApiVersionIsStillBukkit(@TempDir Path dir) throws IOException {
        // Bukkit uses `api-version:` (1.13+ namespacing). PocketMine uses `api:`.
        // The disambiguation must not treat `api-version` as a PocketMine signal.
        Path jar = writeJar(dir.resolve("bukkit-1_13.jar"),
                Map.of("plugin.yml", "name: B\nversion: 1\nmain: x.X\napi-version: 1.13\n"));
        assertEquals(BridgeKind.BUKKIT, BridgeRegistry.detect(jar));
    }

    @Test
    void pluginYmlWithApiIsPocketMine(@TempDir Path dir) throws IOException {
        Path jar = writeJar(dir.resolve("pm.jar"),
                Map.of("plugin.yml", "name: P\nversion: 1\nmain: x.X\napi: 4.0.0\n"));
        assertEquals(BridgeKind.POCKETMINE, BridgeRegistry.detect(jar));
    }

    @Test
    void pluginYmlWithCommentsAndBlanksAhead(@TempDir Path dir) throws IOException {
        // The api: scan must skip leading comments and blank lines before
        // reading the first key.
        String yml = "# header comment\n\n  # indented comment\n\napi: 4.0.0\nname: P\n";
        Path jar = writeJar(dir.resolve("pm-noisy.jar"),
                Map.of("plugin.yml", yml));
        assertEquals(BridgeKind.POCKETMINE, BridgeRegistry.detect(jar));
    }

    @Test
    void noManifestIsNull(@TempDir Path dir) throws IOException {
        Path jar = writeJar(dir.resolve("empty.jar"),
                Map.of("META-INF/randomfile.txt", "hello"));
        assertNull(BridgeRegistry.detect(jar));
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
