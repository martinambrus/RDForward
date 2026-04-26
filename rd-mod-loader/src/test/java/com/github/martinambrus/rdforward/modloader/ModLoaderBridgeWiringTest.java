package com.github.martinambrus.rdforward.modloader;

import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.modloader.fixtures.BridgeFixturePlugin;
import com.github.martinambrus.rdforward.modloader.fixtures.TestFixtureMod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end test for the bridge dispatch path added to {@link ModLoader}.
 * Drops a synthetic Bukkit-format jar (built from the
 * {@link BridgeFixturePlugin} class plus a generated {@code plugin.yml})
 * into a temp mods dir, runs {@link ModLoader#load(Path, ClassLoader)},
 * and asserts the resulting {@link ModContainer} carries the right
 * {@link BridgeKind}, an open {@link URLClassLoader}, and a Bukkit
 * wrapper instance produced by the bridge.
 *
 * <p>Native rdmod coexistence and bridge {@link ModLoader#rebind} are
 * exercised in dedicated tests so a regression in either path fails
 * fast and traceably.
 */
class ModLoaderBridgeWiringTest {

    @BeforeEach
    void clearBefore() {
        ServerEvents.clearAll();
        System.clearProperty(BridgeFixturePlugin.PROP_ENABLED);
        System.clearProperty(BridgeFixturePlugin.PROP_DISABLED);
    }

    @AfterEach
    void clearAfter() {
        ServerEvents.clearAll();
        System.clearProperty(BridgeFixturePlugin.PROP_ENABLED);
        System.clearProperty(BridgeFixturePlugin.PROP_DISABLED);
    }

    @Test
    void bukkitJarLoadsAsBridgeContainer(@TempDir Path dir) throws Exception {
        Path modsDir = Files.createDirectories(dir.resolve("mods"));
        writeBukkitJar(modsDir.resolve("fixture-bukkit.jar"),
                "BridgeFixture",
                BridgeFixturePlugin.class.getName());

        List<ModContainer> containers = ModLoader.load(modsDir, getClass().getClassLoader());
        assertEquals(1, containers.size());
        ModContainer c = containers.get(0);
        assertEquals(BridgeKind.BUKKIT, c.bridgeKind(),
                "Bukkit plugin.yml must be classified as BUKKIT");
        assertEquals("BridgeFixture", c.id(),
                "container id should mirror the plugin.yml name");
        assertNotNull(c.classLoader(), "bridge container must hold its URLClassLoader");
        assertNotNull(c.serverMod(),
                "BukkitPluginWrapper should populate serverInstance");
        assertEquals("BukkitPluginWrapper", c.serverMod().getClass().getSimpleName(),
                "serverMod must be the Bukkit wrapper produced by the bridge");
    }

    @Test
    void rebindProducesFreshClassLoaderForBridgeContainer(@TempDir Path dir) throws Exception {
        Path modsDir = Files.createDirectories(dir.resolve("mods"));
        writeBukkitJar(modsDir.resolve("fixture-bukkit.jar"),
                "BridgeFixture",
                BridgeFixturePlugin.class.getName());

        List<ModContainer> containers = ModLoader.load(modsDir, getClass().getClassLoader());
        ModContainer c = containers.get(0);
        URLClassLoader before = c.classLoader();
        Object instanceBefore = c.serverMod();
        assertNotNull(before);
        assertNotNull(instanceBefore);

        ModLoader.rebind(c);

        URLClassLoader after = c.classLoader();
        Object instanceAfter = c.serverMod();
        assertNotNull(after, "rebind must reinstall a classloader");
        assertNotNull(instanceAfter, "rebind must reinstall the wrapper instance");
        assertNotSame(before, after, "bridge rebind must produce a fresh URLClassLoader");
        assertNotSame(instanceBefore, instanceAfter,
                "bridge rebind must produce a fresh wrapper instance");
        assertEquals(BridgeKind.BUKKIT, c.bridgeKind(),
                "bridgeKind must survive rebind");
    }

    @Test
    void nativeRdmodAndBukkitJarCoexist(@TempDir Path dir) throws Exception {
        Path modsDir = Files.createDirectories(dir.resolve("mods"));
        writeNativeJar(modsDir.resolve("native.jar"),
                TestFixtureMod.MOD_ID,
                TestFixtureMod.class.getName());
        writeBukkitJar(modsDir.resolve("bukkit.jar"),
                "BridgeFixture",
                BridgeFixturePlugin.class.getName());

        List<ModContainer> containers = ModLoader.load(modsDir, getClass().getClassLoader());
        assertEquals(2, containers.size(),
                "both descriptors must surface as containers");

        ModContainer nativeC = containers.stream()
                .filter(c -> c.id().equals(TestFixtureMod.MOD_ID))
                .findFirst().orElseThrow();
        ModContainer bukkitC = containers.stream()
                .filter(c -> c.id().equals("BridgeFixture"))
                .findFirst().orElseThrow();

        assertEquals(BridgeKind.NATIVE, nativeC.bridgeKind(),
                "rdmod.json container must remain NATIVE");
        assertTrue(nativeC.classLoader() instanceof ModClassLoader,
                "native containers must keep ModClassLoader for parent-first API delegation");

        assertEquals(BridgeKind.BUKKIT, bukkitC.bridgeKind(),
                "plugin.yml container must classify as BUKKIT");
    }

    @Test
    void jarWithoutManifestIsSkippedNotFatal(@TempDir Path dir) throws Exception {
        Path modsDir = Files.createDirectories(dir.resolve("mods"));
        // Empty jar with one harmless META-INF entry — no rdmod / paper /
        // fabric / forge / neoforge / bukkit / pocketmine markers.
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(modsDir.resolve("noise.jar")))) {
            jar.putNextEntry(new JarEntry("META-INF/random.txt"));
            jar.write("hello".getBytes(StandardCharsets.UTF_8));
            jar.closeEntry();
        }

        List<ModContainer> containers = ModLoader.load(modsDir, getClass().getClassLoader());
        assertEquals(0, containers.size(),
                "unrecognised jars must be skipped, not promoted to containers");
        assertNull(System.getProperty(BridgeFixturePlugin.PROP_ENABLED),
                "no fixture should have been instantiated");
    }

    private void writeBukkitJar(Path target, String name, String mainClass) throws IOException {
        String yaml = "name: " + name + "\n"
                + "version: '1.0.0'\n"
                + "main: " + mainClass + "\n"
                + "author: testfixture\n";
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(target))) {
            jar.putNextEntry(new JarEntry("plugin.yml"));
            jar.write(yaml.getBytes(StandardCharsets.UTF_8));
            jar.closeEntry();
            copyClassBytes(jar, mainClass);
        }
    }

    private void writeNativeJar(Path target, String modId, String mainClass) throws IOException {
        String descriptor = "{"
                + "\"id\":\"" + modId + "\","
                + "\"version\":\"1.0.0\","
                + "\"entrypoints\":{\"server\":\"" + mainClass + "\"}"
                + "}";
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(target))) {
            jar.putNextEntry(new JarEntry("rdmod.json"));
            jar.write(descriptor.getBytes(StandardCharsets.UTF_8));
            jar.closeEntry();
            copyClassBytes(jar, mainClass);
        }
    }

    private void copyClassBytes(JarOutputStream jar, String fqcn) throws IOException {
        String entryName = fqcn.replace('.', '/') + ".class";
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(entryName)) {
            if (in == null) throw new IOException("class resource not found: " + entryName);
            jar.putNextEntry(new JarEntry(entryName));
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) != -1) jar.write(buf, 0, n);
            jar.closeEntry();
        }
    }
}
