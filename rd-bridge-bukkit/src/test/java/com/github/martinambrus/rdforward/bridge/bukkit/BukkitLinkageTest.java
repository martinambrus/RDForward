package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.LinkageProbePlugin;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises class-load linkage of generated Bukkit stubs. The probe
 * plugin touches a broad cross-section of generated classes in its
 * {@code onEnable}; if any of them is missing, has a mismatched
 * signature, or references a dropped transitive class, the plugin
 * fails to load and the test surfaces the root cause.
 */
class BukkitLinkageTest {

    private static final String[] PROPS = {
            LinkageProbePlugin.PROP_ENABLE_OK,
            LinkageProbePlugin.PROP_TOUCH_COUNT,
            LinkageProbePlugin.PROP_FAILURE
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
    void linkageProbePluginLoadsWithoutClassOrMethodResolutionErrors(@TempDir Path dir)
            throws Exception {
        Path jar = writePluginJar(dir.resolve("probe.jar"));

        StubRdServer rd = new StubRdServer();
        BukkitBridge.install(rd);

        BukkitPluginLoader.LoadedPlugin loaded =
                BukkitPluginLoader.load(jar, getClass().getClassLoader());
        try {
            loaded.serverMod().onEnable(rd);

            String failure = System.getProperty(LinkageProbePlugin.PROP_FAILURE);
            assertNull(failure, "onEnable should not surface a linkage failure: " + failure);
            assertEquals("true", System.getProperty(LinkageProbePlugin.PROP_ENABLE_OK));
            assertTrue(Integer.parseInt(
                    System.getProperty(LinkageProbePlugin.PROP_TOUCH_COUNT, "0")) >= 10,
                    "probe should report touching at least 10 generated classes");

            loaded.serverMod().onDisable();
        } finally {
            loaded.classLoader().close();
        }
    }

    private Path writePluginJar(Path target) throws IOException {
        StringBuilder yml = new StringBuilder();
        yml.append("name: LinkageProbe\n");
        yml.append("version: 1.0.0\n");
        yml.append("main: ").append(LinkageProbePlugin.class.getName()).append('\n');

        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(target))) {
            jar.putNextEntry(new JarEntry("plugin.yml"));
            jar.write(yml.toString().getBytes());
            jar.closeEntry();

            copyClassBytes(jar, LinkageProbePlugin.class.getName());
            copyClassBytes(jar, LinkageProbePlugin.class.getName() + "$ProbeListener");
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
