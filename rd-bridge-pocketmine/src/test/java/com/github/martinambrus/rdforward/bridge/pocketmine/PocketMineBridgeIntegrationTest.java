package com.github.martinambrus.rdforward.bridge.pocketmine;

import com.github.martinambrus.rdforward.api.event.EventResult;
import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.bridge.pocketmine.fixtures.PocketMineTestServer;
import com.github.martinambrus.rdforward.bridge.pocketmine.fixtures.TestPocketMineListener;
import com.github.martinambrus.rdforward.bridge.pocketmine.fixtures.TestPocketMinePlugin;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Integration tests for the PocketMine bridge. Exercises the full path
 * from a fixture jar on disk through {@code PocketMinePluginLoader} and
 * {@code PocketMinePluginWrapper} to rd-api {@code ServerEvents}.
 */
class PocketMineBridgeIntegrationTest {

    private static final String[] PROPS = {
            TestPocketMinePlugin.PROP_ORDER,
            TestPocketMineListener.PROP_JOIN,
            TestPocketMineListener.PROP_BREAK
    };

    @BeforeEach
    void clearBefore() {
        ServerEvents.clearAll();
        PocketMineBridge.uninstall();
        for (String p : PROPS) System.clearProperty(p);
    }

    @AfterEach
    void clearAfter() {
        ServerEvents.clearAll();
        PocketMineBridge.uninstall();
        for (String p : PROPS) System.clearProperty(p);
    }

    @Test
    void pluginYmlLoadsPluginBaseClass(@TempDir Path dir) throws Exception {
        Path jar = writePocketMineJar(dir.resolve("plugin.jar"), "TestPM",
                TestPocketMinePlugin.class.getName(),
                TestPocketMinePlugin.class.getName());

        PocketMinePluginLoader.LoadedPocketMinePlugin loaded =
                PocketMinePluginLoader.load(jar, getClass().getClassLoader());
        try {
            assertEquals("TestPM", loaded.descriptor().id(),
                    "descriptor id must come from plugin.yml name");
            assertNotNull(loaded.plugin(), "plugin instance must have been created");
            assertTrue(loaded.plugin() instanceof TestPocketMinePlugin,
                    "loader must instantiate the main class as a PluginBase subclass");
        } finally {
            loaded.classLoader().close();
        }
    }

    @Test
    void onLoadThenOnEnableCalledInOrder(@TempDir Path dir) throws Exception {
        Path jar = writePocketMineJar(dir.resolve("lifecycle.jar"), "TestPM",
                TestPocketMinePlugin.class.getName(),
                TestPocketMinePlugin.class.getName());

        PocketMineTestServer rd = new PocketMineTestServer();
        PocketMinePluginLoader.LoadedPocketMinePlugin loaded =
                PocketMinePluginLoader.load(jar, getClass().getClassLoader());
        try {
            loaded.serverMod().onEnable(rd);
            assertEquals("load,enable", System.getProperty(TestPocketMinePlugin.PROP_ORDER),
                    "onLoad must fire before onEnable");
            assertTrue(loaded.plugin().isEnabled(), "plugin must be enabled after onEnable");

            loaded.serverMod().onDisable();
            assertEquals("load,enable,disable", System.getProperty(TestPocketMinePlugin.PROP_ORDER),
                    "onDisable must fire after onEnable");
            assertTrue(!loaded.plugin().isEnabled(),
                    "plugin must be disabled after onDisable");
        } finally {
            loaded.classLoader().close();
        }
    }

    @Test
    void handleEventAnnotationBindsToServerEvents(@TempDir Path dir) throws Exception {
        Path jar = writePocketMineJar(dir.resolve("events.jar"), "TestPM",
                TestPocketMinePlugin.class.getName(),
                TestPocketMinePlugin.class.getName(),
                TestPocketMineListener.class.getName());

        PocketMineTestServer rd = new PocketMineTestServer();
        PocketMineBridge.install(rd);
        assertTrue(PocketMineBridge.isInstalled());

        PocketMinePluginLoader.LoadedPocketMinePlugin loaded =
                PocketMinePluginLoader.load(jar, getClass().getClassLoader());
        try {
            loaded.serverMod().onEnable(rd);
            TestPocketMineListener listener = new TestPocketMineListener();
            pocketmine.Server.getInstance().getPluginManager()
                    .registerEvents(listener, loaded.plugin());

            ServerEvents.PLAYER_JOIN.invoker().onPlayerJoin("alice", null);
            assertEquals("alice", System.getProperty(TestPocketMineListener.PROP_JOIN),
                    "listener must receive PlayerJoinEvent through rd-api forwarder");
        } finally {
            loaded.classLoader().close();
            PocketMineBridge.uninstall();
        }
    }

    @Test
    void cancellableEventPropagatesCancellationToRdApi(@TempDir Path dir) throws Exception {
        Path jar = writePocketMineJar(dir.resolve("cancel.jar"), "TestPM",
                TestPocketMinePlugin.class.getName(),
                TestPocketMinePlugin.class.getName(),
                TestPocketMineListener.class.getName());

        PocketMineTestServer rd = new PocketMineTestServer();
        PocketMineBridge.install(rd);

        PocketMinePluginLoader.LoadedPocketMinePlugin loaded =
                PocketMinePluginLoader.load(jar, getClass().getClassLoader());
        try {
            loaded.serverMod().onEnable(rd);
            TestPocketMineListener listener = new TestPocketMineListener();
            pocketmine.Server.getInstance().getPluginManager()
                    .registerEvents(listener, loaded.plugin());

            EventResult allow = ServerEvents.BLOCK_BREAK.invoker().onBlockBreak("bob", 10, 64, -5, 3);
            assertEquals(EventResult.PASS, allow,
                    "uncancelled BlockBreakEvent must pass rd-api");
            assertEquals("bob@10,64,-5", System.getProperty(TestPocketMineListener.PROP_BREAK));

            EventResult deny = ServerEvents.BLOCK_BREAK.invoker().onBlockBreak("veto", 0, 0, 0, 1);
            assertEquals(EventResult.CANCEL, deny,
                    "setCancelled(true) on PocketMine event must bubble back as CANCEL");
        } finally {
            loaded.classLoader().close();
            PocketMineBridge.uninstall();
        }
    }

    private Path writePocketMineJar(Path target, String name, String mainFqcn, String... extraClasses)
            throws IOException {
        StringBuilder yml = new StringBuilder()
                .append("name: ").append(name).append("\n")
                .append("version: 1.0.0\n")
                .append("main: ").append(mainFqcn).append("\n")
                .append("api: 5.0.0\n")
                .append("authors: [Tester]\n");
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(target))) {
            jar.putNextEntry(new JarEntry("plugin.yml"));
            jar.write(yml.toString().getBytes());
            jar.closeEntry();
            for (String fqcn : extraClasses) copyClassBytes(jar, fqcn);
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
