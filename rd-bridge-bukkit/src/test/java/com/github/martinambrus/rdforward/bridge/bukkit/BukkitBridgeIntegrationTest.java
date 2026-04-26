package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.api.event.ListenerInfo;
import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.api.world.BlockTypes;
import com.github.martinambrus.rdforward.api.world.Location;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.TestBukkitListener;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.TestBukkitPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BukkitBridgeIntegrationTest {

    private static final String[] PROPS = {
            TestBukkitPlugin.PROP_LOAD,
            TestBukkitPlugin.PROP_ENABLE,
            TestBukkitPlugin.PROP_DISABLE,
            TestBukkitPlugin.PROP_FIRED,
            TestBukkitPlugin.PROP_MOVE,
            TestBukkitPlugin.PROP_CMD_FIRED
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
    void loadParsesDescriptorAndInstantiatesPlugin(@TempDir Path dir) throws Exception {
        Path jar = writePluginJar(dir.resolve("plugin.jar"),
                "TestBukkit", "2.3.4",
                TestBukkitPlugin.class.getName(),
                List.of("coreMod"));

        BukkitPluginLoader.LoadedPlugin loaded =
                BukkitPluginLoader.load(jar, getClass().getClassLoader());

        try {
            assertEquals("TestBukkit", loaded.descriptor().id());
            assertEquals("2.3.4", loaded.descriptor().version());
            assertEquals(TestBukkitPlugin.class.getName(), loaded.descriptor().serverEntrypoint());
            // Bukkit `depend:` entries reference peer plugins (Vault,
            // WorldEdit, ...) that are not RDForward mods, so
            // BukkitPluginLoader surfaces them as soft dependencies — the
            // DependencyResolver treats them as load-order hints rather
            // than fatal missing requirements. Hard dependencies stay
            // empty.
            assertTrue(loaded.descriptor().dependencies().isEmpty(),
                    "Bukkit `depend:` should not produce hard deps");
            assertEquals("*", loaded.descriptor().softDependencies().get("coreMod"));
            assertNotNull(loaded.plugin());
            assertTrue(loaded.plugin() instanceof TestBukkitPlugin);
        } finally {
            loaded.classLoader().close();
        }
    }

    @Test
    void loadWiresJavaPluginInitFields(@TempDir Path dir) throws Exception {
        Path jar = writePluginJar(dir.resolve("plugin.jar"),
                "InitFieldsPlugin", "1.0.0",
                TestBukkitPlugin.class.getName(),
                List.of());

        BukkitPluginLoader.LoadedPlugin loaded =
                BukkitPluginLoader.load(jar, getClass().getClassLoader());

        try {
            // Real Paper's PluginClassLoader sets these via JavaPlugin#init
            // before onEnable. The bridge loader must do the same so plugins
            // like WorldEdit (getFile()), LuckPerms (getClassLoader()), and
            // any plugin walking getDataFolder() during onLoad/onEnable see
            // populated values rather than nulls.
            assertEquals(jar.toFile().getAbsoluteFile(),
                    loaded.plugin().getFile().getAbsoluteFile(),
                    "JavaPlugin.getFile() must point at the source jar");
            assertSame(loaded.classLoader(), loaded.plugin().getClassLoader(),
                    "JavaPlugin.getClassLoader() must be the per-plugin URLClassLoader");
            assertNotNull(loaded.plugin().getDataFolder(),
                    "JavaPlugin.getDataFolder() must be initialised");
            assertTrue(loaded.plugin().getDataFolder().getPath().endsWith("InitFieldsPlugin"),
                    "data folder path should derive from plugin name; got "
                            + loaded.plugin().getDataFolder().getPath());
        } finally {
            loaded.classLoader().close();
        }
    }

    @Test
    void wrapperOnEnableFiresPluginLifecycleAndWiresListeners(@TempDir Path dir) throws Exception {
        Path jar = writePluginJar(dir.resolve("plugin.jar"),
                "TestBukkit", "1.0.0",
                TestBukkitPlugin.class.getName(),
                List.of());

        BukkitPluginLoader.LoadedPlugin loaded =
                BukkitPluginLoader.load(jar, getClass().getClassLoader());
        try {
            loaded.serverMod().onEnable(null);

            assertEquals("true", System.getProperty(TestBukkitPlugin.PROP_LOAD));
            assertEquals("true", System.getProperty(TestBukkitPlugin.PROP_ENABLE));

            List<ListenerInfo> infos = ServerEvents.BLOCK_PLACE.getListenerInfo();
            assertEquals(1, infos.size(), "BukkitEventAdapter should have wired one BLOCK_PLACE listener");

            ServerEvents.BLOCK_PLACE.invoker().onBlockPlace("alice", 1, 2, 3, 7);
            assertEquals("alice,1,2,3,7", System.getProperty(TestBukkitPlugin.PROP_FIRED));

            ServerEvents.PLAYER_MOVE.invoker().onPlayerMove("alice",
                    (short) 64, (short) 32, (short) 128, (byte) 0, (byte) 0);
            assertEquals("alice,2.0,1.0,4.0,0.0,0.0", System.getProperty(TestBukkitPlugin.PROP_MOVE));

            loaded.serverMod().onDisable();
            assertEquals("true", System.getProperty(TestBukkitPlugin.PROP_DISABLE));
        } finally {
            loaded.classLoader().close();
        }
    }

    @Test
    void pluginYmlCommandsParseIntoPluginCommandMap(@TempDir Path dir) throws Exception {
        Path jar = writePluginJarWithCommands(dir.resolve("plugin.jar"),
                "CmdPlugin", "1.0.0", TestBukkitPlugin.class.getName());

        BukkitPluginLoader.LoadedPlugin loaded =
                BukkitPluginLoader.load(jar, getClass().getClassLoader());
        try {
            assertEquals(1, loaded.bukkitDescriptor().commands().size());
            BukkitPluginDescriptor.CommandSpec spec = loaded.bukkitDescriptor().commands().get("hello");
            assertNotNull(spec);
            assertEquals("Says hello", spec.description());
            assertEquals("/hello [name]", spec.usage());
            assertEquals(List.of("hi", "greet"), spec.aliases());
            assertEquals("myplugin.hello", spec.permission());

            org.bukkit.command.PluginCommand cmd = loaded.plugin().getCommand("hello");
            assertNotNull(cmd);
            assertEquals("hello", cmd.getName());
            assertEquals("Says hello", cmd.getDescription());
            assertNull(cmd.getExecutor(), "executor set in onEnable, not during load");
        } finally {
            loaded.classLoader().close();
        }
    }

    @Test
    void enabledPluginRegistersExecutorsWithRdApiCommandRegistry(@TempDir Path dir) throws Exception {
        Path jar = writePluginJarWithCommands(dir.resolve("plugin.jar"),
                "CmdPlugin", "1.0.0", TestBukkitPlugin.class.getName());

        StubRdServer rd = new StubRdServer();
        rd.players.put("carol",
                new StubRdServer.StubRdPlayer("carol", new Location("stub-world", 0, 64, 0, 0f, 0f)));
        BukkitBridge.install(rd);

        BukkitPluginLoader.LoadedPlugin loaded =
                BukkitPluginLoader.load(jar, getClass().getClassLoader());
        try {
            loaded.serverMod().onEnable(rd);

            assertTrue(rd.commands.registered.containsKey("hello"),
                    "bridge must register PluginCommand 'hello' with rd-api CommandRegistry");
            StubRdServer.StubCommandRegistry.Registered reg = rd.commands.registered.get("hello");
            assertEquals("CmdPlugin", reg.modId());
            assertEquals("Says hello", reg.description());

            StubRdServer.CapturedReply reply = new StubRdServer.CapturedReply();
            rd.commands.dispatch("hello", "carol", false, new String[] { "Bob", "Alice" }, reply);

            assertEquals("carol|hello|Bob,Alice", System.getProperty(TestBukkitPlugin.PROP_CMD_FIRED));
            StubRdServer.StubRdPlayer rdPlayer = rd.players.get("carol");
            assertEquals(List.of("hello ack"), rdPlayer.messages,
                    "executor's sender.sendMessage must reach rd-api player");

            loaded.serverMod().onDisable();
        } finally {
            loaded.classLoader().close();
            BukkitBridge.uninstall();
        }
    }

    @Test
    void missingPluginYmlThrows(@TempDir Path dir) throws Exception {
        Path jar = dir.resolve("broken.jar");
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar))) {
            out.putNextEntry(new JarEntry("README.txt"));
            out.write("hi".getBytes());
            out.closeEntry();
        }
        assertThrows(IOException.class,
                () -> BukkitPluginLoader.load(jar, getClass().getClassLoader()));
    }

    @Test
    void playerMoveDecodesFixedPointCoordinatesAndByteAngles() {
        AtomicInteger fires = new AtomicInteger();
        List<String> moves = new ArrayList<>();
        BukkitEventAdapter.register(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onMove(org.bukkit.event.player.PlayerMoveEvent ev) {
                fires.incrementAndGet();
                moves.add(ev.getPlayer().getName()
                        + ":" + ev.getTo().getX()
                        + "," + ev.getTo().getY()
                        + "," + ev.getTo().getZ()
                        + "," + ev.getTo().getYaw());
            }
        }, "MoveTestPlugin");

        ServerEvents.PLAYER_MOVE.invoker().onPlayerMove("bob",
                (short) 320, (short) 2048, (short) -64, (byte) -128, (byte) 64);

        assertEquals(1, fires.get());
        assertEquals("bob:10.0,64.0,-2.0,180.0", moves.get(0));
    }

    @Test
    void ignoreCancelledFalseWarningLoggedOncePerPlugin() {
        Logger log = Logger.getLogger("RDForward/BukkitBridge");
        CapturingHandler handler = new CapturingHandler();
        log.addHandler(handler);
        try {
            BukkitEventAdapter.register(new TestBukkitListener(), "PluginA");
            BukkitEventAdapter.register(new TestBukkitListener(), "PluginA");
            BukkitEventAdapter.register(new TestBukkitListener(), "PluginB");

            List<String> warnings = handler.warnings();
            assertEquals(2, warnings.size(), "one warning per plugin name");
            assertTrue(warnings.get(0).contains("Plugin 'PluginA'"));
            assertTrue(warnings.get(0).contains("ignoreCancelled=true"));
            assertTrue(warnings.get(0).contains("MONITOR priority"));
            assertTrue(warnings.get(1).contains("Plugin 'PluginB'"));
        } finally {
            log.removeHandler(handler);
        }
    }

    @Test
    void cancelledEventsDoNotReachNonMonitorListeners() {
        List<String> order = new ArrayList<>();
        BukkitEventAdapter.register(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.LOW)
            public void onLowCancel(org.bukkit.event.block.BlockPlaceEvent ev) {
                order.add("LOW");
                ev.setCancelled(true);
            }
        }, "LowCanceller");
        BukkitEventAdapter.register(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.NORMAL)
            public void onNormal(org.bukkit.event.block.BlockPlaceEvent ev) {
                order.add("NORMAL");
            }
        }, "NormalNonCanceller");
        BukkitEventAdapter.register(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
            public void onMonitor(org.bukkit.event.block.BlockPlaceEvent ev) {
                order.add("MONITOR");
            }
        }, "MonitorObserver");

        ServerEvents.BLOCK_PLACE.invoker().onBlockPlace("alice", 0, 0, 0, 1);

        assertEquals(List.of("LOW", "MONITOR"), order,
                "NORMAL listener must NOT receive cancelled event, MONITOR still fires");
    }

    @Test
    void monitorListenerSkipsWarningEvenWithoutIgnoreCancelled() {
        Logger log = Logger.getLogger("RDForward/BukkitBridge");
        CapturingHandler handler = new CapturingHandler();
        log.addHandler(handler);
        try {
            BukkitEventAdapter.register(new MonitorListener(), "MonitorPlugin");
            assertEquals(0, handler.warnings().size(),
                    "MONITOR priority handlers should not trigger the warning");
        } finally {
            log.removeHandler(handler);
        }
    }

    @Test
    void bukkitBridgeInstallExposesRdServerThroughBukkitFacade() {
        StubRdServer rd = new StubRdServer();
        rd.players.put("carol", new StubRdServer.StubRdPlayer("carol",
                new Location("stub-world", 10, 64, -5, 90f, 10f)));

        assertFalse(BukkitBridge.isInstalled());
        BukkitBridge.install(rd);
        assertTrue(BukkitBridge.isInstalled());

        assertNotNull(Bukkit.getServer());
        assertNotNull(Bukkit.getPluginManager());
        assertNotNull(Bukkit.getScheduler());
        assertEquals("RDForward", Bukkit.getName());

        World w = Bukkit.getServer().getWorld("stub-world");
        assertNotNull(w);
        assertEquals(64, w.getMaxHeight());

        Player p = Bukkit.getServer().getPlayer("carol");
        assertNotNull(p);
        assertEquals("carol", p.getName());
        assertEquals(10.0, p.getLocation().getX());
        assertEquals(64.0, p.getLocation().getY());

        Collection<Player> online = Bukkit.getOnlinePlayers();
        assertEquals(1, online.size());

        int count = Bukkit.getServer().broadcastMessage("hello");
        assertEquals(1, count);
        assertEquals(List.of("hello"), rd.broadcasts);

        BukkitBridge.install(rd);
        assertTrue(BukkitBridge.isInstalled(), "install is idempotent");

        BukkitBridge.uninstall();
        assertFalse(BukkitBridge.isInstalled());
        assertNull(Bukkit.getServer());
    }

    @Test
    void bukkitWorldAdapterBridgesBlockReadWriteThroughMaterialMapper() {
        StubRdServer rd = new StubRdServer();
        BukkitBridge.install(rd);
        try {
            World world = Bukkit.getServer().getWorld("stub-world");
            assertNotNull(world);

            Block floor = world.getBlockAt(0, 0, 0);
            assertNotNull(floor);
            assertEquals(Material.STONE, floor.getType());

            assertTrue(world.setBlockType(5, 10, 20, Material.GLASS));
            Block set = world.getBlockAt(5, 10, 20);
            assertEquals(Material.GLASS, set.getType());

            assertTrue(world.setBlockType(1, 2, 3, Material.OAK_LOG));
            assertEquals(Material.OAK_LOG, world.getBlockAt(1, 2, 3).getType());

            world.setTime(500);
            assertEquals(500, world.getTime());
        } finally {
            BukkitBridge.uninstall();
        }
    }

    @Test
    void bukkitSchedulerAdapterForwardsToRdApiScheduler() {
        StubRdServer rd = new StubRdServer();
        BukkitBridge.install(rd);
        try {
            org.bukkit.plugin.Plugin plugin = new NamedPlugin("SchedTest");
            Runnable noop = () -> {};

            BukkitTask t1 = Bukkit.getScheduler().runTask(plugin, noop);
            BukkitTask t2 = Bukkit.getScheduler().runTaskLater(plugin, noop, 40);
            BukkitTask t3 = Bukkit.getScheduler().runTaskTimer(plugin, noop, 0, 20);

            assertNotNull(t1);
            assertNotNull(t2);
            assertNotNull(t3);
            assertEquals(3, rd.scheduler.scheduled.size());
            assertEquals("SchedTest", rd.scheduler.scheduled.get(0).owner);
            assertEquals(0, rd.scheduler.scheduled.get(0).delay);
            assertEquals(40, rd.scheduler.scheduled.get(1).delay);
            assertEquals(20, rd.scheduler.scheduled.get(2).period);

            int cancelled = Bukkit.getScheduler().cancelTasks(plugin);
            assertEquals(3, cancelled);
            assertTrue(rd.scheduler.scheduled.get(0).cancelled);

            BukkitTask asyncT = Bukkit.getScheduler().runTaskAsynchronously(plugin, noop);
            assertNotNull(asyncT);
            assertEquals("SchedTest", rd.scheduler.scheduled.get(3).owner);
        } finally {
            BukkitBridge.uninstall();
        }
    }

    @Test
    void bukkitPlayerAdapterForwardsMessagesAndTeleports() {
        StubRdServer rd = new StubRdServer();
        StubRdServer.StubRdPlayer rdPlayer = new StubRdServer.StubRdPlayer("dave",
                new Location("stub-world", 1, 2, 3, 0f, 0f));
        rd.players.put("dave", rdPlayer);
        BukkitBridge.install(rd);
        try {
            Player bukkitPlayer = Bukkit.getServer().getPlayer("dave");
            assertNotNull(bukkitPlayer);
            assertSame(rdPlayer, com.github.martinambrus.rdforward.bridge.bukkit.BukkitPlayer.backing(bukkitPlayer));

            bukkitPlayer.sendMessage("ping");
            assertEquals(List.of("ping"), rdPlayer.messages);

            bukkitPlayer.teleport(new org.bukkit.Location(null, 50, 60, 70, 45f, -10f));
            assertEquals(1, rdPlayer.teleports.size());
            assertEquals(50.0, rdPlayer.teleports.get(0).x());
            assertEquals(45f, rdPlayer.teleports.get(0).yaw());

            bukkitPlayer.kickPlayer("bye");
            assertEquals("bye", rdPlayer.kickedReason);

            rdPlayer.op = true;
            assertTrue(bukkitPlayer.isOp());
        } finally {
            BukkitBridge.uninstall();
        }
    }

    @Test
    void bukkitPlayerAdapterWrapsNullAsNull() {
        assertNull(BukkitPlayerAdapter.wrap(null, null));
    }

    @Test
    void materialMapperRoundTripsKnownBlocks() {
        assertEquals(BlockTypes.STONE, MaterialMapper.toApi(Material.STONE));
        assertEquals(Material.STONE, MaterialMapper.fromApi(BlockTypes.STONE));

        assertEquals(BlockTypes.AIR, MaterialMapper.toApi(Material.AIR));
        assertEquals(BlockTypes.AIR, MaterialMapper.toApi(null));
        assertEquals(Material.AIR, MaterialMapper.fromApi(null));

        assertEquals(BlockTypes.TNT, MaterialMapper.toApi(Material.TNT));
        assertEquals(Material.TNT, MaterialMapper.fromApi(BlockTypes.TNT));

        assertEquals(Material.AIR, MaterialMapper.fromApi(BlockTypes.byId(999)),
                "unknown block ids fall back to AIR");
    }

    private Path writePluginJarWithCommands(Path target, String name, String version, String mainClass)
            throws IOException {
        StringBuilder yml = new StringBuilder();
        yml.append("name: ").append(name).append('\n');
        yml.append("version: ").append(version).append('\n');
        yml.append("main: ").append(mainClass).append('\n');
        yml.append("commands:\n");
        yml.append("  hello:\n");
        yml.append("    description: Says hello\n");
        yml.append("    usage: /hello [name]\n");
        yml.append("    aliases: [hi, greet]\n");
        yml.append("    permission: myplugin.hello\n");

        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(target))) {
            jar.putNextEntry(new JarEntry("plugin.yml"));
            jar.write(yml.toString().getBytes());
            jar.closeEntry();

            copyClassBytes(jar, mainClass);
            copyClassBytes(jar, TestBukkitListener.class.getName());
        }
        return target;
    }

    private Path writePluginJar(Path target, String name, String version,
                                String mainClass, List<String> depends) throws IOException {
        StringBuilder yml = new StringBuilder();
        yml.append("name: ").append(name).append('\n');
        yml.append("version: ").append(version).append('\n');
        yml.append("main: ").append(mainClass).append('\n');
        if (!depends.isEmpty()) {
            yml.append("depend:\n");
            for (String d : depends) yml.append("  - ").append(d).append('\n');
        }

        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(target))) {
            jar.putNextEntry(new JarEntry("plugin.yml"));
            jar.write(yml.toString().getBytes());
            jar.closeEntry();

            copyClassBytes(jar, mainClass);
            copyClassBytes(jar, TestBukkitListener.class.getName());
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

    /** Listener with only a MONITOR-priority cancellable handler — should not trigger the warning. */
    private static final class MonitorListener implements org.bukkit.event.Listener {
        @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
        public void onPlace(org.bukkit.event.block.BlockPlaceEvent ev) {}
    }

    /** Minimal named Plugin used for scheduler ownership tests. */
    private static final class NamedPlugin implements org.bukkit.plugin.Plugin {
        private final String name;
        NamedPlugin(String name) { this.name = name; }
        @Override public String getName() { return name; }
        @Override public org.bukkit.plugin.PluginDescriptionFile getDescription() { return null; }
        @Override public Logger getLogger() { return Logger.getLogger(name); }
        @Override public org.bukkit.Server getServer() { return Bukkit.getServer(); }
        @Override public boolean isEnabled() { return true; }
        @Override public void onLoad() {}
        @Override public void onEnable() {}
        @Override public void onDisable() {}
    }

    /** Logger handler that captures WARNING records for assertion. */
    private static final class CapturingHandler extends Handler {
        private final List<LogRecord> records = new ArrayList<>();
        @Override public void publish(LogRecord r) { records.add(r); }
        @Override public void flush() {}
        @Override public void close() {}
        List<String> warnings() {
            List<String> out = new ArrayList<>();
            for (LogRecord r : records) if (r.getLevel() == Level.WARNING) out.add(r.getMessage());
            return out;
        }
    }
}
