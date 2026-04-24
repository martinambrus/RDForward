package com.github.martinambrus.rdforward.bridge.fabric;

import com.github.martinambrus.rdforward.api.client.ClientEvents;
import com.github.martinambrus.rdforward.api.client.DrawContext;
import com.github.martinambrus.rdforward.api.client.GameScreen;
import com.github.martinambrus.rdforward.api.registry.RegistryKey;
import com.github.martinambrus.rdforward.bridge.fabric.client.FabricClientBridge;
import com.github.martinambrus.rdforward.bridge.fabric.fixtures.TestFabricClient;
import com.github.martinambrus.rdforward.bridge.fabric.fixtures.TestFabricMain;
import com.github.martinambrus.rdforward.client.api.KeyBinding;
import com.github.martinambrus.rdforward.client.api.KeyBindingRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end verification of the Fabric client bridge: loadForClient +
 * FabricClientBridge.install + fire-through each dispatch path.
 */
class FabricClientBridgeIntegrationTest {

    private static final String[] PROPS = {
            TestFabricMain.PROP_MAIN,
            TestFabricMain.PROP_JOIN,
            TestFabricClient.PROP_CLIENT,
            TestFabricClient.PROP_HUD,
            TestFabricClient.PROP_TICK_END,
            TestFabricClient.PROP_CLIENT_STARTED,
            TestFabricClient.PROP_KEYBINDING,
            TestFabricClient.PROP_NETWORKING,
            TestFabricClient.PROP_WORLD_LAST,
            TestFabricClient.PROP_WORLD_END,
            TestFabricClient.PROP_SCREEN_OPEN,
            TestFabricClient.PROP_SCREEN_CLOSE,
            TestFabricClient.PROP_NOOP_STUBS_OK
    };

    @BeforeEach
    void clearBefore() {
        FabricClientBridge.uninstall();
        ClientEvents.RENDER_HUD.clearListeners();
        ClientEvents.CLIENT_TICK.clearListeners();
        ClientEvents.CLIENT_READY.clearListeners();
        ClientEvents.CLIENT_STOPPING.clearListeners();
        ClientEvents.RENDER_WORLD.clearListeners();
        ClientEvents.SCREEN_OPEN.clearListeners();
        ClientEvents.SCREEN_CLOSE.clearListeners();
        HudRenderCallback.EVENT.clearListeners();
        ClientTickEvents.START_CLIENT_TICK.clearListeners();
        ClientTickEvents.END_CLIENT_TICK.clearListeners();
        ClientLifecycleEvents.CLIENT_STARTED.clearListeners();
        ClientLifecycleEvents.CLIENT_STOPPING.clearListeners();
        WorldRenderEvents.LAST.clearListeners();
        WorldRenderEvents.END.clearListeners();
        ScreenEvents.OPEN.clearListeners();
        ScreenEvents.CLOSE.clearListeners();
        for (String p : PROPS) System.clearProperty(p);
    }

    @AfterEach
    void clearAfter() {
        clearBefore();
    }

