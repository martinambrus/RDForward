package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.api.mod.ModDescriptor;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitPluginLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BukkitPluginLoaderReadDescriptorTest {

    @Test
    void parsesNameVersionAndDeps(@TempDir Path dir) throws IOException {
        Path jar = writePluginJar(dir, "name: TestPlugin\nversion: '2.0'\nmain: com.example.Test\nsoftdepend: [WorldEdit]\n");
        ModDescriptor desc = BukkitPluginLoader.readDescriptor(jar);
        assertEquals("TestPlugin", desc.id());
        assertEquals("2.0", desc.version());
        assertTrue(desc.softDependencies().containsKey("WorldEdit"));
        assertEquals(0, desc.dependencies().size());
    }

    @Test
    void parsesHardDeps(@TempDir Path dir) throws IOException {
        Path jar = writePluginJar(dir, "name: Consumer\nversion: '1.0'\nmain: c.C\ndepend: [WorldEdit]\n");
        ModDescriptor desc = BukkitPluginLoader.readDescriptor(jar);
        assertTrue(desc.dependencies().containsKey("WorldEdit"));
        assertEquals(0, desc.softDependencies().size());
    }

    @Test
    void throwsOnMissingPluginYml(@TempDir Path dir) throws IOException {
        Path jar = writeBareJar(dir.resolve("empty.jar"));
        assertThrows(IOException.class, () -> BukkitPluginLoader.readDescriptor(jar));
    }

    private static Path writePluginJar(Path dir, String pluginYml) throws IOException {
        Path jar = dir.resolve("plugin.jar");
        try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(jar))) {
            jos.putNextEntry(new JarEntry("plugin.yml"));
            jos.write(pluginYml.getBytes(StandardCharsets.UTF_8));
            jos.closeEntry();
        }
        return jar;
    }

    private static Path writeBareJar(Path target) throws IOException {
        Files.createDirectories(target.getParent());
        try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(target))) {
            jos.putNextEntry(new JarEntry("META-INF/MANIFEST.MF"));
            jos.write("Manifest-Version: 1.0\n".getBytes(StandardCharsets.UTF_8));
            jos.closeEntry();
        }
        return target;
    }
}
