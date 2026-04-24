package com.github.martinambrus.rdforward.modloader;

import com.github.martinambrus.rdforward.api.command.Command;
import com.github.martinambrus.rdforward.api.command.CommandRegistry;
import com.github.martinambrus.rdforward.api.command.TabCompleter;
import com.github.martinambrus.rdforward.api.event.EventPriority;
import com.github.martinambrus.rdforward.api.event.EventResult;
import com.github.martinambrus.rdforward.api.event.ListenerInfo;
import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.modloader.admin.EventManager;
import com.github.martinambrus.rdforward.api.mod.ModManager;
import com.github.martinambrus.rdforward.api.network.PluginChannel;
import com.github.martinambrus.rdforward.api.permission.PermissionManager;
import com.github.martinambrus.rdforward.api.player.Player;
import com.github.martinambrus.rdforward.api.registry.RegistryKey;
import com.github.martinambrus.rdforward.api.scheduler.ScheduledTask;
import com.github.martinambrus.rdforward.api.scheduler.Scheduler;
import com.github.martinambrus.rdforward.api.server.Server;
import com.github.martinambrus.rdforward.api.version.ProtocolVersion;
import com.github.martinambrus.rdforward.api.world.World;
import com.github.martinambrus.rdforward.modloader.fixtures.TestFixtureMod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModLoaderIntegrationTest {

    @BeforeEach
    void clearBefore() {
        ServerEvents.clearAll();
        System.clearProperty(TestFixtureMod.PROP_ENABLED);
        System.clearProperty(TestFixtureMod.PROP_DISABLED);
        System.clearProperty(TestFixtureMod.PROP_FIRED);
    }

    @AfterEach
    void clearAfter() {
        ServerEvents.clearAll();
        System.clearProperty(TestFixtureMod.PROP_ENABLED);
        System.clearProperty(TestFixtureMod.PROP_DISABLED);
        System.clearProperty(TestFixtureMod.PROP_FIRED);
    }

    @Test
    void loadEnableFireDisableSweepsListeners(@TempDir Path dir) throws Exception {
        Path modsDir = Files.createDirectories(dir.resolve("mods"));
        writeModJar(modsDir.resolve("fixture.jar"),
                TestFixtureMod.MOD_ID,
                TestFixtureMod.class.getName());

        List<ModContainer> containers = ModLoader.load(modsDir, getClass().getClassLoader());
        assertEquals(1, containers.size());
        assertEquals(TestFixtureMod.MOD_ID, containers.get(0).id());

        StubServer server = new StubServer();
        com.github.martinambrus.rdforward.modloader.ModManager mm =
                new com.github.martinambrus.rdforward.modloader.ModManager(server);
        mm.setContainers(containers);
        mm.enableAll();

        assertEquals("true", System.getProperty(TestFixtureMod.PROP_ENABLED),
                "onEnable should have fired and set the property");

        ServerEvents.BLOCK_PLACE.invoker().onBlockPlace("alice", 10, 64, -3, 5);
        assertEquals("alice,10,64,-3,5", System.getProperty(TestFixtureMod.PROP_FIRED),
                "registered listener should have been called on event dispatch");

        List<ListenerInfo> beforeDisable = ServerEvents.BLOCK_PLACE.getListenerInfo();
        assertTrue(beforeDisable.stream().anyMatch(i -> i.modId().equals(TestFixtureMod.MOD_ID)));

        mm.disableAll();
        assertEquals("true", System.getProperty(TestFixtureMod.PROP_DISABLED));

        List<ListenerInfo> afterDisable = ServerEvents.BLOCK_PLACE.getListenerInfo();
        assertFalse(afterDisable.stream().anyMatch(i -> i.modId().equals(TestFixtureMod.MOD_ID)),
                "sweep should have removed all listeners owned by the mod");

        System.clearProperty(TestFixtureMod.PROP_FIRED);
        EventResult r = ServerEvents.BLOCK_PLACE.invoker().onBlockPlace("bob", 0, 0, 0, 0);
        assertEquals(EventResult.PASS, r);
        assertFalse(System.getProperty(TestFixtureMod.PROP_FIRED) != null,
                "swept listener must not fire again");
    }

    @Test
    void reloadProducesNewClassLoaderAndInstance(@TempDir Path dir) throws Exception {
        Path modsDir = Files.createDirectories(dir.resolve("mods"));
        writeModJar(modsDir.resolve("fixture.jar"),
                TestFixtureMod.MOD_ID,
                TestFixtureMod.class.getName());

        List<ModContainer> containers = ModLoader.load(modsDir, getClass().getClassLoader());
        ModContainer c = containers.get(0);

        StubServer server = new StubServer();
        com.github.martinambrus.rdforward.modloader.ModManager mm =
                new com.github.martinambrus.rdforward.modloader.ModManager(server);
        mm.setContainers(containers);
        mm.enableAll();

        ModClassLoader loaderBefore = c.classLoader();
        Object instanceBefore = c.serverMod();
        assertNotNull(loaderBefore, "classloader should exist after enable");
        assertNotNull(instanceBefore, "server instance should exist after enable");

        mm.reload(TestFixtureMod.MOD_ID);

        ModClassLoader loaderAfter = c.classLoader();
        Object instanceAfter = c.serverMod();
        assertNotNull(loaderAfter, "classloader should exist after reload");
        assertNotNull(instanceAfter, "server instance should exist after reload");
        assertNotSame(loaderBefore, loaderAfter, "reload must create a fresh classloader");
        assertNotSame(instanceBefore, instanceAfter, "reload must create a fresh server instance");
        assertEquals(ModState.ENABLED, c.state());
    }

    @Test
    void detectOrphansReturnsZeroAfterCleanDisable(@TempDir Path dir) throws Exception {
        Path modsDir = Files.createDirectories(dir.resolve("mods"));
        writeModJar(modsDir.resolve("fixture.jar"),
                TestFixtureMod.MOD_ID,
                TestFixtureMod.class.getName());

        List<ModContainer> containers = ModLoader.load(modsDir, getClass().getClassLoader());

        StubServer server = new StubServer();
        com.github.martinambrus.rdforward.modloader.ModManager mm =
                new com.github.martinambrus.rdforward.modloader.ModManager(server);
        mm.setContainers(containers);
        mm.enableAll();

        mm.disableOne(TestFixtureMod.MOD_ID);

        int orphans = mm.detectOrphans(TestFixtureMod.MOD_ID);
        assertEquals(0, orphans,
                "after disable+sweep, no listener/command/task/channel/thread should remain tagged with the mod id");
    }

    @Test
    void eventPriorityOverrideReappliedAfterReload(@TempDir Path dir) throws Exception {
        Path modsDir = Files.createDirectories(dir.resolve("mods"));
        writeModJar(modsDir.resolve("fixture.jar"),
                TestFixtureMod.MOD_ID,
                TestFixtureMod.class.getName());

        EventManager.install(List.of(ServerEvents.class), dir.resolve("event-overrides.json"));

        List<ModContainer> containers = ModLoader.load(modsDir, getClass().getClassLoader());
        StubServer server = new StubServer();
        com.github.martinambrus.rdforward.modloader.ModManager mm =
                new com.github.martinambrus.rdforward.modloader.ModManager(server);
        mm.setContainers(containers);
        mm.enableAll();

        assertEquals(EventPriority.NORMAL,
                ServerEvents.BLOCK_PLACE.findPriority(TestFixtureMod.MOD_ID, null),
                "fixture mod registers at NORMAL by default");
        assertTrue(EventManager.setPriority(
                "ServerEvents#BLOCK_PLACE", TestFixtureMod.MOD_ID, EventPriority.HIGH));

        mm.reload(TestFixtureMod.MOD_ID);

        assertEquals(EventPriority.HIGH,
                ServerEvents.BLOCK_PLACE.findPriority(TestFixtureMod.MOD_ID, null),
                "admin HIGH priority override must be re-applied after a hot reload");

        EventManager.clearAll();
    }

    @Test
    void threadTrackerStopsOwnedThreadsOnSweep() throws Exception {
        ModThreadTracker tracker = new ModThreadTracker();
        java.util.concurrent.CountDownLatch started = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.atomic.AtomicBoolean interrupted = new java.util.concurrent.atomic.AtomicBoolean();

        Thread t = tracker.createThread("modX", () -> {
            started.countDown();
            try {
                Thread.sleep(30_000L);
            } catch (InterruptedException ie) {
                interrupted.set(true);
            }
        }, "worker");
        t.start();
        assertTrue(started.await(2, java.util.concurrent.TimeUnit.SECONDS), "worker should start");

        int stopped = tracker.stopThreadsOwnedBy("modX");
        assertEquals(1, stopped);
        assertTrue(interrupted.get(), "worker thread should have been interrupted");
        assertFalse(t.isAlive(), "worker thread should terminate after interrupt+join");
        assertTrue(tracker.snapshot().isEmpty(), "tracker should drop stopped threads from its map");
    }

    @Test
    void yamlDescriptorDiscoveredAndLoaded(@TempDir Path dir) throws Exception {
        Path modsDir = Files.createDirectories(dir.resolve("mods"));
        String yaml = "id: " + TestFixtureMod.MOD_ID + "\n"
                + "version: 1.0.0\n"
                + "entrypoints:\n"
                + "  server: " + TestFixtureMod.class.getName() + "\n";
        writeJarWithDescriptor(modsDir.resolve("fixture.jar"),
                "rdmod.yaml", yaml, TestFixtureMod.class.getName());

        List<ModContainer> containers = ModLoader.load(modsDir, getClass().getClassLoader());
        assertEquals(1, containers.size());
        assertEquals(TestFixtureMod.MOD_ID, containers.get(0).id());
        assertEquals(TestFixtureMod.class.getName(),
                containers.get(0).descriptor().serverEntrypoint());
    }

    @Test
    void tomlDescriptorDiscoveredAndLoaded(@TempDir Path dir) throws Exception {
        Path modsDir = Files.createDirectories(dir.resolve("mods"));
        String toml = "id = \"" + TestFixtureMod.MOD_ID + "\"\n"
                + "version = \"1.0.0\"\n"
                + "[entrypoints]\n"
                + "server = \"" + TestFixtureMod.class.getName() + "\"\n";
        writeJarWithDescriptor(modsDir.resolve("fixture.jar"),
                "rdmod.toml", toml, TestFixtureMod.class.getName());

        List<ModContainer> containers = ModLoader.load(modsDir, getClass().getClassLoader());
        assertEquals(1, containers.size());
        assertEquals(TestFixtureMod.MOD_ID, containers.get(0).id());
    }

    @Test
    void jsonPreferredWhenMultipleDescriptorsPresent(@TempDir Path dir) throws Exception {
        Path modsDir = Files.createDirectories(dir.resolve("mods"));
        String json = "{\"id\":\"" + TestFixtureMod.MOD_ID + "\",\"version\":\"1.0.0\","
                + "\"entrypoints\":{\"server\":\"" + TestFixtureMod.class.getName() + "\"}}";
        String toml = "id = \"SHOULD-BE-IGNORED\"\nversion = \"9.9.9\"\n";

        try (JarOutputStream jar = new JarOutputStream(
                Files.newOutputStream(modsDir.resolve("fixture.jar")))) {
            jar.putNextEntry(new JarEntry("rdmod.json"));
            jar.write(json.getBytes());
            jar.closeEntry();
            jar.putNextEntry(new JarEntry("rdmod.toml"));
            jar.write(toml.getBytes());
            jar.closeEntry();
            copyClassBytes(jar, TestFixtureMod.class.getName());
        }

        List<ModContainer> containers = ModLoader.load(modsDir, getClass().getClassLoader());
        assertEquals(1, containers.size());
        assertEquals(TestFixtureMod.MOD_ID, containers.get(0).id(),
                "JSON descriptor must take precedence when multiple formats coexist");
    }

    @Test
    void repeatedReloadDoesNotLeakClassLoaders(@TempDir Path dir) throws Exception {
        Path modsDir = Files.createDirectories(dir.resolve("mods"));
        writeModJar(modsDir.resolve("fixture.jar"),
                TestFixtureMod.MOD_ID,
                TestFixtureMod.class.getName());

        List<ModContainer> containers = ModLoader.load(modsDir, getClass().getClassLoader());
        ModContainer c = containers.get(0);

        StubServer server = new StubServer();
        com.github.martinambrus.rdforward.modloader.ModManager mm =
                new com.github.martinambrus.rdforward.modloader.ModManager(server);
        mm.setContainers(containers);
        mm.enableAll();

        final int iterations = 50;
        java.lang.ref.ReferenceQueue<ClassLoader> queue = new java.lang.ref.ReferenceQueue<>();
        java.util.List<java.lang.ref.WeakReference<ClassLoader>> history = new java.util.ArrayList<>();

        for (int i = 0; i < iterations; i++) {
            ModClassLoader prev = c.classLoader();
            assertNotNull(prev, "classloader must exist before reload iteration " + i);
            history.add(new java.lang.ref.WeakReference<>(prev, queue));
            mm.reload(TestFixtureMod.MOD_ID);
            assertEquals(ModState.ENABLED, c.state(),
                    "mod must remain ENABLED after iteration " + i);
            assertNotSame(prev, c.classLoader(),
                    "each reload must produce a fresh classloader");
        }

        // Let the last generation be the only hard reference; drop the rest.
        // Force GC aggressively — enough time for the PhantomReachable sweep.
        for (int attempt = 0; attempt < 20; attempt++) {
            System.gc();
            System.runFinalization();
            try { Thread.sleep(50L); } catch (InterruptedException ignored) {}
        }

        int reclaimed = 0;
        for (java.lang.ref.WeakReference<ClassLoader> ref : history) {
            if (ref.get() == null) reclaimed++;
        }
        // At least half of the historical classloaders must be reclaimable.
        // We don't require 100% because the JVM can hold the most recent
        // generations on the stack / in JIT-optimized frames transiently.
        assertTrue(reclaimed >= iterations / 2,
                "expected most reloaded classloaders to be GC-eligible, got "
                        + reclaimed + "/" + iterations);

        mm.disableOne(TestFixtureMod.MOD_ID);
        int orphans = mm.detectOrphans(TestFixtureMod.MOD_ID);
        assertEquals(0, orphans,
                "after " + iterations + " reloads + final disable, no listener/command/task/"
                        + "channel/thread should remain tagged with the mod id");
    }

    @Test
    void endToEndLifecycleSurvivesSimulatedRestart(@TempDir Path dir) throws Exception {
        Path modsDir = Files.createDirectories(dir.resolve("mods"));
        Path configDir = Files.createDirectories(dir.resolve("mod-config"));
        Path overrideFile = configDir.resolve("event-overrides.json");
        Path jar = modsDir.resolve("fixture.jar");
        writeModJar(jar, TestFixtureMod.MOD_ID, TestFixtureMod.class.getName());

        // --- Session 1: fresh boot, fire event, set admin priority override ---
        EventManager.install(List.of(ServerEvents.class), overrideFile);
        List<ModContainer> containers1 = ModLoader.load(modsDir, getClass().getClassLoader());
        StubServer server1 = new StubServer();
        com.github.martinambrus.rdforward.modloader.ModManager mm1 =
                new com.github.martinambrus.rdforward.modloader.ModManager(server1);
        mm1.setContainers(containers1);
        mm1.enableAll();
        EventManager.applyOverrides(id -> mm1.get(id) != null);

        assertEquals("true", System.getProperty(TestFixtureMod.PROP_ENABLED));
        ServerEvents.BLOCK_PLACE.invoker().onBlockPlace("alice", 1, 2, 3, 4);
        assertEquals("alice,1,2,3,4", System.getProperty(TestFixtureMod.PROP_FIRED),
                "listener must fire during session 1");

        assertEquals(EventPriority.NORMAL,
                ServerEvents.BLOCK_PLACE.findPriority(TestFixtureMod.MOD_ID, null),
                "default priority is NORMAL");
        assertTrue(EventManager.setPriority(
                "ServerEvents#BLOCK_PLACE", TestFixtureMod.MOD_ID, EventPriority.HIGH),
                "admin override must succeed while the mod is live");
        assertTrue(Files.exists(overrideFile),
                "event override must be persisted to disk");

        mm1.disableAll();
        assertEquals("true", System.getProperty(TestFixtureMod.PROP_DISABLED));
        // Don't call EventManager.clearAll() — that would wipe the persisted
        // override file. A real server restart keeps the JSON on disk; only
        // the in-memory caches die. EventManager.install() below reloads them.
        System.clearProperty(TestFixtureMod.PROP_ENABLED);
        System.clearProperty(TestFixtureMod.PROP_DISABLED);
        System.clearProperty(TestFixtureMod.PROP_FIRED);

        // --- Session 2: simulated restart — reload from same override file ---
        EventManager.install(List.of(ServerEvents.class), overrideFile);
        List<ModContainer> containers2 = ModLoader.load(modsDir, getClass().getClassLoader());
        StubServer server2 = new StubServer();
        com.github.martinambrus.rdforward.modloader.ModManager mm2 =
                new com.github.martinambrus.rdforward.modloader.ModManager(server2);
        mm2.setContainers(containers2);
        mm2.enableAll();
        EventManager.applyOverrides(id -> mm2.get(id) != null);

        assertEquals(EventPriority.HIGH,
                ServerEvents.BLOCK_PLACE.findPriority(TestFixtureMod.MOD_ID, null),
                "admin override must survive process restart (rehydrated from JSON)");

        ServerEvents.BLOCK_PLACE.invoker().onBlockPlace("bob", 5, 6, 7, 8);
        assertEquals("bob,5,6,7,8", System.getProperty(TestFixtureMod.PROP_FIRED),
                "listener must still fire after restart");

        mm2.disableAll();

        // --- Session 3: mod removed from disk — reconcile drops stale override ---
        Files.delete(jar);
        EventManager.install(List.of(ServerEvents.class), overrideFile);
        List<ModContainer> containers3 = ModLoader.load(modsDir, getClass().getClassLoader());
        assertTrue(containers3.isEmpty(),
                "removing the jar must mean no container discovered");
        StubServer server3 = new StubServer();
        com.github.martinambrus.rdforward.modloader.ModManager mm3 =
                new com.github.martinambrus.rdforward.modloader.ModManager(server3);
        mm3.setContainers(containers3);
        mm3.enableAll();
        EventManager.applyOverrides(id -> mm3.get(id) != null);

        String persisted = Files.readString(overrideFile);
        assertFalse(persisted.contains(TestFixtureMod.MOD_ID),
                "override file must no longer reference the removed mod after reconcile, got: "
                        + persisted);

        EventManager.clearAll();
    }

    @Test
    void missingDescriptorIsSkippedWithoutFailing(@TempDir Path dir) throws Exception {
        Path modsDir = Files.createDirectories(dir.resolve("mods"));
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(modsDir.resolve("empty.jar")))) {
            jar.putNextEntry(new JarEntry("README.txt"));
            jar.write("nothing here".getBytes());
            jar.closeEntry();
        }

        List<ModContainer> containers = ModLoader.load(modsDir, getClass().getClassLoader());
        assertTrue(containers.isEmpty());
    }

    private void writeJarWithDescriptor(Path target, String entryName, String contents, String mainClass)
            throws IOException {
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(target))) {
            jar.putNextEntry(new JarEntry(entryName));
            jar.write(contents.getBytes());
            jar.closeEntry();
            copyClassBytes(jar, mainClass);
        }
    }

    private void writeModJar(Path target, String modId, String mainClass) throws IOException {
        String descriptor = "{"
                + "\"id\":\"" + modId + "\","
                + "\"version\":\"1.0.0\","
                + "\"entrypoints\":{\"server\":\"" + mainClass + "\"}"
                + "}";

        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(target))) {
            jar.putNextEntry(new JarEntry("rdmod.json"));
            jar.write(descriptor.getBytes());
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

    // --- Stubs used by ModManager's sweep ---

    static final class StubServer implements Server {
        final StubCommandRegistry commands = new StubCommandRegistry();
        final StubScheduler scheduler = new StubScheduler();
        @Override public World getWorld() { return null; }
        @Override public Collection<? extends Player> getOnlinePlayers() { return List.of(); }
        @Override public Player getPlayer(String name) { return null; }
        @Override public Scheduler getScheduler() { return scheduler; }
        @Override public CommandRegistry getCommandRegistry() { return commands; }
        @Override public PermissionManager getPermissionManager() { return null; }
        @Override public ModManager getModManager() { return null; }
        @Override public ProtocolVersion[] getSupportedVersions() { return new ProtocolVersion[0]; }
        @Override public void broadcastMessage(String message) {}
        @Override public PluginChannel openPluginChannel(RegistryKey id) { return null; }
    }

    static final class StubCommandRegistry implements CommandRegistry {
        @Override public void register(String modId, String name, String description, Command handler) {}
        @Override public void registerOp(String modId, String name, String description, int opLevel, Command handler) {}
        @Override public void setTabCompleter(String modId, String name, TabCompleter completer) {}
        @Override public int unregisterByOwner(String modId) { return 0; }
        @Override public boolean exists(String name) { return false; }
        @Override public List<String> listForOpLevel(int opLevel) { return List.of(); }
    }

    static final class StubScheduler implements Scheduler {
        @Override public ScheduledTask runLater(String modId, int delayTicks, Runnable task) { return null; }
        @Override public ScheduledTask runRepeating(String modId, int initialDelay, int periodTicks, Runnable task) { return null; }
        @Override public int cancelByOwner(String modId) { return 0; }
    }
}