    @Test
    void loadForClientInstantiatesClientEntrypointAndFiresLifecycle(@TempDir Path dir) throws Exception {
        Path jar = writeClientModJar(dir.resolve("client-mod.jar"),
                "fabricclient", "1.0.0",
                List.of(TestFabricMain.class.getName()),
                List.of(TestFabricClient.class.getName()),
                "*");

        FabricPluginLoader.LoadedFabricMod loaded =
                FabricPluginLoader.loadForClient(jar, getClass().getClassLoader());
        try {
            assertEquals("fabricclient", loaded.descriptor().id());
            assertEquals(TestFabricClient.class.getName(), loaded.descriptor().clientEntrypoint(),
                    "client entrypoint must win in CLIENT mode");

            FabricClientBridge.install();
            assertTrue(FabricClientBridge.isInstalled());

            loaded.serverMod().onEnable(null);

            assertEquals("true", System.getProperty(TestFabricMain.PROP_MAIN),
                    "main entrypoint must run before client entrypoint");
            assertEquals("true", System.getProperty(TestFabricClient.PROP_CLIENT),
                    "onInitializeClient must fire");

            DrawContext ctx = stubDrawContext(1920, 1080);
            ClientEvents.RENDER_HUD.invoker().onRenderHud(ctx);
            assertEquals("1920x1080", System.getProperty(TestFabricClient.PROP_HUD),
                    "HudRenderCallback must receive the DrawContext from rd-client dispatch");

            ClientEvents.CLIENT_TICK.invoker().onTick();
            assertEquals("true", System.getProperty(TestFabricClient.PROP_TICK_END),
                    "END_CLIENT_TICK must fire when ClientEvents.CLIENT_TICK dispatches");

            ClientEvents.CLIENT_READY.invoker().onReady();
            assertEquals("true", System.getProperty(TestFabricClient.PROP_CLIENT_STARTED),
                    "CLIENT_STARTED lifecycle must fire when CLIENT_READY dispatches");

            assertEquals(TestFabricClient.KEY_BINDING_NAME + ":" + TestFabricClient.KEY_BINDING_CODE,
                    System.getProperty(TestFabricClient.PROP_KEYBINDING),
                    "KeyBindingHelper.registerKeyBinding must hand back the same binding");
            // The registered binding must live in the client registry's global list.
            boolean found = KeyBindingRegistry.getBindings().stream()
                    .anyMatch(b -> TestFabricClient.KEY_BINDING_NAME.equals(b.getName()));
            assertTrue(found, "KeyBindingRegistry must hold the binding registered through the shim");

            // ClientPlayNetworking registration lands in the shim's receiver map.
            RegistryKey channel = new RegistryKey(
                    TestFabricClient.CHANNEL_NAMESPACE, TestFabricClient.CHANNEL_NAME);
            assertNotNull(ClientPlayNetworking.getReceivers().get(channel),
                    "channel receiver must be registered on the client networking shim");

            // And the shim can route an inbound payload to the receiver.
            ClientPlayNetworking.dispatchInbound(channel, "ping".getBytes());
            assertEquals("ping", System.getProperty(TestFabricClient.PROP_NETWORKING),
                    "dispatchInbound must route the payload to the registered handler");

            // RENDER_WORLD dispatch must reach WorldRenderEvents.LAST and END.
            ClientEvents.RENDER_WORLD.invoker().onRenderWorld(0.5f);
            assertEquals("0.5", System.getProperty(TestFabricClient.PROP_WORLD_LAST),
                    "WorldRenderEvents.LAST must fire with tickDelta from ClientEvents.RENDER_WORLD");
            assertEquals("0.5", System.getProperty(TestFabricClient.PROP_WORLD_END),
                    "WorldRenderEvents.END must fire with tickDelta from ClientEvents.RENDER_WORLD");

            // SCREEN_OPEN / SCREEN_CLOSE dispatch must reach ScreenEvents.
            GameScreen stubScreen = stubGameScreen();
            ClientEvents.SCREEN_OPEN.invoker().onOpen(stubScreen);
            assertEquals("open", System.getProperty(TestFabricClient.PROP_SCREEN_OPEN),
                    "ScreenEvents.OPEN must fire when ClientEvents.SCREEN_OPEN dispatches");
            ClientEvents.SCREEN_CLOSE.invoker().onClose(stubScreen);
            assertEquals("close", System.getProperty(TestFabricClient.PROP_SCREEN_CLOSE),
                    "ScreenEvents.CLOSE must fire when ClientEvents.SCREEN_CLOSE dispatches");

            // Noop stubs (particle/entity/armor/model/tooltip, etc.) must not throw.
            assertEquals("true", System.getProperty(TestFabricClient.PROP_NOOP_STUBS_OK),
                    "Noop stubs (particle, entity render, armor, model, tooltip, etc.) must execute without error");
        } finally {
            FabricClientBridge.uninstall();
            loaded.classLoader().close();
        }
    }

    @Test
    void uninstallDetachesForwarders() {
        assertFalse(FabricClientBridge.isInstalled());
        FabricClientBridge.install();
        assertTrue(FabricClientBridge.isInstalled());
        assertEquals(1, ClientEvents.RENDER_HUD.listenerCount());
        assertEquals(1, ClientEvents.CLIENT_TICK.listenerCount());

        FabricClientBridge.uninstall();
        assertFalse(FabricClientBridge.isInstalled());
        assertEquals(0, ClientEvents.RENDER_HUD.listenerCount(),
                "uninstall must drop the HUD forwarder from rd-client's dispatch");
        assertEquals(0, ClientEvents.CLIENT_TICK.listenerCount());
    }

    @Test
    void installIsIdempotent() {
        FabricClientBridge.install();
        FabricClientBridge.install();
        FabricClientBridge.install();
        assertEquals(1, ClientEvents.RENDER_HUD.listenerCount(),
                "install() must not attach duplicate forwarders on repeat calls");
    }

