package com.github.martinambrus.rdforward.bridge.fabric;

import com.github.martinambrus.rdforward.api.command.CommandRegistry;
import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.api.registry.RegistryKey;
import com.github.martinambrus.rdforward.bridge.fabric.fixtures.TestFabricMain;
import com.github.martinambrus.rdforward.bridge.fabric.fixtures.TestFabricServer;
import com.github.martinambrus.rdforward.bridge.fabric.server.FabricServerBridge;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricBridgeIntegrationTest {

    private static final String[] PROPS = {
            TestFabricMain.PROP_MAIN,
            TestFabricMain.PROP_JOIN,
            TestFabricMain.PROP_STARTED,
            TestFabricMain.PROP_STOPPING,
            TestFabricMain.PROP_TICK_END,
            TestFabricMain.PROP_COMMANDS,
            TestFabricMain.PROP_NET_IN,
            TestFabricMain.PROP_LOADER_OK,
            TestFabricMain.PROP_PAYLOAD_OK,
            TestFabricServer.PROP_SERVER
    };

    @BeforeEach
    void clearBefore() {
        FabricServerBridge.uninstall();
        ServerEvents.clearAll();
        ServerLifecycleEvents.SERVER_STARTED.clearListeners();
        ServerLifecycleEvents.SERVER_STOPPING.clearListeners();
        ServerTickEvents.START_SERVER_TICK.clearListeners();
        ServerTickEvents.END_SERVER_TICK.clearListeners();
        CommandRegistrationCallback.EVENT.clearListeners();
        // receivers is a ConcurrentHashMap; clear via unregister loop.
        for (RegistryKey k : ServerPlayNetworking.getReceivers().keySet()) {
            ServerPlayNetworking.unregisterGlobalReceiver(k);
        }
        ServerPlayNetworking.setSender(null);
        for (String p : PROPS) System.clearProperty(p);
    }

    @AfterEach
    void clearAfter() {
        clearBefore();
    }

    @Test
    void loadParsesDescriptorAndInstantiatesEntrypoints(@TempDir Path dir) throws Exception {
        Path jar = writeModJar(dir.resolve("mod.jar"),
                "fabricfixture", "9.9.9",
                List.of(TestFabricMain.class.getName()),
                List.of(TestFabricServer.class.getName()),
                "*");

        FabricPluginLoader.LoadedFabricMod loaded =
                FabricPluginLoader.load(jar, getClass().getClassLoader());

        try {
            assertEquals("fabricfixture", loaded.descriptor().id());
            assertEquals("9.9.9", loaded.descriptor().version());
            assertEquals(TestFabricServer.class.getName(), loaded.descriptor().serverEntrypoint(),
                    "server entrypoint should win over main when present");
        } finally {
            loaded.classLoader().close();
        }
    }

    @Test
    void wrapperOnEnableCallsMainAndServerInitializers(@TempDir Path dir) throws Exception {
        Path jar = writeModJar(dir.resolve("mod.jar"),
                "fabricfixture", "1.0.0",
                List.of(TestFabricMain.class.getName()),
                List.of(TestFabricServer.class.getName()),
                "*");

        FabricPluginLoader.LoadedFabricMod loaded =
                FabricPluginLoader.load(jar, getClass().getClassLoader());
        try {
            FabricServerBridge.install();
            loaded.serverMod().onEnable(null);

            assertEquals("true", System.getProperty(TestFabricMain.PROP_MAIN));
            assertEquals("true", System.getProperty(TestFabricServer.PROP_SERVER));

            assertEquals(1, ServerEvents.PLAYER_JOIN.listenerCount());
            ServerEvents.PLAYER_JOIN.invoker().onPlayerJoin("bob", null);
            assertEquals("bob:null", System.getProperty(TestFabricMain.PROP_JOIN));

            // Fabric lifecycle events must fire when rd-api events dispatch.
            ServerEvents.SERVER_STARTED.invoker().onServerStarted();
            assertEquals("true", System.getProperty(TestFabricMain.PROP_STARTED),
                    "ServerLifecycleEvents.SERVER_STARTED must fire when rd-api SERVER_STARTED dispatches");

            ServerEvents.SERVER_TICK.invoker().onServerTick(1L);
            assertEquals("true", System.getProperty(TestFabricMain.PROP_TICK_END),
                    "ServerTickEvents.END_SERVER_TICK must fire when rd-api SERVER_TICK dispatches");

            // Host fires command registration — listener must see a live registry.
            CommandRegistry stubRegistry = stubCommandRegistry();
            FabricServerBridge.fireCommandRegistration(stubRegistry);
            assertEquals("ready", System.getProperty(TestFabricMain.PROP_COMMANDS),
                    "CommandRegistrationCallback.EVENT must fire with the host's registry");

            // ServerPlayNetworking receiver must route an inbound payload.
            RegistryKey channel = new RegistryKey(
                    TestFabricMain.CHANNEL_NAMESPACE, TestFabricMain.CHANNEL_NAME);
            assertNotNull(ServerPlayNetworking.getReceivers().get(channel),
                    "receiver must be registered on the server networking shim");
            ServerPlayNetworking.dispatchInbound(null, channel, "pong".getBytes());
            assertEquals("pong", System.getProperty(TestFabricMain.PROP_NET_IN),
                    "dispatchInbound must route the payload to the registered handler");

            // FabricLoader singleton must be stable.
            assertEquals("true", System.getProperty(TestFabricMain.PROP_LOADER_OK),
                    "FabricLoader.getInstance() must return the same instance on every call");

            // PayloadTypeRegistry must accept both registration signatures.
            assertEquals("true", System.getProperty(TestFabricMain.PROP_PAYLOAD_OK),
                    "PayloadTypeRegistry registration must succeed without throwing");

            ServerEvents.SERVER_STOPPING.invoker().onServerStopping();
            assertEquals("true", System.getProperty(TestFabricMain.PROP_STOPPING),
                    "ServerLifecycleEvents.SERVER_STOPPING must fire when rd-api SERVER_STOPPING dispatches");

            loaded.serverMod().onDisable();
        } finally {
            FabricServerBridge.uninstall();
            loaded.classLoader().close();
        }
    }

    @Test
    void serverSendWithoutSenderDropsSilently() {
        RegistryKey channel = new RegistryKey("rdforward", "drop-test");
        assertDoesNotThrow(() -> ServerPlayNetworking.send(null, channel, "hi".getBytes()));
    }

    @Test
    void serverSendUsesInstalledSender() {
        RegistryKey channel = new RegistryKey("rdforward", "echo");
        java.util.concurrent.atomic.AtomicReference<String> captured =
                new java.util.concurrent.atomic.AtomicReference<>();
        ServerPlayNetworking.setSender((player, c, p) -> captured.set(c + ":" + new String(p)));
        try {
            ServerPlayNetworking.send(null, channel, "hello".getBytes());
            assertEquals("rdforward:echo:hello", captured.get(),
                    "send() must route through the installed Sender");
        } finally {
            ServerPlayNetworking.setSender(null);
        }
    }

    @Test
    void installIsIdempotent() {
        FabricServerBridge.install();
        FabricServerBridge.install();
        FabricServerBridge.install();
        assertEquals(1, ServerEvents.SERVER_TICK.listenerCount(),
                "install() must not attach duplicate forwarders on repeat calls");
    }

    @Test
    void uninstallDetachesForwarders() {
        FabricServerBridge.install();
        assertTrue(FabricServerBridge.isInstalled());
        assertEquals(1, ServerEvents.SERVER_STARTED.listenerCount());
        FabricServerBridge.uninstall();
        assertEquals(0, ServerEvents.SERVER_STARTED.listenerCount(),
                "uninstall must drop the lifecycle forwarders from rd-api's dispatch");
        assertEquals(0, ServerEvents.SERVER_TICK.listenerCount());
    }

    private CommandRegistry stubCommandRegistry() {
        return new CommandRegistry() {
            @Override public void register(String modId, String name, String description,
                                           com.github.martinambrus.rdforward.api.command.Command handler) {}
            @Override public void registerOp(String modId, String name, String description, int opLevel,
                                             com.github.martinambrus.rdforward.api.command.Command handler) {}
            @Override public void setTabCompleter(String modId, String name,
                                                  com.github.martinambrus.rdforward.api.command.TabCompleter completer) {}
            @Override public int unregisterByOwner(String modId) { return 0; }
            @Override public boolean exists(String name) { return false; }
            @Override public List<String> listForOpLevel(int opLevel) { return List.of(); }
        };
    }

    @Test
    void mainOnlyModFallsBackToMainAsServerEntrypoint(@TempDir Path dir) throws Exception {
        Path jar = writeModJar(dir.resolve("main-only.jar"),
                "mainonly", "1.0.0",
                List.of(TestFabricMain.class.getName()),
                List.of(),
                "*");

        FabricPluginLoader.LoadedFabricMod loaded =
                FabricPluginLoader.load(jar, getClass().getClassLoader());
        try {
            assertEquals(TestFabricMain.class.getName(), loaded.descriptor().serverEntrypoint());
            loaded.serverMod().onEnable(null);
            assertEquals("true", System.getProperty(TestFabricMain.PROP_MAIN));
        } finally {
            loaded.classLoader().close();
        }
    }

    @Test
    void clientOnlyModIsRejected(@TempDir Path dir) throws Exception {
        Path jar = writeModJar(dir.resolve("client-only.jar"),
                "clientonly", "1.0.0",
                List.of(TestFabricMain.class.getName()),
                List.of(),
                "client");

        IOException ex = assertThrows(IOException.class,
                () -> FabricPluginLoader.load(jar, getClass().getClassLoader()));
        assertTrue(ex.getMessage().contains("client-only"));
    }

    @Test
    void missingFabricModJsonThrows(@TempDir Path dir) throws Exception {
        Path jar = dir.resolve("empty.jar");
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar))) {
            out.putNextEntry(new JarEntry("README.txt"));
            out.write("hi".getBytes());
            out.closeEntry();
        }
        assertThrows(IOException.class,
                () -> FabricPluginLoader.load(jar, getClass().getClassLoader()));
    }

    private Path writeModJar(Path target, String id, String version,
                             List<String> mainEntrypoints,
                             List<String> serverEntrypoints,
                             String environment) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":\"").append(id).append("\",");
        sb.append("\"version\":\"").append(version).append("\",");
        sb.append("\"environment\":\"").append(environment).append("\",");
        sb.append("\"entrypoints\":{");
        appendArray(sb, "main", mainEntrypoints);
        if (!serverEntrypoints.isEmpty()) {
            sb.append(",");
            appendArray(sb, "server", serverEntrypoints);
        }
        sb.append("}");
        sb.append("}");

        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(target))) {
            jar.putNextEntry(new JarEntry("fabric.mod.json"));
            jar.write(sb.toString().getBytes());
            jar.closeEntry();

            for (String fqcn : mainEntrypoints) copyClassBytes(jar, fqcn);
            for (String fqcn : serverEntrypoints) copyClassBytes(jar, fqcn);
        }
        return target;
    }

    private void appendArray(StringBuilder sb, String key, List<String> values) {
        sb.append("\"").append(key).append("\":[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(values.get(i)).append("\"");
        }
        sb.append("]");
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
