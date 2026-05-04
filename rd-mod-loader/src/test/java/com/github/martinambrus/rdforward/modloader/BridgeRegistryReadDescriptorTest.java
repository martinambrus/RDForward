package com.github.martinambrus.rdforward.modloader;

import com.github.martinambrus.rdforward.api.mod.ModDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BridgeRegistryReadDescriptorTest {

    @Test
    void bukkitJarReturnsDescriptor(@TempDir Path dir) throws IOException {
        Path jar = writeJar(dir.resolve("bukkit.jar"),
                Map.of("plugin.yml", "name: Hello\nversion: '1.0'\nmain: h.H\n"));
        ModDescriptor desc = BridgeRegistry.readDescriptor(BridgeKind.BUKKIT, jar);
        assertEquals("Hello", desc.id());
    }

    @Test
    void fabricJarReturnsNullNoReadDescriptorMethod(@TempDir Path dir) throws IOException {
        Path jar = writeJar(dir.resolve("fabric.jar"),
                Map.of("fabric.mod.json", "{\"id\":\"f\",\"version\":\"1\"}"));
        // FabricPluginLoader has no readDescriptor() method yet — must return null.
        assertNull(BridgeRegistry.readDescriptor(BridgeKind.FABRIC, jar));
    }

    @Test
    void forgeJarReturnsNullNoReadDescriptorMethod(@TempDir Path dir) throws IOException {
        Path jar = writeJar(dir.resolve("forge.jar"),
                Map.of("META-INF/mods.toml", "modLoader=\"javafml\"\n"));
        assertNull(BridgeRegistry.readDescriptor(BridgeKind.FORGE, jar));
    }

    @Test
    void nativeKindThrows(@TempDir Path dir) throws IOException {
        Path jar = writeJar(dir.resolve("native.jar"),
                Map.of("rdmod.json", "{\"id\":\"x\",\"version\":\"1\"}"));
        try {
            BridgeRegistry.readDescriptor(BridgeKind.NATIVE, jar);
        } catch (IllegalArgumentException e) {
            assertEquals("readDescriptor requires a non-NATIVE BridgeKind", e.getMessage());
        }
    }

    private static Path writeJar(Path target, Map<String, String> entries) throws IOException {
        Files.createDirectories(target.getParent());
        try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(target))) {
            for (Map.Entry<String, String> e : entries.entrySet()) {
                jos.putNextEntry(new JarEntry(e.getKey()));
                jos.write(e.getValue().getBytes(StandardCharsets.UTF_8));
                jos.closeEntry();
            }
        }
        return target;
    }
}
