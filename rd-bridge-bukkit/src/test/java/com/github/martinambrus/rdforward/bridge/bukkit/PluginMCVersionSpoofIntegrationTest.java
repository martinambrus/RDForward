package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.MCVersionProbePlugin;
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

/**
 * End-to-end coverage of the per-plugin MC version spoof. Loads
 * three jars sharing one entrypoint class but with different
 * {@code plugin.yml} names + api-version, plus an in-memory
 * override entry. Each plugin's onEnable records the version
 * strings it observes via Bukkit; the test asserts each sees its
 * tuned value.
 *
 * <p>Also locks in the backward-compat case: a plugin with no
 * api-version and no override sees the historical default tuple
 * verbatim, so existing modern plugins (Essentials, AuthMe,
 * LuckPerms, WorldEdit) observe identical strings to the
 * pre-resolver build.
 */
class PluginMCVersionSpoofIntegrationTest {

    @BeforeEach
    void cleanBefore() {
        ServerEvents.clearAll();
        BukkitBridge.uninstall();
        PluginMCVersionResolver.resetForTests();
        clearProbeProps();
    }

    @AfterEach
    void cleanAfter() {
        ServerEvents.clearAll();
        BukkitBridge.uninstall();
        PluginMCVersionResolver.resetForTests();
        clearProbeProps();
    }

    @Test
    void threePluginsSeeIndependentlyTunedVersions(@TempDir Path dir) throws Exception {
        // Manually pre-seed the override map BEFORE bridge install so
        // the resolver registers DefaultProbe / ApiVersionProbe with
        // the right tuple at load time.
        PluginMCVersionResolver.putOverrideForTests("OverrideProbe", "1.7.9");

        StubRdServer rd = new StubRdServer();
        BukkitBridge.install(rd);

        // 1) No api-version, no override → should observe DEFAULT (1.21.4)
        Path defaultJar = writeProbeJar(dir.resolve("default.jar"), "DefaultProbe", null);
        // 2) api-version: 1.13 → should observe 1.13.2
        Path apiJar = writeProbeJar(dir.resolve("api113.jar"), "ApiVersionProbe", "1.13");
        // 3) Override map entry → should observe 1.7.9 regardless of api-version
        Path overrideJar = writeProbeJar(dir.resolve("override.jar"), "OverrideProbe", "1.21");

        // Mask the probe class on the parent so the per-plugin
        // URLClassLoader actually defines it (parent-first delegation
        // would otherwise hand the test-classpath copy back, putting
        // MCVersionProbePlugin onto the test classloader and defeating
        // the resolver's stack-walk lookup).
        ClassLoader masked = new MaskingClassLoader(
                getClass().getClassLoader(),
                MCVersionProbePlugin.class.getName());
        BukkitPluginLoader.LoadedPlugin defaultLoaded =
                BukkitPluginLoader.load(defaultJar, masked);
        BukkitPluginLoader.LoadedPlugin apiLoaded =
                BukkitPluginLoader.load(apiJar, masked);
        BukkitPluginLoader.LoadedPlugin overrideLoaded =
                BukkitPluginLoader.load(overrideJar, masked);
        try {
            defaultLoaded.serverMod().onEnable(rd);
            apiLoaded.serverMod().onEnable(rd);
            overrideLoaded.serverMod().onEnable(rd);

            // DefaultProbe must see the historical-default strings.
            assertEquals("RDForward-bridge-1.0 (MC: 1.21.4)",
                    System.getProperty(MCVersionProbePlugin.PROP_PREFIX + "DefaultProbe.version"));
            assertEquals("1.21.4-R0.1-SNAPSHOT",
                    System.getProperty(MCVersionProbePlugin.PROP_PREFIX + "DefaultProbe.bukkitVersion"));

            // ApiVersionProbe must see the api-version table value.
            assertEquals("RDForward-bridge-1.0 (MC: 1.13.2)",
                    System.getProperty(MCVersionProbePlugin.PROP_PREFIX + "ApiVersionProbe.version"));
            assertEquals("1.13.2-R0.1-SNAPSHOT",
                    System.getProperty(MCVersionProbePlugin.PROP_PREFIX + "ApiVersionProbe.bukkitVersion"));

            // OverrideProbe override beats the api-version it declared.
            assertEquals("RDForward-bridge-1.0 (MC: 1.7.9)",
                    System.getProperty(MCVersionProbePlugin.PROP_PREFIX + "OverrideProbe.version"));
            assertEquals("1.7.9-R0.1-SNAPSHOT",
                    System.getProperty(MCVersionProbePlugin.PROP_PREFIX + "OverrideProbe.bukkitVersion"));

            defaultLoaded.serverMod().onDisable();
            apiLoaded.serverMod().onDisable();
            overrideLoaded.serverMod().onDisable();
        } finally {
            defaultLoaded.classLoader().close();
            apiLoaded.classLoader().close();
            overrideLoaded.classLoader().close();
        }
    }

    private static void clearProbeProps() {
        for (String key : new String[] {
                "DefaultProbe.version", "DefaultProbe.bukkitVersion",
                "ApiVersionProbe.version", "ApiVersionProbe.bukkitVersion",
                "OverrideProbe.version", "OverrideProbe.bukkitVersion"
        }) {
            System.clearProperty(MCVersionProbePlugin.PROP_PREFIX + key);
        }
    }

    private Path writeProbeJar(Path target, String name, String apiVersion) throws IOException {
        StringBuilder yml = new StringBuilder();
        yml.append("name: ").append(name).append('\n');
        yml.append("version: 1.0.0\n");
        yml.append("main: ").append(MCVersionProbePlugin.class.getName()).append('\n');
        if (apiVersion != null) {
            yml.append("api-version: '").append(apiVersion).append("'\n");
        }
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(target))) {
            jar.putNextEntry(new JarEntry("plugin.yml"));
            jar.write(yml.toString().getBytes());
            jar.closeEntry();
            copyClassBytes(jar, MCVersionProbePlugin.class.getName());
        }
        return target;
    }

    /** Parent classloader that hides one named class so the per-plugin
     *  URLClassLoader defines its own copy from the jar. Required for
     *  the stack-walk-based resolver to attribute frames in that class
     *  to the per-plugin loader rather than the test classpath. */
    private static final class MaskingClassLoader extends ClassLoader {
        private final String hidden;
        MaskingClassLoader(ClassLoader parent, String hidden) {
            super(parent);
            this.hidden = hidden;
        }
        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (hidden.equals(name)) throw new ClassNotFoundException(name);
            return super.loadClass(name, resolve);
        }
        @Override
        public java.io.InputStream getResourceAsStream(String name) {
            String entry = hidden.replace('.', '/') + ".class";
            if (entry.equals(name)) return null;
            return super.getResourceAsStream(name);
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