    @Test
    void clientSendWithoutSenderDropsSilently() {
        RegistryKey channel = new RegistryKey("rdforward", "drop-test");
        // No sender installed — must not throw.
        assertDoesNotThrow(() -> ClientPlayNetworking.send(channel, "hi".getBytes()));
    }

    @Test
    void clientSendUsesInstalledSender() {
        RegistryKey channel = new RegistryKey("rdforward", "echo");
        java.util.concurrent.atomic.AtomicReference<String> captured =
                new java.util.concurrent.atomic.AtomicReference<>();
        ClientPlayNetworking.setSender((c, p) -> captured.set(c + ":" + new String(p)));
        try {
            ClientPlayNetworking.send(channel, "hello".getBytes());
            assertEquals("rdforward:echo:hello", captured.get(),
                    "send() must route through the installed Sender");
        } finally {
            ClientPlayNetworking.setSender(null);
        }
    }

    @Test
    void serverOnlyModRejectedOnClientLoad(@TempDir Path dir) throws Exception {
        Path jar = writeClientModJar(dir.resolve("server-only.jar"),
                "srvonly", "1.0.0",
                List.of(TestFabricMain.class.getName()),
                List.of(),
                "server");
        IOException ex = assertThrows(IOException.class,
                () -> FabricPluginLoader.loadForClient(jar, getClass().getClassLoader()));
        assertTrue(ex.getMessage().contains("server-only"),
                "loadForClient must refuse environment=\"server\" mods");
    }

    @Test
    void clientEntrypointsIgnoredOnServerLoadPath(@TempDir Path dir) throws Exception {
        // A "*" mod with a client entrypoint should still load on the server
        // side — but its ClientModInitializer must NOT be instantiated or called.
        Path jar = writeClientModJar(dir.resolve("universal.jar"),
                "universal", "1.0.0",
                List.of(TestFabricMain.class.getName()),
                List.of(TestFabricClient.class.getName()),
                "*");
        FabricPluginLoader.LoadedFabricMod loaded =
                FabricPluginLoader.load(jar, getClass().getClassLoader());
        try {
            loaded.serverMod().onEnable(null);
            assertEquals("true", System.getProperty(TestFabricMain.PROP_MAIN),
                    "main entrypoint must still run on server-side load");
            assertFalse("true".equals(System.getProperty(TestFabricClient.PROP_CLIENT)),
                    "client entrypoint must NOT fire when loaded via server-side path");
        } finally {
            loaded.classLoader().close();
        }
    }

    private Path writeClientModJar(Path target, String id, String version,
                                   List<String> mainEntrypoints,
                                   List<String> clientEntrypoints,
                                   String environment) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":\"").append(id).append("\",");
        sb.append("\"version\":\"").append(version).append("\",");
        sb.append("\"environment\":\"").append(environment).append("\",");
        sb.append("\"entrypoints\":{");
        appendArray(sb, "main", mainEntrypoints);
        if (!clientEntrypoints.isEmpty()) {
            sb.append(",");
            appendArray(sb, "client", clientEntrypoints);
        }
        sb.append("}");
        sb.append("}");

        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(target))) {
            jar.putNextEntry(new JarEntry("fabric.mod.json"));
            jar.write(sb.toString().getBytes());
            jar.closeEntry();
            for (String fqcn : mainEntrypoints) copyClassBytes(jar, fqcn);
            for (String fqcn : clientEntrypoints) copyClassBytes(jar, fqcn);
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

    private GameScreen stubGameScreen() {
        return new GameScreen() {
            @Override public void open(long window) {}
            @Override public void close() {}
            @Override public boolean isActive() { return true; }
            @Override public void render(int screenWidth, int screenHeight) {}
            @Override public boolean handleKey(int key, int scancode, int action, int mods) { return false; }
            @Override public void handleChar(int codepoint) {}
            @Override public boolean handleClick(int button, int action, double mouseX, double mouseY) { return false; }
            @Override public void cleanup() {}
        };
    }

    private DrawContext stubDrawContext(int w, int h) {
        return new DrawContext() {
            @Override public void drawText(String text, int x, int y, int color) {}
            @Override public void drawTextWithShadow(String text, int x, int y, int color) {}
            @Override public int getTextWidth(String text) { return 0; }
            @Override public int getTextHeight() { return 0; }
            @Override public void fillRect(int x, int y, int width, int height, int color) {}
            @Override public void drawTexture(int textureId, int x, int y, int width, int height) {}
            @Override public int getScreenWidth() { return w; }
            @Override public int getScreenHeight() { return h; }
        };
    }
}
