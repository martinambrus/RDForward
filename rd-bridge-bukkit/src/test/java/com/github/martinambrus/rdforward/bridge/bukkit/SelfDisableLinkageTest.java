package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.SelfDisableProbePlugin;
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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * End-to-end test for the mcbans v3.8 self-disable boot pattern: load a
 * plugin jar through {@link BukkitPluginLoader} (the same path real
 * plugin jars take at server startup), drive its {@code onEnable} through
 * the wrapper, and assert the post-enable check throws
 * {@link PluginSelfDisabledException} -- catching the half-loaded state
 * before listeners and commands wire into the live registries.
 *
 * <p>Also pins that the loader registers the plugin into
 * {@link BukkitBridge}'s name-keyed map (so {@code pm.getPlugin(name)}
 * returns a real instance) and that the failure path drops the entry.
 */
class SelfDisableLinkageTest {

    @BeforeEach
    void clearBefore() {
        ServerEvents.clearAll();
        BukkitEventAdapter.resetWarnedPlugins();
        BukkitBridge.uninstall();
        BukkitBridge.unregisterPlugin(SelfDisableProbePlugin.PLUGIN_NAME);
    }

    @AfterEach
    void clearAfter() {
        ServerEvents.clearAll();
        BukkitEventAdapter.resetWarnedPlugins();
        BukkitBridge.uninstall();
        BukkitBridge.unregisterPlugin(SelfDisableProbePlugin.PLUGIN_NAME);
    }

    @Test
    void selfDisablingPluginIsCaughtByPostEnableGate(@TempDir Path dir) throws Exception {
        Path jar = writePluginJar(dir.resolve("self-disable-probe.jar"));

        StubRdServer rd = new StubRdServer();
        BukkitBridge.install(rd);

        BukkitPluginLoader.LoadedPlugin loaded =
                BukkitPluginLoader.load(jar, getClass().getClassLoader());
        try {
            // After load, the plugin is in the registry so
            // pm.getPlugin(name) returns a live instance.
            assertSame(loaded.plugin(),
                    BukkitBridge.lookupPlugin(SelfDisableProbePlugin.PLUGIN_NAME),
                    "BukkitPluginLoader must register the plugin under its declared name");

            assertThrows(PluginSelfDisabledException.class,
                    () -> loaded.serverMod().onEnable(rd),
                    "post-enable isEnabled() check must throw when plugin self-disabled");

            assertNull(BukkitBridge.lookupPlugin(SelfDisableProbePlugin.PLUGIN_NAME),
                    "wrapper's failure-path finally must unregister the plugin");
        } finally {
            loaded.classLoader().close();
        }
    }

    private Path writePluginJar(Path target) throws IOException {
        StringBuilder yml = new StringBuilder();
        yml.append("name: ").append(SelfDisableProbePlugin.PLUGIN_NAME).append('\n');
        yml.append("version: 1.0.0\n");
        yml.append("main: ").append(SelfDisableProbePlugin.class.getName()).append('\n');

        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(target))) {
            jar.putNextEntry(new JarEntry("plugin.yml"));
            jar.write(yml.toString().getBytes());
            jar.closeEntry();
            copyClassBytes(jar, SelfDisableProbePlugin.class.getName());
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
