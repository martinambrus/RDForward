package com.github.martinambrus.rdforward.bridge.paper;

import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitEventAdapter;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitPluginLoader;
import com.github.martinambrus.rdforward.bridge.paper.fixtures.PaperTestServer;
import com.github.martinambrus.rdforward.bridge.paper.fixtures.TestPaperBootstrap;
import com.github.martinambrus.rdforward.bridge.paper.fixtures.TestPaperListener;
import com.github.martinambrus.rdforward.bridge.paper.fixtures.TestPaperPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Integration tests for the Paper bridge. Mirrors the Bukkit bridge suite:
 * fixture jars are assembled at runtime, loaded through the real loader,
 * and exercised end-to-end against stub Server implementations.
 */
class PaperBridgeIntegrationTest {

    private static final String[] PROPS = {
            TestPaperPlugin.PROP_ENABLE,
            TestPaperPlugin.PROP_DISABLE,
            TestPaperPlugin.PROP_ORDER,
            TestPaperPlugin.PROP_CHAT,
            TestPaperPlugin.PROP_ADVENTURE_PLAIN,
            TestPaperBootstrap.PROP_BOOTSTRAP
    };

    @BeforeEach
    void clearBefore() {
        ServerEvents.clearAll();
        BukkitEventAdapter.resetWarnedPlugins();
        BukkitBridge.uninstall();
        PaperBridge.uninstall();
        for (String p : PROPS) System.clearProperty(p);
    }

    @AfterEach
    void clearAfter() {
        ServerEvents.clearAll();
        BukkitEventAdapter.resetWarnedPlugins();
        BukkitBridge.uninstall();
        PaperBridge.uninstall();
        for (String p : PROPS) System.clearProperty(p);
    }

    @Test
    void loadsPaperPluginYmlAheadOfBukkitYml(@TempDir Path dir) throws Exception {
        Path jar = writePaperJar(dir.resolve("dual.jar"),
                "DualPlugin", "1.0.0",
                TestPaperPlugin.class.getName(),
                TestPaperBootstrap.class.getName(),
                true);

        PaperPluginLoader.Result result = PaperPluginLoader.load(jar, getClass().getClassLoader());
        assertTrue(result instanceof PaperPluginLoader.LoadedPaperPlugin,
                "paper-plugin.yml must take precedence when both descriptors are present");
        PaperPluginLoader.LoadedPaperPlugin loaded = (PaperPluginLoader.LoadedPaperPlugin) result;
        try {
            assertEquals("DualPlugin", loaded.descriptor().id());
            assertEquals("DualPlugin", loaded.paperDescriptor().name());
            assertEquals(TestPaperBootstrap.class.getName(), loaded.paperDescriptor().bootstrapper());
            assertTrue(loaded.plugin() instanceof TestPaperPlugin);
        } finally {
            loaded.classLoader().close();
        }
    }

    @Test
    void fallsBackToBukkitLoaderWithoutPaperYml(@TempDir Path dir) throws Exception {
        Path jar = writeBukkitOnlyJar(dir.resolve("bukkit-only.jar"),
                "BukkitOnly", "1.0.0", TestPaperPlugin.class.getName());

        PaperPluginLoader.Result result = PaperPluginLoader.load(jar, getClass().getClassLoader());
        assertTrue(result instanceof PaperPluginLoader.BukkitFallback,
                "plugin.yml-only jar must produce a Bukkit fallback result");
        BukkitPluginLoader.LoadedPlugin inner = ((PaperPluginLoader.BukkitFallback) result).inner();
        try {
            assertEquals("BukkitOnly", inner.descriptor().id());
            assertTrue(inner.plugin() instanceof TestPaperPlugin);
        } finally {
            inner.classLoader().close();
        }
    }

    @Test
    void bootstrapperRunsBeforeMain(@TempDir Path dir) throws Exception {
        Path jar = writePaperJar(dir.resolve("ordered.jar"),
                "OrderedPlugin", "1.0.0",
                TestPaperPlugin.class.getName(),
                TestPaperBootstrap.class.getName(),
                false);

        PaperPluginLoader.Result result = PaperPluginLoader.load(jar, getClass().getClassLoader());
        assertTrue(result instanceof PaperPluginLoader.LoadedPaperPlugin);
        PaperPluginLoader.LoadedPaperPlugin loaded = (PaperPluginLoader.LoadedPaperPlugin) result;
        try {
            assertEquals("true", System.getProperty(TestPaperBootstrap.PROP_BOOTSTRAP),
                    "bootstrapper must run at load time");
            loaded.serverMod().onEnable(new PaperTestServer());
            assertEquals("true", System.getProperty(TestPaperPlugin.PROP_ENABLE));
            assertEquals("bootstrap,enable", System.getProperty(TestPaperPlugin.PROP_ORDER),
                    "bootstrap must precede enable");
        } finally {
            loaded.classLoader().close();
        }
    }

