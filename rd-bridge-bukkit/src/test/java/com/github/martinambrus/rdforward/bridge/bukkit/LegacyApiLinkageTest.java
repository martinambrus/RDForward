package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.LegacyApiProbePlugin;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * End-to-end linkage test for the pre-Bukkit-1.x API surfaces RDForward
 * exposes for LogBlock 1.41 / LogBlockQuestioner 0.02 — runs the probe
 * plugin through {@link BukkitPluginLoader} (the same path real plugin
 * jars take at server startup) and asserts {@code onEnable} completes
 * without {@code NoClassDefFoundError} / {@code NoSuchMethodError}.
 *
 * <p>The probe touches: {@link org.bukkit.Material#matchMaterial},
 * {@link org.bukkit.event.player.PlayerListener} subclass instantiation,
 * the legacy
 * {@code registerEvent(Event.Type, Listener, Event.Priority, Plugin)}
 * overload, and {@link
 * org.bukkit.configuration.ConfigurationSection#getConfigurationSection}
 * with nested {@code getKeys(false)}.
 */
class LegacyApiLinkageTest {

    private static final String[] PROPS = {
            LegacyApiProbePlugin.PROP_OK,
            LegacyApiProbePlugin.PROP_FAILURE
    };

    @BeforeEach
    void clearBefore() {
        ServerEvents.clearAll();
        BukkitEventAdapter.resetWarnedPlugins();
        BukkitBridge.uninstall();
        for (String p : PROPS) System.clearProperty(p);
    }

    @AfterEach
    void clearAfter() {
        ServerEvents.clearAll();
        BukkitEventAdapter.resetWarnedPlugins();
        BukkitBridge.uninstall();
        for (String p : PROPS) System.clearProperty(p);
    }

    @Test
    void legacyApiProbePluginLoadsAndExercisesEveryLegacySurface(@TempDir Path dir)
            throws Exception {
        Path jar = writePluginJar(dir.resolve("legacy-probe.jar"));

        StubRdServer rd = new StubRdServer();
        BukkitBridge.install(rd);

        BukkitPluginLoader.LoadedPlugin loaded =
                BukkitPluginLoader.load(jar, getClass().getClassLoader());
        try {
            loaded.serverMod().onEnable(rd);

            String failure = System.getProperty(LegacyApiProbePlugin.PROP_FAILURE);
            assertNull(failure, "onEnable should not surface a linkage failure: " + failure);
            assertEquals("true", System.getProperty(LegacyApiProbePlugin.PROP_OK),
                    "probe must mark onEnable as completed");

            loaded.serverMod().onDisable();
        } finally {
            loaded.classLoader().close();
        }
    }

    private Path writePluginJar(Path target) throws IOException {
        StringBuilder yml = new StringBuilder();
        yml.append("name: LegacyApiProbe\n");
        yml.append("version: 1.0.0\n");
        yml.append("main: ").append(LegacyApiProbePlugin.class.getName()).append('\n');

        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(target))) {
            jar.putNextEntry(new JarEntry("plugin.yml"));
            jar.write(yml.toString().getBytes());
            jar.closeEntry();

            copyClassBytes(jar, LegacyApiProbePlugin.class.getName());
            copyClassBytes(jar, LegacyApiProbePlugin.class.getName() + "$ProbeListener");
        }
        return target;
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