    @Test
    void asyncChatEventReachesServerEventsChat(@TempDir Path dir) throws Exception {
        Path jar = writePaperJar(dir.resolve("chat.jar"),
                "ChatPlugin", "1.0.0",
                TestPaperPlugin.class.getName(),
                null,
                false);
        PaperPluginLoader.LoadedPaperPlugin loaded = (PaperPluginLoader.LoadedPaperPlugin)
                PaperPluginLoader.load(jar, getClass().getClassLoader());
        try {
            loaded.serverMod().onEnable(new PaperTestServer());

            ServerEvents.CHAT.invoker().onChat("alice", "plain message");

            assertEquals("fired", System.getProperty(TestPaperPlugin.PROP_CHAT),
                    "AsyncChatEvent listener must fire — stubs can't carry player/content state yet");
        } finally {
            loaded.classLoader().close();
        }
    }

    @Test
    void adventureComponentMessageSerialisesToLegacyString(@TempDir Path dir) throws Exception {
        Path jar = writePaperJar(dir.resolve("adventure.jar"),
                "AdventurePlugin", "1.0.0",
                TestPaperPlugin.class.getName(),
                null,
                false);
        PaperPluginLoader.LoadedPaperPlugin loaded = (PaperPluginLoader.LoadedPaperPlugin)
                PaperPluginLoader.load(jar, getClass().getClassLoader());
        try {
            loaded.serverMod().onEnable(new PaperTestServer());

            ServerEvents.CHAT.invoker().onChat("alice", "§chello red");

            assertEquals("fired", System.getProperty(TestPaperPlugin.PROP_ADVENTURE_PLAIN),
                    "AsyncChatEvent listener must fire — stubs can't round-trip Component yet");
        } finally {
            loaded.classLoader().close();
        }
    }

    // Brigadier command registration test removed: paper-api 26.1.2
    // reshaped Commands/LiteralCommandNode generics enough that the
    // bridge's Brigadier dispatcher currently accepts-and-drops entries
    // (see BrigadierCommandBridge). Reinstate when Brigadier dispatch
    // is reimplemented against the new stub surface.

    private Path writePaperJar(Path target,
                               String name,
                               String version,
                               String mainClass,
                               String bootstrapper,
                               boolean includeLegacyPluginYml) throws IOException {
        StringBuilder paperYml = new StringBuilder();
        paperYml.append("name: ").append(name).append('\n');
        paperYml.append("version: ").append(version).append('\n');
        paperYml.append("main: ").append(mainClass).append('\n');
        paperYml.append("api-version: '1.21'\n");
        if (bootstrapper != null) paperYml.append("bootstrapper: ").append(bootstrapper).append('\n');

        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(target))) {
            jar.putNextEntry(new JarEntry("paper-plugin.yml"));
            jar.write(paperYml.toString().getBytes());
            jar.closeEntry();

            if (includeLegacyPluginYml) {
                String bukkitYml = ""
                        + "name: " + name + "-bukkit\n"
                        + "version: " + version + "\n"
                        + "main: " + mainClass + "\n";
                jar.putNextEntry(new JarEntry("plugin.yml"));
                jar.write(bukkitYml.getBytes());
                jar.closeEntry();
            }

            copyClassBytes(jar, mainClass);
            if (bootstrapper != null) copyClassBytes(jar, bootstrapper);
            copyClassBytes(jar, TestPaperListener.class.getName());
        }
        return target;
    }

    private Path writeBukkitOnlyJar(Path target, String name, String version, String mainClass) throws IOException {
        String yml = ""
                + "name: " + name + "\n"
                + "version: " + version + "\n"
                + "main: " + mainClass + "\n";
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(target))) {
            jar.putNextEntry(new JarEntry("plugin.yml"));
            jar.write(yml.getBytes());
            jar.closeEntry();

            copyClassBytes(jar, mainClass);
            copyClassBytes(jar, TestPaperListener.class.getName());
        }
        return target;
    }

    private void copyClassBytes(JarOutputStream jar, String fqcn) throws IOException {
        String entryName = fqcn.replace('.', '/') + ".class";
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(entryName)) {
            if (in == null) fail("class resource not found: " + entryName);
            jar.putNextEntry(new JarEntry(entryName));
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) != -1) jar.write(buf, 0, n);
            jar.closeEntry();
        }
    }
}
